package com.darkun7.job.commands;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.Job;
import com.darkun7.job.models.PlayerJobData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JobCommand implements CommandExecutor, TabCompleter {

    private final JobPlugin plugin;

    public JobCommand(JobPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Handle shortcut label
        if (label.equalsIgnoreCase("skill") || label.equalsIgnoreCase("skills")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by players.");
                return true;
            }
            Player player = (Player) sender;
            PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            com.darkun7.job.gui.SkillMenuGUI.openJobSelector(player, data, plugin);
            return true;
        }

        // Allow console for admin commands
        if (!(sender instanceof Player) && (args.length == 0 || !args[0].equalsIgnoreCase("addxp"))) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        // If sender is a player, show help menu
        Player player = (sender instanceof Player) ? (Player) sender : null;
        if (player != null && args.length == 0) {
            player.sendMessage(ChatColor.GOLD + "=== Job Menu ===");
            player.sendMessage(ChatColor.YELLOW + "/job list " + ChatColor.WHITE + "- List all available jobs.");
            player.sendMessage(ChatColor.YELLOW + "/job stats " + ChatColor.WHITE + "- View your job stats.");
            player.sendMessage(ChatColor.YELLOW + "/job top [job] " + ChatColor.WHITE + "- View job leaderboards.");
            if (player.hasPermission("job.admin")) {
                player.sendMessage(
                        ChatColor.YELLOW + "/job reload " + ChatColor.WHITE + "- Reload all configurations.");
                player.sendMessage(ChatColor.YELLOW + "/job addxp <job> <amount> [player] " + ChatColor.WHITE
                        + "- Grant XP to a job.");
            }
            player.sendMessage(ChatColor.GRAY + "Tip: Press " + ChatColor.AQUA + "Shift + F" + ChatColor.GRAY
                    + " to open skill menu.");
            return true;
        } else if (player == null && args.length == 0) {
            sender.sendMessage("Usage: /job addxp <job> <amount> [player]");
            return true;
        }

        String sub = args[0].toLowerCase();
        if (sub.equals("reload")) {
            if (player != null && !player.hasPermission("job.admin") || player == null && !sender.isOp()) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to do this.");
                return true;
            }
            plugin.reloadConfig();
            plugin.getJobManager().reloadJobs();
            sender.sendMessage(ChatColor.GREEN + "Plugin configurations and jobs reloaded!");
        } else if (sub.equals("list")) {
            if (player == null) {
                sender.sendMessage("Use in-game.");
                return true;
            }
            player.sendMessage(ChatColor.GOLD + "Available Jobs:");
            for (String name : plugin.getJobManager().getLoadedJobs().keySet()) {
                player.sendMessage(ChatColor.YELLOW + "- " + name);
            }
        } else if (sub.equals("skill") || sub.equals("skills")) {
            if (player == null) {
                sender.sendMessage("Use in-game.");
                return true;
            }
            PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            com.darkun7.job.gui.SkillMenuGUI.openJobSelector(player, data, plugin);
        } else if (sub.equals("stats")) {
            if (player == null) {
                sender.sendMessage("Use in-game.");
                return true;
            }
            PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            player.sendMessage(ChatColor.GOLD + "=== Your Career Path ===");
            for (Map.Entry<String, Integer> entry : data.getLevels().entrySet()) {
                if (entry.getValue() >= 1) {
                    player.sendMessage(
                            ChatColor.YELLOW + entry.getKey() + ": " + ChatColor.WHITE + "Level " + entry.getValue()
                                    + ChatColor.GRAY + " (" + (int) data.getExp(entry.getKey()) + " XP)");
                }
            }
            if (data.getLevels().isEmpty()) {
                player.sendMessage(ChatColor.GRAY + "No job experience yet. Go out and perform actions!");
            }
        } else if (sub.equals("addxp")) {
            boolean isAdmin = (player != null && player.hasPermission("job.admin"))
                    || (player == null && sender.isOp());
            if (!isAdmin) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to do this.");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /job addxp <job> <amount> [player]");
                return true;
            }
            Job job = plugin.getJobManager().getJob(args[1]);
            if (job == null) {
                sender.sendMessage(ChatColor.RED + "Invalid job: " + args[1]);
                return true;
            }
            double amount;
            try {
                amount = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid amount: " + args[2]);
                return true;
            }
            // Determine target player
            Player target;
            if (args.length >= 4) {
                target = plugin.getServer().getPlayerExact(args[3]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player '" + args[3] + "' is not online.");
                    return true;
                }
            } else if (player != null) {
                target = player;
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /job addxp <job> <amount> <player>");
                return true;
            }
            // Apply the XP
            PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(target.getUniqueId());
            data.addExp(job.getName(), amount);
            // Trigger level-up check by simulating small addExp inside TriggerListener
            // logic
            checkLevelUp(target, data, job);
            // Save data
            plugin.getPlayerDataManager().savePlayer(target.getUniqueId());
            sender.sendMessage(ChatColor.GREEN + "Added " + (int) amount + " XP to " + job.getName()
                    + " for " + target.getName() + "!");
            if (!target.equals(player)) {
                target.sendMessage(ChatColor.AQUA + "You received " + (int) amount + " XP in "
                        + ChatColor.GOLD + job.getName() + ChatColor.AQUA + "!");
            }
        } else if (sub.equals("top")) {
            if (player == null) {
                sender.sendMessage("Use in-game.");
                return true;
            }
            String targetJob = (args.length > 1) ? args[1] : "Global";
            if (!targetJob.equalsIgnoreCase("Global")) {
                Job job = plugin.getJobManager().getJob(targetJob);
                if (job == null) {
                    player.sendMessage(ChatColor.RED + "Invalid job: " + targetJob);
                    return true;
                }
                targetJob = job.getName();
            }
            plugin.getLeaderboardManager().updateLeaderboards();
            com.darkun7.job.gui.LeaderboardGUI.openLeaderboard(player, targetJob, plugin);
        }
        return true;
    }

    /** Mirror of TriggerListener#checkLevelUp so addxp can trigger leveling */
    private void checkLevelUp(Player player, PlayerJobData data, Job job) {
        String jobName = job.getName();
        int currentLevel = data.getLevel(jobName);
        double baseExp = plugin.getConfig().getInt("level-curve.base-xp", 100);
        double multiplier = plugin.getConfig().getDouble("level-curve.multiplier", 1.5);
        double exponential = plugin.getConfig().getDouble("level-curve.exponential", 1.2);
        double nextXp = baseExp * Math.pow(multiplier, currentLevel - 1) * Math.pow(currentLevel, exponential);

        while (data.getExp(jobName) >= nextXp) {
            data.setExp(jobName, data.getExp(jobName) - nextXp);
            int newLevel = currentLevel + 1;
            data.setLevel(jobName, newLevel);
            int pointsToAdd = 1;
            data.addSkillPoint(jobName, pointsToAdd);
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "LEVEL UP! " + ChatColor.YELLOW
                    + "You reached " + jobName + " Level " + newLevel);
            player.sendMessage(ChatColor.GREEN + "Gained " + pointsToAdd + " skill point(s)!");
            currentLevel = newLevel;
            nextXp = baseExp * Math.pow(multiplier, currentLevel - 1) * Math.pow(currentLevel, exponential);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            subs.add("list");
            subs.add("stats");
            subs.add("top");
            subs.add("skill");
            if (sender.hasPermission("job.admin")) {
                subs.add("reload");
                subs.add("addxp");
            }
            return subs;
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("addxp") || args[0].equalsIgnoreCase("top"))) {
            List<String> list = new ArrayList<>(plugin.getJobManager().getLoadedJobs().keySet());
            if (args[0].equalsIgnoreCase("top"))
                list.add("Global");
            return list;
        }
        // Tab-complete player names for /job addxp <job> <amount> [player]
        if (args.length == 4 && args[0].equalsIgnoreCase("addxp")) {
            return plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
