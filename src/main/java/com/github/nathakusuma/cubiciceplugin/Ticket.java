package com.github.nathakusuma.cubiciceplugin;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class Ticket extends ListenerAdapter {
    private static final CubicIcePlugin plugin = CubicIcePlugin.getPlugin(CubicIcePlugin.class);

    MessageEmbed wrongChannel = new EmbedBuilder()
            .setColor(Color.RED)
            .setTitle("Channel Salah")
            .setDescription("Kamu hanya bisa melakukan itu di dalam channel tiketmu.")
            .build();


    private boolean isCreator(long userID, long channelID) {
        try {
            PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT * FROM ticket WHERE channel_id=?");
            ps.setLong(1, channelID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return (rs.getLong("creator_id") == userID);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    private boolean isStaff(Member member) {
        return member.getRoles().contains(CubicIcePlugin.staffRole);
    }

    private boolean isCreatorOrStaff(Member member, TextChannel channel) {
        return (isCreator(member.getIdLong(), channel.getIdLong()) || isStaff(member));
    }

    private void closeTicket(TextChannel ticketChannel) {
        try {
            PreparedStatement ps = MySQL.getConnection().prepareStatement("DELETE FROM ticket WHERE channel_id=?");
            ps.setLong(1, ticketChannel.getIdLong());
            ps.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        ticketChannel.delete().queue();
    }

    StringSelectMenu menu = StringSelectMenu.create("ticket:type")
            .setPlaceholder("Pilih jenis tiket")
            .setRequiredRange(1, 1)
            .addOption("Donate", "donate")
            .addOption("Report Player", "report")
            .addOption("Lapor Bug", "bug")
            .addOption("Minta Refund", "refund")
            .addOption("Tanya / Bantuan", "help")
            .addOption("Lainnya", "others")
            .build();


    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot())
            return;
        if (event.getChannel().getName().startsWith("donate-")) {
            if (!isCreator(event.getAuthor().getIdLong(), event.getChannel().getIdLong()))
                return;
            try {
                donateQuestionAsked(event.getChannel().asTextChannel(), event.getMessage());
                donateProofed(event.getChannel().asTextChannel(), event.getMessage());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }


        if (!Objects.requireNonNull(event.getMember()).getRoles().contains(CubicIcePlugin.staffRole))
            return;
        if (!event.getMessage().getContentRaw().startsWith("."))
            return;
        String command = event.getMessage().getContentRaw().replaceFirst(".", "");
        if (command.startsWith("ticket init")) {
            TextChannel textChannel = event.getJDA().getTextChannelById(DataYML.getData().getLong("TicketButton.ChannelID"));
            if (textChannel != null) {
                try {
                    Message message = textChannel.retrieveMessageById(DataYML.getData().getLong("TicketButton.MessageID")).complete();
                    if (message != null) message.delete().queue();
                } catch (ErrorResponseException ignored) {
                }
            }
            event.getChannel().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(CubicIcePlugin.themeColor)
                    .setTitle("Tiket")
                    .setThumbnail("https://i.imgur.com/EJbCMeL.png")
                    .setDescription("Tiket adalah media untuk berkomunikasi langsung dengan para " + CubicIcePlugin.staffRole.getAsMention() + ".\n" +
                            "\u200b\n" +
                            "Dilarang menyalahgunakan tiket. Akan ada sanksi tegas untuk pengguna yang melakukannya.\n" +
                            "\u200b\n" +
                            "**Untuk membuat tiket, pilih jenis tiket di bawah dan ikuti petunjuk berikutnya.**\n" +
                            "\u200b\n" +
                            "Command Tiket:\n" +
                            "**/ticket close** - Menutup tiketmu\n" +
                            "**/ticket addguest @USER** - Menambahkan user lain sebagai tamu di dalam tiketmu\n" +
                            "**/ticket removeguest @USER** - Mengeluarkan tamu dari tiketmu")


                    .build()).setActionRow(this.menu).queue(message -> {
                DataYML.getData().set("TicketButton.ChannelID", message.getChannel().getIdLong());
                DataYML.getData().set("TicketButton.MessageID", message.getIdLong());
                DataYML.saveData();
            });
            event.getMessage().delete().queue();
        } else if (command.startsWith("ticket resetuser")) {
            List<User> mentionedUsers = event.getMessage().getMentions().getUsers();
            if (mentionedUsers.isEmpty()) {
                event.getMessage().reply("```.ticket resetuser @USER```").mentionRepliedUser(false).queue();
            } else {
                User user = mentionedUsers.get(0);
                try {
                    PreparedStatement ps = MySQL.getConnection().prepareStatement("DELETE FROM ticket WHERE creator_id=?");
                    ps.setLong(1, user.getIdLong());
                    ps.executeUpdate();
                    event.getMessage().replyEmbeds(new EmbedBuilder()
                            .setColor(Color.GREEN)
                            .setTitle("Done")
                            .setDescription("Ticket ownership data of " + user.getAsMention() + " has been reset.")
                            .build()).queue();
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (!event.getComponentId().equals("ticket:type"))
            return;
        event.getMessage().editMessageComponents().setActionRow(this.menu).queue();
        boolean exists = true;
        long previousTicketID = 0L;
        try {
            PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT * FROM ticket WHERE creator_id=?");
            ps.setLong(1, event.getUser().getIdLong());
            ResultSet rs = ps.executeQuery();
            exists = rs.next();
            if (exists) previousTicketID = rs.getLong("channel_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (exists) {
            event.replyEmbeds(new EmbedBuilder()
                            .setColor(Color.RED)
                            .setTitle("Kamu masih memiliki tiket terbuka")
                            .setDescription("Silahkan tutup atau gunakan tiketmu yang sebelumnya (<#" + previousTicketID + ">).")
                            .build())
                    .setEphemeral(true).queue();
        } else {
            Modal modal;
            TextInput username = TextInput.create("username", "Apa username Minecraft-mu?", TextInputStyle.SHORT)
                    .setRequired(true).setPlaceholder("Contoh: FrostDX").build();
            TextInput location = TextInput.create("location", "Dimana lokasi kejadiannya?", TextInputStyle.SHORT)
                    .setPlaceholder("Sertakan world dan kordinat jika memungkinkan").setRequired(true).build();
            TextInput time = TextInput.create("time", "Kapan waktu kejadiannya?", TextInputStyle.SHORT)
                    .setPlaceholder("Tulis sedetail mungkin").setRequired(true).build();

            switch (event.getInteraction().getSelectedOptions().get(0).getValue()) {
                case "donate":
                    TextInput donatedStatus = TextInput.create("status", "Apakah kamu sudah selesai donate?", TextInputStyle.SHORT)
                            .setPlaceholder("SUDAH / BELUM").setRequiredRange(5, 5).setRequired(true).build();
                    modal = Modal.create("ticket:donate", "Isi formulir ini terlebih dahulu")
                            .addComponents(ActionRow.of(username), ActionRow.of(donatedStatus)).build();
                    break;

                case "report":
                    TextInput reportedPlayer = TextInput.create("accused", "Siapakah player yang ingin kamu report?", TextInputStyle.SHORT)
                            .setPlaceholder("Contoh: BERTOTOD").setRequired(true).build();
                    TextInput reportReason = TextInput.create("description", "Mengapa kamu melaporkan player ini?", TextInputStyle.PARAGRAPH)
                            .setPlaceholder("Jelaskan kesalahan player tersebut").setRequired(true).build();
                    modal = Modal.create("ticket:report", "Isi formulir ini terlebih dahulu")
                            .addComponents(ActionRow.of(username), ActionRow.of(reportedPlayer), ActionRow.of(location), ActionRow.of(time), ActionRow.of(reportReason)).build();
                    break;

                case "bug":
                    TextInput bugDescription = TextInput.create("description", "Jelaskan mengenai bug yang kamu temukan!", TextInputStyle.PARAGRAPH)
                            .setRequired(true).build();
                    modal = Modal.create("ticket:bug", "Isi formulir ini terlebih dahulu")
                            .addComponents(ActionRow.of(username), ActionRow.of(location), ActionRow.of(time), ActionRow.of(bugDescription)).build();
                    break;

                case "refund":
                    TextInput refundItem = TextInput.create("item", "Apa yang kamu minta untuk direfund?", TextInputStyle.SHORT)
                            .setRequired(true).build();
                    TextInput refundDescription = TextInput.create("description", "Jelaskan mengenai permintaan refundmu!", TextInputStyle.PARAGRAPH)
                            .setPlaceholder("Jelaskan dengan detail agar mudah kami selidiki").setRequired(true).build();
                    modal = Modal.create("ticket:refund", "Isi formulir ini terlebih dahulu")
                            .addComponents(ActionRow.of(username), ActionRow.of(refundItem), ActionRow.of(location), ActionRow.of(time), ActionRow.of(refundDescription)).build();
                    break;

                case "help":
                    TextInput helpDescription = TextInput.create("description", "Apa yang ingin kamu tanyakan?", TextInputStyle.PARAGRAPH)
                            .setRequired(true).build();
                    TextInput helpVerification = TextInput.create("asked-otherplayer", "Apakah kamu sudah coba tanya ke player lain?", TextInputStyle.SHORT)
                            .setPlaceholder("SUDAH / BELUM").setRequired(true).build();
                    modal = Modal.create("ticket:help", "Isi formulir ini terlebih dahulu")
                            .addComponents(ActionRow.of(username), ActionRow.of(helpVerification), ActionRow.of(helpDescription)).build();
                    break;

                default:
                    TextInput othersDescription = TextInput.create("description", "Apa yang bisa kami bantu?", TextInputStyle.PARAGRAPH)
                            .setRequired(true).build();
                    modal = Modal.create("ticket:others", "Isi formulir ini terlebih dahulu")
                            .addComponents(ActionRow.of(username), ActionRow.of(othersDescription)).build();
                    break;
            }
            event.replyModal(modal).queue();
        }
    }

    MessageEmbed notDonatedResponse = new EmbedBuilder()
            .setColor(CubicIcePlugin.themeColor)
            .setTitle("Petunjuk Donate")
            .setDescription("Silakan donate sesuai petunjuk di #INGATGANTI_CHANNELDONATE\n" +
                    "Jika sudah selesai transaksi donate, tekan tombol 💵 lalu kirim screenshot bukti transaksi\n" +
                    "Jika ada pertanyaan, ketik pertanyaan disini lalu tekan tombol ❓\n")
            .build();

    Button donateDone = Button.secondary("ticket:donate_done", "💵");
    Button donateAsk = Button.secondary("ticket:donate_ask", "❓");


    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (!event.getModalId().startsWith("ticket:"))
            return;
        String ticketType = event.getModalId().replaceFirst("ticket:", "");
        Category category = event.getJDA().getCategoryById(plugin.getConfig().getLong("TicketCategoryID"));
        assert category != null;
        String username = event.getValue("username").getAsString();
        boolean isDonateTicket = event.getModalId().equals("ticket:donate");
        boolean donated = event.getValue("status").getAsString().equalsIgnoreCase("sudah");
        category.createTextChannel(ticketType + "-" + username)
                .setTopic(event.getUser().getAsMention())
                .addMemberPermissionOverride(event.getUser().getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL), null)
                .queue(ticketChannel -> {
                    try {
                        PreparedStatement ps = MySQL.getConnection().prepareStatement("INSERT INTO ticket (channel_id, creator_id) VALUES (?,?)");
                        ps.setLong(1, ticketChannel.getIdLong());
                        ps.setLong(2, event.getUser().getIdLong());
                        ps.executeUpdate();
                    } catch (SQLException exception) {
                        exception.printStackTrace();
                    }
                    event.replyEmbeds(new EmbedBuilder()
                                    .setColor(Color.GREEN)
                                    .setTitle("Tiket Dibuat")
                                    .setDescription("Tiketmu berada di " + ticketChannel.getAsMention()).build())
                            .setEphemeral(true).queue();
                    ticketChannel.sendMessage(event.getUser().getAsMention()).setEmbeds(new EmbedBuilder()
                            .setColor(CubicIcePlugin.themeColor)
                            .setTitle("Hai, " + username + "!")
                            .setDescription("Mohon tunggu " + CubicIcePlugin.staffRole.getAsMention() + " untuk merespon tiketmu.\n" +
                                    "Tolong sabar dan jangan spam.\n" +
                                    "Jika kamu tidak bermaksud untuk membuat tiket ini, silakan tutup tiketnya segera.\n" +
                                    "Kirim command **/ticket close** jika ingin menutup tiket").build()).queue();
                    StringBuilder sb = new StringBuilder();
                    for (ModalMapping v : event.getValues()) {
                        String capital = v.getId().substring(0, 1).toUpperCase();
                        sb.append("**").append(capital).append(v.getId().substring(1)).append("**").append(" : ").append(v.getAsString()).append("\n");
                    }
                    if (isDonateTicket) {
                        ticketChannel.sendMessageEmbeds(new EmbedBuilder()
                                .setColor(CubicIcePlugin.themeColor)
                                .setTitle("Formulir Tiket")
                                .setDescription(sb.toString()).build())
                                .queue();
                        if (donated) {
                            try {
                                waitDonateProof(ticketChannel, event.getUser(), null);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            ticketChannel.sendMessageEmbeds(this.notDonatedResponse).setActionRow(this.donateDone, this.donateAsk).queue();
                        }
                    } else {
                        ticketChannel.sendMessage(CubicIcePlugin.staffRole.getAsMention() + " **TIKET BARU 🚨 [ __" + ticketType.toUpperCase() + "__ ]**")
                                .setEmbeds(new EmbedBuilder()
                                        .setColor(CubicIcePlugin.themeColor)
                                        .setTitle("Formulir Tiket")
                                        .setDescription(sb.toString()).build())
                                .queue();
                    }
                });
    }

    private void waitDonateProof(TextChannel ticketChannel, User creator, InteractionHook hook) throws SQLException {
        PreparedStatement ps = MySQL.getConnection().prepareStatement("UPDATE ticket SET waiting_donate_proof=true WHERE channel_id=?");
        ps.setLong(1, ticketChannel.getIdLong());
        ps.executeUpdate();
        if (hook != null) {
            hook.editOriginalEmbeds(this.notDonatedResponse).setActionRow(Button.primary("ticket:donate_done", "💵").asDisabled(), this.donateAsk.asDisabled()).queue();
        }
        ticketChannel.sendMessage(creator.getAsMention() + ", kirim **bukti transaksi** donatemu di sini!").queue();
    }

    private void donateProofed(TextChannel ticketChannel, Message message) throws SQLException {
        PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT waiting_donate_proof FROM ticket WHERE channel_id=?");
        ps.setLong(1, ticketChannel.getIdLong());
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            boolean waiting = rs.getBoolean("waiting_donate_proof");
            if (waiting) {
                PreparedStatement ps2 = MySQL.getConnection().prepareStatement("UPDATE ticket SET waiting_donate_proof=false WHERE channel_id=?");
                ps2.setLong(1, ticketChannel.getIdLong());
                ps2.executeUpdate();
                message.reply(CubicIcePlugin.staffRole.getAsMention() + " **TIKET BARU 🚨 [ __DONATE-DONE__ ]**").mentionRepliedUser(false).queue();
            }
        }
    }

    private void waitDonateQuestion(TextChannel ticketChannel, User creator, InteractionHook hook) throws SQLException {
        PreparedStatement ps = MySQL.getConnection().prepareStatement("UPDATE ticket SET waiting_donate_question=true WHERE channel_id=?");
        ps.setLong(1, ticketChannel.getIdLong());
        ps.executeUpdate();
        hook.editOriginalEmbeds(this.notDonatedResponse).setActionRow(this.donateDone.asDisabled(), Button.primary("ticket:donate_ask", "❓").asDisabled()).queue();
        ticketChannel.sendMessage(creator.getAsMention() + ", kamu mau bertanya apa?").queue();
    }

    private void donateQuestionAsked(TextChannel ticketChannel, Message message) throws SQLException {
        PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT waiting_donate_question FROM ticket WHERE channel_id=?");
        ps.setLong(1, ticketChannel.getIdLong());
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            boolean waiting = rs.getBoolean("waiting_donate_question");
            if (waiting) {
                PreparedStatement ps2 = MySQL.getConnection().prepareStatement("UPDATE ticket SET waiting_donate_question=false WHERE channel_id=?");
                ps2.setLong(1, ticketChannel.getIdLong());
                ps2.executeUpdate();
                message.reply(CubicIcePlugin.staffRole.getAsMention() + " **TIKET BARU 🚨 [ __DONATE-ASK__ ]**").mentionRepliedUser(false).queue();
            }
        }
    }

    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!event.getButton().getId().startsWith("ticket:donate_"))
            return;
        if (!isCreator(event.getUser().getIdLong(), event.getChannel().getIdLong())) {
            event.replyEmbeds(this.wrongChannel).setEphemeral(true).queue();
        }
        String buttonId = event.getButton().getId().replaceFirst("ticket:donate_", "");
        event.deferEdit().queue();
        if (buttonId.equals("done")) {
            try {
                waitDonateProof(event.getChannel().asTextChannel(), event.getUser(), event.getHook());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                waitDonateQuestion(event.getChannel().asTextChannel(), event.getUser(), event.getHook());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("ticket"))
            return;
        Member member = event.getMember();
        assert member != null;
        if (Objects.equals(event.getSubcommandName(), "close")) {
            if (isCreatorOrStaff(member, event.getChannel().asTextChannel())) {
                event.replyEmbeds(new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setTitle("Menutup Tiket")
                        .setDescription("<a:loading:818738342426443787> Tiket akan ditutup dalam 10 detik")
                        .build()).queue();
                Bukkit.getScheduler().runTaskLater(plugin, () -> closeTicket(event.getChannel().asTextChannel()), 200L);
            } else {
                event.replyEmbeds(this.wrongChannel).queue();
            }
        } else if (Objects.equals(event.getSubcommandName(), "addguest")) {
            if (!isCreatorOrStaff(member, event.getChannel().asTextChannel())) {
                event.replyEmbeds(this.wrongChannel).queue();
            } else {
                Member target = Objects.requireNonNull(event.getOption("guest")).getAsMember();
                assert target != null;
                if (target.hasPermission(event.getChannel().asTextChannel(), Permission.VIEW_CHANNEL)) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setColor(Color.RED)
                            .setTitle("Gagal")
                            .setDescription(target.getAsMention() + " sudah berada di tiket ini.")
                            .build()).queue();
                } else {
                    event.getChannel().asTextChannel().getManager().putMemberPermissionOverride(target.getIdLong(),
                            EnumSet.of(Permission.VIEW_CHANNEL), null).queue();
                    event.replyEmbeds(new EmbedBuilder()
                            .setColor(Color.GREEN)
                            .setTitle("Berhasil")
                            .setDescription(target.getAsMention() + " telah ditambahkan sebagai tamu di tiket ini.")
                            .build()).queue();
                }
            }
        } else if (Objects.equals(event.getSubcommandName(), "removeguest")) {
            if (!isCreatorOrStaff(member, event.getChannel().asTextChannel())) {
                event.replyEmbeds(this.wrongChannel).queue();
            } else {
                Member target = Objects.requireNonNull(event.getOption("guest")).getAsMember();
                assert target != null;
                if (!target.hasPermission(event.getChannel().asTextChannel(), Permission.VIEW_CHANNEL)) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setColor(Color.RED)
                            .setTitle("Gagal")
                            .setDescription(target.getAsMention() + " bukan tamu di tiket ini.")
                            .build()).queue();
                } else {
                    event.getChannel().asTextChannel().getManager().removePermissionOverride(target.getIdLong()).queue();
                    event.replyEmbeds(new EmbedBuilder()
                            .setColor(Color.GREEN)
                            .setTitle("Berhasil")
                            .setDescription(target.getAsMention() + " telah dikeluarkan dari tiket ini.")
                            .build()).queue();
                }
            }
        }
    }
}