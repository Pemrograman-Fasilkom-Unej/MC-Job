package com.darkun7.job.listeners.jobs;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CulinaryListener implements Listener {

    private final JobPlugin plugin;

    public CulinaryListener(JobPlugin plugin) {
        this.plugin = plugin;
    }

    private void addXPToJob(Player player, String trigger) {
        plugin.getExperienceManager().awardXP(player, trigger);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.SUGAR_CANE) {
            addXPToJob(event.getPlayer(), "Breaking sugar cane");
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        Player player = (Player) event.getWhoClicked();
        if (event.getRecipe().getResult().getType() == Material.SUGAR) {
            addXPToJob(player, "Craft sugar");
        }
    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null)
            return;

        if (victim instanceof Chicken)
            addXPToJob(killer, "Kill Chicken");
        else if (victim instanceof Sheep)
            addXPToJob(killer, "Kill Sheep");
        else if (victim instanceof Cow && !(victim instanceof MushroomCow))
            addXPToJob(killer, "Kill Cow");
        else if (victim instanceof Pig)
            addXPToJob(killer, "Kill Pig");
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player))
            return;
        Player attacker = (Player) event.getDamager();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(attacker.getUniqueId());

        // Tenderizer
        int tenderLevel = data.getSkillLevel("Tenderizer");
        if (tenderLevel > 0 && event.getEntity() instanceof LivingEntity) {
            double chance = tenderLevel * 0.05; // 5-25%
            if (Math.random() < chance) {
                LivingEntity victim = (LivingEntity) event.getEntity();
                victim.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0));
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 0));
                attacker.sendMessage(ChatColor.YELLOW + "[Tenderizer] Enemy weakened!");
            }
        }

        // Seasoning powder
        int seasoningLevel = data.getSkillLevel("Seasoning powder");
        if (seasoningLevel > 0 && data.isToggledOn("Seasoning powder") && event.getEntity() instanceof LivingEntity) {
            LivingEntity victim = (LivingEntity) event.getEntity();

            // Blindness and Nausea
            victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 0));
            victim.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, victim.getLocation().add(0, 1, 0), 20, 0.5, 0.5,
                    0.5, 0.1);

            // Cow transformation
            if (victim instanceof Cow && !(victim instanceof MushroomCow)) {
                if (Math.random() < 0.3) { // 30% transformation chance
                    Location loc = victim.getLocation();
                    victim.remove();
                    MushroomCow mooshroom = loc.getWorld().spawn(loc, MushroomCow.class);
                    if (Math.random() < 0.5) {
                        mooshroom.setVariant(MushroomCow.Variant.BROWN);
                    } else {
                        mooshroom.setVariant(MushroomCow.Variant.RED);
                    }
                    attacker.sendMessage(ChatColor.GOLD + "[Seasoning powder] Cow transformed into Mooshroom!");
                }
            }

            data.setToggledOn("Seasoning powder", false);
            attacker.sendMessage(ChatColor.GOLD + "[Seasoning powder] Seasoned!");
        }
    }

    @EventHandler
    public void onEat(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        int freshLevel = data.getSkillLevel("Fresh Ingredients");
        if (freshLevel > 0 && event.getItem().getType().isEdible()) {
            double chance = freshLevel * 0.1; // 10-50%
            if (Math.random() < chance) {
                player.setSaturation(player.getSaturation() + 4.0f);
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
                player.sendMessage(ChatColor.GREEN + "[Fresh Ingredients] Extra nutrition!");
            }
        }
    }
}
