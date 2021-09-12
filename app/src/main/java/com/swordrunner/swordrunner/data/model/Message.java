package com.swordrunner.swordrunner.data.model;

import com.google.gson.annotations.SerializedName;

public class Message {
    @SerializedName("message")
    public String message = null;

    @SerializedName("senderId")
    public String senderId;

    @SerializedName("totalId")
    public String totalId;

    @SerializedName("name")
    public String senderName;

    @SerializedName("receiverId")
    public String receiverId;

    @SerializedName("time")
    public String createdAt;

    @SerializedName("date")
    public String date;

    public boolean isSend;

    public Message(String message, String senderId, String totalId, String senderName, String receiverId, String createdAt, boolean isSend) {
        this.message = message;
        this.senderId = senderId;
        this.totalId = totalId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.createdAt = createdAt;
        this.isSend = isSend;
    }

    public String getTotalId() {
        return totalId;
    }

    public void setTotalId(String totalId) {
        this.totalId = totalId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isSend() {
        return isSend;
    }

    public void setSend(boolean send) {
        isSend = send;
    }
}
