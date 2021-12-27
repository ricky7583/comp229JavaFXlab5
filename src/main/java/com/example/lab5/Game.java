package com.example.lab5;

public class Game {
    private final String gameID;
    private final String title;
    private final String date;
    private final String score;

    public Game(String gameID, String title, String date, String score) {
        this.gameID = gameID;
        this.title = title;
        this.date = date;
        this.score = score;
    }

    public String getgameID() {
        return gameID;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getScore() {
        return score;
    }
}
