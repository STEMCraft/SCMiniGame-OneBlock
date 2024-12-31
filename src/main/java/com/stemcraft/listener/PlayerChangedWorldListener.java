package com.stemcraft.listener;

import com.stemcraft.OneBlock;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerChangedWorldListener implements Listener {
    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World prevWorld = event.getFrom();
        String worldName = OneBlock.getWorldName(player);

        if(prevWorld.getName().equalsIgnoreCase(worldName)) {
            OneBlock.stopInstance(player);
        }

        if(player.getWorld().getName().equalsIgnoreCase(worldName)) {
            OneBlock.startInstance(player);
        }
    }
}
