package com.darkun7.job.managers;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private final JobPlugin plugin;
    private final Map<UUID, PlayerJobData> playerData;

    public PlayerDataManager(JobPlugin plugin) {
        this.plugin = plugin;
        this.playerData = new HashMap<>();
    }

    public PlayerJobData getPlayerData(UUID uuid) {
        if (!playerData.containsKey(uuid)) {
            loadPlayer(uuid);
        }
        return playerData.get(uuid);
    }

    private void loadPlayer(UUID uuid) {
        File dataDir = new File(plugin.getDataFolder(), "playerdata");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        File playerFile = new File(dataDir, uuid.toString() + ".yml");
        PlayerJobData data = new PlayerJobData(uuid);

        if (playerFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

            data.setActiveJob(config.getString("active-job", "NONE"));

            if (config.contains("active-skills")) {
                data.getActiveSkillsList().addAll(config.getStringList("active-skills"));
            }

            if (config.contains("levels")) {
                for (String job : config.getConfigurationSection("levels").getKeys(false)) {
                    data.setLevel(job, config.getInt("levels." + job));
                    data.setExp(job, config.getDouble("experience." + job, 0.0));
                    data.setSkillPoints(job, config.getInt("points." + job, 0));
                }
            }

            if (config.contains("skills")) {
                for (String skillName : config.getConfigurationSection("skills").getKeys(false)) {
                    data.setSkillLevel(skillName, config.getInt("skills." + skillName));
                }
            }

            if (config.contains("toggled")) {
                for (String skillName : config.getConfigurationSection("toggled").getKeys(false)) {
                    data.setToggledOn(skillName, config.getBoolean("toggled." + skillName));
                }
            }

            if (config.contains("killed-bosses")) {
                data.getKilledBosses().addAll(config.getStringList("killed-bosses"));
            }

            data.setXpBoosterExpiry(config.getLong("xp-booster-expiry", 0));
        }

        playerData.put(uuid, data);
    }

    public void savePlayer(UUID uuid) {
        PlayerJobData data = playerData.get(uuid);
        if (data == null)
            return;

        File dataDir = new File(plugin.getDataFolder(), "playerdata");
        File playerFile = new File(dataDir, uuid.toString() + ".yml");
        FileConfiguration config = new YamlConfiguration();

        config.set("active-job", data.getActiveJob());
        config.set("active-skills", data.getActiveSkillsList());
        config.set("xp-booster-expiry", data.getXpBoosterExpiry());

        for (Map.Entry<String, Integer> entry : data.getLevels().entrySet()) {
            config.set("levels." + entry.getKey(), entry.getValue());
            config.set("experience." + entry.getKey(), data.getExp(entry.getKey()));
            config.set("points." + entry.getKey(), data.getSkillPoints(entry.getKey()));
        }

        for (Map.Entry<String, Integer> entry : data.getSkillLevels().entrySet()) {
            config.set("skills." + entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Boolean> entry : data.getToggledOn().entrySet()) {
            config.set("toggled." + entry.getKey(), entry.getValue());
        }

        config.set("killed-bosses", data.getKilledBosses());

        try {
            config.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveAll() {
        for (UUID uuid : playerData.keySet()) {
            savePlayer(uuid);
        }
    }
}
