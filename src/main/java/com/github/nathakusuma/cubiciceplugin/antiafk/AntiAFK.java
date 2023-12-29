package com.github.nathakusuma.cubiciceplugin.antiafk;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class AntiAFK {

    private static HashMap<UUID, AfkPlayerData> prevAfkPlayerData = new HashMap<>();
    private static HashMap<UUID, Integer> afkCheckCount = new HashMap<>();

    public static boolean isAFK(UUID uuid, AfkPlayerData playerData) {
        int checkCount = afkCheckCount.getOrDefault(uuid, 0);
        if(checkCount > 5) {
            afkCheckCount.put(uuid, 0);
            return true;
        }
        if(prevAfkPlayerData.getOrDefault(uuid, null).equals(playerData)) afkCheckCount.put(player, ++checkCount);
        else prevAfkPlayerData.put(player, playerData);
        return false;
    }

}
