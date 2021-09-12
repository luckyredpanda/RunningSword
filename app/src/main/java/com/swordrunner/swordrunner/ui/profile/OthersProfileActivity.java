package com.swordrunner.swordrunner.ui.profile;

import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.Utils.StartOneActivities;
import com.swordrunner.swordrunner.api.Client;
import com.swordrunner.swordrunner.api.service.UserRes;
import com.swordrunner.swordrunner.data.model.Comment;
import com.swordrunner.swordrunner.data.model.User;
import com.swordrunner.swordrunner.databinding.ActivityOthersProfileBinding;
import com.swordrunner.swordrunner.ui.chatting.ChattingActivity;
import com.swordrunner.swordrunner.ui.profile.mycomments.MyCommentActivity;
import com.swordrunner.swordrunner.ui.profile.mycomments.MyCommentAdapter;
import com.swordrunner.swordrunner.ui.profile.writingComment.WritingCommentActivity;

import java.io.Serializable;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OthersProfileActivity extends AppCompatActivity {

    private ActivityOthersProfileBinding binding;
    private User user;          //other player, not me
    private Intent intent;
    private Bundle bundle;
    protected RecyclerView otherRecyclerView;
    private static final String TAG = "Other Profile ACT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        MyCommentAdapter myCommentAdapter=new MyCommentAdapter(this);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_others_profile);
        otherRecyclerView=binding.othersProflile.otherRecylerView;
        otherRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

        intent=getIntent();
        bundle=intent.getExtras();
        user=(User)bundle.getSerializable("user");
        if(user.getAvatarUrl() == null || user.getAvatarUrl().equals(""))
            Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.unknown)
                    .into(binding.othersProflile.avatarImg1OthersUserprofile);
        else{
            Glide.with(this)
                    .asBitmap()
                    .load(user.getAvatarUrl())
                    .into(binding.othersProflile.avatarImg1OthersUserprofile);
        }


        getUserData(new CostumCallBackOP() {
            @Override
            public void onSuccess(User user) {
                OthersProfileActivity.this.user=user;
                if (user.getComments() == null) {
                    Log.d(TAG, "onSuccess: user is empty");
                }else{
                    binding.othersProflile.vtUsernameUserprofile.setText(user.getName());
                    myCommentAdapter.InitMyCommentAdapter(user);
                    myCommentAdapter.setItemCount(3);
                    otherRecyclerView.setAdapter(myCommentAdapter);
                    otherRecyclerView.setLayoutManager(new LinearLayoutManager(OthersProfileActivity.this));
                }

            }

            @Override
            public void onFailure() {

            }
        });


        this.getSupportActionBar().setTitle("Friend Profile");
        //Toolbar toolbar = binding.toolbar;
        //setSupportActionBar(toolbar);
        //toolbar.setTitle(bundle.getString("FriendsID"));
        //CollapsingToolbarLayout toolBarLayout = binding.toolbarLayout;
        //toolBarLayout.setTitle(bundle.getString("FriendsID"));

//        FloatingActionButton fab = binding.fab;
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//                startChattingActivity(user);
//            }
//        });


        clickTextViewViewMore(binding.othersProflile.viewmore.getText(),binding.othersProflile.viewmore);
        clickTextViewMyGame(binding.othersProflile.viewMoreGame.getText(),binding.othersProflile.viewMoreGame);
        clickTextViewWritecomment(binding.othersProflile.vtBtWritingComment.getText(),
                binding.othersProflile.vtBtWritingComment);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        MyCommentAdapter myCommentAdapter=new MyCommentAdapter(this);
        getUserData(new CostumCallBackOP() {
            @Override
            public void onSuccess(User user) {
                OthersProfileActivity.this.user=user;
                if (user.getComments() == null) {
                    Log.d(TAG, "onSuccess: user is empty");
                }else{
                    binding.othersProflile.vtUsernameUserprofile.setText(user.getName());
                    myCommentAdapter.InitMyCommentAdapter(user);
                    myCommentAdapter.setItemCount(3);
                    otherRecyclerView.setAdapter(myCommentAdapter);
                    otherRecyclerView.setLayoutManager(new LinearLayoutManager(OthersProfileActivity.this));
                }

            }

            @Override
            public void onFailure() {

            }
        });
        super.onResume();
    }
    private void clickTextViewMyGame(CharSequence text, TextView textView ){
        SpannableStringBuilder spannableString = new SpannableStringBuilder(text);
        ClickableSpan clickableSpan=new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(getApplicationContext(), MyGamelistActivity.class);
                user = (User) bundle.getSerializable("user");
                String usrID = user.getId();
                intent.putExtra("userId", usrID);
                startActivity(intent);

            }
        };
        int end =textView.getText().length();
        spannableString.setSpan(clickableSpan,0,end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

    }

    private void clickTextViewWritecomment(CharSequence text, TextView textView){
        SpannableStringBuilder spannableString=new SpannableStringBuilder(text);
        ClickableSpan clickableSpan=new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startWriteCommentActivity();
            }
        };
        int end = textView.getText().length();
        spannableString.setSpan(clickableSpan,0,end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

    }

    private void clickTextViewViewMore(CharSequence text, TextView textView ){
        SpannableStringBuilder spannableString = new SpannableStringBuilder(text);
        ClickableSpan clickableSpan=new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                StartOneActivities startOneActivities=new StartOneActivities();
                startOneActivities.startMyCommentActivity(OthersProfileActivity.this,user);
            }
        };
        int end =textView.getText().length();
        spannableString.setSpan(clickableSpan,0,end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

    }

    private void startWriteCommentActivity(){
        Intent intent=new Intent(OthersProfileActivity.this, WritingCommentActivity.class);
        Bundle bundle=new Bundle();
        bundle.putSerializable("guest",user);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void startChattingActivity(User user) {
        Intent intent = new Intent(OthersProfileActivity.this, ChattingActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", (Serializable) user);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    /**
     * search a user by ID. After execution it will set the value of result to the private
     * variable user.
     */
    private void getUserData(CostumCallBackOP costumCallBackOP){
        String usrID;
        user=(User) bundle.getSerializable("user");
        usrID=user.getId();

        Call<User> call= Client.get(this).create(UserRes.class).get(usrID);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.code()==200){
                    User userRes=response.body();
                    Log.d(TAG, "onResponse: "+userRes.toString());
                    costumCallBackOP.onSuccess(userRes);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                costumCallBackOP.onFailure();
            }
        });
    }

    private interface CostumCallBackOP {
        void onSuccess(User user);
        void onFailure();
    }

}