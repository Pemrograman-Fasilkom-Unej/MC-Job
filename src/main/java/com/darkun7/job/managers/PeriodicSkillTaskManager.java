package com.darkun7.job.managers;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class PeriodicSkillTaskManager {

    private final JobPlugin plugin;

    public PeriodicSkillTaskManager(JobPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(p.getUniqueId());

                    // Breeder - Rider: speed/jump boost while mounted
                    if (p.isInsideVehicle() && data.getSkillLevel("Rider") > 0) {
                        int lvl = data.getSkillLevel("Rider");
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, lvl - 1, true, false));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 60, lvl - 1, true, false));
                    }

                    // Fisherman - Tide Guardian: Dolphin's Grace + Night Vision while in water
                    if (p.isInWater() && data.getSkillLevel("Tide Guardian") > 0
                            && data.getActiveSkillsList().contains("Tide Guardian")) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 80, 0, true, false));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 300, 0, true, false));
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 40L);
    }
}
