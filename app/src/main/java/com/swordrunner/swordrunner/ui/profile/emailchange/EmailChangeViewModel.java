package com.swordrunner.swordrunner.ui.profile.emailchange;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.api.Client;
import com.swordrunner.swordrunner.api.service.UserRes;
import com.swordrunner.swordrunner.data.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmailChangeViewModel extends ViewModel {

    private Context context;
    private User user;
    private MutableLiveData<String> email;


    public void setContext(Context context) {
        this.context = context;
    }

    public MutableLiveData<String> getEmail() {
        if(this.email==null){
            this.email=new MutableLiveData<>();
            initUser();
            email.setValue(user.getEmail());
        }
        return email;
    }

    public void setEmail(String newemail) {

    }

    public User getUser() {
        if(user==null){
            initUser();
        }
        return user;
    }

    public void changeEmail(String newemail){
        String name = context.getResources().getString(R.string.User_Data);
        initUser();
        if(this.email==null){
            this.email=new MutableLiveData<>();
        }
        SharedPreferences spf=context.getApplicationContext().getSharedPreferences(name,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= spf.edit();
        editor.putString("email",newemail);
        editor.apply();
        this.email.setValue(newemail);
        user.setEmail(newemail);
    }


    public void initUser(){
        String name = context.getResources().getString(R.string.User_Data);
        String username,email,avaterurl,id;
        SharedPreferences spf=context.getApplicationContext().getSharedPreferences(name,Context.MODE_PRIVATE);
        username=spf.getString("username","");
        id=spf.getString("id","");
        email=spf.getString("email","");

        avaterurl=spf.getString("avaterurl","");
        user=new User(id,email,username,avaterurl);
    };




}
