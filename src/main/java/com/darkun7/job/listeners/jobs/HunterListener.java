package com.darkun7.job.listeners.jobs;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HunterListener implements Listener {

    private final JobPlugin plugin;
    public static final Map<UUID, Long> markedEntities = new HashMap<>();

    public HunterListener(JobPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        Player attacker = getAttackingPlayer(event);
        if (attacker != null) {
            PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(attacker.getUniqueId());

            // Marksman
            if (data.getSkillLevel("Marksman") > 0 && data.getActiveSkillsList().contains("Marksman")) {
                if (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                    double bonus = 0.15 * data.getSkillLevel("Marksman");
                    event.setDamage(event.getDamage() * (1.0 + bonus));
                }
            }

            // Spearman
            if (data.getSkillLevel("Spearman") > 0 && data.getActiveSkillsList().contains("Spearman")) {
                ItemStack held = attacker.getInventory().getItemInMainHand();
                if (held.getType() == Material.TRIDENT
                        || held.getType().name().contains("SPEAR")
                        || event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                    double bonus = 0.20 * data.getSkillLevel("Spearman");
                    event.setDamage(event.getDamage() * (1.0 + bonus));
                }
            }
        }

        // Hunter's Mark extra damage
        if (event.getEntity() instanceof LivingEntity) {
            UUID targetUUID = event.getEntity().getUniqueId();
            if (markedEntities.containsKey(targetUUID)) {
                long expiry = markedEntities.get(targetUUID);
                if (System.currentTimeMillis() < expiry) {
                    event.setDamage(event.getDamage() * 1.30);
                } else {
                    markedEntities.remove(targetUUID);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        markedEntities.remove(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL)
            return;
        Player player = event.getPlayer();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data.getSkillLevel("Pearl Mastery") > 0 && data.getActiveSkillsList().contains("Pearl Mastery")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.setFallDistance(0);
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    @EventHandler
    public void onEnderPearlDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL)
            return;
        Player player = (Player) event.getEntity();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data.getSkillLevel("Pearl Mastery") > 0 && data.getActiveSkillsList().contains("Pearl Mastery")) {
            if (player.hasMetadata("pearlCancel")) {
                event.setCancelled(true);
                player.removeMetadata("pearlCancel", plugin);
            }
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (event.getItem().getType() == Material.ROTTEN_FLESH && data.getSkillLevel("Iron Stomach") > 0
                && data.getActiveSkillsList().contains("Iron Stomach")) {
            int lvl = data.getSkillLevel("Iron Stomach");
            player.setFoodLevel(Math.min(20, player.getFoodLevel() + lvl));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60 + 20 * lvl, 0));
            player.removePotionEffect(PotionEffectType.HUNGER);
            player.sendMessage(ChatColor.GREEN + "[Iron Stomach] Restored " + lvl + " hunger!");
        }
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
