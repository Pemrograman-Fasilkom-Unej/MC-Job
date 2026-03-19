package com.darkun7.job.managers;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.Job;
import com.darkun7.job.models.Skill;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class JobManager {

    private final JobPlugin plugin;
    private final Map<String, Job> loadedJobs;

    public JobManager(JobPlugin plugin) {
        this.plugin = plugin;
        this.loadedJobs = new HashMap<>();
        loadJobs();
    }

    public void reloadJobs() {
        loadedJobs.clear();
        loadJobs();
    }

    public void loadJobs() {
        File jobsDir = new File(plugin.getDataFolder(), "jobs");
        if (!jobsDir.exists()) {
            jobsDir.mkdirs();
        }

        File[] files = jobsDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            extractDefaultJobs();
            files = jobsDir.listFiles((dir, name) -> name.endsWith(".yml"));
        }

        if (files != null) {
            for (File file : files) {
                loadJobFromYaml(file);
            }
        }
    }

    private void loadJobFromYaml(File file) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String name = config.getString("name");
        List<String> theme = config.getStringList("theme");
        List<String> xpTriggers = config.getStringList("xp_triggers");

        Job job = new Job(name, theme, xpTriggers);

        // Load skills
        if (config.contains("skills")) {
            for (String skillKey : config.getConfigurationSection("skills").getKeys(false)) {
                String path = "skills." + skillKey;
                String type = config.getString(path + ".type");
                String category = config.getString(path + ".category");
                String description = config.getString(path + ".description");

                Skill skill = new Skill(skillKey, type, category, description);
                skill.setMaxLevel(config.getInt(path + ".max_level", 5));
                skill.setPointsPerLevel(config.getInt(path + ".points_per_level", 1));
                skill.setToggleable(config.getBoolean(path + ".toggleable", "Active".equals(type)));
                skill.setTriggerItem(config.getString(path + ".trigger_item", "STICK"));
                skill.setIcon(config.getString(path + ".icon", skill.getTriggerItem()));
                skill.setCooldown(config.getInt(path + ".cooldown", 0));
                skill.setManaCost(config.getInt(path + ".mana_cost", 0));
                if (config.contains(path + ".preview_text")) {
                    skill.setPreviewText(config.getString(path + ".preview_text"));
                }
                if (config.contains(path + ".activation_text")) {
                    skill.setActivationText(config.getString(path + ".activation_text"));
                }
                skill.setChancePerLevel(config.getDouble(path + ".chance_per_level", 20.0));
                skill.setMaxChance(config.getDouble(path + ".max_chance", 100.0));

                job.addSkill(skill);
            }
        }

        // Load requirements
        job.setRequirements(config.getStringList("requirements"));

        // Load bonuses
        if (config.contains("bonuses") && config.isConfigurationSection("bonuses")) {
            for (String attr : config.getConfigurationSection("bonuses").getKeys(true)) {
                if (config.isDouble("bonuses." + attr) || config.isInt("bonuses." + attr)) {
                    job.setBonus(attr, config.getDouble("bonuses." + attr));
                }
            }
        }

        // Load specific XP rewards
        if (config.contains("rewards")) {
            for (String trigger : config.getConfigurationSection("rewards").getKeys(false)) {
                job.setXpReward(trigger, config.getInt("rewards." + trigger));
            }
        }

        loadedJobs.put(name, job);
    }

    private void extractDefaultJobs() {
        String[] defaultJobs = {
                "Beekeeper", "Breeder", "Tamer", "Brewer", "Enchanter",
                "Farmer", "Smith", "Hunter", "Fisherman", "Lumberjack",
                "Miner", "Nethermancer", "Endwalker", "Journeyman", "Engineer", "Culinary", "Lightworker", "IceLogger"
        };

        for (String jobName : defaultJobs) {
            String resourcePath = "jobs/" + jobName + ".yml";
            try {
                plugin.saveResource(resourcePath, false);
                plugin.getLogger().info("Extracted default job: " + jobName);
            } catch (Exception e) {
                // If the resource doesn't exist or other error, fallback to bootstrap
                plugin.getLogger().warning("Could not extract " + resourcePath + ", falling back to bootstrap.");
            }
        }

        // Final fallback if files still missing after extraction attempts
        File jobsDir = new File(plugin.getDataFolder(), "jobs");
        File[] files = jobsDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            bootstrapFromConcept();
        }
    }

    private void bootstrapFromConcept() {
        File conceptFile = new File(plugin.getDataFolder().getParentFile(), "Job/concept.json");
        if (!conceptFile.exists()) {
            plugin.getLogger().warning("Concept.json not found in plugin parent directory. Path checked: "
                    + conceptFile.getAbsolutePath());
            return;
        }

        try (FileReader reader = new FileReader(conceptFile)) {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Map<String, Object>>>() {
            }.getType();
            Map<String, Map<String, Object>> data = gson.fromJson(reader, type);

            for (Map.Entry<String, Map<String, Object>> entry : data.entrySet()) {
                String jobName = entry.getKey();
                Map<String, Object> jobData = entry.getValue();

                File jobFile = new File(plugin.getDataFolder(), "jobs/" + jobName + ".yml");
                FileConfiguration config = new YamlConfiguration();

                config.set("name", jobName);
                config.set("theme", jobData.get("theme"));
                config.set("xp_triggers", jobData.get("xp_triggers"));

                List<Map<String, String>> skillsData = (List<Map<String, String>>) jobData.get("skills");
                for (Map<String, String> skillData : skillsData) {
                    String skillName = skillData.get("name");
                    String path = "skills." + skillName;
                    config.set(path + ".type", skillData.get("type"));
                    config.set(path + ".category", skillData.get("category"));
                    config.set(path + ".description", skillData.get("description"));
                    config.set(path + ".max_level", 5);
                    config.set(path + ".points_per_level", 1);
                    config.set(path + ".toggleable", "Active".equals(skillData.get("type")));
                    config.set(path + ".trigger_item", "STICK");
                    config.set(path + ".icon", "STICK");
                }

                // Default rewards section to encourage configuration
                for (String trigger : (List<String>) jobData.get("xp_triggers")) {
                    config.set("rewards." + trigger, 10);
                }

                config.save(jobFile);
                plugin.getLogger().info("Bootstrapped job file: " + jobName + ".yml");
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Could not parse concept.json!");
            e.printStackTrace();
        }
    }

    public void applyJobBonuses(org.bukkit.entity.Player player, String jobName) {
        Job job = getJob(jobName);
        if (job == null)
            return;

        int level = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId()).getLevel(jobName);

        int limitLevel = plugin.getConfig().getInt("stats-scaling.limit-level", 25);
        double multiplier = plugin.getConfig().getDouble("stats-scaling.bonus-multiplier-per-level", 0.375);

        // Cap the level for bonus calculation
        int effectiveLevel = Math.min(level, limitLevel);

        for (java.util.Map.Entry<String, Double> entry : job.getBonuses().entrySet()) {
            try {
                String key = entry.getKey().toLowerCase();
                org.bukkit.attribute.Attribute attr = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(key));
                
                // Fallback for 1.21+ (strip generic. and player. prefixes)
                if (attr == null) {
                    String coreKey = key;
                    if (key.startsWith("generic.")) coreKey = key.substring(8);
                    else if (key.startsWith("player.")) coreKey = key.substring(7);
                    else if (key.startsWith("zombie.")) coreKey = key.substring(7);
                    
                    attr = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(coreKey));
                }

                if (attr == null) {
                    plugin.getLogger().warning("Unknown attribute: " + key);
                    continue;
                }
                org.bukkit.attribute.AttributeInstance instance = player.getAttribute(attr);
                if (instance != null) {
                    double baseBonus = entry.getValue();
                    // Formula: Bonus = BaseValue + (BaseValue * (effectiveLevel - 1) * multiplier)
                    double leveledBonus = baseBonus + (baseBonus * (effectiveLevel - 1) * multiplier);
                    instance.setBaseValue(instance.getDefaultValue() + leveledBonus);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Could not apply bonus attribute: " + entry.getKey());
            }
        }
    }

    public void removeJobBonuses(org.bukkit.entity.Player player, String jobName) {
        Job job = getJob(jobName);
        if (job == null)
            return;

        for (java.util.Map.Entry<String, Double> entry : job.getBonuses().entrySet()) {
            try {
                String key = entry.getKey().toLowerCase();
                org.bukkit.attribute.Attribute attr = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(key));
                
                // Fallback for 1.21+
                if (attr == null) {
                    String coreKey = key;
                    if (key.startsWith("generic.")) coreKey = key.substring(8);
                    else if (key.startsWith("player.")) coreKey = key.substring(7);
                    else if (key.startsWith("zombie.")) coreKey = key.substring(7);
                    
                    attr = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(coreKey));
                }

                if (attr != null) {
                    org.bukkit.attribute.AttributeInstance instance = player.getAttribute(attr);
                    if (instance != null) {
                        instance.setBaseValue(instance.getDefaultValue());
                    }
                }
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    public Job getJob(String name) {
        if (name == null)
            return null;
        Job job = loadedJobs.get(name);
        if (job != null)
            return job;

        // Case-insensitive fallback
        for (Job j : loadedJobs.values()) {
            if (j.getName().equalsIgnoreCase(name)) {
                return j;
            }
        }
        return null;
    }

    public Map<String, Job> getLoadedJobs() {
        return loadedJobs;
    }
}
