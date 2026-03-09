package com.darkun7.job.listeners.jobs;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class MinerListener implements Listener {

    private final JobPlugin plugin;
    private final Random rng = new Random();

    public MinerListener(JobPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamageSelf(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        Player player = (Player) event.getEntity();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (data.getSkillLevel("Stone Guard") > 0 && data.getActiveSkillsList().contains("Stone Guard")) {
            if (player.getLocation().getBlockY() < 60) {
                int lvl = data.getSkillLevel("Stone Guard");
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, lvl - 1, true, false));
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        Material type = event.getBlock().getType();

        // Vein Master
        if (data.getSkillLevel("Vein Master") > 0 && data.getActiveSkillsList().contains("Vein Master")) {
            if (type.name().contains("ORE") && !event.getBlock().hasMetadata("placed_by_player")) {
                com.darkun7.job.models.Skill skill = plugin.getJobManager().getJob("Miner").getSkill("Vein Master");
                double chancePerLevel = (skill != null) ? skill.getChancePerLevel() / 100.0 : 0.20;
                double maxChance = (skill != null) ? skill.getMaxChance() / 100.0 : 1.0;
                double chance = Math.min(maxChance, data.getSkillLevel("Vein Master") * chancePerLevel);

                if (rng.nextDouble() < chance) {
                    mineVein(event.getBlock(), type, player, 0);
                    player.sendMessage(ChatColor.GREEN + "[Vein Master] Entire vein mined!");
                }
            }
        }
    }

    private void mineVein(Block start, Material oreType, Player player, int depth) {
        if (depth > 20)
            return;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    Block neighbor = start.getRelative(dx, dy, dz);
                    if (neighbor.getType() == oreType && !neighbor.hasMetadata("veinMined")) {
                        neighbor.setMetadata("veinMined", new FixedMetadataValue(plugin, true));
                        // Award XP for the extra block broken by skill
                        plugin.getExperienceManager().awardBlockXP(player, neighbor);

                        neighbor.breakNaturally(player.getInventory().getItemInMainHand());

                        mineVein(neighbor, oreType, player, depth + 1);
                    }
                }
            }
        }
    }
}
