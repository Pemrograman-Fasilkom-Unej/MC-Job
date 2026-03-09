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
import java.util.Map;

public class SkillMenuGUI {

    public static void openJobSelector(Player player, PlayerJobData data, JobPlugin plugin) {
        String title = ChatColor.translateAlternateColorCodes('&', "&eMy Professions");
        Inventory inv = Bukkit.createInventory(null, 54, title);

        // Fill background
        ItemStack background = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bgMeta = background.getItemMeta();
        bgMeta.setDisplayName(" ");
        background.setItemMeta(bgMeta);
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, background);
        }

        int slot = 10;
        for (Job job : plugin.getJobManager().getLoadedJobs().values()) {
            ItemStack item = createJobIcon(job, data, plugin);
            inv.setItem(slot, item);
            slot++;
            if ((slot + 1) % 9 == 0)
                slot += 2;
        }

        // Leaderboard Button
        ItemStack lbItem = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta lbMeta = lbItem.getItemMeta();
        lbMeta.setDisplayName(ChatColor.GOLD + "★ Job Leaderboards ★");
        List<String> lbLore = new ArrayList<>();
        lbLore.add(ChatColor.GRAY + "Click to view top players!");
        lbMeta.setLore(lbLore);
        lbItem.setItemMeta(lbMeta);
        inv.setItem(45, lbItem);

        // Manage Active Skills Button
        ItemStack manageSkills = new ItemStack(Material.NETHER_STAR);
        ItemMeta manageMeta = manageSkills.getItemMeta();
        manageMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Manage Active Skills");
        List<String> manageLore = new ArrayList<>();
        manageLore.add(ChatColor.GRAY + "Equip up to " + data.getMaxActiveSkillSlots() + " skills to your hotbar.");
        manageLore.add(
                ChatColor.GRAY + "Currently equipping: " + ChatColor.GREEN + data.getActiveSkillsList().size() + "/"
                        + data.getMaxActiveSkillSlots());
        manageLore.add("");
        manageLore.add(ChatColor.YELLOW + "Click to Manage");
        manageMeta.setLore(manageLore);
        manageSkills.setItemMeta(manageMeta);
        inv.setItem(53, manageSkills);

        player.openInventory(inv);
    }

    private static ItemStack createJobIcon(Job job, PlayerJobData data, JobPlugin plugin) {
        // Find a representative material for the job icon
        Material material = Material.BOOK;
        if (!job.getSkills().isEmpty()) {
            material = Material.valueOf(job.getSkills().get(0).getTriggerItem());
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        int level = data.getLevel(job.getName());
        double exp = data.getExp(job.getName());
        boolean isActive = data.getActiveJob().equals(job.getName());
        double multiplier = plugin.getConfig().getDouble("stats-scaling.bonus-multiplier-per-level", 0.1);
        int nextLevelXp = (int) plugin.getExperienceManager().getRequiredXp(level);
        double percent = (exp / nextLevelXp) * 100.0;

        meta.setDisplayName((isActive ? ChatColor.GREEN + "● " : ChatColor.GOLD) + job.getName() + " " + ChatColor.GRAY
                + "[Level " + level + "]");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Progress: " + ChatColor.WHITE + (int) exp + " / " + nextLevelXp + " XP "
                + ChatColor.YELLOW + "(" + (int) percent + "%)");
        lore.add(ChatColor.GRAY + "Next Level in: " + ChatColor.GREEN + (int) (nextLevelXp - exp) + " XP");
        lore.add("");

        if (job.getXpTriggers() != null && !job.getXpTriggers().isEmpty()) {
            lore.add(ChatColor.AQUA + "Main Activities:");
            for (String trigger : job.getXpTriggers()) {
                lore.add(ChatColor.GRAY + " - " + formatAttributeName(trigger));
            }
            lore.add("");
        }

        lore.addAll(job.getTheme());
        lore.add("");

        if (isActive) {
            lore.add(ChatColor.GREEN + "✦ ACTIVE PRIMARY JOB ✦");
            lore.add("");
            lore.add(ChatColor.AQUA + "Attribute Bonuses:");
            for (Map.Entry<String, Double> bonus : job.getBonuses().entrySet()) {
                String attrName = formatAttributeName(bonus.getKey());
                double baseVal = bonus.getValue();
                double val = baseVal + (baseVal * (level - 1) * multiplier);
                String sign = val >= 0 ? "+" : "";
                lore.add(ChatColor.GREEN + "  " + sign + formatBonusValue(bonus.getKey(), val) + " " + attrName);
            }
        } else {
            lore.add(ChatColor.YELLOW + "Shift + Click to set as Primary!");
            // Preview bonuses in gray
            if (!job.getBonuses().isEmpty()) {
                lore.add("");
                lore.add(ChatColor.GRAY + "Bonuses (when Primary):");
                for (Map.Entry<String, Double> bonus : job.getBonuses().entrySet()) {
                    String attrName = formatAttributeName(bonus.getKey());
                    double baseVal = bonus.getValue();
                    double val = baseVal + (baseVal * (level - 1) * multiplier);
                    String sign = val >= 0 ? "+" : "";
                    lore.add(
                            ChatColor.DARK_GRAY + "  " + sign + formatBonusValue(bonus.getKey(), val) + " " + attrName);
                }
            }
        }

        lore.add(ChatColor.WHITE + "Click to view skills!");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static void openSkillDetails(Player player, Job job, PlayerJobData data, JobPlugin plugin) {
        String title = ChatColor.translateAlternateColorCodes('&', "&eSkills: " + job.getName());
        Inventory inv = Bukkit.createInventory(null, 54, title);

        // Fill background
        ItemStack background = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bgMeta = background.getItemMeta();
        bgMeta.setDisplayName(" ");
        background.setItemMeta(bgMeta);
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, background);
        }

        // Skills logic
        List<Skill> skills = job.getSkills();
        int slot = 10;
        for (Skill skill : skills) {
            ItemStack item = createSkillItem(skill, data, plugin);
            inv.setItem(slot, item);
            slot++;
            if ((slot + 1) % 9 == 0)
                slot += 2;
        }

        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back to Professions");
        back.setItemMeta(backMeta);
        inv.setItem(49, back);

        // Job Leaderboard Button
        ItemStack lbItem = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta lbMeta = lbItem.getItemMeta();
        lbMeta.setDisplayName(ChatColor.GOLD + "★ " + job.getName() + " Rankings ★");
        List<String> lbLore = new ArrayList<>();
        lbLore.add(ChatColor.GRAY + "Click to view top " + job.getName() + " players!");
        lbMeta.setLore(lbLore);
        lbItem.setItemMeta(lbMeta);
        inv.setItem(45, lbItem);

        // Stats
        ItemStack info = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(ChatColor.AQUA + job.getName() + " Stats");
        List<String> infoLore = new ArrayList<>();
        infoLore.add(ChatColor.GRAY + "Level: " + ChatColor.GOLD + data.getLevel(job.getName()));
        infoLore.add(ChatColor.GRAY + "Unspent Points: " + ChatColor.GREEN + data.getSkillPoints(job.getName()));
        infoMeta.setLore(infoLore);
        info.setItemMeta(infoMeta);
        inv.setItem(4, info);

        // Reset Button
        ItemStack reset = new ItemStack(Material.BARRIER);
        ItemMeta resetMeta = reset.getItemMeta();
        resetMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "RESET SKILLS");
        List<String> resetLore = new ArrayList<>();
        resetLore.add(ChatColor.GRAY + "Refund all spent points for " + ChatColor.GOLD + job.getName());
        resetLore.add("");
        resetLore.add(ChatColor.RED + "--- PENALTY ---");
        resetLore.add(ChatColor.RED + "● Downgrade Job Level by 1");
        resetLore.add(ChatColor.RED + "● Reset current level Progress to 0 XP");
        resetLore.add("");
        resetLore.add(ChatColor.YELLOW + "Shift + Click to Confirm Reset");
        resetMeta.setLore(resetLore);
        reset.setItemMeta(resetMeta);
        inv.setItem(53, reset);

        player.openInventory(inv);
    }

    private static ItemStack createSkillItem(Skill skill, PlayerJobData data, JobPlugin plugin) {
        Material material = Material.valueOf(skill.getIcon());
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        int currentLevel = data.getSkillLevel(skill.getName());
        boolean isSlotActive = data.getActiveSkillsList().contains(skill.getName());

        meta.setDisplayName(ChatColor.GOLD + skill.getName() + " " + ChatColor.GRAY + "[" + currentLevel + "/"
                + skill.getMaxLevel() + "]");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_GRAY + skill.getType() + " | " + skill.getCategory());

        if (skill.getActivationText() != null && !skill.getActivationText().isEmpty()) {
            lore.add(ChatColor.GRAY + "Trigger: " + ChatColor.AQUA + skill.getActivationText());
        }

        lore.add("");
        lore.addAll(wrapText(ChatColor.WHITE + skill.getDescription(), 32));
        lore.addAll(getCooldownLore(skill, currentLevel));

        if (skill.getPreviewText() != null && !skill.getPreviewText().isEmpty()) {
            String pt = skill.getPreviewText();
            if (pt.contains("{chance}")) {
                int chance = (int) Math.min(skill.getMaxChance(), currentLevel * skill.getChancePerLevel());
                pt = pt.replace("{chance}", String.valueOf(chance));
            }
            lore.addAll(wrapText(ChatColor.LIGHT_PURPLE + pt, 32));
        }

        if (currentLevel > 0 && currentLevel < skill.getMaxLevel()) {
            lore.addAll(getUpgradePreviewLore(skill, currentLevel));
        }
        lore.add("");

        int cost = skill.getPointsPerLevel();
        if (plugin.getConfig().getBoolean("cost-scaling.enabled", true)) {
            int threshold = plugin.getConfig().getInt("cost-scaling.level-threshold", 3);
            int increase = plugin.getConfig().getInt("cost-scaling.cost-increase", 1);
            int targetLevel = currentLevel + 1;
            if (targetLevel > threshold) {
                cost = cost + (targetLevel - threshold) * increase;
            }
        }

        if (currentLevel > 0) {
            lore.add(
                    ChatColor.GRAY + "Active Slot: " + (isSlotActive ? ChatColor.GREEN + "YES" : ChatColor.RED + "NO"));
            lore.add(ChatColor.YELLOW + "Left-Click to toggle Slot (" + data.getActiveSkillsList().size() + "/"
                    + data.getMaxActiveSkillSlots() + ")");
        } else {
            lore.add(ChatColor.RED + "Unlock this skill first! " + ChatColor.AQUA + "(Cost: " + cost + " Pts)");
        }

        if (currentLevel < skill.getMaxLevel()) {
            lore.add(ChatColor.GREEN + "Right-Click to Upgrade " + ChatColor.AQUA + "(Cost: " + cost + " Pts)");
        }

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

    private static List<String> getUpgradePreviewLore(Skill skill, int level) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GOLD + "▶ Next Level Preview:");
        if (skill.getCooldown() > 0) {
            int currentCd = skill.getCooldown();
            if (level > 1) {
                currentCd = Math.max(1, currentCd - (level - 1));
            }
            int nextCd = Math.max(1, skill.getCooldown() - level);
            if (nextCd < currentCd) {
                lore.add(ChatColor.GRAY + "  Cooldown: " + ChatColor.RED + currentCd + "s "
                        + ChatColor.GRAY + "➔ " + ChatColor.GREEN + nextCd + "s");
            }
        }
        String pt = skill.getPreviewText();
        if (pt != null && !pt.isEmpty()) {
            if (pt.contains("{chance}")) {
                int nextChance = (int) Math.min(skill.getMaxChance(), (level + 1) * skill.getChancePerLevel());
                pt = pt.replace("{chance}", String.valueOf(nextChance));
            }
            lore.add(ChatColor.GRAY + "  " + ChatColor.YELLOW + pt);
        }
        return lore;
    }

    /**
     * Wrap a string at word boundaries to fit within maxLen characters per line.
     */
    private static List<String> wrapText(String text, int maxLen) {
        List<String> lines = new ArrayList<>();
        // Extract any leading color code prefix (e.g. "§f")
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

    /** Convert attribute key like 'generic.max_health' -> 'Max Health' */
    private static String formatAttributeName(String key) {
        String simplified = key.replace("generic.", "");
        String[] parts = simplified.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.length() > 0) {
                sb.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    /**
     * Format the bonus value - speed uses 2 decimal, others show whole or 1 decimal
     */
    private static String formatBonusValue(String key, double val) {
        if (key.contains("speed")) {
            return String.format("%.2f", val);
        }
        if (val == Math.floor(val)) {
            return String.format("%.0f", val);
        }
        return String.format("%.1f", val);
    }
}
