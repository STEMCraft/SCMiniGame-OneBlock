package com.stemcraft.command;

import com.stemcraft.STEMCraftCommand;
import com.stemcraft.util.SCPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class OneBlock extends STEMCraftCommand {
    com.stemcraft.OneBlock plugin;

    public OneBlock(com.stemcraft.OneBlock instance) {
        plugin = instance;
    }

    @Override
    public void execute(CommandSender sender, String command, List<String> args) {
        String usage = "Usage: /oneblock <join|leave|delete>";

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
                executeJoin((Player)sender, args);
                break;
            case "leave":
                executeLeave((Player)sender, args);
                break;
            case "delete":
                executeDelete((Player)sender, args);
                break;
            default:
                message(sender, usage);
                break;
        }
    }

    public void executeJoin(Player player, List<String> args) {
        com.stemcraft.OneBlock.joinInstance(player);
    }

    public void executeLeave(Player player, List<String> args) {
        com.stemcraft.OneBlock.saveInstance(player);
        SCPlayer.teleport(player, Bukkit.getWorlds().getFirst().getSpawnLocation());
    }

    public void executeDelete(Player player, List<String> args) {
        SCPlayer.teleport(player, Bukkit.getWorlds().getFirst().getSpawnLocation(), () -> {
            com.stemcraft.OneBlock.deleteInstance(player);
        });
    }
}
