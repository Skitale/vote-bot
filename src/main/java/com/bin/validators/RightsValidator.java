package com.bin.validators;

import com.bin.consts.TwitchConst;
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
        List<String> listBadges = getRightForUser(e);
        logger.debug("list of badges for user {} : {}", e.getUser().getNick(), listBadges);
        for(String badge : listBadges){
            badge = badge.toUpperCase();
            Character firstSymbol = badge.toCharArray()[0];
            for(Character r : listRights){
                if(r.equals(firstSymbol)){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean validationRightForVote(GenericMessageEvent e, Boolean onlySubMode) {
        List<String> listBadges = getRightForUser(e);
        logger.debug("list of badges for user {} : {}", e.getUser().getNick(), listBadges);
        if (onlySubMode) {
            return listBadges.contains(TwitchConst.BADGE_SUB);
        } else {
            return true;
        }
    }

    private List<String> getRightForUser(GenericMessageEvent event){
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
}
