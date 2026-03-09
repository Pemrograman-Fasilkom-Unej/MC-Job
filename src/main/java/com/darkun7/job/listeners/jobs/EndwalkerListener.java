package com.darkun7.job.listeners.jobs;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Projectile;

public class EndwalkerListener implements Listener {

    private final JobPlugin plugin;

    public EndwalkerListener(JobPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        Player attacker = getAttackingPlayer(event);
        if (attacker == null)
            return;
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(attacker.getUniqueId());

        // Dragon Slayer: bonus damage vs bosses/End mobs
        if (data.getSkillLevel("Dragon Slayer") > 0 && data.getActiveSkillsList().contains("Dragon Slayer")) {
            Entity target = event.getEntity();
            boolean isEndMob = target instanceof EnderDragon || target instanceof Enderman
                    || target instanceof Endermite || target instanceof Shulker;
            if (isEndMob || (target instanceof LivingEntity && target.hasMetadata("isBoss"))) {
                double bonus = 0.20 * data.getSkillLevel("Dragon Slayer");
                event.setDamage(event.getDamage() * (1.0 + bonus));
            }
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
