package com.darkun7.job.listeners;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.Job;
import com.darkun7.job.models.PlayerJobData;
import org.bukkit.ChatColor;
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
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.UUID;

public class TriggerListener implements Listener {

    private final JobPlugin plugin;

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
            } else {
                addXPToJob(player, "Breed animals");
            }

            if (type == EntityType.BEE) {
                addXPToJob(player, "Breed bees");
            }
            if (type == EntityType.WOLF || type == EntityType.CAT) {
                addXPToJob(player, "Breed wolf or cat");
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
    }

    @EventHandler
    public void onSmelt(FurnaceExtractEvent event) {
        addXPToJob(event.getPlayer(), "Smelt ores");
    }

    @EventHandler
    public void onEnchant(org.bukkit.event.enchantment.EnchantItemEvent event) {
        addXPToJob(event.getEnchanter(), "Enchant items");
    }

    @EventHandler
    public void onRepair(InventoryClickEvent event) {
        if (event.getInventory() instanceof AnvilInventory && event.getSlotType() == InventoryType.SlotType.RESULT) {
            if (event.getWhoClicked() instanceof Player) {
                addXPToJob((Player) event.getWhoClicked(), "Repair items in anvil");
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
        if (event.getDamager() instanceof Player && (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE)) {
            Player player = (Player) event.getDamager();
            addXPToJob(player, "Deal damage with bow, crossbow, or trident");
        }
    }

    @EventHandler
    public void onTNTExplode(EntityExplodeEvent event) {
        if (event.getEntity() instanceof TNTPrimed) {
            TNTPrimed tnt = (TNTPrimed) event.getEntity();
            if (tnt.getSource() instanceof Player) {
                Player player = (Player) tnt.getSource();
                for (Block block : event.blockList()) {
                    // This uses the anti-abuse logic internally (marks player-placed blocks as
                    // skip)
                    plugin.getExperienceManager().awardBlockXP(player, block);
                }
            }
        }
    }
}
