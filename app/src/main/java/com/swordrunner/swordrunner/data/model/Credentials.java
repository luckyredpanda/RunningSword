package com.swordrunner.swordrunner.data.model;

import com.google.gson.annotations.SerializedName;

public class Credentials {

    @SerializedName("email")
    String email = null;

    @SerializedName("password")
    String password = null;


    public Credentials(String email, String password) {
        this.email = email;
        this.password = password;
    }

}