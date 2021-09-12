package com.swordrunner.swordrunner.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Friend implements Serializable {
    @SerializedName("friendId")
    public String friendId = null;

    @SerializedName("userId")
    public String userId = null;

    @SerializedName("friendEmail")
    public String friendEmail = null;

    @SerializedName("userEmail")
    public String userEmail = null;

    @SerializedName("friendName")
    public String friendName = null;

    @SerializedName("friendAvatar")
    public String friendAvatar = null;

    @SerializedName("userName")
    public String userName = null;

    @SerializedName("userAvatar")
    public String userAvatar = null;

    @SerializedName("lastSentence")
    public String lastSentence;

    public int state = -1;

    public int days = 0;
    public int points = 0;

    public boolean highlight = false;

    public Friend(String friendId, String userId, String friendEmail, String userEmail, String friendName, String friendAvatar, String userName, String userAvatar, String lastSentence) {
        this.friendId = friendId;
        this.userId = userId;
        this.friendEmail = friendEmail;
        this.userEmail = userEmail;
        this.friendName = friendName;
        this.friendAvatar = friendAvatar;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.lastSentence = lastSentence;
    }

    public Friend(String name, int days, int points, String avatar) {
        this.points = points;
        this.days = days;
        this.friendName = name;
        this.friendAvatar = avatar;
        this.highlight = true;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFriendEmail() {
        return friendEmail;
    }

    public void setFriendEmail(String friendEmail) {
        this.friendEmail = friendEmail;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getFriendAvatar() {
        return friendAvatar;
    }

    public void setFriendAvatar(String friendAvatar) {
        this.friendAvatar = friendAvatar;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getLastSentence() {
        return lastSentence;
    }

    public void setLastSentence(String lastSentence) {
        this.lastSentence = lastSentence;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
