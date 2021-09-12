package com.swordrunner.swordrunner.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Game {

    @SerializedName("_id")
    public String id = null;

    @SerializedName("user_id")
    String userId = null;

    public List<String> participants = new ArrayList<>();

    public String name = "New game";

    public double distance = 1.0;

    public Progress progress = new Progress();


    public class Progress {
        public int days = 0;
        public double distance = 0.0;
        public Date lastDay = null;
    }
    public Game(){

    }
    public Game(String id, String userId, List<String> participants, String name, double distance, Progress progress) {
        this.id = id;
        this.userId = userId;
        this.participants = participants;
        this.name = name;
        this.distance = distance;
        this.progress = progress;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}