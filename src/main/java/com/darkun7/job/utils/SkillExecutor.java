package com.darkun7.job.utils;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import com.darkun7.job.util.PartyHook;

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

    // State maps
    public static final Map<UUID, Boolean> hellfireActive = new HashMap<>();
    public static final Map<UUID, Long> berserkExpiry = new HashMap<>();

    public static boolean canBeActivatedDirectly(String skillName) {
        switch (skillName) {
            case "Agronomist":
            case "Nature Embrace":
            case "Nature Bind":
            case "Blaze Burst":
            case "Hellfire":
            case "Void Step":
            case "Void Corruption":
            case "Axe Swing":
            case "Berserk":
            case "Pack Assault":
            case "Alpha Command":
            case "Hunter's Mark":
            case "Deep Pressure":
            case "Volatile Flask":
            case "Stampede":
            case "Poison Coating":
            case "Builder":
            case "Breaker":
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

        switch (skillName) {
            case "Agronomist":
                executeAgronomist(player, level, now);
                break;
            case "Nature Embrace":
                executeNatureEmbrace(player, level, now, plugin);
                break;
            case "Nature Bind":
                executeNatureBind(player, level, now, plugin);
                break;
            case "Blaze Burst":
                executeBlazeBurst(player, level, now);
                break;
            case "Hellfire":
                boolean current = hellfireActive.getOrDefault(player.getUniqueId(), false);
                hellfireActive.put(player.getUniqueId(), !current);
                player.sendMessage(
                        ChatColor.GOLD + "[Hellfire] " + (!current ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
                break;
            case "Void Step":
                executeVoidStep(player, level, now);
                break;
            case "Void Corruption":
                executeVoidCorruption(player, level, now);
                break;
            case "Axe Swing":
                executeAxeSwing(player, level, now);
                break;
            case "Berserk":
                executeBerserk(player, level, now);
                break;
            case "Pack Assault":
                executePackAssault(player, level, now, plugin);
                break;
            case "Alpha Command":
                executeAlphaCommand(player, level, now, plugin);
                break;
            case "Hunter's Mark":
                executeHunterMark(player, level, now);
                break;
            case "Deep Pressure":
                executeDeepPressure(player, level, now);
                break;
            case "Volatile Flask":
                executeVolatileFlask(player, level, now, plugin);
                break;
            case "Stampede":
                executeStampede(player, level, now);
                break;
            case "Poison Coating":
                executePoisonCoating(player, level, plugin);
                break;
            case "Builder":
                executeBuilder(player, level, plugin);
                break;
            case "Breaker":
                executeBreaker(player, level, plugin);
                break;
            default:
                player.sendMessage(ChatColor.RED + "This skill cannot be activated directly.");
                break;
        }
    }

    private static void executeAgronomist(Player player, int level, long now) {
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

    private static void executeBlazeBurst(Player player, int level, long now) {
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

    private static void executeVoidStep(Player player, int level, long now) {
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

    private static void executeAxeSwing(Player player, int level, long now) {
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

    private static void executeBerserk(Player player, int level, long now) {
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

    private static void executePackAssault(Player player, int level, long now, JobPlugin plugin) {
        long lastUsed = packAssaultCooldown.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastUsed < 30000L) {
            long remaining = (30000L - (now - lastUsed)) / 1000L;
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

    private static void executeAlphaCommand(Player player, int level, long now, JobPlugin plugin) {
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

    private static void executeHunterMark(Player player, int level, long now) {
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

    private static void executeDeepPressure(Player player, int level, long now) {
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

    private static void executeVolatileFlask(Player player, int level, long now, JobPlugin plugin) {
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

    private static void executeStampede(Player player, int level, long now) {
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

    private static void executePoisonCoating(Player player, int level, JobPlugin plugin) {
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

    private static boolean isCrop(Material m) {
        return m == Material.WHEAT || m == Material.CARROTS || m == Material.POTATOES || m == Material.BEETROOTS;
    }

    private static void growToMax(Block b) {
        org.bukkit.block.data.Ageable ageable = (org.bukkit.block.data.Ageable) b.getBlockData();
        ageable.setAge(ageable.getMaximumAge());
        b.setBlockData(ageable);
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

    public static long getCooldownRemainingMillis(Player player, String skillName, int level) {
        long now = System.currentTimeMillis();
        long lastUsed = 0;
        long cdMillis = 0;

        switch (skillName) {
            case "Agronomist":
                lastUsed = agronomistCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 60000;
                break;
            case "Nature Embrace":
                lastUsed = natureEmbraceCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 120000;
                break;
            case "Nature Bind":
                lastUsed = natureBindCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 15000;
                break;
            case "Blaze Burst":
                lastUsed = blazeBurstCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 30000;
                break;
            case "Void Step":
                lastUsed = voidStepCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = Math.max(5, 15 - level * 2) * 1000L;
                break;
            case "Axe Swing":
                lastUsed = axeSwingCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = Math.max(2, 10 - level) * 1000L;
                break;
            case "Pack Assault":
                lastUsed = packAssaultCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 30000;
                break;
            case "Alpha Command":
                lastUsed = alphaCommandCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 60000;
                break;
            case "Hunter's Mark":
                lastUsed = hunterMarkCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 30000;
                break;
            case "Deep Pressure":
                lastUsed = deepPressureCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 45000;
                break;
            case "Volatile Flask":
                lastUsed = volatileFlaskCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 25000;
                break;
            case "Stampede":
                lastUsed = stampedeCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 40000;
                break;
            case "Void Corruption":
                lastUsed = voidCorruptionCooldown.getOrDefault(player.getUniqueId(), 0L);
                cdMillis = 20000;
                break;
            default:
                return 0;
        }

        long remaining = cdMillis - (now - lastUsed);
        return remaining > 0 ? remaining : 0;
    }
}
