package com.swordrunner.swordrunner.Utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.swordrunner.swordrunner.data.model.User;
import com.swordrunner.swordrunner.ui.profile.OthersProfileActivity;
import com.swordrunner.swordrunner.ui.profile.mycomments.MyCommentActivity;

public class StartOneActivities {
    Context context;
    private User user;

    public void startMyCommentActivity(Context context,User user){
        Intent intent = new Intent(context, MyCommentActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", user);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }


    public void startProfileActivity(Context context,User user){
        Intent intent = new Intent(context, OthersProfileActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", user);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }




}
