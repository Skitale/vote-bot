package com.bin.bot;

import java.lang.reflect.Field;

public class MessageConst {
    public static final String START_VOTING = "START_VOTING";
    public static final String SORRY_ALREADY_VOTING = "SORRY_ALREADY_VOTING";
    public static final String THANK_YOU = "THANK_YOU";
    public static final String STOP_VOTING = "STOP_VOTING";
    public static final String GAME_NOT_FOUND = "GAME_NOT_FOUND";
    public static final String CLEAR_DATA = "CLEAR_DATA";
    public static final String SORRY_GAME_IN_BLACKLIST = "SORRY_GAME_IN_BLACKLIST";

    public static final String DEFAULT_START_VOTING = "Voting started! Use the command \"!vote <name game>\" to vote. Everyone has the right to one vote. The name of the game should be the same as in the \"Steam\", exceptions are specified by the streamer.";
    public static final String DEFAULT_SORRY_ALREADY_VOTING = "sorry, you already voted.";
    public static final String DEFAULT_THANK_YOU = "thank you for voting!";
    public static final String DEFAULT_STOP_VOTING = "The vote ended.";
    public static final String DEFAULT_GAME_NOT_FOUND = "this game was not found.";
    public static final String DEFAULT_CLEAR_DATA = "clear data voting.";
    public static final String DEFAULT_SORRY_GAME_IN_BLACKLIST = "sorry, this game in blacklist. you can vote again.";

    public static String getDefaultMessage(String key){
        try {
            Class clazz = MessageConst.class;
            Field field = clazz.getField("DEFAULT_" + key);
            return (String) field.get("DEFAULT_" + key);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
