package com.darkun7.job.models;

import java.util.List;

public class Skill {
    private String name;
    private String type; // Active or Passive
    private String category;
    private String description;
    private int maxLevel;
    private int pointsPerLevel;
    private boolean toggleable;
    private String triggerItem;
    private String icon;
    private int cooldown; // in seconds
    private int manaCost;
    private String previewText;
    private String activationText;
    private double chancePerLevel;
    private double maxChance;

    public Skill(String name, String type, String category, String description) {
        this.name = name;
        this.type = type;
        this.category = category;
        this.description = description;
        this.maxLevel = 5;
        this.pointsPerLevel = 1;
        this.toggleable = "Active".equalsIgnoreCase(type);
        this.triggerItem = "STICK";
        this.icon = "STICK";
        this.cooldown = 0;
        this.manaCost = 0;
        this.previewText = "✨ Enhances primary skill effect!";
        this.chancePerLevel = 20.0;
        this.maxChance = 100.0;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public int getPointsPerLevel() {
        return pointsPerLevel;
    }

    public void setPointsPerLevel(int pointsPerLevel) {
        this.pointsPerLevel = pointsPerLevel;
    }

    public boolean isToggleable() {
        return toggleable;
    }

    public void setToggleable(boolean toggleable) {
        this.toggleable = toggleable;
    }

    public String getTriggerItem() {
        return triggerItem;
    }

    public void setTriggerItem(String triggerItem) {
        this.triggerItem = triggerItem;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public int getManaCost() {
        return manaCost;
    }

    public void setManaCost(int manaCost) {
        this.manaCost = manaCost;
    }

    public String getPreviewText() {
        return previewText;
    }

    public void setPreviewText(String previewText) {
        this.previewText = previewText;
    }

    public String getActivationText() {
        return activationText;
    }

    public void setActivationText(String activationText) {
        this.activationText = activationText;
    }

    public double getChancePerLevel() {
        return chancePerLevel;
    }

    public void setChancePerLevel(double chancePerLevel) {
        this.chancePerLevel = chancePerLevel;
    }

    public double getMaxChance() {
        return maxChance;
    }

    public void setMaxChance(double maxChance) {
        this.maxChance = maxChance;
    }
}
