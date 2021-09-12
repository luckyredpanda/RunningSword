package com.swordrunner.swordrunner.ui.profile.mycomments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.Utils.UserOwnInfo;
import com.swordrunner.swordrunner.api.Client;
import com.swordrunner.swordrunner.api.service.UserRes;
import com.swordrunner.swordrunner.data.model.Comment;
import com.swordrunner.swordrunner.data.model.User;
import com.swordrunner.swordrunner.databinding.ActivityMyCommentBinding;

import java.util.ArrayList;
import java.util.concurrent.BlockingDeque;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyCommentActivity extends AppCompatActivity {

    private ArrayList<Comment> comments=new ArrayList<>();
    private ArrayList<User> users=new ArrayList<>();
    private User commentReceiver;
    private String TAG="OBSERVE";
    private static final String KEY_USER_COMMNENT = "commentReceiver"; //Seriabled user with comments
    private MyCommentViewModel myCommentViewModel;
    private ActivityMyCommentBinding binding;
    private MyCommentAdapter adapter;
    private RecyclerView commentRV;
    private Bundle bundle;
    private Intent intent;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_my_comment);
        commentRV = findViewById(R.id.commentRecylerView);

        intent=getIntent();
        bundle=intent.getExtras();
        commentReceiver=(User)bundle.getSerializable("user");

        myCommentViewModel=new ViewModelProvider(this).get(MyCommentViewModel.class);
        adapter=new MyCommentAdapter(MyCommentActivity.this);
        commentRV.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

        getUserData(new CustomCallback() {
            @Override
            public void onSuccess(User user) {
                adapter.InitMyCommentAdapter(user);
                if(adapter.InitMyCommentAdapter(user)){     //If player have one comment at least
                    binding.emptyhints.setVisibility(View.GONE);
                    binding.mycommentsRefreshLayout.setVisibility(View.VISIBLE);
                    commentRV.setAdapter(adapter);
                    commentRV.setLayoutManager(new LinearLayoutManager(MyCommentActivity.this));

                }else {
                    binding.emptyhints.setVisibility(View.VISIBLE);
                    binding.mycommentsRefreshLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure() {

            }
        });

        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Comments");


        binding.mycommentsRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showRecyclerView();
                adapter.notifyDataSetChanged();
                binding.mycommentsRefreshLayout.setRefreshing(false);
            }
        });
    }

    public void showRecyclerView(){
        getUserData(new CustomCallback() {
            @Override
            public void onSuccess(User user) {
                adapter.InitMyCommentAdapter(user);
                if(adapter.InitMyCommentAdapter(user)){     //If player have one comment at least
                    binding.emptyhints.setVisibility(View.GONE);
                    binding.mycommentsRefreshLayout.setVisibility(View.VISIBLE);
                    commentRV.setAdapter(adapter);
                    commentRV.setLayoutManager(new LinearLayoutManager(MyCommentActivity.this));

                }else {
                    binding.emptyhints.setVisibility(View.VISIBLE);
                    binding.mycommentsRefreshLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure() {

            }
        });



    }

    //Initiation of actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actionbar_delete,menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Hide delete menuitem if the owner of comments is not host user himself.
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        UserOwnInfo userOwnInfo=new UserOwnInfo(getApplicationContext());
        User me= userOwnInfo.getUser();
        menu.findItem(R.id.menu_action_delete).setVisible(me.getId().equals(commentReceiver.getId()));
        invalidateOptionsMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    //set action for item on actionbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        UserOwnInfo userOwnInfo=new UserOwnInfo(getApplicationContext());
        User me=userOwnInfo.getObject(KEY_USER_COMMNENT);
        switch (item.getItemId()){
            case R.id.menu_action_delete:
                if (me.getComments().size()>0){
                    showDeleteDialog();
                }else{
                    showEmptyDialog();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showEmptyDialog() {
        AlertDialog.Builder alert=new AlertDialog.Builder(this);
        alert.setMessage("Sorry, there is no comment to delete!");
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        alert.create().show();
    }


    private void showDeleteDialog() {
        AlertDialog.Builder alert=new AlertDialog.Builder(this);
        alert.setMessage("Are you sure to clear all comments you receive?");
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UserOwnInfo userOwnInfo=new UserOwnInfo(getApplicationContext());
                User user=userOwnInfo.getObject(KEY_USER_COMMNENT);
                clearallComments(user);
                finish();
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.create().show();
    }

    private void getUserData(CustomCallback customCallback){

        Call<User> call= Client.get(this).create(UserRes.class).get(commentReceiver.getId());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.code()==200){
                    User userRes=response.body();
                    UserOwnInfo userOwnInfo=new UserOwnInfo(getApplicationContext());
                    userOwnInfo.setObject(KEY_USER_COMMNENT,userRes);
                    customCallback.onSuccess(userRes);
                }else {
                    Log.e(TAG, "onFailure: "+response.code());
                    String msg=getResources().getString(R.string.GetDataFailed);
                    Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "onFailure: ",t);
                String msg=getResources().getString(R.string.GetDataFailed);
                Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT).show();
                customCallback.onFailure();
            }
        });

    }
    private interface CustomCallback{
        void onSuccess(User user);
        void onFailure();
    }

    //a empty commentslist overwrite the existing comments
    private void clearallComments(User user){
        user.getComments().removeAll(user.getComments());
        Call<User> call=Client.get(this).create(UserRes.class).writecomment(user);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.code()!=200){
                    Toast.makeText(MyCommentActivity.this,"No any comment exists",
                            Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(MyCommentActivity.this,"Failed to delete all comments",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }



}