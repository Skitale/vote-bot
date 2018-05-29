package com.bin.parser.serialization;

import com.bin.entity.GamePoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class SerializationHelper {

    private static Logger logger = LoggerFactory.getLogger(SerializationHelper.class);

    private static final String DATA_PATH = "./data/data.bin";
    private static final String SETTINGS_DATA_PATH = "./data/settingsdata.bin";
    private static final String USER_SET_KEY = "USER_SET";
    private static final String GAME_LIST_KEY = "GAME_LIST";
    private static final String MAX_GAMES_TOP = "MAX_GAMES_TOP";
    private static final String SUB_MOD = "SUB_MOD";

    private Map<String, Collection> dataMap = new HashMap<>();
    private Map<String, Object> rowDataMap = new HashMap<>();

    public void serialize(Set<String> userSet, List<GamePoint> gamesList){
        Path p = Paths.get(DATA_PATH);
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(p))){
            oos.writeObject(userSet);
            oos.writeObject(gamesList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void serializeSettings(boolean subMod, int maxGamesTop){
        Path p = Paths.get(SETTINGS_DATA_PATH);
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(p))){
            oos.writeBoolean(subMod);
            oos.writeInt(maxGamesTop);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<String> getUserSet(){
        Set<String> result = new HashSet<>();
        if(dataMap.get(USER_SET_KEY) != null){
            result = (Set<String>) dataMap.get(USER_SET_KEY);
        }
        return result;
    }

    public List<GamePoint> getGameList(){
        List<GamePoint> result = new ArrayList<>();
        if(dataMap.get(GAME_LIST_KEY) != null){
            result = (List<GamePoint>) dataMap.get(GAME_LIST_KEY);
        }
        return result;
    }

    public int getMaxTopGames(){
        int result = 10;
        if(rowDataMap.get(MAX_GAMES_TOP) != null){
            result = (Integer) rowDataMap.get(MAX_GAMES_TOP);
        }
        return result;
    }

    public boolean getCurrentSubMod(){
        boolean result = true;
        if(rowDataMap.get(SUB_MOD) != null){
            result = (Boolean) rowDataMap.get(SUB_MOD);
        }
        return result;
    }

    public void deserialize(){
        Path p = Paths.get(DATA_PATH);
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(p))){
            Set<String> resSet = (Set<String>)ois.readObject();
            List<GamePoint> resList = (List<GamePoint>)ois.readObject();
            dataMap.put(USER_SET_KEY, resSet);
            dataMap.put(GAME_LIST_KEY, resList);
        } catch (IOException e) {
            logger.info("File {} is empty", DATA_PATH);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void deserializeSettings(){
        Path p = Paths.get(SETTINGS_DATA_PATH);
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(p))){
            boolean submod = ois.readBoolean();
            int maxGamesTop = ois.readInt();
            rowDataMap.put(SUB_MOD, submod);
            rowDataMap.put(MAX_GAMES_TOP, maxGamesTop);
        } catch (IOException e) {
            logger.info("File {} is empty", SETTINGS_DATA_PATH);
        }
    }
}
