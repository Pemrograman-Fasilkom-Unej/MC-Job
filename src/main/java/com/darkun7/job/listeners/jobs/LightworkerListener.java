package com.darkun7.job.listeners.jobs;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.inventory.ItemStack;

public class LightworkerListener implements Listener {

    private final JobPlugin plugin;

    public LightworkerListener(JobPlugin plugin) {
        this.plugin = plugin;
    }

    private void addXPToJob(Player player, String trigger) {
        plugin.getExperienceManager().awardXP(player, trigger);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        Player player = (Player) event.getWhoClicked();
        Material result = event.getRecipe().getResult().getType();
        String name = result.name();

        if (name.contains("LEATHER") || result == Material.SADDLE) {
            addXPToJob(player, "Craft leather items");
        } else if (name.endsWith("_WOOL") && !name.equals("WOOL")) { // Dyeing/Crafting colored wool
            addXPToJob(player, "Dye wool");
        } else if (result == Material.WHITE_WOOL) {
            addXPToJob(player, "Craft wool");
        }
    }

    @EventHandler
    public void onShear(PlayerShearEntityEvent event) {
        if (event.getEntity() instanceof Sheep) {
            addXPToJob(event.getPlayer(), "Shear sheep");
        }
    }

    @EventHandler
    public void onSleepLeave(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        int level = data.getSkillLevel("Traveler's Comfort");

        if (level > 0) {
            int duration = level * 1200; // 1 min per level
            int amp = level > 3 ? 1 : 0;
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, amp));
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, 0));
            player.sendMessage(ChatColor.GREEN + "[Traveler's Comfort] You feel well-rested!");
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player))
            return;
        Player attacker = (Player) event.getDamager();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(attacker.getUniqueId());
        int level = data.getSkillLevel("Snare Trap");

        if (level > 0) {
            ItemStack item = attacker.getInventory().getItemInMainHand();
            Material type = item.getType();
            if (type.name().contains("SWORD") || type.name().contains("AXE")
                    || event.getDamager() instanceof Projectile) {
                double chance = level * 0.1; // 10-50%
                if (Math.random() < chance) {
                    if (event.getEntity() instanceof LivingEntity) {
                        LivingEntity victim = (LivingEntity) event.getEntity();
                        Block b = victim.getLocation().getBlock();
                        if (b.getType() == Material.AIR) {
                            b.setType(Material.COBWEB);
                            attacker.sendMessage(ChatColor.GRAY + "[Snare Trap] Target trapped!");
                            // Remove web after certain time
                            new org.bukkit.scheduler.BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (b.getType() == Material.COBWEB) {
                                        b.setType(Material.AIR);
                                    }
                                }
                            }.runTaskLater(plugin, (20 + level * 20)); // 1-5 seconds
                        }
                    }
                }
            }
        }
    }
}
