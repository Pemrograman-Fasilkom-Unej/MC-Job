package com.darkun7.job.listeners.jobs;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Projectile;

public class SmithListener implements Listener {

    private final JobPlugin plugin;

    public SmithListener(JobPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        Player attacker = getAttackingPlayer(event);
        if (attacker == null)
            return;
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(attacker.getUniqueId());

        if (data.getSkillLevel("Heavy Temper") > 0 && data.getActiveSkillsList().contains("Heavy Temper")) {
            ItemStack held = attacker.getInventory().getItemInMainHand();
            String name = held.getType().name();
            if (name.contains("SWORD") || name.contains("SPEAR") || name.contains("MACE")) {
                double pierceBonus = 0.10 * data.getSkillLevel("Heavy Temper");
                event.setDamage(event.getDamage() + pierceBonus * 2.0);
            }
        }
    }

    @EventHandler
    public void onEntityDamageSelf(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        Player player = (Player) event.getEntity();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (data.getSkillLevel("Boiling Blood") > 0 && data.getActiveSkillsList().contains("Boiling Blood")) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FIRE
                    || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK
                    || event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
                int duration = 60 + 40 * data.getSkillLevel("Boiling Blood");
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration, 0, false, true));
            }
        }
    }

    @EventHandler
    public void onAnvilUse(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        if (!(event.getInventory() instanceof AnvilInventory))
            return;
        if (event.getSlotType() != InventoryType.SlotType.RESULT)
            return;
        if (event.getCurrentItem() == null)
            return;

        Player player = (Player) event.getWhoClicked();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        ItemStack result = event.getCurrentItem();

        if (data.getSkillLevel("Tempered Edge") > 0 && data.getActiveSkillsList().contains("Tempered Edge")) {
            String name = result.getType().name();
            if (name.contains("SWORD") || name.contains("AXE") || name.contains("TRIDENT")) {
                int lvl = data.getSkillLevel("Tempered Edge");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 600 + 200 * lvl, lvl - 1));
                        player.sendMessage(
                                ChatColor.GOLD + "[Tempered Edge] Weapon is empowered for " + (30 + 10 * lvl) + "s!");
                    }
                }.runTaskLater(plugin, 1L);
            }
        }

        if (data.getSkillLevel("Reinforced Armor") > 0 && data.getActiveSkillsList().contains("Reinforced Armor")) {
            String name = result.getType().name();
            if (name.contains("HELMET") || name.contains("CHESTPLATE") || name.contains("LEGGINGS")
                    || name.contains("BOOTS")) {
                int lvl = data.getSkillLevel("Reinforced Armor");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 600 + 200 * lvl, 0));
                        player.sendMessage(
                                ChatColor.GRAY + "[Reinforced Armor] Armor is hardened for " + (30 + 10 * lvl) + "s!");
                    }
                }.runTaskLater(plugin, 1L);
            }
        }
    }

    private Player getAttackingPlayer(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player)
            return (Player) event.getDamager();
        if (event.getDamager() instanceof Projectile
                && ((Projectile) event.getDamager()).getShooter() instanceof Player)
            return (Player) ((Projectile) event.getDamager()).getShooter();
        return null;
    }
}
