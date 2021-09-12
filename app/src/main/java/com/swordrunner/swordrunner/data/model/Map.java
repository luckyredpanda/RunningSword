package com.swordrunner.swordrunner.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class Map {

    @SerializedName("_id")
    public String id = null;

    @SerializedName("user_id")
    String userId = null;

    @SerializedName("created_at")
    public Date createdAt = null;

    @SerializedName("start_time")
    public Date startTime = null;

    @SerializedName("end_time")
    public Date endTime = null;

    @SerializedName("start_location")
    public TimeGeoPoint startLocation = null;

    @SerializedName("end_location")
    public TimeGeoPoint endLocation = null;

    @SerializedName("route")
    public List<TimeGeoPoint> route = null;

}