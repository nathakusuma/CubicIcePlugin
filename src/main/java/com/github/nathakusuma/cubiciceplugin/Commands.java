package com.github.nathakusuma.cubiciceplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Commands implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        Player player;
        StringBuilder sb;
        byte i;
        if (args.length == 0)
            sender.sendMessage("" + ChatColor.GOLD + "This server is running CubicIcePlugin by BERTOTOD");
        switch (args[0].toLowerCase()) {
            case "_flyticket":
                if (sender instanceof Player) return true;
                player = Bukkit.getPlayer(args[1]);
                new Fly(player);
                break;
            case "tellraw":
                if (!sender.hasPermission("minecraft.command.tellraw")) return true;
                sb = new StringBuilder();
                for (i = 1; i < args.length; i = (byte) (i + 1)) {
                    sb.append(args[i]).append(" ");
                }
                new Tellraw(sb.toString());
                break;
        }
        return true;
    }
}