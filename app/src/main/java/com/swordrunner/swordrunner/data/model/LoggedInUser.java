package com.swordrunner.swordrunner.data.model;

import com.google.gson.annotations.SerializedName;

public class LoggedInUser {

    @SerializedName("id")
    public String id = null;

    @SerializedName("access_token")
    public String accessToken = null;

    @SerializedName("refresh_token")
    public String refreshToken = null;

}