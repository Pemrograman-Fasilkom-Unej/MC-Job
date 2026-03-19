package com.darkun7.job.gui;

import com.darkun7.job.JobPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class JourneymanSkillGUI {

    public static void openRuinRadarMenu(Player player, int level) {
        String title = ChatColor.DARK_AQUA + "Ruin Radar [Lv " + level + "]";
        Inventory inv = Bukkit.createInventory(null, 27, title);

        // Fill background
        ItemStack bg = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        ItemMeta bgMeta = bg.getItemMeta();
        bgMeta.setDisplayName(" ");
        bg.setItemMeta(bgMeta);
        for (int i = 0; i < 27; i++)
            inv.setItem(i, bg);

        // Level 1 Structures
        addStructureIcon(inv, 10, Material.OAK_BOAT, "Shipwreck", "shipwreck");
        addStructureIcon(inv, 11, Material.SANDSTONE, "Desert Pyramid", "desert_pyramid");
        addStructureIcon(inv, 12, Material.SUSPICIOUS_GRAVEL, "Trail Ruins", "trail_ruins");

        // Level 2 Structures
        if (level >= 2) {
            addStructureIcon(inv, 13, Material.MOSSY_COBBLESTONE, "Jungle Pyramid", "jungle_pyramid");
            addStructureIcon(inv, 14, Material.ICE, "Igloo", "igloo");
            addStructureIcon(inv, 15, Material.CROSSBOW, "Pillager Outpost", "pillager_outpost");
        }

        // Level 3 Structures
        if (level >= 3) {
            addStructureIcon(inv, 16, Material.GILDED_BLACKSTONE, "Bastion Remnant", "bastion_remnant");
            addStructureIcon(inv, 19, Material.NETHER_BRICK, "Fortress", "fortress");
        }

        // Level 4 Structures
        if (level >= 4) {
            addStructureIcon(inv, 20, Material.PRISMARINE_BRICKS, "Ocean Monument", "monument");
            addStructureIcon(inv, 21, Material.TOTEM_OF_UNDYING, "Woodland Mansion", "mansion");
        }

        // Level 5 Structures
        if (level >= 5) {
            addStructureIcon(inv, 22, Material.SCULK_SENSOR, "Ancient City", "ancient_city");
            addStructureIcon(inv, 23, Material.END_PORTAL_FRAME, "Stronghold", "stronghold");
        }

        player.openInventory(inv);
    }

    public static void openPocketStationMenu(Player player, int level) {
        String title = ChatColor.GOLD + "Pocket Station [Lv " + level + "]";
        Inventory inv = Bukkit.createInventory(null, 9, title);

        if (level >= 1) {
            addStationIcon(inv, 2, Material.CRAFTING_TABLE, "Worktable", "Open Crafting Grid");
        }
        if (level >= 2) {
            addStationIcon(inv, 4, Material.CARTOGRAPHY_TABLE, "Cartographer", "Open Cartography Table");
        }
        if (level >= 3) {
            addStationIcon(inv, 6, Material.ANVIL, "Pocket Anvil", "Open Anvil Interface");
        }

        player.openInventory(inv);
    }

    private static void addStructureIcon(Inventory inv, int slot, Material mat, String name, String structureKey) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + name);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Click to track closest structure");
        lore.add(ChatColor.DARK_GRAY + "ID: " + structureKey);
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }

    private static void addStationIcon(Inventory inv, int slot, Material mat, String name, String desc) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + name);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + desc);
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }
}
