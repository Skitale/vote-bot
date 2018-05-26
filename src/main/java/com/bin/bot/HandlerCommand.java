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

import java.util.*;

public class HandlerCommand {

    private SerializationHelper serHelper;
    private List<String> mods;
    private Set<String> participantsList;
    private List<GamePoint> participantsGamesList;
    private List<String> excludeList;
    private List<String> includeList;
    private String ownerNickName;
    private ResourceMessages resourceMessages;
    private boolean isStart = false;
    private boolean onlySubMode = true;

    public HandlerCommand(String owner, List<String> mods, List<String> excludeList, List<String> includeList, Map<String, String> messages) {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                serHelper.serialize(participantsList, participantsGamesList);
            }
        });
        this.mods = mods;
        this.ownerNickName = owner;
        this.serHelper = new SerializationHelper();
        this.serHelper.deserialize();
        this.resourceMessages = new ResourceMessages(messages);
        this.excludeList = excludeList;
        this.includeList = includeList;
        participantsList = serHelper.getUserSet();
        participantsGamesList = serHelper.getGameList();
    }

    public String handleCommand(GenericMessageEvent event, String command, SteamApiDataStorage dataStorage) {
        boolean isSub = checkSubForUser(event);
        if (command == null) return null;
        String msg = event.getMessage();
        User currentUser = event.getUser();

        if (command.equalsIgnoreCase("!startvoting") && isMod(currentUser)) {
            return startVotingCommand();
        } else if (command.equalsIgnoreCase("!subgames") && isMod(currentUser)) {
            return subGamesCommand(dataStorage);
        } else if (command.equalsIgnoreCase("!clearvoting") && isMod(currentUser)) {
            return clearVotingCommand();
        } else if (command.equalsIgnoreCase("!submod") && isMod(currentUser)) {
            return subModCommand();
        }

        if (!isStart) return null;

        if (command.startsWith("!vote") && validationRightForVote(isSub)) {
            return voteCommand(msg, command, currentUser, dataStorage);
        } else if (command.equalsIgnoreCase("!getusers") && isMod(currentUser)) {
            return getUsersCommand();
        } else if (command.equalsIgnoreCase("!getgames") && isMod(currentUser)) {
            return getGamesCommand();
        } else if (command.equalsIgnoreCase("!stopvoting") && isMod(currentUser)) {
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
        if (isExistInParticipantsList(currentUser) && !isMod(currentUser)) {
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
        sb.append("List of 10 top games: ");
        Collections.sort(participantsGamesList, Collections.reverseOrder());
        int maxIndex = participantsGamesList.size() < 10 ? participantsGamesList.size() : 10;
        boolean isExistInOwnerListGame = false;
        for (int i = 0; i < maxIndex; i++) {
            GamePoint currentGame = participantsGamesList.get(i);
            for (Game g : gamesOwner) {
                if (g.getAppid().equals(currentGame.getAppId())) {
                    isExistInOwnerListGame = true;
                    break;
                }
            }
            String nameGame = currentGame.getName();
            String point = currentGame.getPoints().toString();
            String presence = isExistInOwnerListGame ? "yes" : "no";
            sb.append(i + 1).append(") ").append("name: ").append(nameGame).append(", points: ").append(point).append(", availability: ").append(presence).append("; ");
        }
        return sb.toString();
    }

    private String getUsersCommand() {
        StringBuilder sb = new StringBuilder();
        sb.append("User list: ");
        for (String userName : participantsList) {
            sb.append(userName);
            sb.append(";");
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

    private boolean checkSubForUser(GenericMessageEvent event) {
        if (event instanceof MessageEvent) {
            MessageEvent msgEvent = (MessageEvent) event;
            String badgesRow = msgEvent.getV3Tags().get(TwitchConst.BADGES);
            return findSubBadge(badgesRow);
        }
        return false;
    }

    private boolean findSubBadge(String rowBadge) {
        String[] parts = rowBadge.split(",|/");
        for (String part : parts) {
            if (part.equals(TwitchConst.SUB)) {
                return true;
            }
        }
        return false;
    }

    private boolean validationRightForVote(boolean isSub) {
        if (onlySubMode) {
            return isSub;
        } else {
            return true;
        }
    }

    private void attachChanges(){
        serHelper.serialize(participantsList, participantsGamesList);
    }

    private boolean isMod(User user) {
        return mods.contains(user.getNick());
    }

    private boolean isOwner(User user) {
        return user.getNick().equals(ownerNickName);
    }
}
