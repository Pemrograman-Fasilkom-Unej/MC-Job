package com.darkun7.job.listeners.jobs;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EnchanterListener implements Listener {

    private final JobPlugin plugin;
    private final Random rng = new Random();

    public EnchanterListener(JobPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        // ---- ENCHANTER: Scholar - refund a portion of XP ----
        if (data.getSkillLevel("Scholar") > 0 && data.getActiveSkillsList().contains("Scholar")) {
            int refund = (int) (event.getExpLevelCost() * 0.10 * data.getSkillLevel("Scholar"));
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.giveExpLevels(refund);
                    if (refund > 0)
                        player.sendMessage(ChatColor.AQUA + "[Scholar] Refunded " + refund + " XP levels!");
                }
            }.runTaskLater(plugin, 1L);
        }

        // ---- ENCHANTER: Perfect Touch - chance to apply max-level enchant ----
        if (data.getSkillLevel("Perfect Touch") > 0 && data.getActiveSkillsList().contains("Perfect Touch")) {
            com.darkun7.job.models.Skill skill = plugin.getJobManager().getJob("Enchanter").getSkill("Perfect Touch");
            double chancePerLevel = (skill != null) ? skill.getChancePerLevel() / 100.0 : 0.05;
            double maxChance = (skill != null) ? skill.getMaxChance() / 100.0 : 1.0;
            double chance = Math.min(maxChance, data.getSkillLevel("Perfect Touch") * chancePerLevel);

            if (java.util.concurrent.ThreadLocalRandom.current().nextDouble() < chance) {
                // Upgrade each enchantment to its max level
                Map<org.bukkit.enchantments.Enchantment, Integer> upgradedEnchants = new HashMap<>(
                        event.getEnchantsToAdd());
                for (Map.Entry<org.bukkit.enchantments.Enchantment, Integer> entry : upgradedEnchants.entrySet()) {
                    event.getEnchantsToAdd().put(entry.getKey(), entry.getKey().getMaxLevel());
                }
                player.sendMessage(ChatColor.GOLD + "[Perfect Touch] Maximum enchantments achieved!");
            }
        }

        // ---- ENCHANTER: Insight - grant random positive buff after enchanting ----
        if (data.getSkillLevel("Insight") > 0 && data.getActiveSkillsList().contains("Insight")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    PotionEffectType[] buffs = { PotionEffectType.STRENGTH, PotionEffectType.SPEED,
                            PotionEffectType.REGENERATION, PotionEffectType.HASTE,
                            PotionEffectType.LUCK, PotionEffectType.RESISTANCE };
                    PotionEffectType chosen = buffs[rng.nextInt(buffs.length)];
                    int duration = 200 + 100 * data.getSkillLevel("Insight");
                    player.addPotionEffect(new PotionEffect(chosen, duration, 0));
                    player.sendMessage(ChatColor.GOLD + "[Insight] Gained " + chosen.getName() + " from arcane power!");
                }
            }.runTaskLater(plugin, 1L);
        }
    }
}
