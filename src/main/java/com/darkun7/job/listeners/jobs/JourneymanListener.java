package com.darkun7.job.listeners.jobs;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.Action;

public class JourneymanListener implements Listener {

    private final JobPlugin plugin;

    public JourneymanListener(JobPlugin plugin) {
        this.plugin = plugin;
    }

    private void addXPToJob(Player player, String trigger) {
        plugin.getExperienceManager().awardXP(player, trigger);
    }

    @EventHandler
    public void onCraftTemplate(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        Player player = (Player) event.getWhoClicked();

        ItemStack result = event.getRecipe().getResult();
        if (result.getType().name().contains("TRIM_SMITHING_TEMPLATE")) {
            addXPToJob(player, "Craft armor trim template");
        }
    }

    @EventHandler
    public void onBrush(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (event.getClickedBlock() == null)
            return;

        Material blockType = event.getClickedBlock().getType();
        if (blockType == Material.SUSPICIOUS_SAND || blockType == Material.SUSPICIOUS_GRAVEL) {
            ItemStack item = event.getItem();
            if (item != null && item.getType() == Material.BRUSH) {
                // We award XP when they interact. Note: To be more precise,
                // we could check if the block changes type or use a specialized event,
                // but this is a good start for "brush suspicious sand/gravel".
                addXPToJob(event.getPlayer(), "Brush suspicious blocks");
            }
        }
    }
}
