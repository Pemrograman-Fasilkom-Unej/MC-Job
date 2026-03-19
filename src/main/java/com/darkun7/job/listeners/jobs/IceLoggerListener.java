package com.darkun7.job.listeners.jobs;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.PolarBear;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class IceLoggerListener implements Listener {

    private final JobPlugin plugin;
    private final Random rng = new Random();
    private final Map<UUID, Long> frozenCoreCooldowns = new HashMap<>();

    public IceLoggerListener(JobPlugin plugin) {
        this.plugin = plugin;
    }

    // --- Frozen Core ---
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerLethalDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        
        if (player.getHealth() - event.getFinalDamage() <= 0) {
            PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            if (data.getSkillLevel("Frozen Core") > 0 && data.getActiveSkillsList().contains("Frozen Core")) {
                int level = data.getSkillLevel("Frozen Core");
                long cooldownMs = (10 * 60 * 1000) - (level * 60 * 1000); // 10m base, -1m per level. Level 5 = 5m
                
                long now = System.currentTimeMillis();
                long lastUsed = frozenCoreCooldowns.getOrDefault(player.getUniqueId(), 0L);
                
                if (now - lastUsed >= cooldownMs) {
                    event.setCancelled(true);
                    player.setHealth(1.0);
                    
                    // Apply slowness
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 5, 4));
                    
                    // Spawn Ice Barrier (temporary blocks around player)
                    spawnIceBarrier(player);
                    
                    frozenCoreCooldowns.put(player.getUniqueId(), now);
                    player.sendMessage(ChatColor.AQUA + "[Frozen Core] You survived a fatal blow and conjured an ice barrier!");
                }
            }
        }
    }
    
    private void spawnIceBarrier(Player player) {
        Block center = player.getLocation().getBlock();
        // Generate a simple 3x3x3 hollow shell of ice that melts (reverts) after a few seconds
        final Map<Block, Material> originalBlocks = new HashMap<>();
        
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 2; y++) {
                for (int z = -1; z <= 1; z++) {
                    // Hollow interior where player is standing
                    if (x == 0 && z == 0 && (y == 0 || y == 1)) continue;
                    
                    Block b = center.getRelative(x, y, z);
                    if (b.getType() == Material.AIR) {
                        originalBlocks.put(b, b.getType());
                        b.setType(Material.ICE);
                    }
                }
            }
        }
        
        // Revert after 5 seconds
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Map.Entry<Block, Material> entry : originalBlocks.entrySet()) {
                if (entry.getKey().getType() == Material.ICE) {
                    entry.getKey().setType(entry.getValue());
                }
            }
        }, 20L * 5);
    }

    // --- Ice Vein Miner ---
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material type = block.getType();
        
        if (type == Material.ICE || type == Material.PACKED_ICE || type == Material.BLUE_ICE) {
            if (block.hasMetadata("placed_by_player")) return;
            
            PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            if (data.getSkillLevel("Ice Vein Miner") > 0 && data.getActiveSkillsList().contains("Ice Vein Miner")) {
                int level = data.getSkillLevel("Ice Vein Miner");
                
                com.darkun7.job.models.Skill skill = plugin.getJobManager().getJob("IceLogger").getSkill("Ice Vein Miner");
                double chancePerLevel = (skill != null) ? skill.getChancePerLevel() / 100.0 : 0.20;
                double maxChance = (skill != null) ? skill.getMaxChance() / 100.0 : 1.0;
                double chance = Math.min(maxChance, level * chancePerLevel);
                
                if (rng.nextDouble() < chance) {
                    chainBreakIce(block, player, level, type);
                    player.sendMessage(ChatColor.AQUA + "[Ice Vein Miner] Triggered chain reaction!");
                }
            }
        }
    }
    
    private void chainBreakIce(Block start, Player player, int level, Material matchType) {
        Set<Block> visited = new HashSet<>();
        Queue<Block> toBreak = new LinkedList<>();
        toBreak.add(start);
        int maxBlocks = 16 * level; // Up to 80 blocks at level 5
        
        while (!toBreak.isEmpty() && visited.size() < maxBlocks) {
            Block current = toBreak.poll();
            if (visited.contains(current)) continue;
            
            visited.add(current);
            if (current.getType() != matchType) continue;
            
            if (!current.equals(start)) {
                plugin.getExperienceManager().awardBlockXP(player, current);
            }
            
            current.breakNaturally(player.getInventory().getItemInMainHand());
            
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        Block neighbor = current.getRelative(dx, dy, dz);
                        if (!visited.contains(neighbor) && neighbor.getType() == matchType) {
                            toBreak.add(neighbor);
                        }
                    }
                }
            }
        }
    }

    // --- Ice Throw Projectile Handling ---
    @EventHandler
    public void onIceThrowHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Snowball && event.getEntity().hasMetadata("icelogger_icethrow")) {
            if (event.getHitEntity() instanceof LivingEntity) {
                LivingEntity hit = (LivingEntity) event.getHitEntity();
                int level = event.getEntity().getMetadata("icelogger_level").get(0).asInt();
                
                // Damage: base 2.0 + 1.0 per level
                hit.damage(2.0 + (level * 1.0), (Entity) event.getEntity().getShooter());
                // Slowness: duration 3s + 1s per level, amplifier 1 (Slowness II)
                hit.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * (3 + level), 1));
            }
        }
    }

    // --- Polar Companion ---
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity) {
            Player player = (Player) event.getDamager();
            PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            
            if (data.getSkillLevel("Polar Companion") > 0 && data.getActiveSkillsList().contains("Polar Companion")) {
                int level = data.getSkillLevel("Polar Companion");
                com.darkun7.job.models.Skill skill = plugin.getJobManager().getJob("IceLogger").getSkill("Polar Companion");
                
                double chancePerLevel = (skill != null) ? skill.getChancePerLevel() / 100.0 : 0.10;
                double maxChance = (skill != null) ? skill.getMaxChance() / 100.0 : 0.50;
                double chance = Math.min(maxChance, level * chancePerLevel);
                
                if (rng.nextDouble() < chance) {
                    // Check if already has a companion (rudimentary check using metadata)
                    if (player.hasMetadata("polar_companion_active")) return;
                    
                    PolarBear bear = player.getWorld().spawn(player.getLocation(), PolarBear.class);
                    bear.setCustomName(ChatColor.AQUA + player.getName() + "'s Bear");
                    bear.setCustomNameVisible(true);
                    
                    if (level < 3) {
                        bear.setBaby();
                    } else {
                        bear.setAdult();
                    }
                    
                    // Set target to the entity being attacked
                    bear.setTarget((LivingEntity) event.getEntity());
                    
                    // Make it temporary (e.g., 20 seconds + 5 per level)
                    player.setMetadata("polar_companion_active", new FixedMetadataValue(plugin, true));
                    int durationTicks = 20 * (20 + (level * 5));
                    
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        if (bear.isValid()) bear.remove();
                        player.removeMetadata("polar_companion_active", plugin);
                    }, durationTicks);
                }
            }
        }
    }
}
