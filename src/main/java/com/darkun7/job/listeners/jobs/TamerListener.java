package com.darkun7.job.listeners.jobs;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import com.darkun7.job.utils.SkillExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class TamerListener implements Listener {

    private final JobPlugin plugin;

    public TamerListener(JobPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onHit(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player))
            return;
        Player player = (Player) event.getDamager();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (data.getSkillLevel("Pack Assault") > 0 && data.getActiveSkillsList().contains("Pack Assault")) {
            if (SkillExecutor.isCooldownActive(player, "Pack Assault")) {
                return;
            }
            // Trigger the skill effect
            SkillExecutor.executeSkill(player, "Pack Assault", plugin);
            // executeSkill will update the cooldown internally if it follows the same
            // pattern as others
            // However, Pack Assault in SkillExecutor DOES NOT update the cooldown yet.
            // Let's check SkillExecutor.executeSkill for Pack Assault.
        }
    }

    @EventHandler
    public void onEntityDamageSelf(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        Player player = (Player) event.getEntity();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (data.getSkillLevel("Shared Vitality") > 0 && data.getActiveSkillsList().contains("Shared Vitality")) {
            double shareRatio = 0.10 * data.getSkillLevel("Shared Vitality");
            double sharedDmg = event.getDamage() * shareRatio;
            event.setDamage(event.getDamage() - sharedDmg);
            for (Entity nearby : player.getNearbyEntities(20, 20, 20)) {
                if (nearby instanceof Wolf) {
                    Wolf wolf = (Wolf) nearby;
                    if (wolf.isTamed() && player.equals(wolf.getOwner())) {
                        wolf.damage(sharedDmg / 2.0);
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPetAttack(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof org.bukkit.entity.Tameable) {
            org.bukkit.entity.Tameable pet = (org.bukkit.entity.Tameable) event.getDamager();
            if (pet.isTamed() && pet.getOwner() instanceof Player) {
                Player player = (Player) pet.getOwner();
                PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

                if (data.getSkillLevel("Predator Bond") > 0 && data.getActiveSkillsList().contains("Predator Bond")) {
                    int lvl = data.getSkillLevel("Predator Bond");
                    double bonus = 0.15 * lvl;
                    event.setDamage(event.getDamage() * (1.0 + bonus));

                    if (event.getEntity() instanceof org.bukkit.entity.LivingEntity) {
                        org.bukkit.entity.LivingEntity target = (org.bukkit.entity.LivingEntity) event.getEntity();
                        target.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                org.bukkit.potion.PotionEffectType.WITHER, 60, lvl > 2 ? 1 : 0));
                    }
                }
            }
        }
    }
}
