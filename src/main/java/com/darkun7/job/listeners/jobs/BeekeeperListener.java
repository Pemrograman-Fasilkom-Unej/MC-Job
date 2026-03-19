package com.darkun7.job.listeners.jobs;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import com.darkun7.job.utils.SkillExecutor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Bee;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class BeekeeperListener implements Listener {

    private final JobPlugin plugin;
    private final Random rng = new Random();

    public BeekeeperListener(JobPlugin plugin) {
        this.plugin = plugin;
    }

    // From SkillEffectListener: Hive Guardian and Poison Coating application
    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player))
            return;
        Player player = (Player) event.getDamager();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        // Check if skill is in active slot (Slot Limit: 4)
        if (data.getActiveSkillsList().contains("Poison Coating") && data.getSkillLevel("Poison Coating") > 0) {
            if (data.isToggledOn("Poison Coating")) {
                if (event.getEntity() instanceof LivingEntity) {
                    LivingEntity victim = (LivingEntity) event.getEntity();
                    int level = data.getSkillLevel("Poison Coating");
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * (2 + level), 0));
                    player.sendMessage(ChatColor.GREEN + "Poison applied!");
                    data.setToggledOn("Poison Coating", false);
                }
            }
        }

        // Beekeeper - Hive Guardian
        if (data.getActiveSkillsList().contains("Hive Guardian") && data.getSkillLevel("Hive Guardian") > 0) {
            com.darkun7.job.models.Skill skill = plugin.getJobManager().getJob("Beekeeper").getSkill("Hive Guardian");
            double chancePerLevel = (skill != null) ? skill.getChancePerLevel() / 100.0 : 0.20;
            double maxChance = (skill != null) ? skill.getMaxChance() / 100.0 : 1.0;
            double chance = Math.min(maxChance, data.getSkillLevel("Hive Guardian") * chancePerLevel);

            if (java.util.concurrent.ThreadLocalRandom.current().nextDouble() < chance) {
                if (SkillExecutor.isCooldownActive(player, "Hive Guardian")) {
                    return;
                }
                SkillExecutor.updateCooldown(player, "Hive Guardian");
                for (int i = 0; i < 3; i++) {
                    Bee bee = player.getWorld().spawn(player.getLocation(), Bee.class);
                    bee.setTarget((LivingEntity) event.getEntity());
                    // Bees disappear after 15 seconds
                    new org.bukkit.scheduler.BukkitRunnable() {
                        @Override
                        public void run() {
                            if (bee != null && !bee.isDead()) {
                                bee.remove();
                            }
                        }
                    }.runTaskLater(plugin, 15 * 20L);
                }
            }
        }
    }

    // From JobEnvironmentListener: Honey Yield
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        Material type = event.getBlock().getType();

        if (data.getSkillLevel("Honey Yield") > 0 && data.getActiveSkillsList().contains("Honey Yield")) {
            if (type == Material.BEEHIVE || type == Material.BEE_NEST) {
                com.darkun7.job.models.Skill skill = plugin.getJobManager().getJob("Beekeeper").getSkill("Honey Yield");
                double dropChance = (skill != null) ? skill.getMaxChance() / 100.0 : 0.50;
                int lvl = data.getSkillLevel("Honey Yield");
                for (int i = 0; i < lvl; i++) {
                    if (java.util.concurrent.ThreadLocalRandom.current().nextDouble() < dropChance) {
                        event.getBlock().getLocation().getWorld().dropItemNaturally(
                                event.getBlock().getLocation(),
                                new org.bukkit.inventory.ItemStack(Material.HONEYCOMB));
                    }
                }
            }
        }
    }

    // From JobCombatListener: Hive Citizen
    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (event.getItem().getType() == Material.HONEY_BOTTLE && data.getSkillLevel("Hive Citizen") > 0
                && data.getActiveSkillsList().contains("Hive Citizen")) {
            int lvl = data.getSkillLevel("Hive Citizen");
            // Keep the honey bottle
            event.setCancelled(true);
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40 + 20 * lvl, 0));
            player.sendMessage(ChatColor.YELLOW + "[Hive Citizen] Honey consumed without using the bottle!");
        }
    }
}
