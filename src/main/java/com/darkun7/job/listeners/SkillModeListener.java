package com.darkun7.job.listeners;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.utils.SkillModeManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class SkillModeListener implements Listener {

    private final JobPlugin plugin;

    public SkillModeListener(JobPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent event) {
        if (SkillModeManager.isInSkillMode(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (SkillModeManager.isInSkillMode(player)) {
                event.setCancelled(true);
                player.closeInventory();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSwap(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (player.isSneaking()) {
            if (SkillModeManager.isInSkillMode(player)) {
                SkillModeManager.disableSkillMode(player);
                player.sendMessage(org.bukkit.ChatColor.GOLD + "--- SKILL MODE DEACTIVATED ---");
            } else {
                SkillModeManager.enableSkillMode(player, plugin);
            }
            event.setCancelled(true);
        } else if (SkillModeManager.isInSkillMode(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            if (SkillModeManager.isInSkillMode(player)) {
                SkillModeManager.disableSkillMode(player);
                player.sendMessage(org.bukkit.ChatColor.GRAY + "Skill Mode cancelled (Inventory opened).");
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        SkillModeManager.handleQuit(event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        SkillModeManager.handleDeath(event);
    }
}
