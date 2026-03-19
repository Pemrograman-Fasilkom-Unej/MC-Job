package com.darkun7.job.listeners.jobs;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import com.darkun7.job.utils.SkillExecutor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.GameMode;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class EngineerListener implements Listener {

    private final JobPlugin plugin;
    private final Map<UUID, Long> doubleJumpCooldown = new HashMap<>();

    public EngineerListener(JobPlugin plugin) {
        this.plugin = plugin;
    }

    private void addXPToJob(Player player, String trigger) {
        plugin.getExperienceManager().awardXP(player, trigger);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.REDSTONE_BLOCK) {
            addXPToJob(event.getPlayer(), "Breaking redstone block");
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        Player player = (Player) event.getWhoClicked();
        Material result = event.getRecipe().getResult().getType();
        String name = result.name();

        if (name.contains("REDSTONE") || name.contains("RAIL") || name.contains("PISTON") || name.contains("REPEATER")
                || name.contains("COMPARATOR") || name.contains("OBSERVER") || name.contains("HOPPER")
                || name.contains("DISPENSER") || name.contains("DROPPER")) {
            addXPToJob(player, "Craft redstone items or rails items");
        }
    }

    @EventHandler
    public void onTNTSpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof TNTPrimed) {
            TNTPrimed tnt = (TNTPrimed) event.getEntity();
            if (tnt.getSource() instanceof Player) {
                addXPToJob((Player) tnt.getSource(), "Ignite TNT");
            }
        }
    }

    // Spring Boots - Double Jump
    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)
            return;

        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        int level = data.getSkillLevel("Spring boots");
        if (level > 0) {
            event.setCancelled(true);
            player.setAllowFlight(false);
            player.setFlying(false);

            long now = System.currentTimeMillis();
            long lastUsed = doubleJumpCooldown.getOrDefault(player.getUniqueId(), 0L);
            int cooldownSec = 45 - (level - 1) * 7; // Lv1: 45, Lv5: 17 (wait, user said Lv5 15)
            if (level == 5)
                cooldownSec = 15;

            if (now - lastUsed < cooldownSec * 1000L) {
                long remaining = cooldownSec - (now - lastUsed) / 1000;
                player.sendMessage(ChatColor.RED + "Spring boots on cooldown! " + remaining + "s remaining.");
                return;
            }

            doubleJumpCooldown.put(player.getUniqueId(), now);
            player.setVelocity(player.getLocation().getDirection().multiply(0.5).setY(0.8));
            player.sendMessage(ChatColor.GOLD + "[Spring Boots] Double jump!");

            // Re-enable flight capability after a delay when they hit ground?
            // Simplified: just check ground periodically
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnGround()) {
                        player.setAllowFlight(true);
                        this.cancel();
                    }
                }
            }.runTaskTimer(plugin, 10L, 5L);
        }
    }

    // Machination - Hit Logic
    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player))
            return;
        Player attacker = (Player) event.getDamager();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(attacker.getUniqueId());

        if (data.getSkillLevel("Machination") > 0 && data.getActiveSkillsList().contains("Machination")) {
            // Chance based on level: 5% - 25%
            double chance = data.getSkillLevel("Machination") * 0.05;
            if (Math.random() < chance) {
                if (SkillExecutor.isCooldownActive(attacker, "Machination")) {
                    return;
                }
                SkillExecutor.updateCooldown(attacker, "Machination");
                if (event.getEntity() instanceof LivingEntity) {
                    spawnMachinationGolem(attacker, (LivingEntity) event.getEntity(),
                            data.getSkillLevel("Machination"));
                    // attacker.sendMessage(ChatColor.GOLD + "[Machination] Golem deployed!");
                }
            }
        }
    }

    private void spawnMachinationGolem(Player owner, LivingEntity target, int level) {
        IronGolem golem = owner.getWorld().spawn(owner.getLocation(), IronGolem.class);
        golem.setPlayerCreated(true);
        golem.setMetadata("job_summon_owner", new FixedMetadataValue(plugin, owner.getUniqueId().toString()));
        golem.setTarget(target);

        // Health state
        double maxHealth = 100.0;
        double health = 20.0 + (level - 1) * 20.0; // Lv1: 20, Lv3: 60, Lv5: 100
        golem.setMaxHealth(maxHealth);
        golem.setHealth(health);

        // Duration: 10s + 5s per level
        int durationTicks = (10 + level * 5) * 20;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (golem != null && !golem.isDead()) {
                    golem.remove();
                }
            }
        }.runTaskLater(plugin, durationTicks);
    }

    @EventHandler
    public void onSummonTarget(EntityTargetLivingEntityEvent event) {
        if (event.getEntity().hasMetadata("job_summon_owner")) {
            String ownerUuid = event.getEntity().getMetadata("job_summon_owner").get(0).asString();
            if (event.getTarget() != null && event.getTarget().getUniqueId().toString().equals(ownerUuid)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSummonDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager().hasMetadata("job_summon_owner")) {
            String ownerUuid = event.getDamager().getMetadata("job_summon_owner").get(0).asString();
            if (event.getEntity().getUniqueId().toString().equals(ownerUuid)) {
                event.setCancelled(true);
            }
        }
    }
}
