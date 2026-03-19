package com.darkun7.job.listeners;

import com.darkun7.job.JobPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.entity.Item;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Tameable;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TriggerListener implements Listener {

    private final JobPlugin plugin;
    private static final Set<String> discoveredChunks = new HashSet<>();

    public TriggerListener(JobPlugin plugin) {
        this.plugin = plugin;
    }

    private void addXPToJob(Player player, String trigger) {
        plugin.getExperienceManager().awardXP(player, trigger);
    }

    // --- Dynamic Action Triggers ---

    @EventHandler
    public void onBreed(EntityBreedEvent event) {
        if (event.getBreeder() instanceof Player) {
            Player player = (Player) event.getBreeder();
            EntityType type = event.getEntity().getType();

            if (type == EntityType.COW || type == EntityType.PIG) {
                addXPToJob(player, "Breed cows and pigs");
            } else if (type == EntityType.CHICKEN) {
                addXPToJob(player, "Breed chickens");
            } else if (type == EntityType.BEE) {
                addXPToJob(player, "Breed bees");
            } else if (type == EntityType.WOLF || type == EntityType.CAT) {
                addXPToJob(player, "Breed wolf or cat");
            } else {
                addXPToJob(player, "Breed animals");
            }
            addXPToJob(player, "Breed farm animals");
        }
    }

    @EventHandler
    public void onTame(org.bukkit.event.entity.EntityTameEvent event) {
        if (event.getOwner() instanceof Player) {
            addXPToJob((Player) event.getOwner(), "Tame wolf or cat");
        }
    }

    @EventHandler
    public void onHoneyHarvest(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && (event.getClickedBlock().getType() == Material.BEEHIVE
                || event.getClickedBlock().getType() == Material.BEE_NEST)) {
            if (event.getItem() != null && event.getItem().getType() == Material.GLASS_BOTTLE) {
                addXPToJob(event.getPlayer(), "Harvest honey manually");
                addXPToJob(event.getPlayer(), "Bottle honey");
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // Mark the block as player-placed to prevent XP abuse when it's broken
        event.getBlock().setMetadata("placed_by_player", new FixedMetadataValue(plugin, true));

        // Farmer - Plant crops
        Material mat = event.getBlock().getType();
        if (mat == Material.WHEAT || mat == Material.CARROTS || mat == Material.POTATOES || 
            mat == Material.BEETROOTS || mat == Material.PUMPKIN_STEM || mat == Material.MELON_STEM || 
            mat == Material.SUGAR_CANE || mat == Material.SWEET_BERRY_BUSH) {
            addXPToJob(event.getPlayer(), "Plant crops");
        }
    }

    @EventHandler
    public void onBlockBreak(org.bukkit.event.block.BlockBreakEvent event) {
        plugin.getExperienceManager().awardBlockXP(event.getPlayer(), event.getBlock());
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            addXPToJob(event.getPlayer(), "Catch fish");
            if (event.getPlayer().getWorld().hasStorm()) {
                addXPToJob(event.getPlayer(), "Fish during rain");
            }
            if (event.getCaught() instanceof Item) {
                addXPToJob(event.getPlayer(), "Catch treasure");
            }
            
            org.bukkit.block.Biome biome = event.getPlayer().getLocation().getBlock().getBiome();
            String biomeName = biome.name();
            if (biomeName.contains("ICE") || biomeName.contains("FROZEN") || biomeName.contains("SNOW")) {
                addXPToJob(event.getPlayer(), "Fishing in Ice biome");
            }
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        Player player = (Player) event.getWhoClicked();
        Material result = event.getRecipe().getResult().getType();
        
        if (result.name().contains("IRON") || result.name().contains("GOLD") || result.name().contains("DIAMOND")
                || result.name().contains("NETHERITE")) {
            addXPToJob(player, "Craft ore-based items");
        }
        
        if (result.name().contains("WOODEN")) {
            addXPToJob(player, "Craft wooden tools");
        }

        // Beekeeper - Craft beehive/honey blocks
        if (result == Material.BEEHIVE || result == Material.BEE_NEST) {
            addXPToJob(player, "Craft beehive or bee nest");
        } else if (result == Material.HONEY_BLOCK) {
            addXPToJob(player, "Craft honey blocks");
        }

        // Culinary - Craft food items
        if (result == Material.BREAD || result == Material.CAKE || result == Material.GOLDEN_CARROT) {
            addXPToJob(player, "Cook food");
        }
    }

    @EventHandler
    public void onSmelt(FurnaceExtractEvent event) {
        Material type = event.getItemType();
        if (isOreResult(type)) {
            addXPToJob(event.getPlayer(), "Smelt ores");
        } else if (isCookedFoodResult(type)) {
            addXPToJob(event.getPlayer(), "Cook food");
        }
    }

    private boolean isOreResult(Material type) {
        String name = type.name();
        return name.contains("INGOT") || name.contains("DIAMOND") || name.contains("EMERALD") || 
               name.contains("QUARTZ") || name.contains("COAL") || name.contains("NETHERITE") ||
               name.contains("COPPER") || name.contains("REDSTONE") || name.contains("LAPIS");
    }

    private boolean isCookedFoodResult(Material type) {
        String name = type.name();
        return name.contains("COOKED") || name.equals("BAKED_POTATO") || name.equals("DRIED_KELP");
    }

    @EventHandler
    public void onEnchant(org.bukkit.event.enchantment.EnchantItemEvent event) {
        addXPToJob(event.getEnchanter(), "Enchant items");
    }

    @EventHandler
    public void onRepair(InventoryClickEvent event) {
        if (event.getInventory() instanceof AnvilInventory && event.getSlotType() == InventoryType.SlotType.RESULT) {
            if (event.getWhoClicked() instanceof Player) {
                Player player = (Player) event.getWhoClicked();
                addXPToJob(player, "Repair items in anvil");

                // Enchanter - Combine enchanted books
                ItemStack result = event.getCurrentItem();
                if (result != null && result.getType() == Material.ENCHANTED_BOOK) {
                    addXPToJob(player, "Combine enchanted books");
                }
            }
        }
        
        // Enchanter - Disenchant items (Grindstone)
        if (event.getInventory() instanceof GrindstoneInventory && event.getSlotType() == InventoryType.SlotType.RESULT) {
            if (event.getWhoClicked() instanceof Player) {
                addXPToJob((Player) event.getWhoClicked(), "Disenchant items");
            }
        }
    }

    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player killer = event.getEntity().getKiller();
            String worldName = event.getEntity().getWorld().getName().toLowerCase();

            if (event.getEntity() instanceof Monster) {
                addXPToJob(killer, "Kill hostile mobs");
                if (event.getEntity().getType() == EntityType.DROWNED) {
                    addXPToJob(killer, "Kill drowned");
                }
                if (event.getEntity().getType() == EntityType.STRAY) {
                    addXPToJob(killer, "Kill Strays");
                }
            } else if (event.getEntity() instanceof Animals) {
                addXPToJob(killer, "Kill passive mobs");
            }

            if (worldName.contains("nether")) {
                addXPToJob(killer, "Kill Nether mobs");
            } else if (worldName.contains("end")) {
                addXPToJob(killer, "Kill Endermen");
                addXPToJob(killer, "Kill Ender Dragon");
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            ItemStack held = player.getInventory().getItemInMainHand();
            boolean isProjectile = event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE;
            boolean isSpearOrTrident = held != null && (held.getType() == Material.TRIDENT || held.getType().name().contains("SPEAR"));

            if (isProjectile || isSpearOrTrident) {
                addXPToJob(player, "Deal damage with bow, crossbow, or trident");
            }
        }
    }

    @EventHandler
    public void onTNTExplode(EntityExplodeEvent event) {
        if (event.getEntity() instanceof TNTPrimed) {
            TNTPrimed tnt = (TNTPrimed) event.getEntity();
            if (tnt.getSource() instanceof Player) {
                Player player = (Player) tnt.getSource();
                for (Block block : event.blockList()) {
                    plugin.getExperienceManager().awardBlockXP(player, block);
                }
            }
        }
    }

    @EventHandler
    public void onCureStart(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() == EntityType.ZOMBIE_VILLAGER) {
            ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
            if (item != null && item.getType() == Material.GOLDEN_APPLE) {
                ZombieVillager zv = (ZombieVillager) event.getRightClicked();
                if (zv.hasPotionEffect(org.bukkit.potion.PotionEffectType.WEAKNESS)) {
                    zv.setMetadata("cured_by", new FixedMetadataValue(plugin, event.getPlayer().getUniqueId().toString()));
                }
            }
        }
    }

    @EventHandler
    public void onCureComplete(EntityTransformEvent event) {
        if (event.getTransformReason() == EntityTransformEvent.TransformReason.CURED) {
            if (event.getEntity().hasMetadata("cured_by")) {
                String uuidStr = event.getEntity().getMetadata("cured_by").get(0).asString();
                Player player = Bukkit.getPlayer(UUID.fromString(uuidStr));
                if (player != null) {
                    addXPToJob(player, "Cure zombie villager");
                }
            }
        }
    }

    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {
        if (event.getLootTable().getKey().getKey().contains("end_city")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getLocation().distanceSquared(event.getLootContext().getLocation()) < 100) {
                    addXPToJob(p, "Loot End Cities");
                }
            }
        }
    }

    @EventHandler
    public void onPearlLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof EnderPearl && event.getEntity().getShooter() instanceof Player) {
            addXPToJob((Player) event.getEntity().getShooter(), "Use Ender Pearls");
        }
    }

    @EventHandler
    public void onShear(PlayerShearEntityEvent event) {
        if (event.getEntity() instanceof Sheep) {
            addXPToJob(event.getPlayer(), "Shear sheep");
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (event.getRightClicked() instanceof Cow && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.BUCKET) {
            addXPToJob(player, "Milk cows");
        }
        
        if (event.getRightClicked() instanceof Tameable) {
            Tameable pet = (Tameable) event.getRightClicked();
            if (pet.isTamed() && pet.getOwner().getUniqueId().equals(player.getUniqueId())) {
                Material food = player.getInventory().getItemInMainHand().getType();
                if (food.isEdible() || food == Material.BONE || food == Material.ROTTEN_FLESH) {
                    addXPToJob(player, "Heal pets");
                }
            }
        }
    }

    @EventHandler
    public void onBoneMeal(PlayerInteractEvent event) {
        if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK && event.getItem() != null && event.getItem().getType() == Material.BONE_MEAL) {
            addXPToJob(event.getPlayer(), "Use bone meal");
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getLocation().getY() < 40) {
            String chunkKey = player.getWorld().getName() + ":" + player.getLocation().getChunk().getX() + "," + player.getLocation().getChunk().getZ();
            if (!discoveredChunks.contains(chunkKey)) {
                discoveredChunks.add(chunkKey);
                addXPToJob(player, "Discover new cave areas");
            }
        }
    }
}
