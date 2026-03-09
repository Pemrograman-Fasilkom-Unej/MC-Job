package com.darkun7.job.listeners.jobs;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class BreederListener implements Listener {

    private final JobPlugin plugin;

    public BreederListener(JobPlugin plugin) {
        this.plugin = plugin;
    }

    // From SkillEffectListener: Stockmaster
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null)
            return;
        Player killer = event.getEntity().getKiller();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(killer.getUniqueId());

        if (data.getActiveSkillsList().contains("Stockmaster")) {
            int stockmasterLevel = data.getSkillLevel("Stockmaster");
            if (stockmasterLevel > 0) {
                for (ItemStack drop : event.getDrops()) {
                    if (drop.getType().name().contains("MEAT") || drop.getType().name().contains("LEATHER")) {
                        drop.setAmount(drop.getAmount() + stockmasterLevel);
                    }
                }
            }
        }
    }

    // From JobCombatListener: Mutation
    @EventHandler
    public void onBreed(EntityBreedEvent event) {
        if (!(event.getBreeder() instanceof Player))
            return;
        Player player = (Player) event.getBreeder();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (data.getSkillLevel("Mutation") > 0 && data.getActiveSkillsList().contains("Mutation")) {
            com.darkun7.job.models.Skill skill = plugin.getJobManager().getJob("Breeder").getSkill("Mutation");
            double chancePerLevel = (skill != null) ? skill.getChancePerLevel() / 100.0 : 0.20;
            double maxChance = (skill != null) ? skill.getMaxChance() / 100.0 : 1.0;
            double chance = Math.min(maxChance, data.getSkillLevel("Mutation") * chancePerLevel);

            if (java.util.concurrent.ThreadLocalRandom.current().nextDouble() < chance) {
                LivingEntity offspring = event.getEntity();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        AttributeInstance maxHp = offspring.getAttribute(Attribute.MAX_HEALTH);
                        if (maxHp != null) {
                            maxHp.setBaseValue(maxHp.getDefaultValue() * 2.0);
                            offspring.setHealth(maxHp.getValue());
                        }

                        String typeName = offspring.getType().name().toLowerCase().replace("_", " ");
                        typeName = typeName.substring(0, 1).toUpperCase() + typeName.substring(1);
                        String superName = ChatColor.YELLOW + "Super " + typeName;

                        offspring.setCustomName(superName);
                        offspring.setCustomNameVisible(true);
                        offspring.setMetadata("superAnimal", new FixedMetadataValue(plugin, 2));

                        player.sendMessage(
                                ChatColor.GREEN + "[Mutation] A " + superName + ChatColor.GREEN + " is born!");
                    }
                }.runTaskLater(plugin, 2L);
            }
        }
    }

    // Super animal drop boost
    @EventHandler
    public void onSuperAnimalDeath(EntityDeathEvent event) {
        if (event.getEntity().hasMetadata("superAnimal")) {
            int multiplier = event.getEntity().getMetadata("superAnimal").get(0).asInt();
            List<ItemStack> extraDrops = new ArrayList<>(event.getDrops());
            for (ItemStack drop : extraDrops) {
                drop.setAmount(drop.getAmount() * multiplier);
            }
        }
    }
}
