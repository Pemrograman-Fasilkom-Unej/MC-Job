package com.darkun7.job.managers;

import com.darkun7.job.JobPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardManager {

    private final JobPlugin plugin;
    private final Map<String, List<LeaderboardEntry>> jobLeaderboards;
    private List<LeaderboardEntry> globalLeaderboard;

    public LeaderboardManager(JobPlugin plugin) {
        this.plugin = plugin;
        this.jobLeaderboards = new HashMap<>();
        this.globalLeaderboard = new ArrayList<>();
        updateLeaderboards();
    }

    public void updateLeaderboards() {
        File dataDir = new File(plugin.getDataFolder(), "playerdata");
        if (!dataDir.exists())
            return;

        Map<UUID, Map<String, Integer>> playerJobLevels = new HashMap<>();
        File[] files = dataDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null)
            return;

        for (File file : files) {
            try {
                UUID uuid = UUID.fromString(file.getName().replace(".yml", ""));
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);

                if (config.contains("levels")) {
                    Map<String, Integer> levels = new HashMap<>();
                    for (String job : config.getConfigurationSection("levels").getKeys(false)) {
                        levels.put(job, config.getInt("levels." + job));
                    }
                    playerJobLevels.put(uuid, levels);
                }
            } catch (Exception e) {
                // Skip invalid files
            }
        }

        // Global Leaderboard (Sum of all levels)
        globalLeaderboard = playerJobLevels.entrySet().stream()
                .map(entry -> {
                    int totalLevel = entry.getValue().values().stream().mapToInt(Integer::intValue).sum();
                    return new LeaderboardEntry(entry.getKey(), totalLevel);
                })
                .sorted(Comparator.comparingInt(LeaderboardEntry::getScore).reversed())
                .limit(10)
                .collect(Collectors.toList());

        // Per-Job Leaderboards
        jobLeaderboards.clear();
        for (String jobName : plugin.getJobManager().getLoadedJobs().keySet()) {
            List<LeaderboardEntry> entries = playerJobLevels.entrySet().stream()
                    .filter(entry -> entry.getValue().keySet().stream().anyMatch(k -> k.equalsIgnoreCase(jobName)))
                    .map(entry -> {
                        String key = entry.getValue().keySet().stream()
                                .filter(k -> k.equalsIgnoreCase(jobName))
                                .findFirst().get();
                        return new LeaderboardEntry(entry.getKey(), entry.getValue().get(key));
                    })
                    .sorted(Comparator.comparingInt(LeaderboardEntry::getScore).reversed())
                    .limit(10)
                    .collect(Collectors.toList());
            jobLeaderboards.put(jobName, entries);
        }
    }

    public List<LeaderboardEntry> getGlobalTop() {
        return globalLeaderboard;
    }

    public List<LeaderboardEntry> getJobTop(String jobName) {
        return jobLeaderboards.getOrDefault(jobName, new ArrayList<>());
    }

    public static class LeaderboardEntry {
        private final UUID uuid;
        private final int score;
        private final String playerName;

        public LeaderboardEntry(UUID uuid, int score) {
            this.uuid = uuid;
            this.score = score;
            OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
            this.playerName = op.getName() != null ? op.getName() : "Unknown";
        }

        public UUID getUuid() {
            return uuid;
        }

        public int getScore() {
            return score;
        }

        public String getPlayerName() {
            return playerName;
        }
    }
}
