package com.darkun7.job.gui;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.managers.LeaderboardManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardGUI {

    public static void openLeaderboard(Player player, String jobName, JobPlugin plugin) {
        String title = jobName.equalsIgnoreCase("Global")
                ? ChatColor.GOLD + "Global Leaderboard"
                : ChatColor.GREEN + jobName + " Rankings";

        Inventory inv = Bukkit.createInventory(null, 45, title);

        // Fill background
        ItemStack background = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta bgMeta = background.getItemMeta();
        bgMeta.setDisplayName(" ");
        background.setItemMeta(bgMeta);
        for (int i = 0; i < 45; i++) {
            inv.setItem(i, background);
        }

        List<LeaderboardManager.LeaderboardEntry> entries = jobName.equalsIgnoreCase("Global")
                ? plugin.getLeaderboardManager().getGlobalTop()
                : plugin.getLeaderboardManager().getJobTop(jobName);

        int[] slots = { 13, 21, 23, 29, 30, 31, 32, 33, 39, 41 }; // Ranking slots for top 10

        for (int i = 0; i < entries.size(); i++) {
            LeaderboardManager.LeaderboardEntry entry = entries.get(i);
            inv.setItem(slots[i], createPlayerHead(entry, i + 1));
        }

        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back to Professions");
        back.setItemMeta(backMeta);
        inv.setItem(0, back);

        player.openInventory(inv);
    }

    private static ItemStack createPlayerHead(LeaderboardManager.LeaderboardEntry entry, int rank) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        meta.setOwningPlayer(Bukkit.getOfflinePlayer(entry.getUuid()));

        String color = ChatColor.GRAY.toString();
        if (rank == 1)
            color = ChatColor.YELLOW.toString();
        else if (rank == 2)
            color = ChatColor.WHITE.toString();
        else if (rank == 3)
            color = ChatColor.GOLD.toString();

        meta.setDisplayName(color + "#" + rank + " " + entry.getPlayerName());

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Total Level: " + ChatColor.AQUA + entry.getScore());
        lore.add("");
        lore.add(ChatColor.DARK_GRAY + "Professional Elite");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
