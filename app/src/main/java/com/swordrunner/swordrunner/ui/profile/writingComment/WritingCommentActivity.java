package com.swordrunner.swordrunner.ui.profile.writingComment;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.Utils.UserOwnInfo;
import com.swordrunner.swordrunner.api.Client;
import com.swordrunner.swordrunner.api.service.UserRes;
import com.swordrunner.swordrunner.data.model.Comment;
import com.swordrunner.swordrunner.data.model.User;
import com.swordrunner.swordrunner.databinding.ActivityWritingCommentBinding;

import java.util.Date;
import java.util.LinkedList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class WritingCommentActivity extends AppCompatActivity {

    private EditText edcomment;
    private RatingBar rating;
    private ActivityWritingCommentBinding binding;
    private Intent intent;
    private Bundle bundle;
    private User other;             //Comment receiver
    private User me;                //The man who write a comment
    private Comment comment;
    private static final String TAG = "writing comment";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_writing_comment);
        edcomment =findViewById(R.id.editTextTextMultiLine);
        rating=findViewById(R.id.ratingBar);
        intent=getIntent();
        bundle=intent.getExtras();
        other=(User)bundle.getSerializable("guest");
        if(other==null){
            Log.e(TAG, "onCreate: other is empty");
        }else {
            initView();
        }
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Writting Comment");
    }

    private void initView() {
        if (other.getName().isEmpty()){
            binding.vtUsername.setText(other.getId());
        }else{
            binding.vtUsername.setText(other.getName());
            String avatar = other.getAvatarUrl();
            if(avatar == null || avatar.equals("")){
                Glide.with(this)
                        .asBitmap()
                        .load(R.drawable.unknown)
                        .into(binding.userImagesetting);
            }else {
                Glide.with(this)
                        .asBitmap()
                        .load(other.getAvatarUrl())
                        .into(binding.userImagesetting);
            }
        }
    }


    //initial menu in toolbar

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_change,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_save:
                if (edcomment.getText()!=null && rating.getRating()!=0){
                    storeData();
                    finish();
                }else {
                    Toast.makeText(WritingCommentActivity.this,"Please full  all editText!!",Toast.LENGTH_SHORT);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void storeData() {
        String TAG="StData";
        //get user(myself) from sharepreference
        UserOwnInfo userOwnInfo=new UserOwnInfo(getApplicationContext());
        LinkedList<Comment> comments;
        me=userOwnInfo.getUser();
        Date date =new Date();
        comment=new Comment(other.getId(),
                me.getName(),
                me.getId(),
                edcomment.getText().toString(),
                rating.getRating(),
                date,
                me.getAvatarUrl());
        comments=other.getComments();
        Log.d(TAG, "rating: "+rating.getRating());

        comments.addFirst(comment);
        other.setComments(comments);

        Call<User> call= Client.get(this,false).create(UserRes.class).writecomment(other);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.code()==200){
                    Log.d(TAG, "onResponse: Write a Comment"+other.toString());
                }else {
                    Log.e(TAG, "onResponse: Failed to write a comment");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "onFailure: ",t);
                String msg=getResources().getString(R.string.WriteCommentFailed);
                Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


}

