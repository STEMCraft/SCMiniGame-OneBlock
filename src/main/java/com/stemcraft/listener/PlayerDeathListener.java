package com.stemcraft.listener;

import com.stemcraft.OneBlock;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerDeathListener implements Listener {
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        String worldName = OneBlock.getWorldName(player);

        if(player.getWorld().getName().equalsIgnoreCase(worldName)) {
            OneBlock.delete(player);
        }
    }


    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        String worldName = OneBlock.getWorldName(player);

        if(player.getWorld().getName().equalsIgnoreCase(worldName)) {
            event.setRespawnLocation(Bukkit.getWorlds().getFirst().getSpawnLocation());
        }
    }
}
