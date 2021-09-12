package com.swordrunner.swordrunner.api.service;

import com.swordrunner.swordrunner.data.model.ChangeUserName;
import com.swordrunner.swordrunner.data.model.Credentials;
import com.swordrunner.swordrunner.data.model.LoggedInUser;
import com.swordrunner.swordrunner.data.model.User;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import com.swordrunner.swordrunner.data.model.Home;

import retrofit2.http.Path;

public interface UserRes {

    @POST("users/login")
    Call<LoggedInUser> login(@Body Credentials credentials);

    @POST("users/register")
    Call<User> register(@Body Credentials credentials);

    @GET("users/home")
    Call<Home> getHome();

    @GET("users/searchbyid/{id}")
    Call<User> get(@Path("id") String id);

    @POST("users/searchByEmail")
    Call<User> searchFriendByEmail(@Body User user);

    @POST("users/changename")
    Call<User> changeUser(@Body ChangeUserName changeUserName);

    @POST("users/searchByName")
    Call<User> searchFriendByName(@Body User user);

    @POST("users/change")
    Call<User> change(@Body Credentials credentials);

    @POST("users/changeEM")
    Call<User> changeEM(@Body User user);

    @POST("users/searchUserbyID")
    Call<User> getUserByID(@Body User user);

    @POST("users/searchbyemail")
    Call<User> getFriendByEmail(@Body User user);

    @Multipart
    @POST("users/upload")
    Call<String> uploadavatar(@Part MultipartBody.Part requestImag);

    @POST("users/updateUrl")
    Call<User> updateUrl(@Body User user);

    @POST("users/writecomment")
    Call<User> writecomment(@Body User user);

    @POST("users/paySafehouse")
    Call<Void> paySafehouse();

    @POST("users/createGame")
    Call<User> createGame(@Body User user);

    @DELETE("users/deleteAccount")
    Call<Void> wipe();
}