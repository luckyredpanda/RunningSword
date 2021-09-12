package com.swordrunner.swordrunner.data.model;

import com.google.gson.annotations.SerializedName;

public class Home {

    public int days = 0;
    public int coins = 0;

    public double distance = 0.0;
    public double targetDistance = 10000000.0;

    public String period = "00:00";

    public boolean safehouse = false;

    public String userAvatar = "";

    public int points = 0;

    public String name = "";

    @SerializedName("distance_today")
    public double distanceToday = 0.0;

}