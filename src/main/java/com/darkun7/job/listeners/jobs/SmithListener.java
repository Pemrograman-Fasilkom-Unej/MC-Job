package com.darkun7.job.listeners.jobs;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import com.darkun7.job.utils.SkillExecutor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Projectile;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Vex;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SmithListener implements Listener {

    private final JobPlugin plugin;

    public SmithListener(JobPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        Player attacker = getAttackingPlayer(event);
        if (attacker == null)
            return;
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(attacker.getUniqueId());

        if (data.getSkillLevel("Heavy Temper") > 0 && data.getActiveSkillsList().contains("Heavy Temper")) {
            ItemStack held = attacker.getInventory().getItemInMainHand();
            String name = held.getType().name();
            if (name.contains("SWORD") || name.contains("SPEAR") || name.contains("MACE")) {
                double pierceBonus = 0.10 * data.getSkillLevel("Heavy Temper");
                event.setDamage(event.getDamage() + pierceBonus * 2.0);
            }
        }

        // Spectral Weapon
        if (data.getActiveSkillsList().contains("Spectral Weapon") && data.getSkillLevel("Spectral Weapon") > 0) {
            com.darkun7.job.models.Skill skill = plugin.getJobManager().getJob("Smith").getSkill("Spectral Weapon");
            double chancePerLevel = (skill != null) ? skill.getChancePerLevel() / 100.0 : 20.0 / 100.0;
            double maxChance = (skill != null) ? skill.getMaxChance() / 100.0 : 1.0;
            double chance = Math.min(maxChance, data.getSkillLevel("Spectral Weapon") * chancePerLevel);

            if (java.util.concurrent.ThreadLocalRandom.current().nextDouble() < chance) {
                if (SkillExecutor.isCooldownActive(attacker, "Spectral Weapon")) {
                    return;
                }
                SkillExecutor.updateCooldown(attacker, "Spectral Weapon");
                if (event.getEntity() instanceof LivingEntity) {
                    LivingEntity victim = (LivingEntity) event.getEntity();
                    int level = data.getSkillLevel("Spectral Weapon");
                    spawnSpectralWeapons(attacker, victim, level);
                    // attacker.sendMessage(ChatColor.AQUA + "[Spectral Weapon] Weapons summoned!");
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageSelf(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        Player player = (Player) event.getEntity();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (data.getSkillLevel("Boiling Blood") > 0 && data.getActiveSkillsList().contains("Boiling Blood")) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FIRE
                    || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK
                    || event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
                int duration = 60 + 40 * data.getSkillLevel("Boiling Blood");
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration, 0, false, true));
            }
        }
    }

    @EventHandler
    public void onAnvilUse(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        if (!(event.getInventory() instanceof AnvilInventory))
            return;
        if (event.getSlotType() != InventoryType.SlotType.RESULT)
            return;
        if (event.getCurrentItem() == null)
            return;

        Player player = (Player) event.getWhoClicked();
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        ItemStack result = event.getCurrentItem();

        if (data.getSkillLevel("Tempered Edge") > 0 && data.getActiveSkillsList().contains("Tempered Edge")) {
            String name = result.getType().name();
            if (name.contains("SWORD") || name.contains("AXE") || name.contains("SPEAR")) {
                int lvl = data.getSkillLevel("Tempered Edge");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 600 + 200 * lvl, lvl - 1));
                        player.sendMessage(
                                ChatColor.GOLD + "[Tempered Edge] Weapon is empowered for " + (30 + 10 * lvl) + "s!");
                    }
                }.runTaskLater(plugin, 1L);
            }
        }
    }

    private void spawnSpectralWeapons(Player owner, LivingEntity target, int level) {
        List<Material> weapons = new ArrayList<>();
        weapons.add(Material.IRON_SWORD); // Lv 1
        if (level >= 2)
            weapons.add(Material.IRON_SPEAR); // Lv 2
        if (level >= 3)
            weapons.add(Material.IRON_AXE); // Lv 3
        if (level >= 4)
            weapons.add(Material.MACE); // Lv 4+

        for (Material mat : weapons) {
            Vex vex = owner.getWorld().spawn(owner.getLocation(), Vex.class);
            vex.setInvulnerable(true);
            vex.setCanPickupItems(false);
            vex.setRemoveWhenFarAway(true);
            vex.getEquipment().setItemInMainHand(new ItemStack(mat));
            vex.getEquipment().setItemInMainHandDropChance(0.0f);
            vex.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0, false, false));
            vex.setMetadata("job_summon_owner", new FixedMetadataValue(plugin, owner.getUniqueId().toString()));
            vex.setTarget(target);

            // Disappear after 8 seconds
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (vex != null && !vex.isDead()) {
                        vex.remove();
                    }
                }
            }.runTaskLater(plugin, 8 * 20L);
        }
    }

    private Player getAttackingPlayer(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player)
            return (Player) event.getDamager();
        if (event.getDamager() instanceof Projectile
                && ((Projectile) event.getDamager()).getShooter() instanceof Player)
            return (Player) ((Projectile) event.getDamager()).getShooter();
        return null;
    }

    @EventHandler
    public void onSummonTarget(EntityTargetLivingEntityEvent event) {
        if (event.getEntity().hasMetadata("job_summon_owner")) {
            String ownerUuid = event.getEntity().getMetadata("job_summon_owner").get(0).asString();
            if (event.getTarget() != null && event.getTarget().getUniqueId().toString().equals(ownerUuid)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSummonDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager().hasMetadata("job_summon_owner")) {
            String ownerUuid = event.getDamager().getMetadata("job_summon_owner").get(0).asString();
            if (event.getEntity().getUniqueId().toString().equals(ownerUuid)) {
                event.setCancelled(true);
            }
        }
    }
}
