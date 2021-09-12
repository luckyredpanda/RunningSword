package com.swordrunner.swordrunner.api.service;

import com.swordrunner.swordrunner.data.model.GenericResponse;
import com.swordrunner.swordrunner.data.model.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MapRes {

    @POST("maps")
    Call<GenericResponse> create(@Body Map map);

}