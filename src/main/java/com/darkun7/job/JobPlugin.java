package com.darkun7.job;

import com.darkun7.job.commands.JobCommand;
import com.darkun7.job.listeners.JobActiveSkillListener;
import com.darkun7.job.listeners.PlayerJoinListener;
import com.darkun7.job.listeners.SkillListener;
import com.darkun7.job.listeners.TriggerListener;
import com.darkun7.job.listeners.jobs.*;
import com.darkun7.job.managers.JobManager;
import com.darkun7.job.managers.PlayerDataManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class JobPlugin extends JavaPlugin {

    private static JobPlugin instance;
    private JobManager jobManager;
    private PlayerDataManager playerDataManager;
    private com.darkun7.job.managers.LeaderboardManager leaderboardManager;
    private com.darkun7.job.managers.ExperienceManager experienceManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        // Ensure jobs directory exists
        File jobsDir = new File(getDataFolder(), "jobs");
        if (!jobsDir.exists()) {
            jobsDir.mkdirs();
        }

        this.jobManager = new JobManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.leaderboardManager = new com.darkun7.job.managers.LeaderboardManager(this);
        this.experienceManager = new com.darkun7.job.managers.ExperienceManager(this);

        // Register commands
        getCommand("job").setExecutor(new JobCommand(this));
        getCommand("skill").setExecutor(new JobCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new SkillListener(this), this);
        getServer().getPluginManager().registerEvents(new TriggerListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new JobActiveSkillListener(this), this);
        getServer().getPluginManager().registerEvents(new com.darkun7.job.listeners.SkillModeListener(this), this);
        getServer().getPluginManager().registerEvents(new com.darkun7.job.listeners.BossKillListener(this), this);
        getServer().getPluginManager().registerEvents(new com.darkun7.job.listeners.BoosterListener(this), this);

        // Register Job-Specific Listeners
        getServer().getPluginManager().registerEvents(new BeekeeperListener(this), this);
        getServer().getPluginManager().registerEvents(new BreederListener(this), this);
        getServer().getPluginManager().registerEvents(new BrewerListener(this), this);
        getServer().getPluginManager().registerEvents(new EnchanterListener(this), this);
        getServer().getPluginManager().registerEvents(new EndwalkerListener(this), this);
        getServer().getPluginManager().registerEvents(new FarmerListener(this), this);
        getServer().getPluginManager().registerEvents(new FishermanListener(this), this);
        getServer().getPluginManager().registerEvents(new HunterListener(this), this);
        getServer().getPluginManager().registerEvents(new LumberjackListener(this), this);
        getServer().getPluginManager().registerEvents(new MinerListener(this), this);
        getServer().getPluginManager().registerEvents(new NethermancerListener(this), this);
        getServer().getPluginManager().registerEvents(new SmithListener(this), this);
        getServer().getPluginManager().registerEvents(new TamerListener(this), this);

        // Start periodic tasks
        new com.darkun7.job.managers.PeriodicSkillTaskManager(this).start();

        getLogger().info("Job Plugin enabled!");
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }
        getLogger().info("Job Plugin disabled!");
    }

    public static JobPlugin getInstance() {
        return instance;
    }

    public JobManager getJobManager() {
        return jobManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public com.darkun7.job.managers.LeaderboardManager getLeaderboardManager() {
        return leaderboardManager;
    }

    public com.darkun7.job.managers.ExperienceManager getExperienceManager() {
        return experienceManager;
    }
}
