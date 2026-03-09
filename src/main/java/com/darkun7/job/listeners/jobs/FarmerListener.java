package com.darkun7.job.listeners.jobs;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class FarmerListener implements Listener {

    private final JobPlugin plugin;

    public FarmerListener(JobPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block origin = event.getBlock();

        // Protection for Nature's Embrace
        if (origin.hasMetadata("nature_embrace_protected")) {
            event.setCancelled(true);
            return;
        }

        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        // Harvester
        if (data.getSkillLevel("Harvester") > 0 && data.getActiveSkillsList().contains("Harvester")) {
            if (isMatureCrop(origin)) {
                com.darkun7.job.models.Skill skill = plugin.getJobManager().getJob("Farmer").getSkill("Harvester");
                double chancePerLevel = (skill != null) ? skill.getChancePerLevel() / 100.0 : 0.20;
                double maxChance = (skill != null) ? skill.getMaxChance() / 100.0 : 1.0;
                double chance = Math.min(maxChance, data.getSkillLevel("Harvester") * chancePerLevel);

                if (java.util.concurrent.ThreadLocalRandom.current().nextDouble() < chance) {
                    int radius = 1 + data.getSkillLevel("Harvester");

                    for (int dx = -radius; dx <= radius; dx++) {
                        for (int dz = -radius; dz <= radius; dz++) {
                            Block b = origin.getRelative(dx, 0, dz);
                            if (isMatureCrop(b)) {
                                // Award XP before breaking
                                if (!b.equals(origin)) {
                                    plugin.getExperienceManager().awardBlockXP(player, b);
                                }

                                Material cropType = b.getType();
                                b.breakNaturally(player.getInventory().getItemInMainHand());
                                replant(b, cropType);
                            }
                        }
                    }
                }
            }
        }
    }

    private void replant(Block b, Material cropType) {
        Material seed = getSeedForCrop(cropType);
        if (seed == null)
            return;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (b.getRelative(0, -1, 0).getType() == Material.FARMLAND) {
                    b.setType(getYoungCrop(seed));
                }
            }
        }.runTaskLater(plugin, 2L);
    }

    private boolean isMatureCrop(Block block) {
        Material type = block.getType();
        if (type == Material.WHEAT || type == Material.CARROTS || type == Material.POTATOES
                || type == Material.BEETROOTS) {
            if (block.getBlockData() instanceof org.bukkit.block.data.Ageable) {
                org.bukkit.block.data.Ageable age = (org.bukkit.block.data.Ageable) block.getBlockData();
                return age.getAge() >= age.getMaximumAge();
            }
        }
        return false;
    }

    private Material getSeedForCrop(Material cropType) {
        switch (cropType) {
            case WHEAT:
                return Material.WHEAT_SEEDS;
            case CARROTS:
                return Material.CARROT;
            case POTATOES:
                return Material.POTATO;
            case BEETROOTS:
                return Material.BEETROOT_SEEDS;
            default:
                return null;
        }
    }

    private Material getYoungCrop(Material seed) {
        switch (seed) {
            case WHEAT_SEEDS:
                return Material.WHEAT;
            case CARROT:
                return Material.CARROTS;
            case POTATO:
                return Material.POTATOES;
            case BEETROOT_SEEDS:
                return Material.BEETROOTS;
            default:
                return seed;
        }
    }
}
