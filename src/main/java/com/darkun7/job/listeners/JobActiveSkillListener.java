package com.darkun7.job.listeners;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.darkun7.job.utils.SkillModeManager;
import org.bukkit.event.player.PlayerItemHeldEvent;
import java.util.*;

public class JobActiveSkillListener implements Listener {

    private final JobPlugin plugin;
    private final Map<UUID, Long> agronomistCooldown = new HashMap<>();
    private final Map<UUID, Long> natureEmbraceCooldown = new HashMap<>();
    private final Map<UUID, Long> natureBindCooldown = new HashMap<>();
    private final Map<UUID, Long> builderCooldown = new HashMap<>();
    private final Map<UUID, Long> breakerCooldown = new HashMap<>();

    // For two-step active skills (Builder/Breaker needs 2 block selections)
    private final Map<UUID, Block> builderFirst = new HashMap<>();
    private final Map<UUID, Block> breakerFirst = new HashMap<>();

    // Triple click detection
    private final Map<UUID, Long> lastClickTime = new HashMap<>();
    private final Map<UUID, Integer> clickCount = new HashMap<>();

    public JobActiveSkillListener(JobPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND)
            return;
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        long now = System.currentTimeMillis();

        // Stampede Trigger: Right-Click while riding
        if (player.isInsideVehicle()
                && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            if (data.getSkillLevel("Stampede") > 0 && data.getActiveSkillsList().contains("Stampede")) {
                com.darkun7.job.utils.SkillExecutor.executeSkill(player, "Stampede", plugin);
                event.setCancelled(true);
                return;
            }
        }

        // Skill Mode Interaction
        if (SkillModeManager.isInSkillMode(player)) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                int slot = player.getInventory().getHeldItemSlot();
                List<String> mappedSkills = SkillModeManager.getMappedSkills(player);
                if (slot < mappedSkills.size()) {
                    String skillName = mappedSkills.get(slot);
                    com.darkun7.job.utils.SkillExecutor.executeSkill(player, skillName, plugin);
                    SkillModeManager.disableSkillMode(player);
                }
                event.setCancelled(true);
            }
            return;
        }

        // Triple Click Detection to open GUI
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // Block Selection for Builder/Breaker
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
                if (player.hasMetadata("builder_active")) {
                    handleSelection(player, event.getClickedBlock(), data, plugin, "Builder");
                    event.setCancelled(true);
                    return;
                }
                if (player.hasMetadata("breaker_active")) {
                    handleSelection(player, event.getClickedBlock(), data, plugin, "Breaker");
                    event.setCancelled(true);
                    return;
                }
            }

            // Require empty hand
            if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
                return;
            }

            long last = lastClickTime.getOrDefault(uuid, 0L);
            int count = clickCount.getOrDefault(uuid, 0);

            if (now - last < 600L) {
                count++;
            } else {
                count = 1; // Reset to 1 if time between clicks is too long
            }
            lastClickTime.put(uuid, now);
            clickCount.put(uuid, count);

            if (count >= 3) {
                clickCount.remove(uuid);
                com.darkun7.job.gui.SkillMenuGUI.openJobSelector(player, data, plugin);
                event.setCancelled(true);
            }
        }
    }

    // Removed old NATURE BIND PROJECTILE HIT logic as it is now RayTrace based

    // ===========================
    // HELPERS
    // ===========================
    private void handleSelection(Player player, Block clicked, PlayerJobData data, JobPlugin plugin, String skill) {
        UUID uuid = player.getUniqueId();
        Map<UUID, Block> selectionMap = skill.equals("Builder") ? builderFirst : breakerFirst;
        int level = data.getSkillLevel(skill);

        if (!selectionMap.containsKey(uuid)) {
            selectionMap.put(uuid, clicked);
            player.sendMessage(ChatColor.GREEN + "[" + skill + "] First corner set at " + blockCoords(clicked));
        } else {
            Block pos1 = selectionMap.remove(uuid);
            Block pos2 = clicked;
            int maxVol = 20 + (level * 20);

            if (skill.equals("Builder")) {
                Material mat = player.getInventory().getItemInMainHand().getType();
                if (!mat.isBlock() || mat == Material.AIR) {
                    player.sendMessage(ChatColor.RED + "You must hold a block to build with!");
                } else {
                    int filled = fillArea(pos1, pos2, mat, player, maxVol);
                    player.sendMessage(ChatColor.GREEN + "[Builder] Filled " + filled + " blocks with " + mat.name());
                }
                player.removeMetadata("builder_active", plugin);
            } else {
                maxVol = 16 * level; // Specific limit for Breaker
                int broken = breakArea(pos1, pos2, player, maxVol);
                player.sendMessage(ChatColor.GREEN + "[Breaker] Broke " + broken + " blocks!");
                player.removeMetadata("breaker_active", plugin);
            }
        }
    }

    private int fillArea(Block pos1, Block pos2, Material mat, Player player, int maxVol) {
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());
        int filled = 0;

        ItemStack held = player.getInventory().getItemInMainHand();

        for (int x = minX; x <= maxX && filled < maxVol; x++) {
            for (int y = minY; y <= maxY && filled < maxVol; y++) {
                for (int z = minZ; z <= maxZ && filled < maxVol; z++) {
                    // Check if player still has blocks
                    if (held.getType() != mat || held.getAmount() <= 0) {
                        player.sendMessage(ChatColor.RED + "Out of materials! Builder stopped.");
                        return filled;
                    }

                    Block b = player.getWorld().getBlockAt(x, y, z);
                    if (b.getType() == Material.AIR || b.getType() == Material.CAVE_AIR) {
                        b.setType(mat);
                        b.setMetadata("placed_by_player", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
                        held.setAmount(held.getAmount() - 1);
                        filled++;
                    }
                }
            }
        }
        return filled;
    }

    private int breakArea(Block pos1, Block pos2, Player player, int maxVol) {
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());
        int broken = 0;
        for (int x = minX; x <= maxX && broken < maxVol; x++) {
            for (int y = minY; y <= maxY && broken < maxVol; y++) {
                for (int z = minZ; z <= maxZ && broken < maxVol; z++) {
                    Block b = player.getWorld().getBlockAt(x, y, z);
                    Material type = b.getType();

                    // Limit: Hardness under Obsidian (50.0)
                    if (type != Material.AIR && type.getHardness() < 50.0f
                            && type != Material.BEDROCK && type != Material.BARRIER
                            && !type.name().contains("PORTAL")) {
                        b.breakNaturally(player.getInventory().getItemInMainHand());
                        broken++;
                    }
                }
            }
        }
        return broken;
    }

    private String blockCoords(Block b) {
        return "(" + b.getX() + "," + b.getY() + "," + b.getZ() + ")";
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (SkillModeManager.isInSkillMode(player)) {
            int newSlot = event.getNewSlot();
            List<String> mappedSkills = SkillModeManager.getMappedSkills(player);

            if (newSlot < mappedSkills.size()) {
                String skillName = mappedSkills.get(newSlot);
                com.darkun7.job.utils.SkillExecutor.executeSkill(player, skillName, plugin);
                SkillModeManager.disableSkillMode(player);
            } else {
                // If they click an empty slot, maybe just exit skill mode?
                SkillModeManager.disableSkillMode(player);
                player.sendMessage(ChatColor.GRAY + "Skill Mode cancelled.");
            }
            event.setCancelled(true);
        }
    }
}
