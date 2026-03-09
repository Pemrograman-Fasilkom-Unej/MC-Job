package com.darkun7.job.listeners.jobs;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class FishermanListener implements Listener {

    private final JobPlugin plugin;
    private final Random rng = new Random();

    // Track Deep Pressure cooldown
    private final Map<UUID, Long> deepPressureCooldown = new HashMap<>();

    public FishermanListener(JobPlugin plugin) {
        this.plugin = plugin;
    }

    // From JobCombatListener: Deep Pressure
    @EventHandler
    public void onEntityDamageSelf(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        Player player = (Player) event.getEntity();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (data.getSkillLevel("Deep Pressure") > 0 && data.getActiveSkillsList().contains("Deep Pressure")) {
            long lastTrigger = deepPressureCooldown.getOrDefault(player.getUniqueId(), 0L);
            long now = System.currentTimeMillis();
            if (now - lastTrigger > 10000L) { // 10s cooldown
                deepPressureCooldown.put(player.getUniqueId(), now);
                int lvl = data.getSkillLevel("Deep Pressure");
                int duration = 60 + 20 * lvl;
                int affected = 0;
                for (Entity e : player.getNearbyEntities(5, 5, 5)) {
                    if (e instanceof LivingEntity && !e.equals(player)) {
                        ((LivingEntity) e)
                                .addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, lvl - 1));
                        ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, duration, 0));
                        affected++;
                    }
                }
                if (affected > 0) {
                    player.sendMessage(ChatColor.BLUE + "[Deep Pressure] Released an agonizing wave around you!");
                }
            }
        }
    }

    // From JobEnvironmentListener: Angler Savant, Sunken Fortune
    @EventHandler
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (data.getSkillLevel("Angler Savant") > 0 && data.getActiveSkillsList().contains("Angler Savant")) {
            if (event.getState() == PlayerFishEvent.State.FISHING) {
                int lvl = data.getSkillLevel("Angler Savant");
                player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 600, lvl - 1, true, false));
            }
        }

        if (data.getSkillLevel("Sunken Fortune") > 0 && data.getActiveSkillsList().contains("Sunken Fortune")) {
            if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH && event.getCaught() instanceof Item) {
                com.darkun7.job.models.Skill skill = plugin.getJobManager().getJob("Fisherman")
                        .getSkill("Sunken Fortune");
                double chancePerLevel = (skill != null) ? skill.getChancePerLevel() / 100.0 : 0.10;
                double maxChance = (skill != null) ? skill.getMaxChance() / 100.0 : 1.0;
                double chance = Math.min(maxChance, data.getSkillLevel("Sunken Fortune") * chancePerLevel);

                if (rng.nextDouble() < chance) {
                    Item caught = (Item) event.getCaught();
                    ItemStack bonus = caught.getItemStack().clone();
                    player.getWorld().dropItemNaturally(player.getLocation(), bonus);

                    // Double Minecraft (Vanilla) XP Award
                    event.setExpToDrop(event.getExpToDrop() * 2);

                    player.sendMessage(ChatColor.AQUA + "[Sunken Fortune] Loot duplicated and Minecraft XP doubled!");
                }
            }
        }
    }
}
