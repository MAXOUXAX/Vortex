package me.maxouxax.vortex.forwarding;

import me.maxouxax.vortex.BOT;

import java.util.HashMap;

public class TaskPullMessages implements Runnable {

    private BOT bot;
    private HashMap<ForwardedChannel, String> messageCache = new HashMap<>();

    public TaskPullMessages() {
        this.bot = BOT.getInstance();
        bot.getForwardingManager().getForwardedChannelArrayList().forEach(forwardedChannel -> {
            messageCache.put(forwardedChannel, forwardedChannel.getSource().getLatestMessageId());
        });
    }

    @Override
    public void run() {
        messageCache.forEach((forwardedChannel, s) -> {
            String latestMessageId = forwardedChannel.getSource().getLatestMessageId();
            if(!latestMessageId.equalsIgnoreCase(s)){
                forwardedChannel.getSource().retrieveMessageById(latestMessageId).queue(message -> {
                    bot.getForwardingManager().forwardMessage(forwardedChannel, message);
                });
                messageCache.put(forwardedChannel, latestMessageId);
            }
        });
    }

}
