package com.github.nathakusuma.cubiciceplugin;

import com.github.nathakusuma.cubicicediscord.CubicIceDiscord;
//import com.github.nathakusuma.cubiciceplugin.antiafk.AntiAFK;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;

public final class CubicIcePlugin extends JavaPlugin {
    public static final Color themeColor = new Color(0x8CD7F8);
    public static JDA bot = null;
    public static Role staffRole = null;

    public static Guild guild = null;

    public void onEnable() {
        saveDefaultConfig();
        DataYML.saveDefaultData();
        MySQL.createTable();
        getCommand("staffchat").setExecutor(new StaffChat());
        getCommand("cubiciceplugin").setExecutor(new MinecraftCommand());
        getServer().getPluginManager().registerEvents(new StaffChat(), this);
//        AntiAFK.scheduleRepeatingCheck();
        bot = CubicIceDiscord.getBot();
        bot.addEventListener(new Ticket());
        bot.addEventListener(new StaffChat());
        guild = bot.getGuildById(getConfig().getString("GuildID"));
        DiscordCommand.upsertCommands();
        staffRole = bot.getRoleById(getConfig().getString("StaffRoleID"));
    }


    public void onDisable() {
        bot.shutdownNow();
    }
}