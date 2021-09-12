package com.swordrunner.swordrunner.api.service;

import com.swordrunner.swordrunner.data.model.Game;
import com.swordrunner.swordrunner.data.model.GenericResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface GameRes {

    @POST("games")
    Call<Game> create(@Body Game game);

    @GET("games")
    Call<List<Game>> get();

    @POST("games/updateParticipant")
    Call<Game> changeParticipant(@Body Game game);

    @POST("games/searchByGameId")
    Call<Game> searchGame(@Body Game game);

}