//package com.github.nathakusuma.cubiciceplugin.antiafk;
//
//import com.github.nathakusuma.cubiciceplugin.CubicIcePlugin;
//import org.bukkit.Bukkit;
//import org.bukkit.Location;
//import org.bukkit.entity.Player;
//
//import java.util.HashMap;
//import java.util.UUID;
//
//public class AntiAFK {
//    private static HashMap<UUID, AFKPlayerData> prevAfkPlayerData = new HashMap<>();
//    private static HashMap<UUID, Integer> afkCheckCount = new HashMap<>();
//
//    private static boolean isInAFKRegion(Player player) {
//        boolean isSameWorld = player.getWorld().equals(AFKConfig.getWorldName());
//        int playerX = player.getLocation().getBlockX();
//        boolean isInRadiusX = playerX <= AFKConfig.getX() + AFKConfig.getRadius() && playerX >= AFKConfig.getX() - AFKConfig.getRadius();
//        int playerZ = player.getLocation().getBlockZ();
//        boolean isInRadiusZ = playerZ <= AFKConfig.getZ() + AFKConfig.getRadius() && playerZ >= AFKConfig.getZ() - AFKConfig.getRadius();
//        return isSameWorld && isInRadiusX && isInRadiusZ;
//    }
//
//    public static boolean isAFK(UUID uuid) {
//        return afkCheckCount.getOrDefault(uuid, 0) > AFKConfig.getTolerance();
//    }
//
//    private static void afkCheck(Player player, AFKPlayerData playerData) {
//        if(isInAFKRegion(player)) return;
//        UUID uuid = player.getUniqueId();
//        if(!prevAfkPlayerData.containsKey(uuid)) {
//            prevAfkPlayerData.put(uuid, playerData);
//            afkCheckCount.put(uuid, 0);
//            return;
//        }
//        int checkCount = afkCheckCount.get(uuid);
//        if(isAFK(uuid)) {
//            afkCheckCount.put(uuid, 0);
//            player.teleport(new Location(Bukkit.getWorld(AFKConfig.getWorldName()), AFKConfig.getX(), AFKConfig.getY(), AFKConfig.getZ()));
//            return;
//        }
//        if(prevAfkPlayerData.get(uuid).equals(playerData)) afkCheckCount.put(uuid, ++checkCount);
//        else prevAfkPlayerData.put(uuid, playerData);
//    }
//
//    public static void scheduleRepeatingCheck() {
//        CubicIcePlugin plugin = CubicIcePlugin.getPlugin(CubicIcePlugin.class);
//        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
//            for(Player p : Bukkit.getOnlinePlayers()) {
//                Location location = p.getLocation();
//                AFKPlayerData playerData = new AFKPlayerData(location.getYaw(), location.getPitch());
//                afkCheck(p, playerData);
//            }
//        }, 0, AFKConfig.getInterval());
//    }
//
//}
