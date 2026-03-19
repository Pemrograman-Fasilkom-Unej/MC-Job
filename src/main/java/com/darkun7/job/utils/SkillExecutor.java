package com.darkun7.job.utils;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import com.darkun7.job.util.PartyHook;
import org.bukkit.World;
import org.bukkit.Bukkit;
import org.bukkit.generator.structure.Structure;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkillExecutor {

    // Cooldown maps
    private static final Map<UUID, Long> agronomistCooldown = new HashMap<>();
    private static final Map<UUID, Long> natureEmbraceCooldown = new HashMap<>();
    private static final Map<UUID, Long> natureBindCooldown = new HashMap<>();
    private static final Map<UUID, Long> blazeBurstCooldown = new HashMap<>();
    private static final Map<UUID, Long> voidStepCooldown = new HashMap<>();
    private static final Map<UUID, Long> axeSwingCooldown = new HashMap<>();
    private static final Map<UUID, Long> hunterMarkCooldown = new HashMap<>();
    private static final Map<UUID, Long> deepPressureCooldown = new HashMap<>();
    private static final Map<UUID, Long> volatileFlaskCooldown = new HashMap<>();
    private static final Map<UUID, Long> stampedeCooldown = new HashMap<>();
    private static final Map<UUID, Long> packAssaultCooldown = new HashMap<>();
    private static final Map<UUID, Long> alphaCommandCooldown = new HashMap<>();
    private static final Map<UUID, Long> voidCorruptionCooldown = new HashMap<>();
    private static final Map<UUID, Long> escapeRopeCooldown = new HashMap<>();
    private static final Map<UUID, Long> ruinRadarCooldown = new HashMap<>();
    private static final Map<UUID, Long> pocketStationCooldown = new HashMap<>();
    private static final Map<UUID, Long> weatherControlCooldown = new HashMap<>();
    private static final Map<UUID, Long> sugarRushCooldown = new HashMap<>();
    private static final Map<UUID, Long> seasoningPowderCooldown = new HashMap<>();
    private static final Map<UUID, Long> flagBearerCooldown = new HashMap<>();
    private static final Map<UUID, Long> temporaryCampCooldown = new HashMap<>();
    private static final Map<UUID, Long> hiveGuardianCooldown = new HashMap<>();
    private static final Map<UUID, Long> spectralWeaponCooldown = new HashMap<>();
    private static final Map<UUID, Long> machinationCooldown = new HashMap<>();
    private static final Map<UUID, Long> fireBallCooldown = new HashMap<>();

    // State maps
    public static final Map<UUID, Boolean> hellfireActive = new HashMap<>();
    public static final Map<UUID, Long> berserkExpiry = new HashMap<>();

    public static boolean canBeActivatedDirectly(String skillName) {
        switch (skillName.toLowerCase()) {
            case "agronomist":
            case "nature's embrace":
            case "nature's bind":
            case "blaze burst":
            case "void step":
            case "axe swing":
            case "berserk":
            case "pack assault":
            case "alpha command":
            case "hunter's mark":
            case "deep pressure":
            case "volatile flask":
            case "stampede":
            case "poison coating":
            case "builder":
            case "breaker":
            case "escape rope":
            case "ruin radar":
            case "pocket station":
            case "weather control":
            case "sugar rush":
            case "seasoning powder":
            case "flag bearer":
            case "temporary camp":
            case "fire ball":
                return true;
            default:
                return false;
        }
    }

    public static void executeSkill(Player player, String skillName, JobPlugin plugin) {
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        int level = data.getSkillLevel(skillName);
        if (level == 0)
            return;

        long now = System.currentTimeMillis();

        switch (skillName.toLowerCase()) {
            case "agronomist":
                executeAgronomist(player, level, now, plugin);
                break;
            case "nature's embrace":
                executeNatureEmbrace(player, level, now, plugin);
                break;
            case "nature's bind":
                executeNatureBind(player, level, now, plugin);
                break;
            case "blaze burst":
                executeBlazeBurst(player, level, now, plugin);
                break;
            case "void step":
                executeVoidStep(player, level, now, plugin);
                break;
            case "axe swing":
                executeAxeSwing(player, level, plugin);
                break;
            case "berserk":
                executeBerserk(player, level, plugin);
                break;
            case "pack assault":
                executePackAssault(player, level, plugin);
                break;
            case "alpha command":
                executeAlphaCommand(player, level, plugin);
                break;
            case "hunter's mark":
                executeHuntersMark(player, level, plugin);
                break;
            case "deep pressure":
                executeDeepPressure(player, level, plugin);
                break;
            case "volatile flask":
                executeVolatileFlask(player, level, plugin);
                break;
            case "stampede":
                executeStampede(player, level, plugin);
                break;
            case "poison coating":
                executePoisonCoating(player, plugin);
                break;
            case "builder":
                executeBuilder(player, level, plugin);
                break;
            case "breaker":
                executeBreaker(player, level, plugin);
                break;
            case "escape rope":
                executeEscapeRope(player, level, plugin);
                break;
            case "ruin radar":
                executeRuinRadar(player, level, plugin);
                break;
            case "pocket station":
                executePocketStation(player, level, plugin);
                break;
            case "weather control":
                executeWeatherControl(player, level, now, plugin);
                break;
            case "sugar rush":
                executeSugarRush(player, level, now, plugin);
                break;
            case "seasoning powder":
                executeSeasoningPowder(player, plugin);
                break;
            case "flag bearer":
                executeFlagBearer(player, level, now, plugin);
                break;
            case "temporary camp":
                executeTemporaryCamp(player, level, now, plugin);
                break;
            case "fire ball":
                executeFireBall(player, level, now, plugin);
                break;
            default:
                player.sendMessage(ChatColor.RED + "This skill cannot be activated directly.");
                break;
        }
    }

    private static void executeAgronomist(Player player, int level, long now, JobPlugin plugin) {
        long lastUsed = agronomistCooldown.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastUsed < 60000L) {
            long remaining = 60 - (now - lastUsed) / 1000;
            player.sendMessage(ChatColor.RED + "Agronomist on cooldown! " + remaining + "s remaining.");
            return;
        }
        agronomistCooldown.put(player.getUniqueId(), now);
        int radius = 3 + level;
        Location center = player.getLocation();
        int count = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Block b = center.clone().add(dx, 0, dz).getBlock();
                if (b.getType() == Material.FARMLAND) {
                    Block crop = b.getRelative(0, 1, 0);
                    if (isCrop(crop.getType())) {
                        growToMax(crop);
                        count++;
                    }
                }
            }
        }
        player.sendMessage(ChatColor.GREEN + "[Agronomist] Grew " + count + " crops instantly!");
    }

    private static void executeNatureEmbrace(Player player, int level, long now, JobPlugin plugin) {
        long lastUsed = natureEmbraceCooldown.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastUsed < 120000L) {
            long remaining = 120 - (now - lastUsed) / 1000;
            player.sendMessage(ChatColor.RED + "Nature Embrace on cooldown! " + remaining + "s remaining.");
            return;
        }
        natureEmbraceCooldown.put(player.getUniqueId(), now);
        int radius = 3;
        Location center = player.getLocation();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Block ground = center.clone().add(dx, -1, dz).getBlock();
                Block above = ground.getRelative(0, 1, 0);

                // Allow replacing Air or non-solid blocks (except protected ones)
                Material gMat = ground.getType();
                if ((gMat.isAir() || gMat.isSolid()) && gMat != Material.BEDROCK && gMat != Material.BARRIER
                        && !gMat.name().contains("PORTAL") && above.getType().isAir()) {
                    Material oldGround = ground.getType();
                    ground.setType(Material.FARMLAND);
                    above.setType(Material.WHEAT);

                    ground.setMetadata("nature_embrace_protected", new FixedMetadataValue(plugin, true));
                    above.setMetadata("nature_embrace_protected", new FixedMetadataValue(plugin, true));

                    org.bukkit.block.data.Ageable ageable = (org.bukkit.block.data.Ageable) above.getBlockData();
                    ageable.setAge(ageable.getMaximumAge());
                    above.setBlockData(ageable);

                    final Block g = ground;
                    final Block a = above;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (a.getType() == Material.WHEAT) {
                                a.setType(Material.AIR);
                                a.removeMetadata("nature_embrace_protected", plugin);
                            }
                            if (g.getType() == Material.FARMLAND) {
                                g.setType(oldGround);
                                g.removeMetadata("nature_embrace_protected", plugin);
                            }
                        }
                    }.runTaskLater(plugin, 15 * 20L);
                }
            }
        }

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                ticks++;
                if (ticks > 15 * 10) {
                    cancel();
                    return;
                }

                // Heal caster
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, 0, true, false));
                if (ticks % 2 == 0) { // Slower hunger recovery (every 20 ticks / 1 second equivalent in this loop)
                    player.setFoodLevel(Math.min(20, player.getFoodLevel() + 1));
                }

                // Heal nearby (Party only if available)
                boolean partyActive = PartyHook.isPartyActive();
                for (Entity e : player.getNearbyEntities(5, 5, 5)) {
                    if (e instanceof Player) {
                        Player target = (Player) e;
                        if (!partyActive || PartyHook.isInSameParty(player, target)) {
                            target.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, 0, true, false));
                            if (ticks % 2 == 0) {
                                target.setFoodLevel(Math.min(20, target.getFoodLevel() + 1));
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);

        player.sendMessage(ChatColor.GREEN + "[Nature Embrace] Channeling nature's power!");
    }

    private static void executeNatureBind(Player player, int level, long now, JobPlugin plugin) {
        long lastUsed = natureBindCooldown.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastUsed < 15000L) {
            long remaining = (15000L - (now - lastUsed)) / 1000L;
            player.sendMessage(ChatColor.RED + "Nature Bind on cooldown! " + remaining + "s remaining.");
            return;
        }
        natureBindCooldown.put(player.getUniqueId(), now);

        Location loc = player.getEyeLocation();
        Vector dir = player.getLocation().getDirection().normalize().multiply(0.5);
        for (int i = 0; i < 40; i++) {
            loc.add(dir);
            player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 1, 0, 0, 0, 0);
        }

        org.bukkit.util.RayTraceResult result = player.getWorld().rayTraceEntities(player.getEyeLocation(),
                player.getLocation().getDirection(), 20, e -> e instanceof LivingEntity && !e.equals(player));
        if (result != null && result.getHitEntity() != null) {
            LivingEntity target = (LivingEntity) result.getHitEntity();
            int duration = 60 + 20 * level;
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, 2));
            target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration / 2, 0));
            player.sendMessage(ChatColor.GREEN + "[Nature Bind] Bound " + target.getName() + " with vines!");
        } else {
            player.sendMessage(ChatColor.GREEN + "[Nature Bind] Launched binding vine!");
        }
    }

    private static void executeBlazeBurst(Player player, int level, long now, JobPlugin plugin) {
        long lastUsed = blazeBurstCooldown.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastUsed < 30000L) {
            long remaining = (30000L - (now - lastUsed)) / 1000L;
            player.sendMessage(ChatColor.RED + "Blaze Burst on cooldown! " + remaining + "s remaining.");
            return;
        }
        blazeBurstCooldown.put(player.getUniqueId(), now);
        int radius = 4 + level;
        player.getWorld().createExplosion(player.getLocation(), 0, false, false, player);
        for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof LivingEntity && !e.equals(player)) {
                ((LivingEntity) e).damage(3.0 + 2.0 * level, player);
                e.setFireTicks(80 + 20 * level);
            }
        }
        player.sendMessage(ChatColor.GOLD + "[Blaze Burst] Inferno released!");
    }

    private static void executeVoidStep(Player player, int level, long now, JobPlugin plugin) {
        long lastUsed = voidStepCooldown.getOrDefault(player.getUniqueId(), 0L);
        int cdSec = Math.max(5, 15 - level * 2);
        if (now - lastUsed < cdSec * 1000L) {
            long remaining = (cdSec * 1000L - (now - lastUsed)) / 1000L;
            player.sendMessage(ChatColor.RED + "Void Step on cooldown! " + remaining + "s remaining.");
            return;
        }
        voidStepCooldown.put(player.getUniqueId(), now);
        Vector dir = player.getLocation().getDirection().normalize().multiply(8 + level * 2);
        Location dest = player.getLocation().add(dir);
        dest.setY(dest.getWorld().getHighestBlockYAt(dest) + 1);
        player.teleport(dest);
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 30, 0.5, 1, 0.5);
        player.sendMessage(ChatColor.DARK_PURPLE + "[Void Step] Blinked forward!");
    }

    private static void executeVoidCorruption(Player player, int level, long now) {
        long lastUsed = voidCorruptionCooldown.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastUsed < 20000L) {
            long remaining = (20000L - (now - lastUsed)) / 1000L;
            player.sendMessage(ChatColor.RED + "Void Corruption on cooldown! " + remaining + "s remaining.");
            return;
        }
        LivingEntity target = getTargetEntity(player, 20);
        if (target != null) {
            voidCorruptionCooldown.put(player.getUniqueId(), now);
            int duration = 60 + 20 * level;
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, level - 1));
            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, duration, 0));
            player.sendMessage(
                    ChatColor.DARK_PURPLE + "[Void Corruption] Target afflicted for " + (duration / 20) + "s!");
        } else {
            player.sendMessage(ChatColor.RED + "No valid target in sight for Void Corruption!");
        }
    }

    private static void executeAxeSwing(Player player, int level, JobPlugin plugin) {
        long now = System.currentTimeMillis();
        long lastUsed = axeSwingCooldown.getOrDefault(player.getUniqueId(), 0L);
        int cdSec = Math.max(2, 10 - level);
        if (now - lastUsed < cdSec * 1000L) {
            long remaining = (cdSec * 1000L - (now - lastUsed)) / 1000L;
            player.sendMessage(ChatColor.RED + "Axe Swing on cooldown! " + remaining + "s remaining.");
            return;
        }
        axeSwingCooldown.put(player.getUniqueId(), now);
        double range = 3.0 + (0.5 * level);
        player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, player.getLocation().add(0, 1, 0), 5, range, 0.5, range);
        for (Entity e : player.getNearbyEntities(range, 2.0, range)) {
            if (e instanceof LivingEntity && !e.equals(player)) {
                ((LivingEntity) e).damage(6.0 + (level * 2.5), player);
            }
        }
        player.sendMessage(ChatColor.GOLD + "[Axe Swing] Unleashed!");
    }

    private static void executeBerserk(Player player, int level, JobPlugin plugin) {
        long now = System.currentTimeMillis();
        long expiry = berserkExpiry.getOrDefault(player.getUniqueId(), 0L);
        if (now < expiry) {
            player.sendMessage(ChatColor.RED + "Berserk already active!");
            return;
        }
        int duration = 5 + 3 * level;
        berserkExpiry.put(player.getUniqueId(), now + (duration * 1000L));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration * 20, level - 1));
        player.sendMessage(ChatColor.RED + "[Berserk] RAGE activated for " + duration + "s!");
    }

    private static void executePackAssault(Player player, int level, JobPlugin plugin) {
        long now = System.currentTimeMillis();
        long lastUsed = packAssaultCooldown.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastUsed < 45000L) {
            long remaining = (45000L - (now - lastUsed)) / 1000L;
            player.sendMessage(ChatColor.RED + "Pack Assault on cooldown! " + remaining + "s remaining.");
            return;
        }
        LivingEntity target = getTargetEntity(player, 20);
        if (target != null) {
            packAssaultCooldown.put(player.getUniqueId(), now);
            int duration = 5 + 2 * level;
            for (int i = 0; i < 3 + level; i++) {
                org.bukkit.entity.Wolf wolf = player.getWorld().spawn(player.getLocation(),
                        org.bukkit.entity.Wolf.class);
                wolf.setAdult();
                wolf.setAngry(true);
                wolf.setTarget(target);
                new org.bukkit.scheduler.BukkitRunnable() {
                    @Override
                    public void run() {
                        if (wolf != null && !wolf.isDead()) {
                            wolf.remove();
                        }
                    }
                }.runTaskLater(plugin, duration * 20L);
            }
            player.sendMessage(ChatColor.AQUA + "[Pack Assault] Pack summoned for " + duration + "s!");
        } else {
            player.sendMessage(ChatColor.RED + "No valid target in sight for Pack Assault!");
        }
    }

    private static void executeAlphaCommand(Player player, int level, JobPlugin plugin) {
        long now = System.currentTimeMillis();
        long lastUsed = alphaCommandCooldown.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastUsed < 60000L) {
            long remaining = (60000L - (now - lastUsed)) / 1000L;
            player.sendMessage(ChatColor.RED + "Alpha Command on cooldown! " + remaining + "s remaining.");
            return;
        }
        alphaCommandCooldown.put(player.getUniqueId(), now);
        int duration = 10 + 5 * level;
        for (Entity e : player.getNearbyEntities(15, 15, 15)) {
            if (e instanceof org.bukkit.entity.Wolf) {
                org.bukkit.entity.Wolf wolf = (org.bukkit.entity.Wolf) e;
                if (wolf.isTamed() && wolf.getOwner() != null
                        && wolf.getOwner().getUniqueId().equals(player.getUniqueId())) {
                    wolf.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration * 20, level > 3 ? 1 : 0));
                    wolf.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration * 20, 1));
                }
            }
        }
        player.sendMessage(ChatColor.AQUA + "[Alpha Command] Empowered pets for " + duration + "s!");
    }

    private static void executeHuntersMark(Player player, int level, JobPlugin plugin) {
        long now = System.currentTimeMillis();
        long lastUsed = hunterMarkCooldown.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastUsed < 30000L) {
            long remaining = (30000L - (now - lastUsed)) / 1000L;
            player.sendMessage(ChatColor.RED + "Hunter's Mark on cooldown! " + remaining + "s remaining.");
            return;
        }
        LivingEntity target = getTargetEntity(player, 30);
        if (target != null) {
            hunterMarkCooldown.put(player.getUniqueId(), now);
            target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200 + (100 * level), 0));
            target.damage(3.0 * level, player);
            player.sendMessage(ChatColor.GREEN + "[Hunter's Mark] Applied to " + target.getName() + "!");
        } else {
            player.sendMessage(ChatColor.RED + "No valid target for Hunter's Mark!");
        }
    }

    private static void executeDeepPressure(Player player, int level, JobPlugin plugin) {
        long now = System.currentTimeMillis();
        long lastUsed = deepPressureCooldown.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastUsed < 45000L) {
            long remaining = (45000L - (now - lastUsed)) / 1000L;
            player.sendMessage(ChatColor.RED + "Deep Pressure on cooldown! " + remaining + "s remaining.");
            return;
        }
        LivingEntity target = getTargetEntity(player, 15);
        if (target != null) {
            deepPressureCooldown.put(player.getUniqueId(), now);
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100 + (20 * level), level));
            target.damage(2.0 + (1.5 * level), player);
            player.sendMessage(ChatColor.BLUE + "[Deep Pressure] " + target.getName() + " crushed!");
        } else {
            player.sendMessage(ChatColor.RED + "No target for Deep Pressure!");
        }
    }

    private static void executeVolatileFlask(Player player, int level, JobPlugin plugin) {
        long now = System.currentTimeMillis();
        long lastUsed = volatileFlaskCooldown.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastUsed < 25000L) {
            long remaining = (25000L - (now - lastUsed)) / 1000L;
            player.sendMessage(ChatColor.RED + "Volatile Flask on cooldown! " + remaining + "s remaining.");
            return;
        }
        volatileFlaskCooldown.put(player.getUniqueId(), now);
        Snowball flask = player.launchProjectile(Snowball.class);
        flask.setMetadata("volatileFlask", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
        flask.setMetadata("flaskLevel", new FixedMetadataValue(plugin, level));
        player.sendMessage(ChatColor.LIGHT_PURPLE + "[Volatile Flask] Tossed!");
    }

    private static void executeStampede(Player player, int level, JobPlugin plugin) {
        long now = System.currentTimeMillis();
        if (!player.isInsideVehicle()) {
            player.sendMessage(ChatColor.RED + "You must be riding a vehicle (horse, pig, etc) to use Stampede!");
            return;
        }
        long lastUsed = stampedeCooldown.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastUsed < 40000L) {
            long remaining = (40000L - (now - lastUsed)) / 1000L;
            player.sendMessage(ChatColor.RED + "Stampede on cooldown! " + remaining + "s remaining.");
            return;
        }
        stampedeCooldown.put(player.getUniqueId(), now);
        Entity vehicle = player.getVehicle();
        Vector push = player.getLocation().getDirection().normalize().multiply(1.5 + (0.2 * level));
        vehicle.setVelocity(push);

        // Rider Buffs
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100 + (20 * level), level > 3 ? 1 : 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100 + (20 * level), 0));

        for (Entity e : vehicle.getNearbyEntities(3, 3, 3)) {
            if (e instanceof LivingEntity && !e.equals(player) && !e.equals(vehicle)) {
                ((LivingEntity) e).damage(4.0 + (1.5 * level), player);
                Vector knockback = e.getLocation().toVector().subtract(vehicle.getLocation().toVector()).normalize()
                        .multiply(1.2);
                e.setVelocity(knockback);
            }
        }
        player.sendMessage(ChatColor.GOLD + "[Stampede] CHARGE!");
    }

    private static void executePoisonCoating(Player player, JobPlugin plugin) {
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        boolean current = data.isToggledOn("Poison Coating");
        data.setToggledOn("Poison Coating", !current);
        player.sendMessage(ChatColor.YELLOW + "[Poison Coating] "
                + (!current ? ChatColor.GREEN + "ACTIVE (Next hit applies poison)" : ChatColor.RED + "REMOVED"));
    }

    private static void executeBuilder(Player player, int level, JobPlugin plugin) {
        player.setMetadata("builder_active", new FixedMetadataValue(plugin, true));
        player.sendMessage(ChatColor.YELLOW + "[Builder] Active. Right-click two blocks to fill the area. "
                + ChatColor.GRAY + "(Remember to hold a block during selection)");
    }

    private static void executeBreaker(Player player, int level, JobPlugin plugin) {
        player.setMetadata("breaker_active", new FixedMetadataValue(plugin, true));
        player.sendMessage(ChatColor.YELLOW + "[Breaker] Active. Right-click two blocks to break the area.");
    }

    private static void executeSpectralWeapon(Player player, int level, JobPlugin plugin) {
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        boolean current = data.isToggledOn("Spectral Weapon");
        data.setToggledOn("Spectral Weapon", !current);
        player.sendMessage(ChatColor.AQUA + "[Spectral Weapon] "
                + (!current ? ChatColor.GREEN + "ACTIVE (Next hit summons spectral weapons)"
                        : ChatColor.RED + "REMOVED"));
    }

    private static void executeEscapeRope(Player player, int level, JobPlugin plugin) {
        long now = System.currentTimeMillis();
        long lastUsed = escapeRopeCooldown.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastUsed < 60000L) {
            long remaining = 60 - (now - lastUsed) / 1000;
            player.sendMessage(ChatColor.RED + "Escape Rope on cooldown! " + remaining + "s remaining.");
            return;
        }

        Location loc = player.getLocation();
        boolean isNether = loc.getWorld().getEnvironment() == org.bukkit.World.Environment.NETHER;
        int maxHeight = isNether ? 125 : loc.getWorld().getMaxHeight() - 2;

        boolean found = false;
        for (int y = loc.getBlockY() + 2; y < maxHeight; y++) {
            Block b1 = loc.getWorld().getBlockAt(loc.getBlockX(), y, loc.getBlockZ());
            Block b2 = loc.getWorld().getBlockAt(loc.getBlockX(), y + 1, loc.getBlockZ());
            if (b1.getType().isAir() && b2.getType().isAir() && b1.getRelative(0, -1, 0).getType().isSolid()) {
                Location target = b1.getLocation().add(0.5, 0, 0.5);
                target.setYaw(loc.getYaw());
                target.setPitch(loc.getPitch());
                player.teleport(target);
                player.getWorld().spawnParticle(Particle.CLOUD, loc, 20, 0.2, 0.2, 0.2, 0.1);
                player.getWorld().spawnParticle(Particle.CLOUD, target, 20, 0.2, 0.2, 0.2, 0.1);
                player.sendMessage(ChatColor.AQUA + "[Escape Rope] Teleported to the surface!");
                escapeRopeCooldown.put(player.getUniqueId(), now);
                found = true;
                break;
            }
        }
        if (!found) {
            player.sendMessage(ChatColor.RED + "No safe surface found above you!");
        }
    }

    private static void executeRuinRadar(Player player, int level, JobPlugin plugin) {
        long lastUsed = ruinRadarCooldown.getOrDefault(player.getUniqueId(), 0L);
        long now = System.currentTimeMillis();
        if (now - lastUsed < 30000L) {
            long remaining = 30 - (now - lastUsed) / 1000;
            player.sendMessage(ChatColor.RED + "Ruin Radar on cooldown! " + remaining + "s remaining.");
            return;
        }
        com.darkun7.job.gui.JourneymanSkillGUI.openRuinRadarMenu(player, level);
    }

    public static void startStructureRadar(Player player, String structureId, JobPlugin plugin) {
        Structure type = getStructureById(structureId);
        if (type == null) {
            player.sendMessage(ChatColor.RED + "Unknown structure type!");
            return;
        }

        org.bukkit.util.StructureSearchResult result = player.getWorld().locateNearestStructure(player.getLocation(),
                type, 2000, false);
        if (result == null || result.getLocation() == null) {
            player.sendMessage(ChatColor.RED + "No " + structureId + " found within 2000 blocks!");
            return;
        }

        ruinRadarCooldown.put(player.getUniqueId(), System.currentTimeMillis());
        Location target = result.getLocation();
        player.sendMessage(ChatColor.AQUA + "[Ruin Radar] Tracking nearest " + structureId + "...");

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline() || ticks > 30 * 20) { // 30 seconds max
                    cancel();
                    return;
                }
                ticks += 20;
                Location pLoc = player.getEyeLocation();
                Vector dir = target.clone().subtract(pLoc).toVector().normalize();
                for (double d = 1; d <= 3; d += 0.5) {
                    Location part = pLoc.clone().add(dir.clone().multiply(d));
                    player.spawnParticle(Particle.DUST, part, 1, new Particle.DustOptions(org.bukkit.Color.RED, 1.0f));
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private static Structure getStructureById(String id) {
        switch (id.toLowerCase()) {
            case "shipwreck":
                return Structure.SHIPWRECK;
            case "desert_pyramid":
                return Structure.DESERT_PYRAMID;
            case "trail_ruins":
                return Structure.TRAIL_RUINS;
            case "jungle_pyramid":
                return Structure.JUNGLE_PYRAMID;
            case "igloo":
                return Structure.IGLOO;
            case "pillager_outpost":
                return Structure.PILLAGER_OUTPOST;
            case "bastion_remnant":
                return Structure.BASTION_REMNANT;
            case "fortress":
                return Structure.FORTRESS;
            case "monument":
                return Structure.MONUMENT;
            case "mansion":
                return Structure.MANSION;
            case "ancient_city":
                return Structure.ANCIENT_CITY;
            case "stronghold":
                return Structure.STRONGHOLD;
            default:
                return null;
        }
    }

    private static void executePocketStation(Player player, int level, JobPlugin plugin) {
        com.darkun7.job.gui.JourneymanSkillGUI.openPocketStationMenu(player, level);
    }

    private static void executeWeatherControl(Player player, int level, long now, JobPlugin plugin) {
        long lastUsed = weatherControlCooldown.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastUsed < 300000L) {
            long remaining = 300 - (now - lastUsed) / 1000;
            player.sendMessage(ChatColor.RED + "Weather Control on cooldown! " + remaining + "s remaining.");
            return;
        }
        weatherControlCooldown.put(player.getUniqueId(), now);

        World world = player.getWorld();
        if (!world.hasStorm()) {
            world.setStorm(true);
            world.setThundering(false);
            player.sendMessage(ChatColor.AQUA + "[Weather Control] Rain started!");
        } else if (!world.isThundering()) {
            world.setThundering(true);
            player.sendMessage(ChatColor.DARK_AQUA + "[Weather Control] Thunderstorm started!");
        } else {
            world.setStorm(false);
            world.setThundering(false);
            player.sendMessage(ChatColor.YELLOW + "[Weather Control] Skies cleared!");
        }
        // Particles
        for (int i = 0; i < 20; i++) {
            player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0, i * 0.5, 0), 5, 0.2, 0.2, 0.2,
                    0.05);
        }
    }

    private static void executeMachination(Player player, JobPlugin plugin) {
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        boolean current = data.isToggledOn("Machination");
        data.setToggledOn("Machination", !current);
        player.sendMessage(ChatColor.GOLD + "[Machination] "
                + (!current ? ChatColor.GREEN + "ACTIVE (Next hit deploys Golem)" : ChatColor.RED + "REMOVED"));
    }

    private static void executeSugarRush(Player player, int level, long now, JobPlugin plugin) {
        long lastUsed = sugarRushCooldown.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastUsed < 60000L) {
            long remaining = 60 - (now - lastUsed) / 1000;
            player.sendMessage(ChatColor.RED + "Sugar rush on cooldown! " + remaining + "s remaining.");
            return;
        }
        int hungerCost = 6;
        if (player.getFoodLevel() < hungerCost) {
            player.sendMessage(ChatColor.RED + "Not enough hunger to use Sugar rush! (Requires 3 drumsticks)");
            return;
        }
        player.setFoodLevel(player.getFoodLevel() - hungerCost);

        sugarRushCooldown.put(player.getUniqueId(), now);

        int duration = (10 + level * 5) * 20; // 15s to 35s
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, duration, 1));
        player.sendMessage(ChatColor.AQUA + "[Sugar rush] Speeding up! Hunger consumed.");
        player.getWorld().spawnParticle(Particle.FIREWORK, player.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
    }

    private static void executeSeasoningPowder(Player player, JobPlugin plugin) {
        PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        boolean current = data.isToggledOn("Seasoning powder");
        data.setToggledOn("Seasoning powder", !current);
        player.sendMessage(ChatColor.GOLD + "[Seasoning powder] "
                + (!current ? ChatColor.GREEN + "ACTIVE (Next hit seasons your target)" : ChatColor.RED + "REMOVED"));
    }

    private static void executeFlagBearer(Player player, int level, long now, JobPlugin plugin) {
        long lastUsed = flagBearerCooldown.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastUsed < 120000L) {
            long remaining = 120 - (now - lastUsed) / 1000;
            player.sendMessage(ChatColor.RED + "Flag bearer on cooldown! " + remaining + "s remaining.");
            return;
        }
        flagBearerCooldown.put(player.getUniqueId(), now);

        Location loc = player.getLocation();
        ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class);
        stand.setInvisible(true);
        stand.setMarker(true);
        stand.setGravity(false);
        stand.getEquipment().setHelmet(new ItemStack(Material.WHITE_BANNER));

        int durationSec = 10 + (level * 5);
        int durationTicks = durationSec * 20;

        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, durationTicks, level > 3 ? 1 : 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, durationTicks, level > 2 ? 1 : 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, durationTicks, level > 1 ? 1 : 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, 4)); // Heavy slowness

        player.sendMessage(ChatColor.GOLD + "[Flag bearer] FOR THE CAUSE! (Duration: " + durationSec + "s)");

        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                stand.remove();
            }
        }.runTaskLater(plugin, durationTicks);
    }

    private static void executeTemporaryCamp(Player player, int level, long now, JobPlugin plugin) {
        if (player.getWorld().getEnvironment() != org.bukkit.World.Environment.NORMAL) {
            player.sendMessage(ChatColor.RED + "Temporary Camp can only be used in the Overworld!");
            return;
        }
        long lastUsed = temporaryCampCooldown.getOrDefault(player.getUniqueId(), 0L);
        int cooldownSec = 300 - (level - 1) * 60;
        if (now - lastUsed < cooldownSec * 1000L) {
            long remaining = cooldownSec - (now - lastUsed) / 1000;
            player.sendMessage(ChatColor.RED + "Temporary Camp on cooldown! " + remaining + "s remaining.");
            return;
        }
        temporaryCampCooldown.put(player.getUniqueId(), now);

        Location base = player.getLocation().getBlock().getLocation();

        // ===== Tent poles =====
        base.clone().add(-1, 0, 0).getBlock().setType(Material.OAK_FENCE);
        base.clone().add(1, 0, 0).getBlock().setType(Material.OAK_FENCE);

        // ===== Side walls =====
        base.clone().add(-1, 1, 0).getBlock().setType(Material.WHITE_WOOL);
        base.clone().add(1, 1, 0).getBlock().setType(Material.WHITE_WOOL);

        // ===== Extended roof sides =====
        for (int z = -1; z <= 1; z++) {
            base.clone().add(-1, 2, z).getBlock().setType(Material.WHITE_WOOL);
            base.clone().add(1, 2, z).getBlock().setType(Material.WHITE_WOOL);
        }

        // ===== Roof ridge =====
        for (int z = -1; z <= 1; z++) {
            base.clone().add(0, 3, z).getBlock().setType(Material.WHITE_WOOL);
        }

        // ===== Bed =====
        BlockFace facing = player.getFacing();

        Location footLoc = base.clone().add(0, 0, 1);
        Location headLoc = footLoc.clone().add(facing.getModX(), 0, facing.getModZ());

        Bed footData = (Bed) Bukkit.createBlockData(Material.WHITE_BED);
        footData.setFacing(facing);
        footData.setPart(Bed.Part.FOOT);
        footLoc.getBlock().setBlockData(footData);

        Bed headData = (Bed) Bukkit.createBlockData(Material.WHITE_BED);
        headData.setFacing(facing);
        headData.setPart(Bed.Part.HEAD);
        headLoc.getBlock().setBlockData(headData);

        // ===== Level upgrades =====
        if (level >= 2) {
            base.clone().add(-1, 0, -1).getBlock().setType(Material.CAMPFIRE);
            base.clone().add(1, 0, -1).getBlock().setType(Material.CRAFTING_TABLE);
        }

        if (level >= 3) {
            base.clone().add(0, 0, -2).getBlock().setType(Material.CHEST);
            base.clone().add(0, 3, -1).getBlock().setType(Material.LANTERN);
        }

        player.sendMessage(ChatColor.GREEN + "[Temporary Camp] Rest here traveller.");
    }

    private static boolean isCrop(Material m) {
        return m == Material.WHEAT || m == Material.CARROTS || m == Material.POTATOES || m == Material.BEETROOTS;
    }

    private static void growToMax(Block b) {
        org.bukkit.block.data.Ageable ageable = (org.bukkit.block.data.Ageable) b.getBlockData();
        ageable.setAge(ageable.getMaximumAge());
        b.setBlockData(ageable);
    }

    private static void executeFireBall(Player player, int level, long now, JobPlugin plugin) {
        long lastUsed = fireBallCooldown.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastUsed < 15000L) {
            long remaining = 15 - (now - lastUsed) / 1000;
            player.sendMessage(ChatColor.RED + "Fire Ball on cooldown! " + remaining + "s remaining.");
            return;
        }

        int hungerCost = 2; // Extra cost
        if (player.getFoodLevel() < hungerCost) {
            player.sendMessage(ChatColor.RED + "Not enough hunger to launch Fire Ball!");
            return;
        }
        player.setFoodLevel(player.getFoodLevel() - hungerCost);
        fireBallCooldown.put(player.getUniqueId(), now);

        Location start = player.getEyeLocation();
        Vector direction = start.getDirection().normalize();

        new BukkitRunnable() {
            Location loc = start.clone();
            int distance = 0;
            final int maxDistance = 20 + (level * 5);

            @Override
            public void run() {
                for (int i = 0; i < 2; i++) { // Advance twice per tick for smoother look
                    loc.add(direction.clone().multiply(0.5));
                    distance++;

                    // Visual trail
                    loc.getWorld().spawnParticle(Particle.DUST, loc, 1,
                            new Particle.DustOptions(org.bukkit.Color.RED, 1.0f));
                    loc.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0, 0.02);

                    // Check for hits
                    for (Entity entity : loc.getWorld().getNearbyEntities(loc, 0.5, 0.5, 0.5)) {
                        if (entity instanceof LivingEntity && !entity.equals(player)) {
                            explode((LivingEntity) entity);
                            cancel();
                            return;
                        }
                    }

                    // Check for block collision
                    if (loc.getBlock().getType().isSolid()) {
                        explode(null);
                        cancel();
                        return;
                    }

                    if (distance > maxDistance * 2) {
                        cancel();
                        return;
                    }
                }
            }

            private void explode(LivingEntity target) {
                // Explosion particles
                loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 1);
                loc.getWorld().spawnParticle(Particle.DUST, loc, 30, 0.5, 0.5, 0.5,
                        new Particle.DustOptions(org.bukkit.Color.RED, 1.5f));
                loc.getWorld().spawnParticle(Particle.FLAME, loc, 20, 0.3, 0.3, 0.3, 0.1);

                if (target != null) {
                    target.damage(4.0 + (level * 2.0), player);
                    target.setFireTicks(40 + (level * 20));
                    player.sendMessage(ChatColor.GOLD + "[Fire Ball] Hit " + target.getName() + "!");
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        player.sendMessage(ChatColor.GOLD + "[Fire Ball] Launched!");
    }

    private static LivingEntity getTargetEntity(Player player, int range) {
        for (Entity e : player.getNearbyEntities(range, range, range)) {
            if (e instanceof LivingEntity && !e.equals(player)) {
                Vector toEntity = e.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                Vector dir = player.getLocation().getDirection();
                if (toEntity.dot(dir) > 0.9) {
                    return (LivingEntity) e;
                }
            }
        }
        return null;
    }

    public static long getSkillCooldownMillis(Player player, String skillName, int level) {
        long now = System.currentTimeMillis();
        long lastUsed = 0;
        long cdMillis = 0;
        switch (skillName.toLowerCase()) {
            case "agronomist":
                lastUsed = agronomistCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 30000;
                break;
            case "nature's embrace":
                lastUsed = natureEmbraceCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 45000;
                break;
            case "nature's bind":
                lastUsed = natureBindCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 60000;
                break;
            case "blaze burst":
                lastUsed = blazeBurstCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 30000;
                break;
            case "void step":
                lastUsed = voidStepCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 60000;
                break;
            case "weather control":
                lastUsed = weatherControlCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 300000;
                break;
            case "sugar rush":
                lastUsed = sugarRushCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 40000;
                break;
            case "seasoning powder":
                lastUsed = seasoningPowderCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 30000;
                break;
            case "flag bearer":
                lastUsed = flagBearerCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 120000;
                break;
            case "temporary camp":
                lastUsed = temporaryCampCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = (300 - (level - 1) * 60) * 1000;
                break;
            case "hive guardian":
                lastUsed = hiveGuardianCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 30000;
                break;
            case "spectral weapon":
                lastUsed = spectralWeaponCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 45000;
                break;
            case "machination":
                lastUsed = machinationCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 60000;
                break;
            case "pack assault":
                lastUsed = packAssaultCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 45000;
                break;
            default:
                return 0;
        }

        long remaining = cdMillis - (now - lastUsed);
        return remaining > 0 ? remaining : 0;
    }

    public static boolean isCooldownActive(Player player, String skillName) {
        long now = System.currentTimeMillis();
        long lastUsed = 0;
        long cdMillis = 0;

        switch (skillName.toLowerCase()) {
            case "hive guardian":
                lastUsed = hiveGuardianCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 30000;
                break;
            case "spectral weapon":
                lastUsed = spectralWeaponCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 45000;
                break;
            case "machination":
                lastUsed = machinationCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 60000;
                break;
            case "pack assault":
                lastUsed = packAssaultCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 45000;
                break;
        }

        return (now - lastUsed) < cdMillis;
    }

    public static void updateCooldown(Player player, String skillName) {
        long now = System.currentTimeMillis();
        switch (skillName.toLowerCase()) {
            case "hive guardian":
                hiveGuardianCooldown.put(player.getUniqueId(), now);
                break;
            case "spectral weapon":
                spectralWeaponCooldown.put(player.getUniqueId(), now);
                break;
            case "machination":
                machinationCooldown.put(player.getUniqueId(), now);
                break;
            case "pack assault":
                packAssaultCooldown.put(player.getUniqueId(), now);
                break;
        }
    }
}
