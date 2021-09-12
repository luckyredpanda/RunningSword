package com.swordrunner.swordrunner.ui.friends;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.Utils.WebAddress;
import com.swordrunner.swordrunner.api.Client;
import com.swordrunner.swordrunner.api.service.FriendRes;
import com.swordrunner.swordrunner.api.service.UserRes;
import com.swordrunner.swordrunner.data.model.Friend;
import com.swordrunner.swordrunner.data.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddFriendActivity extends AppCompatActivity {
    private Friend testFriend;
    private ImageView search, add;
    private EditText emailText, userNameText;
    private CircleImageView avatar;
    private TextView hint;
    private TextView storeAvatar,storeId;
    private WebSocket webSocket;
    private String SERVER_PATH = WebAddress.WEB_SOCKET_ADDRESS;
    private static final String TAG = "AddFriendActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        initializeLayout();
        getUserId();
        initiateSocketConnection();
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String thisName = userNameText.getText().toString();
                if(thisName.equals("")){
                    hint.setText("Please fill in friend name to search for a friend");
                    hint.setTextColor(Color.RED);
                }else{
                    searchFromUserByName(thisName);
                }

            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!storeId.getText().equals("")){
                    getData(getUserId(), new CurrentUserCallback() {
                        @Override
                        public void onSuccess(User user) {
                            addFriend(user.getName(),user.getAvatarUrl(),user.getEmail());
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("friendId",storeId.getText().toString());
                                webSocket.send(jsonObject.toString());

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure() {

                        }
                    });
                }else {
                    hint.setText("Please search for a correct user before add");
                    hint.setTextColor(Color.RED);
                }
            }
        });
    }
    private void addFriend(String userName, String userAvatar, String userEmail){
        Friend friend = new Friend("","","","","","","","","");
        friend.setUserId(getUserId());
        Log.d(TAG, "addFriend: "+storeAvatar.getText().toString());
        friend.setFriendAvatar(storeAvatar.getText().toString());
        friend.setFriendId(storeId.getText().toString());
        friend.setFriendName(userNameText.getText().toString());
        friend.setFriendEmail(emailText.getText().toString());
        friend.setUserAvatar(userAvatar);
        friend.setUserEmail(userEmail);
        friend.setUserName(userName);
        Call<Friend> call = Client.get(this,false).create(FriendRes.class).add(friend);
        call.enqueue(new Callback<Friend>() {
            @Override
            public void onResponse(Call<Friend> call, Response<Friend> response) {
                if(response.code()==200){
                    Intent intent = new Intent();
                    intent.setAction("action.addFriend");
                    sendBroadcast(intent);
                    finish();
                }
                else{
                    hint.setText("Friend already exists");
                    hint.setTextColor(Color.RED);
                }
            }

            @Override
            public void onFailure(Call<Friend> call, Throwable t) {
                Toast.makeText(AddFriendActivity.this, "Adding process went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void initializeLayout(){
        search = findViewById(R.id.search);
        add = findViewById(R.id.add_finish);
        emailText = findViewById(R.id.edit_e_mail_add_friend);
        userNameText = findViewById(R.id.edit_username_add_friend);
        hint = findViewById(R.id.hint_add_friend);
        storeAvatar = findViewById(R.id.store_avatar);
        storeId = findViewById(R.id.store_id);
        avatar = findViewById(R.id.image_avatar_add_friend);
   }
   private void searchFromFriend(String userId, String friendId){
       Friend friend = new Friend("","","","","","","","","");
       friend.setFriendId(friendId);
       friend.setUserId(userId);
       Call<Friend> call = Client.get(this,false).create(FriendRes.class).searchOneWithId(friend);
       call.enqueue(new Callback<Friend>() {
           @Override
           public void onResponse(Call<Friend> call, Response<Friend> response) {
               if(response.code()==200){
                   hint.setText("Friend already exists");
                   hint.setTextColor(Color.RED);
                   storeAvatar.setText("");
                   storeId.setText("");
               }else{
                   hint.setText("User exists, click right to add friend");
                   hint.setTextColor(Color.BLUE);
               }
           }

           @Override
           public void onFailure(Call<Friend> call, Throwable t) {

           }
       });
   }
    private void searchFromUserByName(String username){
        User user = new User("","","","");
        user.setName(username);
        Call<User> call = Client.get(this,false).create(UserRes.class).searchFriendByName(user);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code()==200){
                    if(!response.body().getId().equals(getUserId())){
                        emailText.setText(response.body().getEmail());
                        if(response.body().getAvatarUrl()!=null&&!response.body().getAvatarUrl().equals("")){
                            Glide.with(getApplicationContext())
                                    .asBitmap()
                                    .load(response.body().getAvatarUrl())
                                    .into(avatar);
                        }else if(response.body().getAvatarUrl().equals("")){
                            Glide.with(getApplicationContext())
                                    .asBitmap()
                                    .load(R.drawable.unknown)
                                    .into(avatar);
                        }
                        storeAvatar.setText(response.body().getAvatarUrl());
                        storeId.setText(response.body().getId());
                        hint.setText("User exists, click right to add friend");
                        hint.setTextColor(Color.BLUE);
                    }else {
                        emailText.setText(response.body().getEmail());
                        hint.setTextColor(Color.RED);
                        hint.setText("Can't select yourself as friend");
                    }
                }else {
                    hint.setTextColor(Color.RED);
                    hint.setText("User doesn't exist");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }
    private void searchFromUserByEmail(String email){
        User user = new User("","","","");
        user.setEmail(email);
        Call<User> call = Client.get(this,false).create(UserRes.class).searchFriendByEmail(user);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.code()==200){
                    if(!response.body().getId().equals(getUserId())){
                        userNameText.setText(response.body().getName());
                        if(!response.body().getAvatarUrl().equals("")){
                            Glide.with(getApplicationContext())
                                    .asBitmap()
                                    .load(response.body().getAvatarUrl())
                                    .into(avatar);
                        }
                        storeId.setText(response.body().getId());
                        storeAvatar.setText(response.body().getAvatarUrl());
                        hint.setText("User exists, click right to add friend");
                        hint.setTextColor(Color.BLUE);
                    }
                    else{
                        userNameText.setText(response.body().getName());
                        hint.setText("Can't select yourself as friend");
                        hint.setTextColor(Color.RED);
                    }
                }else {
                    hint.setTextColor(Color.RED);
                    hint.setText("User doesn't exist");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
   }

    private String getUserId(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("swordrunnerpref", Context.MODE_PRIVATE);
        String currentUserId = pref.getString("id","").toString();
        return currentUserId;
    }
    private void initiateSocketConnection(){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(SERVER_PATH).build();
        webSocket = client.newWebSocket(request, new SocketListener());
    }
    private class SocketListener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, okhttp3.Response response) {
            super.onOpen(webSocket, response);
            runOnUiThread(()->{
                Toast.makeText(AddFriendActivity.this, "Friends Connection Successful!", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            super.onMessage(webSocket, text);
            runOnUiThread(()->{

            });
        }
    }
    //get info of current user
    private void getData(String id, CurrentUserCallback currentUserCallback) {
        Call<User> call = Client.get(getApplicationContext()).create(UserRes.class).get(id);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                currentUserCallback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }

        });
    }
    public interface CurrentUserCallback{
        void onSuccess(User user);
        void onFailure();
    }
}
