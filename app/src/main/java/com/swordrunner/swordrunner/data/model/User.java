package com.swordrunner.swordrunner.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.LinkedList;

public class User implements Serializable {

    @SerializedName("_id")
    public String id = null;

    @SerializedName("email")
    String email = null;

    @SerializedName("name")
    public String name = null;

    @SerializedName("avatar")
    public String avatarUrl = null;

    @SerializedName("comments")
    private LinkedList<Comment> comments = new LinkedList<>();

    @SerializedName("games")
    private LinkedList<UserGame> games = new LinkedList<>();

    @SerializedName("survivalDays")
    private int survivalDays;



    public User(String id, String email, String name, String avatarUrl) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    public User() {
    }

    public User(String id, String email, String name, String avatarUrl, LinkedList<Comment> comments) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.comments = comments;
    }
    //Used for exacting commenter list from user.comments
    public User(String id, String name, String avatarUrl) {
        this.id = id;
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    public int getSurvivalDays() {
        return survivalDays;
    }

    public void setSurvivalDays(int survivalDays) {
        this.survivalDays = survivalDays;
    }

    public LinkedList<UserGame> getGames() {
        return games;
    }

    public void setGames(LinkedList<UserGame> games) {
        this.games = games;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public LinkedList<Comment> getComments() {
        return comments;
    }

    public void setComments(LinkedList<Comment> comments) {
        this.comments = comments;
    }

    public String commentstoString(){
        String commentstostring=null;
        for (int i = 0; i < comments.size(); i++) {
            commentstostring+=comments.get(i).toString();
        }
        return commentstostring;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", comments={" + commentstoString() +
                "}}";
    }


    /**
     * To prevent a user with large data e.g.comments stored in Sharepreference
     * @param user
     * @return
     */
    public User onlySaveNecessaryUserAttribute(User user){
        User userwithNecessaryAttriute=new User(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getAvatarUrl()
        );
        return userwithNecessaryAttriute;
    }

}