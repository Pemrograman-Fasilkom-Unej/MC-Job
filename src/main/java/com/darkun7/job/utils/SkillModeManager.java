package com.darkun7.job.utils;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import com.darkun7.job.models.Skill;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SkillModeManager {

    private static final Map<UUID, ItemStack[]> originalHotbars = new HashMap<>();
    private static final Map<UUID, Long> skillModeExpiry = new HashMap<>();
    private static final Map<UUID, List<String>> activeSkillMapping = new HashMap<>();
    private static final Set<UUID> inSkillMode = new HashSet<>();

    public static void enableSkillMode(Player player, JobPlugin plugin) {
        UUID uuid = player.getUniqueId();
        if (inSkillMode.contains(uuid))
            return;

        // Save hotbar (slots 0-8)
        ItemStack[] hotbar = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            hotbar[i] = player.getInventory().getItem(i);
        }
        originalHotbars.put(uuid, hotbar);
        inSkillMode.add(uuid);
        skillModeExpiry.put(uuid, System.currentTimeMillis() + 10000L); // 10s timeout

        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        List<String> activeSkills = data.getActiveSkillsList();

        for (int i = 0; i < 9; i++) {
            player.getInventory().setItem(i, null);
        } // Clear hotbar

        List<Skill> drawableSkills = new ArrayList<>();
        for (String skillName : activeSkills) {
            if (!com.darkun7.job.utils.SkillExecutor.canBeActivatedDirectly(skillName)) {
                continue;
            }

            Skill skill = null;
            for (com.darkun7.job.models.Job job : plugin.getJobManager().getLoadedJobs().values()) {
                for (Skill s : job.getSkills()) {
                    if (s.getName().equalsIgnoreCase(skillName)) {
                        skill = s;
                        break;
                    }
                }
                if (skill != null)
                    break;
            }
            if (skill != null)
                drawableSkills.add(skill);
        }

        List<String> mappedSkillNames = new ArrayList<>();
        for (Skill s : drawableSkills) {
            mappedSkillNames.add(s.getName());
        }
        activeSkillMapping.put(uuid, mappedSkillNames);

        for (int i = 0; i < Math.min(drawableSkills.size(), 9); i++) {
            Skill skill = drawableSkills.get(i);
            String skillName = skill.getName();

            Material mat = Material.KNOWLEDGE_BOOK;
            try {
                if (skill.getIcon() != null) {
                    mat = Material.valueOf(skill.getIcon().toUpperCase());
                }
            } catch (Exception e) {
            }

            ItemStack icon = new ItemStack(mat);
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + "Skill: " + skillName);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Right-click or press key " + (i + 1) + " to trigger.");
                meta.setLore(lore);
                icon.setItemMeta(meta);
            }
            player.getInventory().setItem(i, icon);
        }

        player.sendMessage(ChatColor.GOLD + "--- SKILL MODE ACTIVE ---");
        player.sendMessage(ChatColor.YELLOW + "Hotbar swapped with active skills. Selecting one will trigger it.");

        // Timeout task
        new BukkitRunnable() {
            @Override
            public void run() {
                if (inSkillMode.contains(uuid)
                        && System.currentTimeMillis() >= skillModeExpiry.getOrDefault(uuid, 0L)) {
                    disableSkillMode(player);
                    player.sendMessage(ChatColor.RED + "Skill Mode expired. Hotbar restored.");
                }
            }
        }.runTaskLater(plugin, 200L); // check in 10s
    }

    public static void disableSkillMode(Player player) {
        UUID uuid = player.getUniqueId();
        if (!inSkillMode.contains(uuid))
            return;

        revertHotbar(player);
        inSkillMode.remove(uuid);
        originalHotbars.remove(uuid);
        skillModeExpiry.remove(uuid);
        activeSkillMapping.remove(uuid);
    }

    private static void revertHotbar(Player player) {
        ItemStack[] original = originalHotbars.get(player.getUniqueId());
        if (original != null) {
            for (int i = 0; i < 9; i++) {
                player.getInventory().setItem(i, original[i]);
            }
        }
    }

    public static boolean isInSkillMode(Player player) {
        return inSkillMode.contains(player.getUniqueId());
    }

    public static void handleQuit(Player player) {
        disableSkillMode(player);
    }

    public static void handleDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();
        if (!inSkillMode.contains(uuid))
            return;

        // Remove our skill icons from drops
        event.getDrops().removeIf(item -> item != null && item.getType() == Material.KNOWLEDGE_BOOK
                && item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Skill: "));

        // Add original items to drops
        ItemStack[] original = originalHotbars.get(uuid);
        if (original != null) {
            for (ItemStack item : original) {
                if (item != null && item.getType() != Material.AIR) {
                    event.getDrops().add(item);
                }
            }
        }

        // Cleanup maps but don't try to set items (player is dead)
        inSkillMode.remove(uuid);
        originalHotbars.remove(uuid);
        skillModeExpiry.remove(uuid);
        activeSkillMapping.remove(uuid);
    }

    public static List<String> getMappedSkills(Player player) {
        return activeSkillMapping.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }
}
