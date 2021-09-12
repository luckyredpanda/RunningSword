package com.swordrunner.swordrunner.api;

import android.content.Context;
import android.content.SharedPreferences;

import com.swordrunner.swordrunner.Utils.WebAddress;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;

public class Client {

    public static Retrofit get(Context ctx) {
        return get(ctx, true);
    }

    public static Retrofit get(Context ctx, boolean auth) {

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor);


        if (auth) {

            // get token
            SharedPreferences pref = ctx.getSharedPreferences("swordrunnerpref", MODE_PRIVATE);
            String token = pref.getString("access_token", "");

            // add header
            Interceptor requestInterceptor = chain -> {
                Request newRequest  = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer " + token)
                        .build();
                return chain.proceed(newRequest);
            };

            builder.addInterceptor(requestInterceptor);
        }

        OkHttpClient client = builder.build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(WebAddress.RETROFIT_ADDRESS)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit;
    }

}
