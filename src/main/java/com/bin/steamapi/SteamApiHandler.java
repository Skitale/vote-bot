package com.bin.steamapi;

import com.github.goive.steamapi.SteamApi;
import com.github.goive.steamapi.exceptions.SteamApiException;
import com.lukaspradel.steamapi.data.json.ownedgames.Game;
import com.lukaspradel.steamapi.data.json.ownedgames.GetOwnedGames;
import com.lukaspradel.steamapi.webapi.client.SteamWebApiClient;
import com.lukaspradel.steamapi.webapi.request.GetOwnedGamesRequest;
import com.lukaspradel.steamapi.webapi.request.builders.SteamWebApiRequestFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SteamApiHandler {
    private SteamApi steamApi;
    private SteamWebApiClient steamWebApi;

    public SteamApiHandler(String steamKey) {
        steamApi = new SteamApi();
        steamWebApi = new SteamWebApiClient.SteamWebApiClientBuilder(steamKey).build();
    }

    public List<Game> getAllGames() {
        List<Game> allGames = new ArrayList<>();
        try {
            Map<Integer, String> list = steamApi.listApps();
            for (Map.Entry<Integer, String> entry : list.entrySet()) {
                Game g = new Game();
                g.setName(entry.getValue());
                g.setAppid(entry.getKey());
                allGames.add(g);
            }
        } catch (SteamApiException e) {
            e.printStackTrace();
        }
        return allGames;
    }

    public List<Game> getOwnerGames(String ownerSteamProfileId) {
        List<Game> games = new ArrayList<>();
        try {
            GetOwnedGamesRequest request = SteamWebApiRequestFactory.createGetOwnedGamesRequest(ownerSteamProfileId);
            GetOwnedGames data = steamWebApi.processRequest(request);
            games = data.getResponse().getGames();
        } catch (com.lukaspradel.steamapi.core.exception.SteamApiException e) {
            e.printStackTrace();
        }
        return games;
    }

    public SteamApiDataStorage getDataStorage(String ownerSteamProfileId) {
        SteamApiDataStorage steamApiDataStorage = new SteamApiDataStorage();
        steamApiDataStorage.setListAllGames(getAllGames());
        steamApiDataStorage.setListOwnerGames(getOwnerGames(ownerSteamProfileId));
        return steamApiDataStorage;
    }
}
