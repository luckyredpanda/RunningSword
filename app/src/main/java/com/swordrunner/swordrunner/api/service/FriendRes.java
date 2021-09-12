package com.swordrunner.swordrunner.api.service;

import com.swordrunner.swordrunner.data.model.Credentials;
import com.swordrunner.swordrunner.data.model.Friend;
import com.swordrunner.swordrunner.data.model.LoggedInUser;
import com.swordrunner.swordrunner.data.model.User;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FriendRes {

    @POST("friend/add")
    Call<Friend> add(@Body Friend friend);

    @GET("friend/{id}")
    Call<Friend> getall(@Path("userId") String userId);

    @POST("friend/searchWithId")
    Call<Friend> searchOneWithId(@Body Friend friend);

    @POST("friend/allfriends")
    Call<ArrayList<Friend>> showAllFriends(@Body Friend friend);

    @POST("friend/allUserFriends")
    Call<ArrayList<Friend>> showUserAsFriend(@Body Friend friend);

    @POST("friend/updateSentence")
    Call<Friend> updateSentence(@Body Friend friend);

    @POST("friend/updateFriendName")
    Call<Friend> updateFriendName(@Body Friend friend);

    @POST("friend/updateFriendEmail")
    Call<Friend> updateFriendEmail(@Body Friend friend);

    @POST("friend/updateFriendAvatar")
    Call<Friend> updateFriendAvatar(@Body Friend friend);

    @POST("friend/updateUserEmail")
    Call<Friend> updateUserName(@Body Friend friend);

    @POST("friend/updateUserEmail")
    Call<Friend> updateUserEmail(@Body Friend friend);

    @POST("friend/updateUserAvatar")
    Call<Friend> updateUserAvatar(@Body Friend friend);

    @POST("friend/delete")
    Call<Friend> deleteFriend(@Body Friend friend);

}
