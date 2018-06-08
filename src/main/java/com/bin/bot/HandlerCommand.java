package com.bin.bot;

import com.bin.consts.CommandConst;
import com.bin.consts.MessageConst;
import com.bin.consts.ResourceMessages;
import com.bin.entity.GamePoint;
import com.bin.parser.serialization.SerializationHelper;
import com.bin.steamapi.SteamApiDataStorage;
import com.bin.validators.RightsValidator;
import com.lukaspradel.steamapi.data.json.ownedgames.Game;
import org.pircbotx.User;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HandlerCommand {
    private static Logger logger = LoggerFactory.getLogger(HandlerCommand.class);

    private SerializationHelper serHelper;
    private RightsValidator validatorRights;
    private Set<String> participantsList;
    private List<GamePoint> participantsGamesList;
    private List<String> excludeList;
    private List<String> includeList;
    private String ownerNickName;
    private ResourceMessages resourceMessages;
    private int maxGamesTop;
    private boolean isStart = false;
    private boolean onlySubMode = true;

    public HandlerCommand(String owner, Map<String, List<Character>> rightsMap, List<String> excludeList, List<String> includeList, Map<String, String> messages) {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                serHelper.serialize(participantsList, participantsGamesList);
                serHelper.serializeSettings(onlySubMode, maxGamesTop);
            }
        });
        this.ownerNickName = owner;
        this.validatorRights = new RightsValidator(rightsMap, ownerNickName);
        this.serHelper = new SerializationHelper();
        this.serHelper.deserialize();
        this.serHelper.deserializeSettings();
        this.resourceMessages = new ResourceMessages(messages);
        this.excludeList = excludeList;
        this.includeList = includeList;
        onlySubMode = serHelper.getCurrentSubMod();
        maxGamesTop = serHelper.getMaxTopGames();
        participantsList = serHelper.getUserSet();
        participantsGamesList = serHelper.getGameList();
        logger.info("Sub mod = {}", onlySubMode);
        logger.info("Max top value = {}", maxGamesTop);
    }

    public String handleCommand(GenericMessageEvent event, String command, SteamApiDataStorage dataStorage) {
        if (command == null) return null;
        String msg = event.getMessage();
        User currentUser = event.getUser();

        if (validatorRights.validationCommandForAccess(event, CommandConst.START_VOT)) {
            return startVotingCommand();
        } else if (validatorRights.validationCommandForAccess(event, CommandConst.SUB_GAMES)) {
            return subGamesCommand(dataStorage);
        } else if (validatorRights.validationCommandForAccess(event, CommandConst.CLEAR_VOT)) {
            return clearVotingCommand();
        } else if (validatorRights.validationCommandForAccess(event, CommandConst.SUB_MOD)) {
            return subModCommand();
        } else if (validatorRights.validationCommandForAccess(event, CommandConst.MAX_TOP)) {
            return maxTopCommand(msg, command);
        }

        if (!isStart) return null;

        if (validatorRights.validationRightForVote(event, onlySubMode)) {
            return voteCommand(msg, command, currentUser, dataStorage);
        } else if (validatorRights.validationCommandForAccess(event, CommandConst.GET_USERS)) {
            return getUsersCommand();
        } else if (validatorRights.validationCommandForAccess(event, CommandConst.GET_GAMES)) {
            return getGamesCommand(dataStorage);
        } else if (validatorRights.validationCommandForAccess(event, CommandConst.STOP_VOT)) {
            return stopVotingCommand();
        }
        return null;
    }

    private boolean isExistInParticipantsList(User user) {
        return participantsList.contains(user.getNick());
    }

    private String addGame(Game game, User currentUser) {
        for (String nameGame : excludeList) {
            if (game.getName().toLowerCase().equals(nameGame.toLowerCase())) {
                return currentUser.getNick() + resourceMessages.getMessage(MessageConst.SORRY_GAME_IN_BLACKLIST);
            }
        }

        participantsList.add(currentUser.getNick());
        for (GamePoint g : participantsGamesList) {
            if (g.getAppId().equals(game.getAppid())) {
                g.setPoints(g.getPoints() + 1);
                attachChanges();
                return currentUser.getNick() + resourceMessages.getMessage(MessageConst.THANK_YOU);
            }
        }

        participantsGamesList.add(new GamePoint(game, 1));
        attachChanges();
        return currentUser.getNick() + resourceMessages.getMessage(MessageConst.THANK_YOU);
    }

    private String tryAddGameWithoutSteam(String nameGame, User currentUser) {
        for (String nameG : includeList) {
            if (nameG.toLowerCase().equals(nameGame.toLowerCase())) {
                participantsList.add(currentUser.getNick());
                for (GamePoint gp : participantsGamesList) {
                    if (gp.getName().toLowerCase().equals(nameGame.toLowerCase())) {
                        gp.setPoints(gp.getPoints() + 1);
                        attachChanges();
                        return currentUser.getNick() + resourceMessages.getMessage(MessageConst.THANK_YOU);
                    }
                }
                Game game = new Game();
                game.setName(nameG);
                participantsGamesList.add(new GamePoint(game, 1));
                attachChanges();
                return currentUser.getNick() + resourceMessages.getMessage(MessageConst.THANK_YOU);
            }
        }
        return currentUser.getNick() + resourceMessages.getMessage(MessageConst.GAME_NOT_FOUND);
    }

    private String startVotingCommand() {
        if (isStart) return null;
        isStart = true;
        return resourceMessages.getMessage(MessageConst.START_VOTING);
    }

    private String stopVotingCommand() {
        if (!isStart) return null;
        isStart = false;
        return resourceMessages.getMessage(MessageConst.STOP_VOTING);
    }

    private String clearVotingCommand() {
        participantsGamesList.clear();
        participantsList.clear();
        attachChanges();
        return resourceMessages.getMessage(MessageConst.CLEAR_DATA);
    }

    private String voteCommand(String msg, String command, User currentUser, SteamApiDataStorage dataStorage) {
        if (msg.equals(command)) {
            return null;
        }
        if (isExistInParticipantsList(currentUser)) {
            return currentUser.getNick() + resourceMessages.getMessage(MessageConst.SORRY_ALREADY_VOTING);
        }

        String msgGame = msg.substring(command.length() + 1, msg.length());
        List<Game> gameList = dataStorage.getListAllGames();
        for (Game g : gameList) {
            if (g.getName().toLowerCase().equals(msgGame.toLowerCase())) {
                return addGame(g, currentUser);
            }
        }
        return tryAddGameWithoutSteam(msgGame, currentUser);
    }

    private String subGamesCommand(SteamApiDataStorage dataStorage) {
        List<Game> gamesOwner = dataStorage.getGamesListOwner();
        StringBuilder sb = new StringBuilder();
        String maxNumTop = String.valueOf(maxGamesTop);
        sb.append(resourceMessages.getMessage(MessageConst.LIST_OF_TOP_TITLE, maxNumTop));
        Collections.sort(participantsGamesList, Collections.reverseOrder());
        int maxIndex = participantsGamesList.size() < maxGamesTop ? participantsGamesList.size() : maxGamesTop;
        for (int i = 0; i < maxIndex; i++) {
            GamePoint currentGame = participantsGamesList.get(i);
            boolean isExistInOwnerListGame = existInOwnerListGame(currentGame, gamesOwner);

            String counter = String.valueOf(i + 1);
            String nameGame = currentGame.getName();
            String point = currentGame.getPoints().toString();
            String presence = isExistInOwnerListGame ? "yes" : "no";
            sb.append(resourceMessages.getMessage(MessageConst.LIST_OF_TOP_GAME_ITEM, counter, nameGame, point, presence));
        }
        if(maxIndex != 0) {
            return sb.toString();
        } else {
            return resourceMessages.getMessage(MessageConst.EMPTY_LIST_GAMES);
        }
    }

    private boolean existInOwnerListGame(GamePoint currentGame, List<Game> gameList){
        for (Game g : gameList) {
            if (g.getAppid().equals(currentGame.getAppId())) {
                return true;
            }
        }
        return false;
    }

    private String getUsersCommand() {
        StringBuilder sb = new StringBuilder();
        sb.append(resourceMessages.getMessage(MessageConst.LIST_OF_USERS_TITLE));
        for (String userName : participantsList) {
            sb.append(resourceMessages.getMessage(MessageConst.LIST_OF_USERS_USER_ITEM, userName));
        }
        if(participantsList.size() != 0) {
            return sb.toString();
        } else {
            return resourceMessages.getMessage(MessageConst.EMPTY_LIST_USERS);
        }
    }

    private String getGamesCommand(SteamApiDataStorage dataStorage) {
        List<Game> gamesOwner = dataStorage.getGamesListOwner();
        StringBuilder sb = new StringBuilder();
        sb.append(resourceMessages.getMessage(MessageConst.LIST_OF_ALL_GAMES));
        for(int i = 0; i < participantsGamesList.size(); i++){
            GamePoint currentGame = participantsGamesList.get(i);
            boolean isExistInOwnerListGame = existInOwnerListGame(currentGame, gamesOwner);

            String counter = String.valueOf(i + 1);
            String nameGame = currentGame.getName();
            String points = currentGame.getPoints().toString();
            String presence = isExistInOwnerListGame ? "yes" : "no";
            sb.append(resourceMessages.getMessage(MessageConst.LIST_OF_ALL_GAMES_ITEM, counter, nameGame, points, presence));
        }
        if(participantsGamesList.size() != 0) {
            return sb.toString();
        } else {
            return resourceMessages.getMessage(MessageConst.EMPTY_LIST_GAMES);
        }
    }

    private String subModCommand() {
        if (onlySubMode) {
            onlySubMode = false;
        } else {
            onlySubMode = true;
        }
        attachSettings();
        return resourceMessages.getMessage(MessageConst.SUB_MOD)
                + (onlySubMode ? resourceMessages.getMessage(MessageConst.SUB_MOD_ON)
                : resourceMessages.getMessage(MessageConst.SUB_MOD_OFF));
    }

    private String maxTopCommand(String msg, String command){
        if (msg.equals(command)) {
            return null;
        }
        String msgContent = msg.substring(command.length() + 1, msg.length());
        Pattern pattern = Pattern.compile("^(\\d)+$");
        Matcher matcher = pattern.matcher(msgContent);
        if(matcher.find()){
            if(Integer.valueOf(msgContent) == 0) return null;
            maxGamesTop = Integer.valueOf(msgContent);
            attachSettings();
            return resourceMessages.getMessage(MessageConst.CHANGE_MAX_TOP, String.valueOf(maxGamesTop));
        }
        return null;
    }

    private void attachChanges(){
        serHelper.serialize(participantsList, participantsGamesList);
    }

    private void attachSettings(){
        serHelper.serializeSettings(onlySubMode, maxGamesTop);
    }
}
