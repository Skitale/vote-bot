package com.bin;


import com.bin.bot.Bot;
import com.bin.parser.ConfigConst;
import com.bin.parser.Parser;
import com.bin.parser.ResourceConfig;
import com.bin.steamapi.SteamApiDataStorage;
import com.bin.steamapi.SteamApiHandler;

import org.slf4j.Logger;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.slf4j.LoggerFactory;

public class Main {

	public static Logger logger = LoggerFactory.getLogger(Main.class);
    public static String BOTNAME;
    public static String OAUTH;
    public static String STREAMER_PROFILE_STEAM_ID;
	public static String CHANNEL;

	public static PircBotX bot;

	public static void main(String[] args) throws Exception {
		if(args.length != 0) {
			CHANNEL = args[0].toLowerCase();
		} else {
			logger.error("Channel not specified");
			return;
		}

		Parser parser = new Parser();
		parser.parseFiles();

        ResourceConfig resourceConfig = new ResourceConfig(parser.getConfigurationValues());
        BOTNAME = resourceConfig.getValue(ConfigConst.BOT_NAME).toLowerCase();
        OAUTH = resourceConfig.getValue(ConfigConst.OAUTH);
        STREAMER_PROFILE_STEAM_ID = resourceConfig.getValue(ConfigConst.STREAMER_PROFILE_STEAM_ID);

        logger.info("Loading games from steam...");
        SteamApiHandler steamApiHandler = new SteamApiHandler(resourceConfig.getValue(ConfigConst.STEAM_DEV_KEY));
		SteamApiDataStorage dataStorage = steamApiHandler.getDataStorage(STREAMER_PROFILE_STEAM_ID);
		logger.info("Complete");

		Configuration config = new Configuration.Builder()
				.setName(BOTNAME)
				.addServer("irc.chat.twitch.tv", 6667)
				.setServerPassword(OAUTH)
				.addListener(new Bot(dataStorage, parser.getExcludeList(), parser.getIncludeList(), parser.getMessages()))
				.addAutoJoinChannel("#" + CHANNEL)
				.setAutoReconnectDelay(3000)
				.setAutoReconnectAttempts(3)
				.buildConfiguration();

		bot = new PircBotX(config);
		bot.startBot();
	}
}