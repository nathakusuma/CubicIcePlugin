package com.github.nathakusuma.cubiciceplugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.bukkit.ChatColor;


public class MySQL {
    private static final CubicIcePlugin plugin = CubicIcePlugin.getPlugin(CubicIcePlugin.class);
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String hostname = plugin.getConfig().getString("MySQL.Hostname");
            String port = plugin.getConfig().getString("MySQL.Port");
            String database = plugin.getConfig().getString("MySQL.Database");
            String username = plugin.getConfig().getString("MySQL.Username");
            String password = plugin.getConfig().getString("MySQL.Password");
            connection = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database, username, password);
            plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&aConnected to MySQL Database."));
        }
        return connection;
    }

    public static void createTable() {
        try {
            getConnection().prepareStatement(
                    "CREATE TABLE IF NOT EXISTS ticket (channel_id BIGINT NOT NULL,creator_id BIGINT NOT NULL,waiting_donate_proof BOOLEAN NOT NULL DEFAULT FALSE,waiting_donate_question BOOLEAN NOT NULL DEFAULT FALSE,PRIMARY KEY (channel_id))"
            ).executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}