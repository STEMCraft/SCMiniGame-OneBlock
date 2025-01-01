package com.stemcraft.command;

import com.stemcraft.STEMCraftCommand;
import com.stemcraft.util.SCHologram;
import com.stemcraft.util.SCPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class OneBlock extends STEMCraftCommand {
    com.stemcraft.OneBlock plugin;

    public OneBlock(com.stemcraft.OneBlock instance) {
        plugin = instance;
    }

    @Override
    public void execute(CommandSender sender, String command, List<String> args) {
        if (!sender.hasPermission("stemcraft.oneblock")) {
            message(sender, "You do not have permission to use this command");
            return;
        }

        if(!(sender instanceof Player)) {
            error(sender, "This command cannot be run from the console");
            return;
        }

        if (args.isEmpty()) {
            args.add("join");
        }

        switch (args.getFirst().toLowerCase()) {
            case "join":
                executeJoin((Player)sender);
                break;
            case "leave":
                executeLeave((Player)sender);
                break;
            case "delete":
                executeDelete((Player)sender);
                break;
            case "hologram":
                executeHologram((Player)sender, args);
                break;
            default:
                messageUsage(sender);
                break;
        }
    }

    public void messageUsage(CommandSender sender) {
        String usage = "Usage: /oneblock <join|leave|delete|hologram <create|delete>>";
        message(sender, usage);
    }

    public void executeJoin(Player player) {
        com.stemcraft.OneBlock.joinInstance(player);
    }

    public void executeLeave(Player player) {
        com.stemcraft.OneBlock.saveInstance(player);
        SCPlayer.teleport(player, Bukkit.getWorlds().getFirst().getSpawnLocation());
    }

    public void executeDelete(Player player) {
        SCPlayer.teleport(player, Bukkit.getWorlds().getFirst().getSpawnLocation(), () -> {
            com.stemcraft.OneBlock.deleteInstance(player);
        });
    }

    public void executeHologram(Player player, List<String> args) {
        if(args.size() < 2 || args.get(1).equalsIgnoreCase("create")) {
            Location location = player.getLocation();

            Block block = player.getTargetBlockExact(15);
            if(block != null) {
                location = block.getLocation();
            }

            SCHologram.create(location, "oneblock-score");
        } else if(args.get(1).equalsIgnoreCase("delete")) {
            List<UUID> list = SCHologram.find(player.getLocation(), "oneblock-score", 10);

            list.forEach(SCHologram::delete);

        } else {
            messageUsage(player);
        }
    }
}
