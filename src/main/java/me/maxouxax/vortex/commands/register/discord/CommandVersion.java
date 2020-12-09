package me.maxouxax.vortex.commands.register.discord;

import me.maxouxax.vortex.BOT;
import me.maxouxax.vortex.commands.Command;
import me.maxouxax.vortex.commands.CommandMap;
import me.maxouxax.vortex.utils.EmbedCrafter;
import net.dv8tion.jda.api.entities.MessageChannel;

public class CommandVersion {

    private final BOT bot;
    private final CommandMap commandMap;

    public CommandVersion(CommandMap commandMap) {
        this.commandMap = commandMap;
        this.bot = BOT.getInstance();
    }

    @Command(name="version",type= Command.ExecutorType.USER,description="Affiche les informations sur la version du BOT", help = "version", example = "version")
    private void version(MessageChannel channel){
        try{
            EmbedCrafter builder = new EmbedCrafter();
            builder.setTitle("Thérèse • by MAXOUXAX • Amazingly powerful", bot.getConfigurationManager().getStringValue("websiteUrl"));
            builder.setColor(3447003);
            builder.addField("Je suis en version", bot.getVersion(), true);
            builder.addField("Je gère", commandMap.getDiscordCommands().size()+" commandes Discord", true);
            channel.sendMessage(builder.build()).queue();
        }catch (Exception e) {
            bot.getErrorHandler().handleException(e);
            channel.sendMessage("An error occured > " + e.getMessage()).queue();
        }
    }

}
