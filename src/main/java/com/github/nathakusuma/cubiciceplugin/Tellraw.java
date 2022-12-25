package com.github.nathakusuma.cubiciceplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Tellraw {
    public Tellraw(String message) {
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}