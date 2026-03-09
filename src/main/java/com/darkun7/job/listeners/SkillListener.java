package com.darkun7.job.listeners;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.gui.SkillMenuGUI;
import com.darkun7.job.models.Job;
import com.darkun7.job.models.PlayerJobData;
import com.darkun7.job.models.Skill;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class SkillListener implements Listener {

    private final JobPlugin plugin;

    public SkillListener(JobPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = ChatColor.stripColor(event.getView().getTitle());
        if (!title.contains("My Professions") && !title.contains("Skills:") && !title.contains("Rankings")
                && !title.contains("Leaderboard") && !title.contains("Manage Active Skills")) {
            return;
        }

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player))
            return;

        Player player = (Player) event.getWhoClicked();
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || currentItem.getType() == Material.AIR)
            return;

        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        // Handle leaderboard/rankings button (Shared across menus)
        if (currentItem.getType() == Material.GOLD_BLOCK) {
            String dName = ChatColor.stripColor(currentItem.getItemMeta().getDisplayName());
            if (dName.contains("Job Leaderboards")) {
                com.darkun7.job.gui.LeaderboardGUI.openLeaderboard(player, "Global", plugin);
                return;
            } else if (dName.contains("Rankings")) {
                String jName = dName.replace("★ ", "").replace(" Rankings ★", "").trim();
                com.darkun7.job.gui.LeaderboardGUI.openLeaderboard(player, jName, plugin);
                return;
            }
        }

        if (title.contains("Manage Active Skills")) {
            // Handle ActiveSkillManagerGUI interactions
            if (currentItem.getType() == Material.BOOKSHELF || (currentItem.getType() == Material.ARROW
                    && currentItem.getItemMeta().getDisplayName().contains("Back"))) {
                SkillMenuGUI.openJobSelector(player, data, plugin);
                return;
            }

            // Handle Pagination
            if (currentItem.getType() == Material.ARROW) {
                String dName = ChatColor.stripColor(currentItem.getItemMeta().getDisplayName());
                if (dName.contains("Page")) {
                    try {
                        int nextPage = Integer
                                .parseInt(dName.substring(dName.indexOf("(") + 1, dName.indexOf(")")).trim());
                        com.darkun7.job.gui.ActiveSkillManagerGUI.openSkillManager(player, data, plugin, nextPage);
                    } catch (Exception e) {
                    }
                }
                return;
            }

            if (currentItem.getType() == Material.BOOK || currentItem.getType() == Material.PAPER)
                return; // Info Items

            String skillName = null;
            String itemName = ChatColor.stripColor(currentItem.getItemMeta().getDisplayName());

            if (currentItem.getType() == Material.LIME_TERRACOTTA || currentItem.getType() == Material.RED_TERRACOTTA) {
                // Extract from lore for Terracotta
                List<String> lore = currentItem.getItemMeta().getLore();
                if (lore != null) {
                    for (String line : lore) {
                        if (ChatColor.stripColor(line).startsWith("Skill: ")) {
                            skillName = ChatColor.stripColor(line).replace("Skill: ", "").trim();
                            break;
                        }
                    }
                }
            } else if (itemName.contains("[Lv")) {
                // Extract from display name for Icon
                skillName = itemName.substring(0, itemName.indexOf("[Lv")).trim();
            }

            if (skillName != null) {
                List<String> activeSlots = data.getActiveSkillsList();
                int currentPage = 1;
                if (title.contains("(Page ")) {
                    try {
                        currentPage = Integer
                                .parseInt(title.substring(title.indexOf("(Page ") + 6, title.indexOf(")")).trim());
                    } catch (Exception e) {
                    }
                }

                if (activeSlots.contains(skillName)) {
                    activeSlots.remove(skillName);
                    player.sendMessage(ChatColor.YELLOW + "Removed " + skillName + " from active slots.");
                } else {
                    int maxSlots = data.getMaxActiveSkillSlots();
                    if (activeSlots.size() >= maxSlots) {
                        player.sendMessage(
                                ChatColor.RED + "You can only equip " + maxSlots + " active skills at a time!");
                    } else {
                        activeSlots.add(skillName);
                        player.sendMessage(ChatColor.GREEN + "Equipped " + skillName + "!");
                    }
                }
                // Refresh the GUI on the same page
                com.darkun7.job.gui.ActiveSkillManagerGUI.openSkillManager(player, data, plugin, currentPage);
            }
        } else if (title.contains("My Professions")) {
            // Handle primary job selection (Shift+Click)
            if (currentItem.getType() == Material.ARROW) {
                // Fallback or ignore
                return;
            }
            if (event.isShiftClick()) {
                String jobName = ChatColor.stripColor(currentItem.getItemMeta().getDisplayName());
                if (jobName.contains("●"))
                    jobName = jobName.replace("●", "").trim();
                if (jobName.contains("["))
                    jobName = jobName.substring(0, jobName.indexOf("[")).trim();

                String oldJob = data.getActiveJob();

                if (!oldJob.equals("NONE")) {
                    plugin.getJobManager().removeJobBonuses(player, oldJob);
                }

                data.setActiveJob(jobName);
                plugin.getJobManager().applyJobBonuses(player, jobName);
                player.sendMessage(ChatColor.GREEN + "Activated primary job: " + ChatColor.GOLD + jobName);
                SkillMenuGUI.openJobSelector(player, data, plugin);
                return;
            }

            // Handle manage skills button
            if (currentItem.getType() == Material.NETHER_STAR) {
                String dName = ChatColor.stripColor(currentItem.getItemMeta().getDisplayName());
                if (dName.contains("Manage Active Skills")) {
                    com.darkun7.job.gui.ActiveSkillManagerGUI.openSkillManager(player, data, plugin);
                    return;
                }
            }

            String jobName = ChatColor.stripColor(currentItem.getItemMeta().getDisplayName());
            if (jobName.contains("●"))
                jobName = jobName.replace("●", "").trim();
            if (jobName.contains("["))
                jobName = jobName.substring(0, jobName.indexOf("[")).trim();
            Job job = plugin.getJobManager().getJob(jobName);
            if (job != null) {
                SkillMenuGUI.openSkillDetails(player, job, data, plugin);
            }
        } else if (title.contains("Skills:")) {
            // Handle skill interaction or back button
            if (currentItem.getType() == Material.ARROW) {
                SkillMenuGUI.openJobSelector(player, data, plugin);
                return;
            }

            String currentJobName = title.split(":")[1].trim();
            Job job = plugin.getJobManager().getJob(currentJobName);
            if (job == null)
                return;

            // Handle Reset Button
            if (currentItem.getType() == Material.BARRIER) {
                if (event.isShiftClick()) {
                    performSkillReset(player, job, data);
                    SkillMenuGUI.openSkillDetails(player, job, data, plugin);
                } else {
                    player.sendMessage(ChatColor.RED + "Please Shift + Click to confirm skill reset.");
                }
                return;
            }

            String skillDisplayName = ChatColor.stripColor(currentItem.getItemMeta().getDisplayName());
            for (Skill skill : job.getSkills()) {
                if (skillDisplayName.contains(skill.getName())) {
                    int currentLevel = data.getSkillLevel(skill.getName());
                    if (event.isLeftClick()) {
                        if (currentLevel > 0) {
                            List<String> activeSlots = data.getActiveSkillsList();
                            if (activeSlots.contains(skill.getName())) {
                                activeSlots.remove(skill.getName());
                                player.sendMessage(
                                        ChatColor.YELLOW + "Removed " + skill.getName() + " from active slots.");
                            } else {
                                int maxSlots = data.getMaxActiveSkillSlots();
                                if (activeSlots.size() >= maxSlots) {
                                    player.sendMessage(ChatColor.RED + "You can only equip " + maxSlots
                                            + " active skills at a time!");
                                } else {
                                    activeSlots.add(skill.getName());
                                    player.sendMessage(ChatColor.GREEN + "Equipped " + skill.getName() + "!");
                                }
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "You must unlock this skill first!");
                        }
                    } else if (event.isRightClick()) {
                        if (currentLevel < skill.getMaxLevel()) {
                            int cost = calculateUpgradeCost(skill, currentLevel + 1);

                            if (data.getSkillPoints(job.getName()) >= cost) {
                                data.setSkillLevel(skill.getName(), currentLevel + 1);
                                data.addSkillPoint(job.getName(), -cost);
                                player.sendMessage(ChatColor.GREEN + "Upgraded " + skill.getName() + " to level "
                                        + (currentLevel + 1));
                            } else {
                                player.sendMessage(
                                        ChatColor.RED + "Not enough points! This upgrade costs " + cost + " points.");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Already at maximum level!");
                        }
                    }
                    SkillMenuGUI.openSkillDetails(player, job, data, plugin);
                    break;
                }
            }
        } else if (title.contains("Rankings") || title.contains("Leaderboard")) {
            if (currentItem.getType() == Material.ARROW) {
                SkillMenuGUI.openJobSelector(player, data, plugin);
            }
        }
    }

    private void performSkillReset(Player player, Job job, PlayerJobData data) {
        String jobName = job.getName();
        int totalRefund = 0;

        for (Skill skill : job.getSkills()) {
            int currentLevel = data.getSkillLevel(skill.getName());
            for (int lvl = 1; lvl <= currentLevel; lvl++) {
                totalRefund += calculateUpgradeCost(skill, lvl);
            }
            data.setSkillLevel(skill.getName(), 0);
            data.setToggledOn(skill.getName(), false);
        }

        // Apply Penalities
        int currentLevel = data.getLevel(jobName);
        if (currentLevel > 1) {
            data.setLevel(jobName, currentLevel - 1);
        }
        data.setExp(jobName, 0);
        data.addSkillPoint(jobName, totalRefund);

        player.sendMessage(ChatColor.GREEN + "Skills for " + jobName + " have been reset!");
        player.sendMessage(ChatColor.YELLOW + "Refunded " + totalRefund + " points. (Penalty: Level -1, XP Reset)");
    }

    private int calculateUpgradeCost(Skill skill, int targetLevel) {
        int baseCost = skill.getPointsPerLevel();
        if (!plugin.getConfig().getBoolean("cost-scaling.enabled", true)) {
            return baseCost;
        }

        int threshold = plugin.getConfig().getInt("cost-scaling.level-threshold", 3);
        int increase = plugin.getConfig().getInt("cost-scaling.cost-increase", 1);

        if (targetLevel > threshold) {
            return baseCost + (targetLevel - threshold) * increase;
        }

        return baseCost;
    }
}
