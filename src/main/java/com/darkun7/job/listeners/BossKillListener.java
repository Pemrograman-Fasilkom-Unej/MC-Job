package com.darkun7.job.listeners;

import com.darkun7.job.JobPlugin;
import com.darkun7.job.models.PlayerJobData;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.HashMap;
import java.util.Map;

public class BossKillListener implements Listener {

    private final JobPlugin plugin;
    private final Map<EntityType, String> bossNames = new HashMap<>();

    public BossKillListener(JobPlugin plugin) {
        this.plugin = plugin;
        bossNames.put(EntityType.ENDER_DRAGON, "Ender Dragon");
        bossNames.put(EntityType.WITHER, "Wither");
        bossNames.put(EntityType.ELDER_GUARDIAN, "Elder Guardian");
        bossNames.put(EntityType.WARDEN, "Warden");
    }

    @EventHandler
    public void onBossKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null)
            return;

        EntityType type = event.getEntityType();
        if (bossNames.containsKey(type)) {
            Player killer = event.getEntity().getKiller();
            PlayerJobData data = plugin.getPlayerDataManager().getPlayerData(killer.getUniqueId());
            String bossName = bossNames.get(type);

            if (!data.getKilledBosses().contains(bossName)) {
                data.addKilledBoss(bossName);
                killer.sendMessage(ChatColor.GOLD + "★ CONGRATULATIONS ★");
                killer.sendMessage(ChatColor.YELLOW + "You have defeated the " + ChatColor.RED + bossName
                        + ChatColor.YELLOW + "!");
                killer.sendMessage(ChatColor.GREEN + "You have earned a permanent " + ChatColor.AQUA + "+1 Skill Slot"
                        + ChatColor.GREEN + "!");
                killer.sendMessage(ChatColor.GRAY + "Total slots: " + data.getMaxActiveSkillSlots());

                // Save data immediately for persistence
                plugin.getPlayerDataManager().savePlayer(killer.getUniqueId());
            }
        }
    }
}
