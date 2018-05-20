package com.bin.entity;

import com.lukaspradel.steamapi.data.json.ownedgames.Game;

public class GamePoint implements Comparable<GamePoint>{
    private Game game;
    private Integer points;

    public GamePoint(Game game, Integer points) {
        this.game = game;
        this.points = points;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    @Override
    public int compareTo(GamePoint o) {
        return Integer.compare(this.points, o.points);
    }
}
