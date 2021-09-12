package com.swordrunner.swordrunner.api.service;

import com.swordrunner.swordrunner.data.model.Credentials;
import com.swordrunner.swordrunner.data.model.LoggedInUser;
import com.swordrunner.swordrunner.data.model.Message;
import com.swordrunner.swordrunner.data.model.User;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MessageRes {
    @POST("message/add")
    Call<Message> addMessage(@Body Message message);

    @POST("message/allMessages")
    Call<ArrayList<Message>> getAllMessage(@Body Message message);

    @POST("message/lastMessage")
    Call<Message> getLastMessage(@Body Message message);

    @POST("message/delete")
    Call<Message> deleteMessage(@Body Message message);
}
