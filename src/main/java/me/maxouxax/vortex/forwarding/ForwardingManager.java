package me.maxouxax.vortex.forwarding;

import me.maxouxax.vortex.BOT;
import me.maxouxax.vortex.database.DatabaseManager;
import me.maxouxax.vortex.utils.EmbedCrafter;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ForwardingManager {

    private final ArrayList<ForwardedChannel> forwardedChannelArrayList = new ArrayList<>();
    private final BOT bot;

    public ForwardingManager() throws SQLException {
        this.bot = BOT.getInstance();
        Connection connection = DatabaseManager.getDatabaseAccess().getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM forwarded_channels");

        final ResultSet resultSet = preparedStatement.executeQuery();

        while(resultSet.next()){
            String uuid = resultSet.getString("uuid");
            String guildId = resultSet.getString("guild_id");
            Guild guild = bot.getJda().getGuildById(guildId);
            String sourceChannelId = resultSet.getString("source_channel_id");
            String targetChannelId = resultSet.getString("target_channel_id");
            String roleId = resultSet.getString("role_id");
            if (guild != null) {
                forwardedChannelArrayList.add(new ForwardedChannel(
                        UUID.fromString(uuid),
                        guild,
                        guild.getTextChannelById(sourceChannelId),
                        guild.getTextChannelById(targetChannelId),
                        guild.getRoleById(roleId)
                        ));
            }else{
                bot.getLogger().error("Invalid guild_id: "+guildId);
            }
        }
        connection.close();
    }

    public boolean alreadyExists(Guild guild, TextChannel source, TextChannel target){
        return forwardedChannelArrayList.stream().anyMatch(forwardedChannel -> forwardedChannel.getSource().getId().equalsIgnoreCase(source.getId()) && forwardedChannel.getTarget().getId().equalsIgnoreCase(target.getId()) && forwardedChannel.getGuild().getId().equalsIgnoreCase(guild.getId()));
    }

    public boolean configureNewForwardedChannel(Guild guild, TextChannel source, TextChannel target, Role role) throws SQLException {
        if(alreadyExists(guild, source, target)){
            return false;
        }else{
            UUID uuid = UUID.randomUUID();
            ForwardedChannel forwardedChannel = new ForwardedChannel(uuid, guild, source, target, role);
            forwardedChannelArrayList.add(forwardedChannel);
            updateDatabase(forwardedChannel);
        }
        return true;
    }

    private void updateDatabase(ForwardedChannel forwardedChannel) throws SQLException {
        Connection connection = DatabaseManager.getDatabaseAccess().getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE forwarded_channels SET guild_id = ?, source_channel_id = ?, target_channel_id = ?, role_id = ?, updated_at = ? WHERE uuid = ?");
        preparedStatement.setString(1, forwardedChannel.getGuild().getId());
        preparedStatement.setString(2, forwardedChannel.getSource().getId());
        preparedStatement.setString(3, forwardedChannel.getTarget().getId());
        preparedStatement.setString(4, forwardedChannel.getRole().getId());
        preparedStatement.setDate(5, new Date(System.currentTimeMillis()));
        preparedStatement.setString(6, forwardedChannel.getUuid().toString());

        final int updateCount = preparedStatement.executeUpdate();

        if (updateCount < 1) {
            PreparedStatement insertPreparedStatement = connection.prepareStatement("INSERT INTO forwarded_channels (uuid, guild_id, source_channel_id, target_channel_id, role_id, updated_at) VALUES (?, ?, ?, ?, ?, ?)");
            insertPreparedStatement.setString(1, forwardedChannel.getUuid().toString());
            insertPreparedStatement.setString(2, forwardedChannel.getGuild().getId());
            insertPreparedStatement.setString(3, forwardedChannel.getSource().getId());
            insertPreparedStatement.setString(4, forwardedChannel.getTarget().getId());
            insertPreparedStatement.setString(5, forwardedChannel.getRole().getId());
            insertPreparedStatement.setDate(6, new Date(System.currentTimeMillis()));
            insertPreparedStatement.execute();
        }
        connection.close();
    }

    public ArrayList<ForwardedChannel> getForwardedChannelArrayList() {
        return forwardedChannelArrayList;
    }

    public void deleteForwardedChannel(ForwardedChannel forwardedChannel) {
        try {
            forwardedChannelArrayList.remove(forwardedChannel);
            Connection connection = DatabaseManager.getDatabaseAccess().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM forwarded_channels WHERE uuid = ?");
            preparedStatement.setString(1, forwardedChannel.getUuid().toString());

            preparedStatement.execute();
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void forwardMessage(ForwardedChannel forwardedChannel, Message message) {
        MessageBuilder messageBuilder = new MessageBuilder(forwardedChannel.getRole().getAsMention());
        messageBuilder.append("\n").setEmbed(new EmbedCrafter().setTitle(":rotating_light: Alerte disponibilité "+forwardedChannel.getRole().getName(), "https://discord.gg/an2x2cn").setDescription(message.getContentRaw()).setColor(15158332).build());
        List<MessageEmbed> embeds = message.getEmbeds();
        System.out.println("embeds = " + embeds);
        if(!embeds.isEmpty()) {
            embeds.forEach(messageEmbed -> {
                System.out.println("messageEmbed = " + messageEmbed);
                System.out.println("messageEmbed.getDescription() = " + messageEmbed.getDescription());
                if (messageEmbed != null && (Objects.requireNonNull(messageEmbed.getDescription()).equalsIgnoreCase("Powered by distill.io") || Objects.requireNonNull(messageEmbed.getDescription()).equalsIgnoreCase(":lelogodesbavards: Bulletin d'information pour @deleted-role"))) {
                    embeds.remove(messageEmbed);
                }
            });
        }

        forwardedChannel.getTarget().sendMessage(messageBuilder.build()).queue();

        if (!embeds.isEmpty()) {
            embeds.forEach(messageEmbed -> {
                forwardedChannel.getTarget().sendMessage(messageEmbed).queueAfter(1, TimeUnit.SECONDS);
            });
        }
    }

}
