package me.maxouxax.vortex.utils;

import me.maxouxax.vortex.BOT;

import java.util.Arrays;

public class ErrorHandler {

    private final BOT bot;

    public ErrorHandler() {
        this.bot = BOT.getInstance();
    }

    public void handleException(Throwable exception){
        bot.getLogger().error("Une erreur est survenue !\n"+exception.getMessage());
        exception.printStackTrace();
        bot.getLogger().error(exception.getMessage()+"\n"+Arrays.toString(exception.getStackTrace()), false);
    }



}
