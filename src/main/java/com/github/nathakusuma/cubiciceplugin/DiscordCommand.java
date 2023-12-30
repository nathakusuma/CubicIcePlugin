package com.github.nathakusuma.cubiciceplugin;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class DiscordCommand {

    public static void upsertCommands() {
        CubicIcePlugin.guild.upsertCommand("ticket", "Perintah tiket").addSubcommands(
                new SubcommandData("close", "Menutup tiket"),
                new SubcommandData("addguest", "Menambahkan user lain sebagai tamu di dalam tiketmu")
                        .addOption(OptionType.USER, "guest", "User yang ingin ditambahkan"),
                new SubcommandData("removeguest", "Mengeluarkan tamu dari tiketmu")
                        .addOption(OptionType.USER, "guest", "User yang ingin dikeluarkan")
        ).queue();
    }

}
