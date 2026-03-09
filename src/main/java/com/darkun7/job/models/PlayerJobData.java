package com.darkun7.job.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerJobData {
    private final UUID uuid;
    private String activeJob; // The job currently providing attribute bonuses
    private final Map<String, Integer> levels; // job -> level
    private final Map<String, Double> exp; // job -> current xp
    private final Map<String, Integer> skillPoints; // job -> available points
    private final Map<String, Integer> skillLevels; // skill name -> level
    private final Map<String, Boolean> toggledOn; // skill name -> enabled
    private final List<String> activeSkills; // List of currently active skills (max based on bosses)
    private final List<String> killedBosses; // List of boss names killed for +1 slot each
    private long xpBoosterExpiry; // timestamp (ms) when booster ends

    public PlayerJobData(UUID uuid) {
        this.uuid = uuid;
        this.activeJob = "NONE";
        this.levels = new HashMap<>();
        this.exp = new HashMap<>();
        this.skillPoints = new HashMap<>();
        this.skillLevels = new HashMap<>();
        this.toggledOn = new HashMap<>();
        this.activeSkills = new java.util.ArrayList<>();
        this.killedBosses = new java.util.ArrayList<>();
        this.xpBoosterExpiry = 0;
    }

    public long getXpBoosterExpiry() {
        return xpBoosterExpiry;
    }

    public void setXpBoosterExpiry(long expiry) {
        this.xpBoosterExpiry = expiry;
    }

    public boolean isXpBoosterActive() {
        return System.currentTimeMillis() < xpBoosterExpiry;
    }

    public String getActiveJob() {
        return activeJob;
    }

    public void setActiveJob(String activeJob) {
        this.activeJob = activeJob;
    }

    public List<String> getActiveSkillsList() {
        return activeSkills;
    }

    public List<String> getKilledBosses() {
        return killedBosses;
    }

    public int getMaxActiveSkillSlots() {
        return 4 + killedBosses.size();
    }

    public void addKilledBoss(String bossName) {
        if (!killedBosses.contains(bossName)) {
            killedBosses.add(bossName);
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getLevel(String job) {
        return levels.getOrDefault(job, 1);
    }

    public void setLevel(String job, int level) {
        this.levels.put(job, level);
    }

    public double getExp(String job) {
        return exp.getOrDefault(job, 0.0);
    }

    public void setExp(String job, double amount) {
        this.exp.put(job, amount);
    }

    public void addExp(String job, double amount) {
        setExp(job, getExp(job) + amount);
    }

    public int getSkillPoints(String job) {
        return skillPoints.getOrDefault(job, 0);
    }

    public void setSkillPoints(String job, int points) {
        this.skillPoints.put(job, points);
    }

    public void addSkillPoint(String job, int amount) {
        setSkillPoints(job, getSkillPoints(job) + amount);
    }

    public int getSkillLevel(String skillName) {
        return skillLevels.getOrDefault(skillName, 0);
    }

    public void setSkillLevel(String skillName, int level) {
        this.skillLevels.put(skillName, level);
    }

    public boolean isToggledOn(String skillName) {
        return toggledOn.getOrDefault(skillName, false);
    }

    public void setToggledOn(String skillName, boolean toggled) {
        this.toggledOn.put(skillName, toggled);
    }

    public Map<String, Integer> getSkillLevels() {
        return skillLevels;
    }

    public Map<String, Boolean> getToggledOn() {
        return toggledOn;
    }

    public Map<String, Integer> getLevels() {
        return levels;
    }

    public Map<String, Double> getExpMap() {
        return exp;
    }

    public Map<String, Integer> getSkillPointsMap() {
        return skillPoints;
    }
}
