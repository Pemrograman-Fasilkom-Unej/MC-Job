package com.darkun7.job.gui;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.Job;
import com.darkun7.job.models.PlayerJobData;
import com.darkun7.job.models.Skill;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ActiveSkillManagerGUI {

    private static final int SKILLS_PER_PAGE = 16;

    public static void openSkillManager(Player player, PlayerJobData data, JobPlugin plugin) {
        openSkillManager(player, data, plugin, 1);
    }

    public static void openSkillManager(Player player, PlayerJobData data, JobPlugin plugin, int page) {
        String title = ChatColor.translateAlternateColorCodes('&', "&eManage Active Skills &7(Page " + page + ")");
        Inventory inv = Bukkit.createInventory(null, 54, title);

        // Fill background
        ItemStack background = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bgMeta = background.getItemMeta();
        bgMeta.setDisplayName(" ");
        background.setItemMeta(bgMeta);
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, background);
        }

        // Filter for Active skills only
        java.util.Set<String> addedSkillNames = new java.util.HashSet<>();
        List<Skill> activeOnlySkills = new ArrayList<>();

        for (Job job : plugin.getJobManager().getLoadedJobs().values()) {
            if (data.getLevel(job.getName()) > 0) {
                for (Skill skill : job.getSkills()) {
                    if (data.getSkillLevel(skill.getName()) > 0 && !addedSkillNames.contains(skill.getName())) {
                        activeOnlySkills.add(skill);
                        addedSkillNames.add(skill.getName());
                    }
                }
            }
        }

        List<String> activeSkills = data.getActiveSkillsList();
        int totalPages = (int) Math.ceil((double) activeOnlySkills.size() / SKILLS_PER_PAGE);
        if (totalPages == 0)
            totalPages = 1;
        if (page > totalPages)
            page = totalPages;

        // Info Item
        ItemStack infoStr = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoStr.getItemMeta();
        int maxSlots = data.getMaxActiveSkillSlots();
        infoMeta.setDisplayName(ChatColor.AQUA + "Active Skill Slots: " + activeSkills.size() + " / " + maxSlots);
        List<String> infoLore = new ArrayList<>();
        infoLore.add(ChatColor.GRAY + "Select up to " + maxSlots + " skills to equip them.");
        infoLore.add(ChatColor.GRAY + "Equipped skills appear in your Skill Hotbar");
        infoLore.add(ChatColor.GRAY + "when you press " + ChatColor.YELLOW + "Shift + F");
        infoMeta.setLore(infoLore);
        infoStr.setItemMeta(infoMeta);
        inv.setItem(4, infoStr);

        // Grid layout: 4 skills per row, 2 slots each (Icon + Terracotta)
        // Rows: 10-17, 19-26, 28-35, 37-44
        int startIndex = (page - 1) * SKILLS_PER_PAGE;
        int endIndex = Math.min(startIndex + SKILLS_PER_PAGE, activeOnlySkills.size());

        int[] rows = { 10, 19, 28, 37 };
        int currentSkillIdx = startIndex;

        for (int rowStart : rows) {
            for (int col = 0; col < 8; col += 2) {
                if (currentSkillIdx >= endIndex)
                    break;

                Skill skill = activeOnlySkills.get(currentSkillIdx);
                boolean isActive = activeSkills.contains(skill.getName());

                // Icon Item
                ItemStack icon = createSkillIcon(skill, data, isActive);
                inv.setItem(rowStart + col, icon);

                // Terracotta Status Indicator
                ItemStack status = createStatusIndicator(skill, isActive);
                inv.setItem(rowStart + col + 1, status);

                currentSkillIdx++;
            }
            if (currentSkillIdx >= endIndex)
                break;
        }

        // Navigation
        if (page > 1) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prev.getItemMeta();
            prevMeta.setDisplayName(ChatColor.YELLOW + "Previous Page (" + (page - 1) + ")");
            prev.setItemMeta(prevMeta);
            inv.setItem(48, prev);
        }

        ItemStack pageInfo = new ItemStack(Material.PAPER);
        ItemMeta pageMeta = pageInfo.getItemMeta();
        pageMeta.setDisplayName(ChatColor.GRAY + "Page " + page + " / " + totalPages);
        pageInfo.setItemMeta(pageMeta);
        inv.setItem(49, pageInfo);

        if (page < totalPages) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.setDisplayName(ChatColor.YELLOW + "Next Page (" + (page + 1) + ")");
            next.setItemMeta(nextMeta);
            inv.setItem(50, next);
        }

        // Back button
        ItemStack back = new ItemStack(Material.BOOKSHELF);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back to Professions");
        back.setItemMeta(backMeta);
        inv.setItem(53, back);

        player.openInventory(inv);
    }

    private static ItemStack createSkillIcon(Skill skill, PlayerJobData data, boolean isActive) {
        Material finalMat = Material.WRITABLE_BOOK;
        try {
            finalMat = Material.valueOf(skill.getIcon() != null ? skill.getIcon() : skill.getTriggerItem());
        } catch (Exception e) {
        }

        ItemStack item = new ItemStack(finalMat);
        ItemMeta meta = item.getItemMeta();
        int currentLevel = data.getSkillLevel(skill.getName());

        meta.setDisplayName((isActive ? ChatColor.GREEN : ChatColor.RED) + skill.getName() + " " + ChatColor.GRAY
                + "[Lv " + currentLevel + "]");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_GRAY + skill.getType());
        if (skill.getActivationText() != null && !skill.getActivationText().isEmpty()) {
            lore.add(ChatColor.GRAY + "Trigger: " + ChatColor.AQUA + skill.getActivationText());
        }
        lore.add("");
        lore.addAll(wrapText(ChatColor.WHITE + skill.getDescription(), 32));
        lore.addAll(getCooldownLore(skill, currentLevel));
        lore.add("");
        lore.add(isActive ? ChatColor.GREEN + "▶ CURRENTLY ACTIVE" : ChatColor.RED + "▶ INACTIVE");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to Toggle");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createStatusIndicator(Skill skill, boolean isActive) {
        Material mat = isActive ? Material.LIME_TERRACOTTA : Material.RED_TERRACOTTA;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(isActive ? ChatColor.GREEN + "STATUS: ACTIVE" : ChatColor.RED + "STATUS: INACTIVE");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Skill: " + ChatColor.WHITE + skill.getName());
        lore.add("");
        lore.add(isActive ? ChatColor.YELLOW + "Click to Deactivate" : ChatColor.YELLOW + "Click to Activate");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static List<String> getCooldownLore(Skill skill, int level) {
        List<String> lore = new ArrayList<>();
        if (skill.getCooldown() > 0) {
            int cd = skill.getCooldown();
            if (level > 1) {
                cd = Math.max(1, cd - (level - 1));
            }
            lore.add(ChatColor.GRAY + "Cooldown: " + ChatColor.AQUA + cd + "s");
        }
        return lore;
    }

    private static List<String> wrapText(String text, int maxLen) {
        List<String> lines = new ArrayList<>();
        String prefix = "";
        String raw = text;
        if (text.length() >= 2 && text.charAt(0) == ChatColor.COLOR_CHAR) {
            prefix = text.substring(0, 2);
            raw = text.substring(2);
        }
        String[] words = raw.split(" ");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            if (current.length() + word.length() + 1 > maxLen && current.length() > 0) {
                lines.add(prefix + current.toString().trim());
                current = new StringBuilder();
            }
            current.append(word).append(" ");
        }
        if (current.length() > 0) {
            lines.add(prefix + current.toString().trim());
        }
        return lines;
    }
}
