package com.bin.steamapi;

import com.lukaspradel.steamapi.data.json.ownedgames.Game;

import java.util.List;

public class SteamApiDataStorage {
    private List<Game> listAllGames;
    private List<Game> listOwnerGames;

    public List<Game> getListAllGames() {
        return listAllGames;
    }

    public void setListAllGames(List<Game> listAllGames) {
        this.listAllGames = listAllGames;
    }

    public List<Game> getGamesListOwner() {
        return listOwnerGames;
    }

    public void setListOwnerGames(List<Game> listOwnerGames) {
        this.listOwnerGames = listOwnerGames;
    }
}
