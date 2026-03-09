package com.darkun7.job.listeners;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BoosterListener implements Listener {

    private final JobPlugin plugin;
    private final Random random = new Random();
    private final NamespacedKey boosterKey;

    public BoosterListener(JobPlugin plugin) {
        this.plugin = plugin;
        this.boosterKey = new NamespacedKey(plugin, "is_xp_booster");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.PAPER)
            return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;

        // Identification via PersistentDataContainer
        if (meta.getPersistentDataContainer().has(boosterKey, PersistentDataType.BYTE)) {
            event.setCancelled(true);

            PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            int durationMinutes = plugin.getConfig().getInt("xp-booster.duration-minutes", 5);
            long durationMs = (long) durationMinutes * 60 * 1000;

            long currentExpiry = data.getXpBoosterExpiry();
            long newExpiry = Math.max(System.currentTimeMillis(), currentExpiry) + durationMs;

            data.setXpBoosterExpiry(newExpiry);

            item.setAmount(item.getAmount() - 1);
            player.sendMessage(
                    ChatColor.GREEN + "Activated Job XP Booster! (2x XP for " + durationMinutes + " minutes)");
            player.sendMessage(ChatColor.YELLOW + "New expiry: " + ChatColor.WHITE
                    + formatTime(newExpiry - System.currentTimeMillis()));
        }
    }

    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null)
            return;

        // Check for RuneMobs rarity
        NamespacedKey rKey = new NamespacedKey("runemobs", "mob_rarity");
        if (victim.getPersistentDataContainer().has(rKey, PersistentDataType.STRING)) {
            String rarity = victim.getPersistentDataContainer().get(rKey, PersistentDataType.STRING);

            List<String> allowedRarities = plugin.getConfig().getStringList("xp-booster.rarities");
            if (allowedRarities != null && allowedRarities.contains(rarity)) {
                double chance = plugin.getConfig().getDouble("xp-booster.drop-chance", 0.05);
                if (random.nextDouble() < chance) {
                    victim.getWorld().dropItemNaturally(victim.getLocation(), createBoosterItem(plugin));
                    killer.sendMessage(ChatColor.GOLD + "The elite monster dropped a Job XP Booster!");
                }
            }
        }
    }

    public ItemStack createBoosterItem(JobPlugin plugin) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = plugin.getConfig().getString("xp-booster.item.name", "&6&lJob XP Booster (2x)");
            int duration = plugin.getConfig().getInt("xp-booster.duration-minutes", 5);
            List<String> lore = plugin.getConfig().getStringList("xp-booster.item.lore");

            List<String> coloredLore = new ArrayList<>();
            if (lore != null) {
                for (String line : lore) {
                    coloredLore.add(ChatColor.translateAlternateColorCodes('&',
                            line.replace("%duration%", String.valueOf(duration))));
                }
            }

            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            meta.setLore(coloredLore);

            // Mark as booster using PersistentDataContainer
            meta.getPersistentDataContainer().set(boosterKey, PersistentDataType.BYTE, (byte) 1);

            item.setItemMeta(meta);
        }
        return item;
    }

    private String formatTime(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        seconds %= 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
