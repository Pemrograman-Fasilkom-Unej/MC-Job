package com.darkun7.job.managers;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.Job;
import com.darkun7.job.models.PlayerJobData;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ExperienceManager {

    private final JobPlugin plugin;
    private final java.util.Map<UUID, java.util.Map<String, Double>> xpBuffer = new java.util.HashMap<>();
    private final java.util.Map<UUID, Integer> flushTasks = new java.util.HashMap<>();

    public ExperienceManager(JobPlugin plugin) {
        this.plugin = plugin;
    }

    public void addExperience(Player player, String jobName, double amount) {
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        Job job = plugin.getJobManager().getLoadedJobs().get(jobName);
        if (job == null)
            return;

        // Apply XP Booster Multiplier
        if (data.isXpBoosterActive()) {
            amount *= 2.0;
        }

        // Apply XP immediately to data
        data.addExp(jobName, amount);

        // Buffer the display
        UUID uuid = player.getUniqueId();
        xpBuffer.computeIfAbsent(uuid, k -> new java.util.HashMap<>()).merge(jobName, amount, Double::sum);

        // Schedule flush
        if (flushTasks.containsKey(uuid)) {
            plugin.getServer().getScheduler().cancelTask(flushTasks.get(uuid));
        }

        int taskId = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            flushExperience(player);
            flushTasks.remove(uuid);
        }, 40L).getTaskId(); // 2 second delay

        flushTasks.put(uuid, taskId);

        checkLevelUp(player, data, job);
    }

    private void flushExperience(Player player) {
        UUID uuid = player.getUniqueId();
        java.util.Map<String, Double> buffered = xpBuffer.remove(uuid);
        if (buffered == null || buffered.isEmpty())
            return;

        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(uuid);

        // If multiple jobs gained XP, show the one with the most gain
        String topJob = buffered.entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .get().getKey();

        Job job = plugin.getJobManager().getLoadedJobs().get(topJob);
        if (job != null) {
            showExperienceProgress(player, job, data, buffered.get(topJob));
        }
    }

    public void awardBlockXP(Player player, org.bukkit.block.Block block) {
        if (!isEligibleForXP(block)) {
            return;
        }

        org.bukkit.Material type = block.getType();
        if (type == org.bukkit.Material.WHEAT || type == org.bukkit.Material.CARROTS
                || type == org.bukkit.Material.POTATOES
                || type == org.bukkit.Material.BEETROOTS) {
            awardXP(player, "Harvest crops");
        } else if (type == org.bukkit.Material.NETHER_QUARTZ_ORE || type == org.bukkit.Material.ANCIENT_DEBRIS
                || type == org.bukkit.Material.NETHER_GOLD_ORE) {
            awardXP(player, "Mine Nether ores");
        } else if (type.name().contains("LOG") || type.name().contains("WOOD")) {
            awardXP(player, "Chop logs");
        } else if (type.name().contains("ORE") || type == org.bukkit.Material.COAL_ORE
                || type == org.bukkit.Material.IRON_ORE
                || type == org.bukkit.Material.GOLD_ORE || type == org.bukkit.Material.DIAMOND_ORE
                || type == org.bukkit.Material.EMERALD_ORE
                || type == org.bukkit.Material.LAPIS_ORE || type == org.bukkit.Material.REDSTONE_ORE) {
            awardXP(player, "Mine ores");
        } else if (type == org.bukkit.Material.STONE || type == org.bukkit.Material.DEEPSLATE
                || type == org.bukkit.Material.ANDESITE
                || type == org.bukkit.Material.DIORITE || type == org.bukkit.Material.GRANITE) {
            awardXP(player, "Break stone-type blocks");
        } else if (type.name().contains("LEAVES")) {
            awardXP(player, "Break leaves");
        }
    }

    public boolean isEligibleForXP(org.bukkit.block.Block block) {
        // Prevent abuse: Player-placed blocks don't give XP for regular triggers
        if (block.hasMetadata("placed_by_player")) {
            // Exceptions: Crops should still be checkable for maturity even if "placed"
            // (planted)
            if (!isCrop(block.getType())) {
                return false;
            }
        }

        // For crops, only award if mature
        if (isCrop(block.getType())) {
            return isMatureCrop(block);
        }

        return true;
    }

    private boolean isCrop(org.bukkit.Material type) {
        return type == org.bukkit.Material.WHEAT || type == org.bukkit.Material.CARROTS
                || type == org.bukkit.Material.POTATOES || type == org.bukkit.Material.BEETROOTS;
    }

    private boolean isMatureCrop(org.bukkit.block.Block block) {
        if (block.getBlockData() instanceof org.bukkit.block.data.Ageable) {
            org.bukkit.block.data.Ageable age = (org.bukkit.block.data.Ageable) block.getBlockData();
            return age.getAge() >= age.getMaximumAge();
        }
        return false;
    }

    public void awardXP(Player player, String trigger) {
        boolean partyXpShareEnabled = plugin.getConfig().getBoolean("party-xp-share.enabled", false);
        double sharePercentage = plugin.getConfig().getDouble("party-xp-share.percentage", 0.5);
        int shareRadius = plugin.getConfig().getInt("party-xp-share.radius", 50);

        boolean inParty = com.darkun7.job.util.PartyHook.isInParty(player);

        for (Job job : plugin.getJobManager().getLoadedJobs().values()) {
            if (job.getXpTriggers().contains(trigger) || job.getXpRewards().containsKey(trigger)) {
                int amount = job.getXpRewards().getOrDefault(trigger, 10);

                addExperience(player, job.getName(), amount);

                // Share to party members
                if (partyXpShareEnabled && inParty) {
                    int sharedAmount = (int) (amount * sharePercentage);
                    if (sharedAmount > 0) {
                        for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                            if (p.equals(player) || p.isDead() || p.getGameMode() == org.bukkit.GameMode.SPECTATOR)
                                continue;
                            if (p.getWorld().equals(player.getWorld())
                                    && p.getLocation().distance(player.getLocation()) <= shareRadius) {
                                if (com.darkun7.job.util.PartyHook.isInSameParty(player, p)) {
                                    addExperience(p, job.getName(), sharedAmount);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void checkLevelUp(Player player, PlayerJobData data, Job job) {
        String jobName = job.getName();
        int currentLevel = data.getLevel(jobName);
        double nextXp = getRequiredXp(currentLevel);

        if (data.getExp(jobName) >= nextXp) {
            data.setExp(jobName, data.getExp(jobName) - nextXp);
            int newLevel = currentLevel + 1;
            data.setLevel(jobName, newLevel);

            int pointsToAdd = calculatePointsForLevel(newLevel);
            data.addSkillPoint(jobName, pointsToAdd);

            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "LEVEL UP! " + ChatColor.YELLOW
                    + "You reached " + jobName + " Level " + newLevel);
            if (pointsToAdd > 0) {
                player.sendMessage(ChatColor.GREEN + "Gained " + pointsToAdd + " skill point(s)!");
            }

            // Refresh attribute bonuses if this is the active primary job
            if (data.getActiveJob().equalsIgnoreCase(jobName)) {
                plugin.getJobManager().applyJobBonuses(player, jobName);
            }

            // Recursive check
            checkLevelUp(player, data, job);
        }
    }

    public double getRequiredXp(int level) {
        int baseExp = plugin.getConfig().getInt("level-curve.base-xp", 100);
        double multiplier = plugin.getConfig().getDouble("level-curve.multiplier", 1.5);
        double exponential = plugin.getConfig().getDouble("level-curve.exponential", 1.2);

        return baseExp * Math.pow(multiplier, level - 1) * Math.pow(level, exponential);
    }

    private int calculatePointsForLevel(int level) {
        String mode = plugin.getConfig().getString("point-gain.mode", "FIXED");

        if (plugin.getConfig().contains("point-gain.milestones." + level)) {
            return plugin.getConfig().getInt("point-gain.milestones." + level);
        }

        if (mode.equalsIgnoreCase("FIXED")) {
            return plugin.getConfig().getInt("point-gain.fixed-amount", 1);
        } else if (mode.equalsIgnoreCase("EVERY_X_LEVELS")) {
            int interval = plugin.getConfig().getInt("point-gain.every-x-levels", 3);
            return (level % interval == 0) ? 1 : 0;
        }

        return 0;
    }

    public void showExperienceProgress(Player player, Job job, PlayerJobData data, double gained) {
        int level = data.getLevel(job.getName());
        double currentXp = data.getExp(job.getName());
        double nextXp = getRequiredXp(level);

        double percent = (currentXp / nextXp) * 100.0;
        String progressBar = createProgressBar(currentXp, nextXp, 10);

        String message = ChatColor.GOLD + job.getName() + " Lvl " + level + ": " +
                ChatColor.YELLOW + "[" + progressBar + ChatColor.YELLOW + "] " +
                ChatColor.WHITE + (int) percent + "% " +
                ChatColor.GREEN + "(+" + (int) gained + " XP)";

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    private String createProgressBar(double current, double max, int totalBars) {
        int filledBars = (int) ((current / max) * totalBars);
        StringBuilder bar = new StringBuilder();
        bar.append(ChatColor.GREEN);
        for (int i = 0; i < filledBars; i++) {
            bar.append("■");
        }
        bar.append(ChatColor.GRAY);
        for (int i = filledBars; i < totalBars; i++) {
            bar.append("■");
        }
        return bar.toString();
    }
}
