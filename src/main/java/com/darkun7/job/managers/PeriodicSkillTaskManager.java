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
            private int cycleCount = 0;
            @Override
            public void run() {
                cycleCount++;
                boolean isMinuteCycle = cycleCount % 30 == 0;

                for (Player p : Bukkit.getOnlinePlayers()) {
                    PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(p.getUniqueId());

                    // Nethermancer - Nether Survival XP
                    if (isMinuteCycle && p.getWorld().getName().toLowerCase().contains("nether")) {
                        plugin.getExperienceManager().awardXP(p, "Survive in the Nether over time");
                    }

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

                    // Journeyman - Hide in Shadow: Invisibility at night
                    if (data.getSkillLevel("Hide in Shadow") > 0
                            && data.getActiveSkillsList().contains("Hide in Shadow")) {
                        long time = p.getWorld().getTime();
                        if (time >= 13000 && time <= 23000) {
                            // Night time invisibility
                            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0, true, false));
                        }
                    }

                    // Engineer - IR glasses: Night vision underground
                    int level = data.getSkillLevel("IR glasses");

                    if (level > 0) {
                        int y = p.getLocation().getBlockY();

                        int threshold = 24 - (level - 1) * 16;

                        if (y >= threshold) {
                            p.addPotionEffect(new PotionEffect(
                                    PotionEffectType.NIGHT_VISION,
                                    300,
                                    0,
                                    true,
                                    false));
                        }
                    }

                    // Engineer - Spring Boots: Double jump flight allowance
                    if (data.getSkillLevel("Spring boots") > 0 && p.getGameMode() != org.bukkit.GameMode.CREATIVE
                            && p.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
                        if (!p.getAllowFlight()) {
                            p.setAllowFlight(true);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 40L);
    }
}
