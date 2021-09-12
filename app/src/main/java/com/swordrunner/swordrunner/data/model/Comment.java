package com.swordrunner.swordrunner.data.model;

import android.text.format.DateUtils;

import com.google.gson.annotations.SerializedName;
import com.swordrunner.swordrunner.Utils.DateUtilss;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;


public class Comment implements Serializable {

    //@SerializedName("owner")
    private String owner=null; // user who was commented

    @SerializedName("ownerID")
    private String ownerID;

    @SerializedName("commenterName")
    private String commenter=null;

    @SerializedName("commenterID")
    private String commenterID;

    @SerializedName("content")
    private String commentContent =null;

    @SerializedName("rate")
    private Float rate=null; //from 1-5. 5 is the highest score.

    @SerializedName("date")
    public String date;

    @SerializedName("commenterAvatar") // Temporay parament for test. It is relative to the parament with the same name in User.
    public
    String url=null;

    public Comment(String owner, String commenter, String commentContent, Float rate, Date date, String url) {
        this.date = String.valueOf(date.getTime());
        this.owner = owner;
        this.commenter = commenter;
        this.commentContent = commentContent;
        this.rate = rate;
        this.url = url;
    }

    public Comment(String ownerID, String commenter, String commenterID, String commentContent, Float rate, Date date, String url) {
        this.date = String.valueOf(date.getTime());
        this.ownerID = ownerID;
        this.commenter = commenter;
        this.commenterID = commenterID;
        this.commentContent = commentContent;
        this.rate = rate;
        this.url = url;
    }

    public Date getDate() {
        DateUtilss dateUtilss=new DateUtilss();
        return dateUtilss.timeStampToDate(date);
    }

    public void setDate(Date date) {
        DateUtilss dateUtilss=new DateUtilss();
        this.date = String.valueOf(date.getTime());
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getCommenter() {
        return commenter;
    }

    public void setCommenter(String commenter) {
        this.commenter = commenter;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public Float getRate() {
        return rate;
    }

    public void setRate(Float rate) {
        this.rate = rate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public String getCommenterID() {
        return commenterID;
    }

    public void setCommenterID(String commenterID) {
        this.commenterID = commenterID;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "owner='" + owner + '\'' +
                ", ownerID='" + ownerID + '\'' +
                ", commenter='" + commenter + '\'' +
                ", commenterID='" + commenterID + '\'' +
                ", commentContent='" + commentContent + '\'' +
                ", date=" + date +
                '}';
    }

}
