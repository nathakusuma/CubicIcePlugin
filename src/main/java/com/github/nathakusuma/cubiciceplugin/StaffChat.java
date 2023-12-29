package com.github.nathakusuma.cubiciceplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class StaffChat extends ListenerAdapter implements CommandExecutor, Listener {
    private final CubicIcePlugin plugin = CubicIcePlugin.getPlugin(CubicIcePlugin.class);
    private final long channelId = this.plugin.getConfig().getLong("StaffChatChannelID");
    private TextChannel channel = null;
    private static List<UUID> state = null;
    private BossBar bossBar;
    private final String permission = "cubicice.staffchat";

    private TextChannel getChannel() {
        if (this.channel == null) this.channel = CubicIcePlugin.bot.getTextChannelById(this.channelId);
        return this.channel;
    }

    private BossBar getBossBar() {
        if (this.bossBar == null) {
            this.bossBar = Bukkit.createBossBar(
                    ChatColor.translateAlternateColorCodes('&', "&f&lStaff Chat"), BarColor.PINK, BarStyle.SOLID);

            this.bossBar.setVisible(true);
        }
        return this.bossBar;
    }

    private boolean isEnabled(UUID uuid) {
        if (state == null) {
            state = new ArrayList<>();
        }
        return state.contains(uuid);
    }

    private void setEnabled(UUID uuid) {
        if (!isEnabled(uuid)) state.add(uuid);
        Player p = Bukkit.getPlayer(uuid);
        if (p == null || !p.isOnline())
            return;
        getBossBar().addPlayer(p);
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&d[&5Staff Chat&d] &7»&r &aStaff Chat diaktifkan"));
    }


    private void setDisabled(UUID uuid) {
        if (isEnabled(uuid)) state.remove(uuid);
        Player p = Bukkit.getPlayer(uuid);
        if (p == null || !p.isOnline())
            return;
        getBossBar().removePlayer(p);
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&d[&5Staff Chat&d] &7»&r &cStaff Chat dinonaktifkan"));
    }


    private void sendStaffChat(String author, String message) {
        Bukkit.broadcast(ChatColor.translateAlternateColorCodes('&', "&d[&5Staff Chat&d] " + author + " &7»&r " + message), permission);
        getChannel().sendMessage("**" + author + "** » " + message).queue();
    }


    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = null;
        if (sender instanceof Player) player = (Player) sender;
        if (args.length == 0) {
            if (!(sender instanceof Player)) return false;
            if (!isEnabled(player.getUniqueId())) {
                setEnabled(player.getUniqueId());
            } else {
                setDisabled(player.getUniqueId());
            }
        } else {
            StringBuilder sb = new StringBuilder();
            for (String s : args) {
                sb.append(s).append(" ");
            }
            sendStaffChat(sender.getName(), sb.toString().trim());
        }
        return true;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().hasPermission(permission))
            return;
        if (!isEnabled(event.getPlayer().getUniqueId()))
            return;
        event.setCancelled(true);
        sendStaffChat(event.getPlayer().getName(), event.getMessage());
    }


    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot())
            return;
        if (event.getChannel().getIdLong() != this.channelId)
            return;
        Bukkit.broadcast(ChatColor.translateAlternateColorCodes('&', "&9[&1Staff Chat&9] " + event
                .getAuthor().getName() + " &7»&r " + event.getMessage().getContentDisplay()), permission);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPermission(permission))
            return;
        getChannel().sendMessage("_**" + event.getPlayer().getName() + " masuk**_").queue();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!event.getPlayer().hasPermission(permission))
            return;
        getChannel().sendMessage("_**" + event.getPlayer().getName() + " keluar**_").queue();
        setDisabled(event.getPlayer().getUniqueId());
    }
}