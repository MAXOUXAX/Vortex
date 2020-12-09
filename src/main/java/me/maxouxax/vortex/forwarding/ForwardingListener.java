package me.maxouxax.vortex.forwarding;

import me.maxouxax.vortex.BOT;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ForwardingListener implements EventListener {

    private final BOT bot;

    public ForwardingListener() {
        bot = BOT.getInstance();
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if(event instanceof MessageReceivedEvent) onMessage((MessageReceivedEvent)event);
    }

    private void onMessage(MessageReceivedEvent event) {
        ArrayList<ForwardedChannel> forwardedChannels = bot.getForwardingManager().getForwardedChannelArrayList().stream().filter(forwardedChannel -> forwardedChannel.getGuild().getId().equalsIgnoreCase(event.getGuild().getId()) && forwardedChannel.getSource().getId().equalsIgnoreCase(event.getChannel().getId())).collect(Collectors.toCollection(ArrayList::new));
        if(!forwardedChannels.isEmpty()){
            forwardedChannels.forEach(forwardedChannel -> {
                Message message = event.getMessage();
                MessageBuilder messageBuilder = new MessageBuilder(forwardedChannel.getRole().getAsMention());
                messageBuilder.append("\n").append(message.getContentRaw());
                forwardedChannel.getTarget().sendMessage(messageBuilder.build()).queue();
            });
        }
    }

}
