package com.github.nathakusuma.cubiciceplugin;

import com.github.nathakusuma.cubicicediscord.CubicIceDiscord;
import net.dv8tion.jda.api.JDA;
import org.bukkit.plugin.java.JavaPlugin;

public final class CubicIcePlugin extends JavaPlugin {

    JDA bot = null;

    @Override
    public void onEnable() {
        getCommand("staffchat").setExecutor(new StaffChat());
        getServer().getPluginManager().registerEvents(new StaffChat(), this);
        bot = CubicIceDiscord.getBot();
        bot.addEventListener(new StaffChat());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
