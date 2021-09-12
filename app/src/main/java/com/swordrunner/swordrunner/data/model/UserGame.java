package com.swordrunner.swordrunner.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserGame implements Serializable {

    @SerializedName("gameID")
    public String gameId = null;

    @SerializedName("isSingle")
    public boolean isSingle = false;

    public UserGame(String gameId){
        this.gameId = gameId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public boolean isSingle() {
        return isSingle;
    }

    public void setSingle(boolean single) {
        isSingle = single;
    }
}
