package com.bin.entity;

import com.lukaspradel.steamapi.data.json.ownedgames.Game;

import java.io.Serializable;

public class GamePoint implements Comparable<GamePoint>, Serializable{
    private Integer points;
    private Integer appId;
    private String name;

    public GamePoint(Game game, Integer points) {
        this.appId = game.getAppid();
        this.name = game.getName();
        this.points = points;
    }

    public Integer getAppId() {
        return appId;
    }

    public void setAppid(Integer appId) {
        this.appId = appId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public String toString() {
        return "GamePoint{" +
                "points=" + points +
                ", appId=" + appId +
                ", name='" + name + '\'' +
                '}';
    }
}
