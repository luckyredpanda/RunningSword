package com.swordrunner.swordrunner.ui.profile;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.api.Client;
import com.swordrunner.swordrunner.api.service.FriendRes;
import com.swordrunner.swordrunner.api.service.UserRes;
import com.swordrunner.swordrunner.data.model.ChangeUserName;
import com.swordrunner.swordrunner.data.model.Friend;
import com.swordrunner.swordrunner.data.model.User;
import com.swordrunner.swordrunner.ui.friends.AddFriendActivity;

public class ChangeUserNameActivity extends AppCompatActivity {

    Button button;
    EditText editText;
    private static final String TAG = "ChangeUserNameActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_user_name);
        editText = findViewById(R.id.change_user_name_edittext);
        button = findViewById(R.id.change_user_name_button);
        String name = getIntent().getStringExtra("username");
        editText.setHint(name);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closesoftKeyboard();
                String newName = editText.getText().toString();
                if (newName.equals("")){
                    Toast.makeText(ChangeUserNameActivity.this, "Please enter new name", Toast.LENGTH_SHORT).show();
                }
                else{
                    String id = getUserId();
                    changeUserName(newName, id, new NameChangeCallback() {
                        @Override
                        public void onSuccess() {
                            Friend friend1 = new Friend("","","","","","","","","");
                            Friend friend2 = new Friend("","","","","","","","","");
                            friend1.setUserId(getUserId());
                            friend1.setUserName(newName);
                            friend2.setFriendId(getUserId());
                            friend2.setFriendName(newName);
                            changeFriendName(friend2);
                            changeFriendUserName(friend1);
                            refreshData();
                            editText.setText("");
                            editText.setHint(name);
                            Intent intent = new Intent(getApplicationContext(),SelfsettingActivity.class);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onFailure() {

                        }
                    });

                }
            }
        });

    }
    private void changeFriendName(Friend friend){
        Call<Friend> call = Client.get(this,false).create(FriendRes.class).updateFriendName(friend);
        call.enqueue(new Callback<Friend>() {
            @Override
            public void onResponse(Call<Friend> call, Response<Friend> response) {
                if(response.code() == 200){
                    Log.d(TAG, "onResponse: change friend name success");
                }else {
                    String msg=getResources().getString(R.string.UsernameChangeFailed);
                    Toast.makeText(ChangeUserNameActivity.this,msg, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Friend> call, Throwable t) {
                Log.e(TAG, "onFailure: changeFriendName",t);
                String msg=getResources().getString(R.string.UsernameChangeFailed);
                Toast.makeText(ChangeUserNameActivity.this,msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void changeFriendUserName(Friend friend){
        Call<Friend> call = Client.get(this,false).create(FriendRes.class).updateUserName(friend);
        call.enqueue(new Callback<Friend>() {
            @Override
            public void onResponse(Call<Friend> call, Response<Friend> response) {
                if(response.code() == 200) {
                    Log.d(TAG, "onResponse: change user name success");
                }else {
                    String msg=getResources().getString(R.string.UsernameChangeFailed);
                    Toast.makeText(ChangeUserNameActivity.this,msg, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<Friend> call, Throwable t) {
                Log.e(TAG, "onFailure: changeFriendName",t);
                String msg=getResources().getString(R.string.UsernameChangeFailed);
                Toast.makeText(ChangeUserNameActivity.this,msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void changeUserName(String name,String id, NameChangeCallback nameChangeCallback){
        ChangeUserName changeUserName = new ChangeUserName(id,name);
        Call<User> call = Client.get(this,false).create(UserRes.class).changeUser(changeUserName);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.code() == 200){
                    nameChangeCallback.onSuccess();
                }else if(response.code()==404){
                    String msg=getResources().getString(R.string.DuplicateNameError);
                    Snackbar.make(findViewById(android.R.id.content),msg,Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                nameChangeCallback.onFailure();
            }
        });
    }
    private String getUserId(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("swordrunnerpref", Context.MODE_PRIVATE);
        String currentUserId = pref.getString("id","").toString();
        return currentUserId;
    }
    private void refreshData(){
        Intent intent = new Intent();
        intent.setAction("action.refreshName");
        sendBroadcast(intent);
    }
    public interface NameChangeCallback{
        void onSuccess();
        void onFailure();
    }
    private void closesoftKeyboard(){
        View view=this.getCurrentFocus();
        if(view!=null){
            InputMethodManager inputMethodManager=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

}