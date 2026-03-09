package com.darkun7.job.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Job {
    private String name;
    private List<String> theme;
    private List<String> xpTriggers;
    private List<Skill> skills;
    private Map<String, Integer> xpRewards; // Mapping triggers to XP reward
    private List<String> requirements;
    private Map<String, Double> bonuses; // Attribute bonuses like generic.max_health: 2.0

    public Job(String name, List<String> theme, List<String> xpTriggers) {
        this.name = name;
        this.theme = theme;
        this.xpTriggers = xpTriggers;
        this.skills = new ArrayList<>();
        this.xpRewards = new HashMap<>();
        this.requirements = new ArrayList<>();
        this.bonuses = new HashMap<>();

        // Default XP for testing
        for (String trigger : xpTriggers) {
            xpRewards.put(trigger, 10);
        }
    }

    public String getName() {
        return name;
    }

    public List<String> getTheme() {
        return theme;
    }

    public List<String> getXpTriggers() {
        return xpTriggers;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void addSkill(Skill skill) {
        this.skills.add(skill);
    }

    public Map<String, Integer> getXpRewards() {
        return xpRewards;
    }

    public void setXpReward(String trigger, int amount) {
        this.xpRewards.put(trigger, amount);
    }

    public List<String> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<String> requirements) {
        this.requirements = requirements;
    }

    public Map<String, Double> getBonuses() {
        return bonuses;
    }

    public void setBonus(String attribute, double value) {
        this.bonuses.put(attribute, value);
    }

    public Skill getSkill(String name) {
        for (Skill skill : skills) {
            if (skill.getName().equalsIgnoreCase(name)) {
                return skill;
            }
        }
        return null;
    }
}
