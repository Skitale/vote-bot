package com.bin.bot;

import com.bin.consts.MessageConst;
import com.bin.consts.ResourceMessages;
import com.bin.consts.TwitchConst;
import com.bin.entity.GamePoint;
import com.bin.parser.serialization.SerializationHelper;
import com.bin.steamapi.SteamApiDataStorage;
import com.lukaspradel.steamapi.data.json.ownedgames.Game;
import org.pircbotx.User;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HandlerCommand {
    private static Logger logger = LoggerFactory.getLogger(HandlerCommand.class);

    private SerializationHelper serHelper;
    private Set<String> participantsList;
    private List<GamePoint> participantsGamesList;
    private List<String> excludeList;
    private List<String> includeList;
    private String ownerNickName;
    private ResourceMessages resourceMessages;
    private int maxGamesTop;
    private boolean isStart = false;
    private boolean onlySubMode = true;

    public HandlerCommand(String owner, List<String> excludeList, List<String> includeList, Map<String, String> messages) {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                serHelper.serialize(participantsList, participantsGamesList);
                serHelper.serializeSettings(onlySubMode, maxGamesTop);
            }
        });
        this.ownerNickName = owner;
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
    }

    public String handleCommand(GenericMessageEvent event, String command, SteamApiDataStorage dataStorage) {
        if (command == null) return null;
        List<String> rightsForCurrentUser = getRightForUser(event);
        logger.debug("list of badges for user {} : {}", event.getUser().getNick(), rightsForCurrentUser);
        String msg = event.getMessage();
        User currentUser = event.getUser();

        if (command.equalsIgnoreCase("!startvoting") && isOwner(currentUser)) {
            return startVotingCommand();
        } else if (command.equalsIgnoreCase("!subgames") && isMod(currentUser, rightsForCurrentUser)) {
            return subGamesCommand(dataStorage);
        } else if (command.equalsIgnoreCase("!clearvoting") && isOwner(currentUser)) {
            return clearVotingCommand();
        } else if (command.equalsIgnoreCase("!submod") && isOwner(currentUser)) {
            return subModCommand();
        } else if (command.equalsIgnoreCase("!maxtop") && isOwner(currentUser)){
            return maxTopCommand(msg, command);
        }

        if (!isStart) return null;

        if (command.startsWith("!vote") && validationRightForVote(rightsForCurrentUser)) {
            return voteCommand(msg, command, currentUser, dataStorage);
        } else if (command.equalsIgnoreCase("!getusers") && isOwner(currentUser)) {
            return getUsersCommand();
        } else if (command.equalsIgnoreCase("!getgames") && isOwner(currentUser)) {
            return getGamesCommand();
        } else if (command.equalsIgnoreCase("!stopvoting") && isOwner(currentUser)) {
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
        if (isExistInParticipantsList(currentUser) /*&& !isMod(currentUser, Collections.emptyList())*/) {
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
        boolean isExistInOwnerListGame = false;
        for (int i = 0; i < maxIndex; i++) {
            GamePoint currentGame = participantsGamesList.get(i);
            for (Game g : gamesOwner) {
                if (g.getAppid().equals(currentGame.getAppId())) {
                    isExistInOwnerListGame = true;
                    break;
                }
            }
            String counter = String.valueOf(i + 1);
            String nameGame = currentGame.getName();
            String point = currentGame.getPoints().toString();
            String presence = isExistInOwnerListGame ? "yes" : "no";
            sb.append(resourceMessages.getMessage(MessageConst.LIST_OF_TOP_GAME_ITEM, counter, nameGame, point, presence));
        }
        return sb.toString();
    }

    private String getUsersCommand() {
        StringBuilder sb = new StringBuilder();
        sb.append(resourceMessages.getMessage(MessageConst.LIST_OF_USERS_TITLE));
        for (String userName : participantsList) {
            sb.append(resourceMessages.getMessage(MessageConst.LIST_OF_USERS_USER_ITEM, userName));
        }
        return sb.toString();
    }

    private String getGamesCommand() {
        StringBuilder sb = new StringBuilder();
        sb.append("List of participants' games: ");
        for (GamePoint g : participantsGamesList) {
            String nameGame = g.getName();
            String point = g.getPoints().toString();
            sb.append("name: ").append(nameGame).append(", points: ").append(point).append("; ");
        }
        return sb.toString();
    }

    private String subModCommand() {
        if (onlySubMode) {
            onlySubMode = false;
        } else {
            onlySubMode = true;
        }
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
            maxGamesTop = Integer.valueOf(msgContent);
            attachSettings();
            return resourceMessages.getMessage(MessageConst.CHANGE_MAX_TOP, String.valueOf(maxGamesTop));
        }
        return null;
    }

    private List<String> getRightForUser(GenericMessageEvent event){
        if(event instanceof MessageEvent){
            MessageEvent msgEvent = (MessageEvent) event;
            String badges = msgEvent.getV3Tags().get(TwitchConst.BADGES);
            return findAndGetBadge(badges);
        }
        return Collections.emptyList();
    }

    public static List<String> findAndGetBadge(String badges){
        String [] badgesArray = badges.split("(/\\d+)|,"); // example string "subscriber/6,bits/25000"
        List<String>  result = new ArrayList<>();
        for(String item : badgesArray){
            if(!item.equals("")){
                result.add(item);
            }
        }
        return result;
    }

    private boolean validationRightForVote(List<String> listRight) {
        if (onlySubMode) {
            return listRight.contains(TwitchConst.BADGE_SUB);
        } else {
            return true;
        }
    }

    private void attachChanges(){
        serHelper.serialize(participantsList, participantsGamesList);
    }

    private void attachSettings(){
        serHelper.serializeSettings(onlySubMode, maxGamesTop);
    }


    private boolean isMod(User user, List<String> rights) {
        return rights.contains(TwitchConst.BADGE_MODERATOR) || isOwner(user);
    }

    private boolean isOwner(User user) {
        return user.getNick().equals(ownerNickName);
    }
}
