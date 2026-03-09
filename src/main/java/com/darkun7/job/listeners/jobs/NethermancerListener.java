package com.darkun7.job.listeners.jobs;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.Projectile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NethermancerListener implements Listener {

    private final JobPlugin plugin;
    public static final Map<UUID, Boolean> hellfireActive = new HashMap<>();

    public NethermancerListener(JobPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        Player attacker = getAttackingPlayer(event);
        if (attacker == null)
            return;
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(attacker.getUniqueId());

        // Hellfire
        if (hellfireActive.getOrDefault(attacker.getUniqueId(), false)) {
            if (data.getSkillLevel("Hellfire") > 0 && data.getActiveSkillsList().contains("Hellfire")) {
                if (event.getEntity() instanceof LivingEntity) {
                    int fireTicks = 40 + (20 * data.getSkillLevel("Hellfire"));
                    event.getEntity().setFireTicks(fireTicks);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageSelf(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        Player player = (Player) event.getEntity();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        // Flame Affinity: Gain regeneration while receiving fire damage
        if (data.getSkillLevel("Flame Affinity") > 0 && data.getActiveSkillsList().contains("Flame Affinity")) {
            if (isFireDamage(event.getCause())) {
                int level = data.getSkillLevel("Flame Affinity");
                // Grant Regen for 5 seconds (100 ticks).
                // Level 1-2: Regen I, Level 3-4: Regen II, Level 5: Regen III
                int amplifier = (level - 1) / 2;
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, amplifier, true, true));
            }
        }

        // Wither Tolerance
        if (data.getSkillLevel("Wither Tolerance") > 0 && data.getActiveSkillsList().contains("Wither Tolerance")) {
            if (event.getCause() == EntityDamageEvent.DamageCause.WITHER) {
                double reduction = 0.15 * data.getSkillLevel("Wither Tolerance");
                event.setDamage(event.getDamage() * (1.0 - reduction));
                PotionEffect wither = player.getPotionEffect(PotionEffectType.WITHER);
                if (wither != null) {
                    int reducedDuration = (int) (wither.getDuration() * (1.0 - reduction));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, reducedDuration,
                            wither.getAmplifier(), true, true));
                }
            }
        }
    }

    private boolean isFireDamage(EntityDamageEvent.DamageCause cause) {
        return cause == EntityDamageEvent.DamageCause.FIRE
                || cause == EntityDamageEvent.DamageCause.FIRE_TICK
                || cause == EntityDamageEvent.DamageCause.LAVA
                || cause == EntityDamageEvent.DamageCause.HOT_FLOOR;
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
