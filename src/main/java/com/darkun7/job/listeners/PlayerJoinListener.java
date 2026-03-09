package com.darkun7.job.listeners;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.Job;
import com.darkun7.job.models.PlayerJobData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private final JobPlugin plugin;

    public PlayerJoinListener(JobPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Initializing data for the player
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(uuid);

        // Check if this is a first-time initialization (levels map is empty)
        if (data.getLevels().isEmpty()) {
            plugin.getLogger().info("Initializing default job data for new player: " + player.getName());

            // Pre-populate all jobs at level 1
            for (Job job : plugin.getJobManager().getLoadedJobs().values()) {
                data.setLevel(job.getName(), 1);
            }

            // Set default job if configured
            String defaultJobName = plugin.getConfig().getString("default-job", "NONE");
            if (!defaultJobName.equals("NONE") && plugin.getJobManager().getJob(defaultJobName) != null) {
                data.setActiveJob(defaultJobName);

                // Apply bonuses for the default job
                plugin.getJobManager().applyJobBonuses(player, defaultJobName);

                player.sendMessage(ChatColor.GREEN + "Welcome! Your primary profession has been set to: "
                        + ChatColor.GOLD + defaultJobName);
            }

            // Save immediately
            plugin.getPlayerDataManager().savePlayer(uuid);
        } else {
            // If returning player, ensure their active job bonuses are applied
            String activeJob = data.getActiveJob();
            if (!activeJob.equals("NONE")) {
                plugin.getJobManager().applyJobBonuses(player, activeJob);
            }
        }
    }
}
