package com.bin.validators;

import com.bin.bot.Bot;
import com.bin.consts.TwitchConst;
import com.bin.consts.CommandConst;
import org.pircbotx.User;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RightsValidator {
    private static Logger logger = LoggerFactory.getLogger(RightsValidator.class);

    private Map<String, List<Character>> rightsMap;
    private String ownerNickName;

    public RightsValidator(Map<String, List<Character>> rightsMap, String ownerNickName){
        this.rightsMap = rightsMap;
        this.ownerNickName = ownerNickName;
    }

    public boolean validationCommandForAccess(GenericMessageEvent e, String command){
        if (!checkEqualCommandInEvent(e, command)) return false;
        command = command.replaceAll("!","");
        User currentUser = e.getUser();
        List<Character> listRights = Collections.emptyList();
        if(rightsMap.get(command) != null) {
            listRights = rightsMap.get(command);
        }
        if(listRights.contains('A')) return true;
        if(currentUser.getNick().equals(ownerNickName)){
            for(Character c : listRights){
                if(c.equals('O')){
                    return true;
                }
            }
        }
        List<String> listBadges = getBadgesForUser(e);
        logger.debug("list of badges for user {} : {}", e.getUser().getNick(), listBadges);
        for(String badge : listBadges){
            badge = badge.toLowerCase();
            Character codeBadge = TwitchConst.mapOfBadge.get(badge);
            for(Character r : listRights){
                if(r.equals(codeBadge)){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean validationRightForVote(GenericMessageEvent e, Boolean onlySubMode) {
        if (!checkEqualCommandInEvent(e, CommandConst.VOT)) return false;
        if (onlySubMode) {
            List<String> listBadges = getBadgesForUser(e);
            logger.debug("list of badges for user {} : {}", e.getUser().getNick(), listBadges);
            return listBadges.contains(TwitchConst.BADGE_SUB);
        } else {
            return true;
        }
    }

    private List<String> getBadgesForUser(GenericMessageEvent event){
        if(event instanceof MessageEvent){
            MessageEvent msgEvent = (MessageEvent) event;
            String badges = msgEvent.getV3Tags().get(TwitchConst.BADGES);
            return findAndGetBadge(badges);
        }
        return Collections.emptyList();
    }

    private List<String> findAndGetBadge(String badges){
        String [] badgesArray = badges.split("(/\\d+)|,"); // example string "subscriber/6,bits/25000"
        List<String>  result = new ArrayList<>();
        for(String item : badgesArray){
            if(!item.equals("")){
                result.add(item);
            }
        }
        return result;
    }

    private boolean checkEqualCommandInEvent(GenericMessageEvent e, String command) {
        String eventCommand = Bot.getCommandFromMessage(e.getMessage());
        return command.equalsIgnoreCase(eventCommand);
    }
}
