package com.darkun7.job.listeners.jobs;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.Projectile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class LumberjackListener implements Listener {

    private final JobPlugin plugin;
    public static final Map<UUID, Long> berserkExpiry = new HashMap<>();
    private final Random rng = new Random();

    public LumberjackListener(JobPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        Player attacker = getAttackingPlayer(event);
        if (attacker == null)
            return;
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(attacker.getUniqueId());

        // Berserk
        if (berserkExpiry.containsKey(attacker.getUniqueId())) {
            if (System.currentTimeMillis() < berserkExpiry.get(attacker.getUniqueId())) {
                ItemStack held = attacker.getInventory().getItemInMainHand();
                if (held.getType().name().contains("AXE")) {
                    event.setDamage(event.getDamage() * (1.0 + 0.15 * data.getSkillLevel("Berserk")));
                    if (event.getEntity() instanceof LivingEntity) {
                        ((LivingEntity) event.getEntity())
                                .addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0));
                    }
                }
            } else {
                berserkExpiry.remove(attacker.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onEntityDamageSelf(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        Player player = (Player) event.getEntity();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        // Barkskin
        if (data.getSkillLevel("Barkskin") > 0 && data.getActiveSkillsList().contains("Barkskin")) {
            double reduction = 0.04 * data.getSkillLevel("Barkskin");
            event.setDamage(event.getDamage() * (1.0 - reduction));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        Material type = event.getBlock().getType();

        // Timber Fury
        if (data.getSkillLevel("Timber Fury") > 0 && data.getActiveSkillsList().contains("Timber Fury")) {
            if ((type.name().contains("LOG") || type.name().contains("WOOD") || 
                 type.name().contains("CRIMSON_STEM") || type.name().contains("WARPED_STEM") || 
                 type.name().contains("CRIMSON_HYPHAE") || type.name().contains("WARPED_HYPHAE"))
                    && !event.getBlock().hasMetadata("placed_by_player")) {
                com.darkun7.job.models.Skill skill = plugin.getJobManager().getJob("Lumberjack")
                        .getSkill("Timber Fury");
                double chancePerLevel = (skill != null) ? skill.getChancePerLevel() / 100.0 : 0.20;
                double maxChance = (skill != null) ? skill.getMaxChance() / 100.0 : 1.0;
                double chance = Math.min(maxChance, data.getSkillLevel("Timber Fury") * chancePerLevel);

                if (rng.nextDouble() < chance) {
                    breakWholeTree(event.getBlock(), player, data.getSkillLevel("Timber Fury"));
                    player.sendMessage(ChatColor.GREEN + "[Timber Fury] Whole tree felled!");
                }
            }
        }
    }

    private void breakWholeTree(Block start, Player player, int level) {
        Set<Block> visited = new HashSet<>();
        Queue<Block> toBreak = new LinkedList<>();
        toBreak.add(start);
        int maxBlocks = 64 * level;
        while (!toBreak.isEmpty() && visited.size() < maxBlocks) {
            Block current = toBreak.poll();
            if (visited.contains(current))
                continue;
            visited.add(current);
            String name = current.getType().name();
            if (!name.contains("LOG") && !name.contains("LEAVES") && 
                !name.contains("WART_BLOCK") && current.getType() != Material.SHROOMLIGHT &&
                !name.contains("CRIMSON_STEM") && !name.contains("WARPED_STEM") &&
                !name.contains("CRIMSON_HYPHAE") && !name.contains("WARPED_HYPHAE"))
                continue;

            if (!current.equals(start)) {
                plugin.getExperienceManager().awardBlockXP(player, current);
            }

            current.breakNaturally(player.getInventory().getItemInMainHand());

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = 0; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        Block neighbor = current.getRelative(dx, dy, dz);
                        if (!visited.contains(neighbor)) {
                            toBreak.add(neighbor);
                        }
                    }
                }
            }
        }
        Block ground = start.getRelative(0, -1, 0);
        Material sapling = getSaplingForLog(start.getType());
        
        Material groundType = ground.getType();
        boolean validGround = groundType == Material.GRASS_BLOCK || groundType == Material.DIRT || 
                              groundType == Material.PODZOL || groundType == Material.MYCELIUM ||
                              groundType == Material.CRIMSON_NYLIUM || groundType == Material.WARPED_NYLIUM ||
                              groundType == Material.NETHERRACK || groundType == Material.SOUL_SOIL;

        if (sapling != null && validGround) {
            start.setType(sapling);
        }
    }

    private Material getSaplingForLog(Material logType) {
        if (logType.name().contains("OAK"))
            return Material.OAK_SAPLING;
        if (logType.name().contains("BIRCH"))
            return Material.BIRCH_SAPLING;
        if (logType.name().contains("SPRUCE"))
            return Material.SPRUCE_SAPLING;
        if (logType.name().contains("JUNGLE"))
            return Material.JUNGLE_SAPLING;
        if (logType.name().contains("ACACIA"))
            return Material.ACACIA_SAPLING;
        if (logType.name().contains("DARK_OAK"))
            return Material.DARK_OAK_SAPLING;
        if (logType.name().contains("CRIMSON"))
            return Material.CRIMSON_FUNGUS;
        if (logType.name().contains("WARPED"))
            return Material.WARPED_FUNGUS;
        return Material.OAK_SAPLING;
    }

    private Player getAttackingPlayer(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player)
            return (Player) event.getDamager();
        if (event.getDamager() instanceof Projectile
                && ((Projectile) event.getDamager()).getShooter() instanceof Player)
            return (Player) ((Projectile) event.getDamager()).getShooter();
        return null;
    }
}
