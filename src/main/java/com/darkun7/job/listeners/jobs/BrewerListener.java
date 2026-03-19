package com.darkun7.job.listeners.jobs;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BrewerListener implements Listener {

    private final JobPlugin plugin;
    private final Map<Location, UUID> lastBrewer = new HashMap<>();

    public BrewerListener(JobPlugin plugin) {
        this.plugin = plugin;
    }

    private void addXPToJob(Player player, String trigger) {
        plugin.getExperienceManager().awardXP(player, trigger);
    }

    @EventHandler
    public void onBrewOpen(InventoryClickEvent event) {
        if (event.getInventory().getType() == InventoryType.BREWING) {
            lastBrewer.put(event.getInventory().getLocation(), event.getWhoClicked().getUniqueId());
        }
    }

    @EventHandler
    public void onBrew(BrewEvent event) {
        Location loc = event.getBlock().getLocation();

        // Award XP for Brewing
        if (lastBrewer.containsKey(loc)) {
            Player player = Bukkit.getPlayer(lastBrewer.get(loc));
            if (player != null) {
                addXPToJob(player, "Brew potions");

                // Nethermancer - Brew Fire Resistance potions
                if (event.getContents().getIngredient() != null && event.getContents().getIngredient().getType() == Material.MAGMA_CREAM) {
                    addXPToJob(player, "Brew Fire Resistance potions");
                }
            }
        }

        // 1. Extra Brew (from JobEnvironmentListener)
        handleExtraBrew(event, loc);

        // 2. Efficient Mixture (from SkillEffectListener)
        handleEfficientMixture(event, loc);
    }

    private void handleExtraBrew(BrewEvent event, Location loc) {
        Player brewer = null;
        double closestDist = 25; // Brew stand range check
        for (Player p : Bukkit.getOnlinePlayers()) {
            double dist = p.getLocation().distance(loc);
            if (dist < closestDist) {
                closestDist = dist;
                brewer = p;
            }
        }
        if (brewer == null)
            return;

        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(brewer.getUniqueId());

        if (data.getSkillLevel("Extra Brew") > 0 && data.getActiveSkillsList().contains("Extra Brew")) {
            final double multiplier = 1.0 + 0.20 * data.getSkillLevel("Extra Brew");
            final Player finalBrewer = brewer;
            new BukkitRunnable() {
                @Override
                public void run() {
                    BrewerInventory inv = (BrewerInventory) event.getContents();
                    for (int i = 0; i < 3; i++) {
                        ItemStack item = inv.getItem(i);
                        if (item != null && item.getItemMeta() instanceof PotionMeta) {
                            PotionMeta meta = (PotionMeta) item.getItemMeta();
                            List<PotionEffect> newEffects = new ArrayList<>();
                            for (PotionEffect effect : meta.getCustomEffects()) {
                                int newDuration = (int) (effect.getDuration() * multiplier);
                                newEffects.add(new PotionEffect(effect.getType(), newDuration, effect.getAmplifier()));
                            }
                            meta.clearCustomEffects();
                            for (PotionEffect e : newEffects) {
                                meta.addCustomEffect(e, true);
                            }
                            item.setItemMeta(meta);
                        }
                    }
                    finalBrewer.sendMessage(ChatColor.LIGHT_PURPLE + "[Extra Brew] Potions last "
                            + (int) ((multiplier - 1) * 100) + "% longer!");
                    addXPToJob(finalBrewer, "Extend or upgrade potions");
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    private void handleEfficientMixture(BrewEvent event, Location loc) {
        if (!lastBrewer.containsKey(loc))
            return;

        UUID brewerUUID = lastBrewer.get(loc);
        Player player = plugin.getServer().getPlayer(brewerUUID);
        if (player == null)
            return;

        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(brewerUUID);
        if (!data.getActiveSkillsList().contains("Efficient Mixture"))
            return;

        int level = data.getSkillLevel("Efficient Mixture");
        if (level <= 0)
            return;

        com.darkun7.job.models.Skill skill = plugin.getJobManager().getJob("Brewer").getSkill("Efficient Mixture");
        double chancePerLevel = (skill != null) ? skill.getChancePerLevel() / 100.0 : 0.20;
        double maxChance = (skill != null) ? skill.getMaxChance() / 100.0 : 1.0;
        double chance = Math.min(maxChance, level * chancePerLevel);

        if (java.util.concurrent.ThreadLocalRandom.current().nextDouble() > chance)
            return;

        BrewerInventory inv = event.getContents();
        boolean upgraded = false;

        for (int i = 0; i < 3; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && (item.getType() == Material.POTION ||
                    item.getType() == Material.SPLASH_POTION ||
                    item.getType() == Material.LINGERING_POTION)) {

                if (upgradePotion(item)) {
                    upgraded = true;
                }
            }
        }

        if (upgraded) {
            player.sendMessage(ChatColor.LIGHT_PURPLE + "[Efficient Mixture] " + ChatColor.AQUA
                    + "Your potion was upgraded to a higher tier!");
            addXPToJob(player, "Extend or upgrade potions");
        }
    }

    private boolean upgradePotion(ItemStack potion) {
        if (!(potion.getItemMeta() instanceof PotionMeta))
            return false;
        PotionMeta meta = (PotionMeta) potion.getItemMeta();

        PotionType type = meta.getBasePotionType();
        if (type == null)
            return false;

        PotionType upgradedType = getUpgradedType(type);
        if (upgradedType != null && upgradedType != type) {
            meta.setBasePotionType(upgradedType);
            potion.setItemMeta(meta);
            return true;
        }

        return false;
    }

    private PotionType getUpgradedType(PotionType type) {
        switch (type) {
            case REGENERATION:
                return PotionType.STRONG_REGENERATION;
            case SWIFTNESS:
                return PotionType.STRONG_SWIFTNESS;
            case STRENGTH:
                return PotionType.STRONG_STRENGTH;
            case HEALING:
                return PotionType.STRONG_HEALING;
            case HARMING:
                return PotionType.STRONG_HARMING;
            case POISON:
                return PotionType.STRONG_POISON;
            case LEAPING:
                return PotionType.STRONG_LEAPING;
            case TURTLE_MASTER:
                return PotionType.STRONG_TURTLE_MASTER;
            case SLOWNESS:
                return PotionType.STRONG_SLOWNESS;
            default:
                return null;
        }
    }

    @EventHandler
    public void onSplash(org.bukkit.event.entity.PotionSplashEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            Player player = (Player) event.getEntity().getShooter();
            PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

            if (data.getSkillLevel("Toxic Mastery") > 0 && data.getActiveSkillsList().contains("Toxic Mastery")) {
                int lvl = data.getSkillLevel("Toxic Mastery");
                double multiplier = 1.0 + (0.2 * lvl);

                for (org.bukkit.entity.LivingEntity entity : event.getAffectedEntities()) {
                    for (PotionEffect effect : event.getPotion().getEffects()) {
                        if (effect.getType().equals(PotionEffectType.POISON)
                                || effect.getType().equals(PotionEffectType.WITHER)) {
                            event.setIntensity(entity, 0.0);
                            int newDur = (int) (effect.getDuration() * multiplier);
                            int newAmp = effect.getAmplifier() + (lvl > 2 ? 1 : 0);
                            entity.addPotionEffect(new PotionEffect(effect.getType(), newDur, newAmp));
                        } else if (effect.getType().equals(PotionEffectType.INSTANT_DAMAGE)) {
                            event.setIntensity(entity, event.getIntensity(entity) * multiplier);
                        }
                    }
                }
                addXPToJob(player, "Use splash or lingering potions");
            }
        }
    }
}
