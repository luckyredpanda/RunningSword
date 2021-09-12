package com.swordrunner.swordrunner.data.model;

import com.google.gson.annotations.SerializedName;

public class ChangeUserName {

    @SerializedName("id")
    public String id = null;

    @SerializedName("name")
    public String name = null;

    public ChangeUserName(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
