package me.maxouxax.vortex.commands.register.discord;

import me.maxouxax.vortex.BOT;
import me.maxouxax.vortex.commands.Command;
import me.maxouxax.vortex.commands.CommandMap;
import me.maxouxax.vortex.forwarding.ForwardedChannel;
import me.maxouxax.vortex.utils.EmbedCrafter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class CommandForward {

    private final BOT bot;
    private final CommandMap commandMap;

    public CommandForward(CommandMap commandMap) {
        this.commandMap = commandMap;
        this.bot = BOT.getInstance();
    }

    @Command(name="forward",
            type = Command.ExecutorType.USER,
            description="Permet de configurer un profil de transfert",
            help = "forward help\nforward configure <#salon cible> <@role>\nforward list\nforward remove <#salon source>", example = "forward configure <#salon cible> <@role>")
    private void forward(TextChannel channel, Message message, String[] args) throws SQLException {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            channel.sendMessage(commandMap.getHelpEmbed("forward")).queue();
        } else {
            String arg1 = args[0];
            if (arg1.equalsIgnoreCase("configure")) {
                if (args.length >= 3) {
                    if (message.getMentionedChannels().size() == 1 && message.getMentionedRoles().size() == 1) {
                        TextChannel targetChannel = message.getMentionedChannels().get(0);
                        Role role = message.getMentionedRoles().get(0);
                        if (bot.getForwardingManager().configureNewForwardedChannel(channel.getGuild(), channel, targetChannel, role)) {
                            channel.sendMessage(new EmbedCrafter().setTitle("Configuration d'un transfert réussie !")
                                    .setDescription("Désormais, l'ensemble des messages postés dans " + channel.getAsMention()
                                            + " seront transférés dans " + targetChannel.getAsMention()
                                            + " et le rôle " + role.getAsMention() + " sera notifié !")
                                    .setColor(Color.GREEN).build()).queue();
                        } else {
                            channel.sendMessage(new EmbedCrafter().setTitle("Configuration d'un transfert échouée")
                                    .setDescription("Un transfert entre ces deux salons est déjà configuré !\n" +
                                            "Consultez la liste des transferts configurés avec `" + commandMap.getDiscordTag() + "forward list` " +
                                            "et supprimez un transfert avec `" + commandMap.getDiscordTag() + "forward remove` !")
                                    .setColor(Color.RED).build()).queue();
                        }
                    } else {
                        channel.sendMessage(commandMap.getHelpEmbed("forward")).queue();
                    }
                } else {
                    channel.sendMessage(commandMap.getHelpEmbed("forward")).queue();
                }
            } else if (arg1.equalsIgnoreCase("list")) {
                channel.sendMessage(listChannels(bot.getForwardingManager().getForwardedChannelArrayList()).build()).queue();
            } else if (arg1.equalsIgnoreCase("remove")) {
                if (message.getMentionedChannels().size() == 1) {
                    TextChannel source = message.getMentionedChannels().get(0);
                    ArrayList<ForwardedChannel> forwardedChannelArrayList = bot.getForwardingManager().getForwardedChannelArrayList().stream().filter(forwardedChannel -> forwardedChannel.getSource().getId().equalsIgnoreCase(source.getId())).collect(Collectors.toCollection(ArrayList::new));
                    if (forwardedChannelArrayList.size() > 1) {
                        /*if(forwardedChannelArrayList.size() < 10) {
                            EmbedCrafter embedCrafter = listChannels(forwardedChannelArrayList);
                            embedCrafter.setTitle("Veuillez choisir le transfert à supprimer")
                                    .setDescription("Plusieurs configurations de transfert ont été trouvées pour le salon source " + source.getAsMention() + "\nVeuillez choisir le salon approprié avec les réactions ci-dessous\n\n" + embedCrafter.getDescription())
                                    .setColor(Color.CYAN);
                            channel.sendMessage(embedCrafter.build()).queue(interactiveMessage -> {
                                for (int i = 0; i < forwardedChannelArrayList.size(); i++) {
                                    interactiveMessage.addReaction(bot.getJda().getEmotesByName(EmojiMatcher.getEmojiFromInteger(i).getValue(), true).get(0)).queue();
                                }

                            });
                         */
                        channel.sendMessage("Impossible de répondre à cette requête, plusieurs transferts ayant le même salon source ont été trouvés !").queue();
                    } else {
                        channel.sendMessage("Impossible de répondre à cette requête, plus de 10 transferts ayant le même salon source ont été trouvés !").queue();
                    }

                }
            }
        }
    }

    public EmbedCrafter listChannels(ArrayList<ForwardedChannel> forwardedChannelArrayList){
        EmbedCrafter embedCrafter = new EmbedCrafter().setTitle("Liste des configurations de transfert")
                .setColor(Color.YELLOW)
                .setDescription("**Salon source** - **Salon cible** - **Rôle**\n");
        StringBuilder stringBuilder = new StringBuilder();
        forwardedChannelArrayList.forEach(forwardedChannel -> {
            stringBuilder.append(forwardedChannel.getSource().getAsMention()).append(" - ").append(forwardedChannel.getTarget().getAsMention()).append(" - ").append(forwardedChannel.getRole().getAsMention()).append("\n");
        });
        embedCrafter.setDescription(embedCrafter.getDescription()+stringBuilder.toString());
        return embedCrafter;
    }

}
