package me.maxouxax.stockbot.forwarding;

import me.maxouxax.stockbot.BOT;
import me.maxouxax.stockbot.database.DatabaseManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;

public class ForwardingManager {

    private final ArrayList<ForwardedChannel> forwardedChannelArrayList = new ArrayList<ForwardedChannel>();
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
}
