package com.github.nathakusuma.cubiciceplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.Objects;

public class Fly {
public Fly(Player player) {
  CubicIcePlugin plugin = CubicIcePlugin.getPlugin(CubicIcePlugin.class);
  Map<String, Object> groups = Objects.requireNonNull(plugin.getConfig().getConfigurationSection("Fly.groups")).getValues(false);
  for (Map.Entry<String, Object> entry : groups.entrySet()) {
    if (player.hasPermission("cubicice.fly." + entry.getKey())) {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tempfly add " + player.getName() + " " + entry.getValue());
      Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&8[&dCubicIceFly&8] &2" + player.getName() + " &atelah menggunakan &d&lFly Ticket &adan mendapatkan " + entry.getValue() + " waktu terbang."));
      Location loc = player.getLocation();
      for (int i = 0; i < 3; i++) {
        Location newLocation = loc.add((new Vector(Math.random() - 0.5D, 0.0D, Math.random() - 0.5D)).multiply(3));
        player.getWorld().spawnEntity(newLocation, EntityType.FIREWORK);
      } 
      break;
    } 
  } 
}
}