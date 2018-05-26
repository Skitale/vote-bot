package com.bin.bot;

import com.bin.Main;
import com.bin.steamapi.SteamApiDataStorage;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.PingEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Bot extends ListenerAdapter {

    //Mods usernames must be lowercase
    private List<String> mods = new ArrayList<String>();
    private SteamApiDataStorage dataStorage;
    private HandlerCommand handlerCommand;

    public Bot(SteamApiDataStorage steamApiDataStorage, List<String> excludeList, List<String> includeList, Map<String, String> messages) {
        this.dataStorage = steamApiDataStorage;
        String ownerNickName = Main.CHANNEL;
        mods.add(ownerNickName);
        handlerCommand = new HandlerCommand(ownerNickName, mods, excludeList, includeList, messages);
    }

    //This will return the response from the command
    private String runCommands(GenericMessageEvent event, String command) {
        return handlerCommand.handleCommand(event, command, dataStorage);
    }

    /**
     * PircBotx will return the exact message sent and not the raw line
     */
    @Override
    public void onGenericMessage(GenericMessageEvent event) throws Exception {
        String message = event.getMessage();
        String command = getCommandFromMessage(message);

        String response = runCommands(event, command);
        if (response != null) sendMessage(response);
    }

    /**
     * The command will always be the first part of the message
     * We can split the string into parts by spaces to get each word
     * The first word if it starts with our command notifier "!" will get returned
     * Otherwise it will return null
     */
    private String getCommandFromMessage(String message) {
        String[] msgParts = message.split(" ");
        if (msgParts.length != 0 && msgParts[0].startsWith("!")) {
            return msgParts[0];
        } else {
            return null;
        }
    }

    /**
     * We MUST respond to this or else we will get kicked
     */
    @Override
    public void onPing(PingEvent event) throws Exception {
        Main.bot.sendRaw().rawLineNow(String.format("PONG %s\r\n", event.getPingValue()));
    }

    private void sendMessage(String message) {
        Main.bot.sendIRC().message("#" + Main.CHANNEL, message);
    }
}