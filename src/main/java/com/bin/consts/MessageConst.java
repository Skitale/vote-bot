package com.bin.consts;

import java.lang.reflect.Field;

public class MessageConst {
    public static final String START_VOTING = "START_VOTING";
    public static final String SORRY_ALREADY_VOTING = "SORRY_ALREADY_VOTING";
    public static final String THANK_YOU = "THANK_YOU";
    public static final String STOP_VOTING = "STOP_VOTING";
    public static final String GAME_NOT_FOUND = "GAME_NOT_FOUND";
    public static final String CLEAR_DATA = "CLEAR_DATA";
    public static final String SORRY_GAME_IN_BLACKLIST = "SORRY_GAME_IN_BLACKLIST";
    public static final String SUB_MOD = "SUB_MOD";
    public static final String SUB_MOD_ON = "SUB_MOD_ON";
    public static final String SUB_MOD_OFF = "SUB_MOD_OFF";
    public static final String CHANGE_MAX_TOP = "CHANGE_MAX_TOP";
    public static final String LIST_OF_TOP_TITLE = "LIST_OF_TOP_TITLE";
    public static final String LIST_OF_TOP_GAME_ITEM = "LIST_OF_TOP_GAME_ITEM";
    public static final String LIST_OF_USERS_TITLE = "LIST_OF_USERS_TITLE";
    public static final String LIST_OF_USERS_USER_ITEM = "LIST_OF_USERS_USER_ITEM";
    public static final String LIST_OF_ALL_GAMES = "LIST_OF_ALL_GAMES";
    public static final String LIST_OF_ALL_GAMES_ITEM = "LIST_OF_ALL_GAMES_ITEM";
    public static final String EMPTY_LIST_GAMES = "EMPTY_LIST_GAMES";
    public static final String EMPTY_LIST_USERS = "EMPTY_LIST_USERS";

    private static final String DEFAULT_START_VOTING = "Voting started! Use the command \"!vote <name game>\" to vote. Everyone has the right to one vote. The name of the game should be the same as in the \"Steam\", exceptions are specified by the streamer.";
    private static final String DEFAULT_SORRY_ALREADY_VOTING = "sorry, you already voted.";
    private static final String DEFAULT_THANK_YOU = "thank you for voting!";
    private static final String DEFAULT_STOP_VOTING = "The vote ended.";
    private static final String DEFAULT_GAME_NOT_FOUND = "this game was not found.";
    private static final String DEFAULT_CLEAR_DATA = "clear data voting.";
    private static final String DEFAULT_SORRY_GAME_IN_BLACKLIST = "sorry, this game in blacklist. you can vote again.";
    private static final String DEFAULT_SUB_MOD = "sub mode is";
    private static final String DEFAULT_SUB_MOD_ON = "on.";
    private static final String DEFAULT_SUB_MOD_OFF = "off.";
    private static final String DEFAULT_CHANGE_MAX_TOP = "Change max top value to {1}";
    private static final String DEFAULT_LIST_OF_TOP_TITLE = "List of {1} top games:";
    private static final String DEFAULT_LIST_OF_TOP_GAME_ITEM = "{1}) name: {2}, points: {3}, availability: {4}; ";
    private static final String DEFAULT_LIST_OF_USERS_TITLE = "User list:";
    private static final String DEFAULT_LIST_OF_USERS_USER_ITEM = "{1}; ";
    private static final String DEFAULT_LIST_OF_ALL_GAMES = "List of participants' games: ";
    private static final String DEFAULT_LIST_OF_ALL_GAMES_ITEM = "{1}) name: {2}, points: {3}, availability: {4}; ";
    private static final String DEFAULT_EMPTY_LIST_GAMES = "Currently there are no games in the list.";
    private static final String DEFAULT_EMPTY_LIST_USERS = "Currently there are no users in the list.";

    public static String getDefaultMessage(String key){
        try {
            Class clazz = MessageConst.class;
            Field field = clazz.getDeclaredField("DEFAULT_" + key);
            return (String) field.get("DEFAULT_" + key);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
