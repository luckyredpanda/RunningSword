package com.swordrunner.swordrunner.ui.chatting;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.Utils.StartOneActivities;
import com.swordrunner.swordrunner.Utils.WebAddress;
import com.swordrunner.swordrunner.api.Client;
import com.swordrunner.swordrunner.api.service.FriendRes;
import com.swordrunner.swordrunner.api.service.MessageRes;
import com.swordrunner.swordrunner.data.model.Friend;
import com.swordrunner.swordrunner.data.model.Message;
import com.swordrunner.swordrunner.data.model.User;
import com.swordrunner.swordrunner.notification.App;
import com.swordrunner.swordrunner.ui.friends.FriendsFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import retrofit2.Call;
import retrofit2.Callback;

public class ChattingActivity extends AppCompatActivity {

    private Friend friend = null;
    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private ArrayList<JSONObject> messageList = new ArrayList<>();
    private ImageButton sendButton;
    private EditText chatMessage;
    private String chatWords;
    private String SERVER_PATH = WebAddress.WEB_SOCKET_ADDRESS;
    private WebSocket webSocket;
    private String currentUserName;
    private String currentUserId;
    private NotificationManagerCompat notificationManager;
    private StartOneActivities startOneActivities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);
        initFriend();
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle(friend.getFriendName());

        notificationManager = NotificationManagerCompat.from(this);

        initiateSocketConnection();
        initRecycler();
        getCurrentUserInfo();

        initMessageList();
        //storeUser("message.txt","Hello World!");

        sendButton = findViewById(R.id.button_chat_send);
        chatMessage = findViewById(R.id.edit_chat_message);
        chatMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(start>=0&&after>=1){
                    sendButton.setVisibility(View.VISIBLE);
                }else if(start==0&&after==0){
                    sendButton.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        initSendButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actionbar_chatting,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_action_more:
                startOthersProfile();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startOthersProfile() {
        String friendID=friend.getFriendId();
        String friendName=friend.getFriendName();
        String avatar=friend.getFriendAvatar();

        User friend=new User();
        friend.setId(friendID);
        friend.setName(friendName);
        friend.setAvatarUrl(avatar);
        startOneActivities =new StartOneActivities();
        startOneActivities.startProfileActivity(this,friend);
        finish();
    }

    private void initSendButton(){
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chatWords = chatMessage.getText().toString();
                chatMessage.setText("");
                //test fileReader
                //Toast.makeText(ChattingActivity.this, readFile, Toast.LENGTH_SHORT).show();

                //this block is storing the chatting message in the database
                String totalClickId;
                int compare =friend.getUserId().compareTo(friend.getFriendId());
                Log.d("ChattingActivity", "this test "+compare);
                if(compare > 0){
                    //that is, initCurrentId > initFriendId
                    totalClickId = friend.getFriendId() + friend.getUserId();
                }else{
                    // initCurrentId < initFriendId
                    totalClickId = friend.getUserId() + friend.getFriendId();
                }
                Message clickMessage = new Message(chatWords,friend.getUserId(),totalClickId,friend.getFriendName(),friend.getFriendId(),getCurrentDate() + " " + getCurrentTime(),true);
                addMessage(clickMessage);

                //this block is refresh last sentence in friend list
                refreshLastSentence(friend.getFriendId(),friend.getUserId(),chatWords);
                refreshLastSentence(friend.getUserId(),friend.getFriendId(),chatWords);
                Intent intent = new Intent();
                intent.setAction("action.changeSentence");
                sendBroadcast(intent);


                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("name",friend.getUserName());
                    jsonObject.put("id",friend.getFriendId());
                    jsonObject.put("currentid",friend.getUserId());
                    jsonObject.put("message",chatWords);
                    jsonObject.put("time",getCurrentDate() + " " + getCurrentTime());
                    jsonObject.put("avatar",friend.getUserAvatar());

                    webSocket.send(jsonObject.toString());
                    jsonObject.put("isSent",true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                messageList.add(jsonObject);
                //messageList.add(new Message(chatWords, friend,"9:00",true));
                mMessageAdapter.SetList(messageList);
                mMessageRecycler.smoothScrollToPosition(messageList.size());
            }
        });
    }
    public Bitmap loadBitmap(String url){
        Bitmap bm = null;
        InputStream is = null;
        BufferedInputStream bis = null;
        URL imageUrl = null;
        try {
            imageUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection)imageUrl.openConnection();
            connection.connect();
            is = connection.getInputStream();
            bis = new BufferedInputStream(is,8192);
            bm = BitmapFactory.decodeStream(bis);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(bis != null)
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bm;
    }
    public void sendOnChannel1(String message,String name,String avatar){
        String title = friend.getFriendName();
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),Uri.parse(avatar));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.common_full_open_on_phone)
                .setLargeIcon(bitmap)
                .setContentTitle(name)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();
        notificationManager.notify(1,notification);
    }
    public void sendOnChannel2(){

    }
    private void initFriend(){
        friend = (Friend) getIntent().getSerializableExtra("friend");
    }
    private void initRecycler(){
        mMessageRecycler = (RecyclerView)findViewById(R.id.recycler_chat);
        mMessageAdapter = new MessageListAdapter(this,messageList);
        mMessageRecycler.setAdapter(mMessageAdapter);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        //mMessageRecycler.smoothScrollToPosition(messageList.size()-1);
    }
    //store message from local storage
    private void storeUser(String fileName, String fileContents){
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(fileName, MODE_PRIVATE);
            fos.write(fileContents.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fos!=null)
            {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //read message from local storage
    private String readUser(String fileName){
        FileInputStream fis = null;
        String str = null;
        try {
            fis = openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            while ((str = br.readLine())!=null){
                sb.append(str).append("\n");
            }
            str = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fis!=null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return str;

    }
    private String initStoreMessage(String date, String message, boolean isSend){
        String storedMessage = null;
        if(isSend){
            storedMessage = "1"+date+message;
        }else{
            storedMessage = "0"+date+message;
        }
        return null;
    }
    //to use okhttp to connect
    private void initiateSocketConnection(){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(SERVER_PATH).build();
        webSocket = client.newWebSocket(request, new SocketListener());
    }
    //receive message from the socket server
    private class SocketListener extends WebSocketListener{
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);
            //perform UI update on thread
            runOnUiThread(() ->{
                Log.d("SocketListener", "Socket Connection Successful"+currentUserId);
                //Toast.makeText(ChattingActivity.this, "Socket Connection Successful!", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            super.onMessage(webSocket, text);
            //perform UI update on thread
            runOnUiThread(() ->{
                try {
                    Intent intent = new Intent();
                    intent.setAction("action.changeSentence");
                    sendBroadcast(intent);
                    JSONObject jsonObject = new JSONObject(text);
                    jsonObject.put("isSent",false);
                    String currentid = jsonObject.getString("id");
                    String id = jsonObject.getString("currentid");
                    if(currentid.equals(friend.getUserId())&&id.equals(friend.getFriendId())){
                        String message = jsonObject.getString("message");
                        String title = jsonObject.getString("name");
                        String avatar = jsonObject.getString("avatar");
                        sendOnChannel1(message, title, avatar);
                        messageList.add(jsonObject);
                        mMessageAdapter.SetList(messageList);
                        mMessageRecycler.smoothScrollToPosition(messageList.size());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }

    }
    //init message with data from database
    /**
     * compare the the two user's id, the smaller one add the other one will be the
     * total id
     */
    private void initMessageList(){
        String initCurrentId, initFriendId;
        String totalId;
        initCurrentId = currentUserId;
        initFriendId = friend.getFriendId();
        if(initCurrentId.compareTo(initFriendId) > 0){
            //that is, initCurrentId > initFriendId
            totalId = initFriendId + initCurrentId;
        }else{
            // initCurrentId < initFriendId
            totalId = initCurrentId + initFriendId;
        }
        Message initMessage = new Message("","",totalId,"","","",false);
        getAllMessage(initMessage, new MessageCallback() {
            @Override
            public void onSuccess(ArrayList<Message> messages) {

                checkMessages(messages);
            }

            @Override
            public void onFailure() {

            }
        });

    }
    private void checkMessages(ArrayList<Message> initMessages){
        for(int i = 0; i < initMessages.size(); i++){
            JSONObject initJsonObject = new JSONObject();
            try {
                if(initMessages.get(i).getSenderId().equals(currentUserId)){
                    //which means that the current user acts as the sender for this message
                    initJsonObject.put("isSent",true);
                }else{
                    //which means that the current user acts as the receiver for this message
                    initJsonObject.put("isSent",false);
                }
                initJsonObject.put("name",friend.getFriendName());
                initJsonObject.put("id",friend.getFriendId());
                initJsonObject.put("currentid",friend.getUserId());
                initJsonObject.put("message",initMessages.get(i).getMessage());
                initJsonObject.put("time",initMessages.get(i).getCreatedAt());
                initJsonObject.put("avatar",friend.getFriendAvatar());

                messageList.add(initJsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            mMessageAdapter.SetList(messageList);
            mMessageRecycler.smoothScrollToPosition(messageList.size());
        }
    }
    //get all message from the database
    private void getAllMessage(Message message,MessageCallback messageCallback){
        Call<ArrayList<Message>> call = Client.get(getApplicationContext(),false).create(MessageRes.class).getAllMessage(message);
        call.enqueue(new Callback<ArrayList<Message>>() {
            @Override
            public void onResponse(Call<ArrayList<Message>> call, retrofit2.Response<ArrayList<Message>> response) {
                if(response.code()==200){
                    messageCallback.onSuccess(response.body());
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Message>> call, Throwable t) {

            }
        });
    }
    //add message to the database
    private void addMessage(Message message){
        Call<Message> call = Client.get(getApplicationContext(),false).create(MessageRes.class).addMessage(message);
        call.enqueue(new Callback<Message>() {
            @Override
            public void onResponse(Call<Message> call, retrofit2.Response<Message> response) {
                if(response.code()==200){
                    Log.d("addMessage", "add message successful");
                }
            }

            @Override
            public void onFailure(Call<Message> call, Throwable t) {

            }
        });
    }
    public interface MessageCallback{
        void onSuccess(ArrayList<Message> messages);
        void onFailure();
    }
    //get name and ID of current user
    private void getCurrentUserInfo(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("swordrunnerpref", Context.MODE_PRIVATE);
        currentUserName = pref.getString("name","").toString();
        currentUserId = pref.getString("id","").toString();
    }
    //get the date of the sent message
    private String getCurrentDate(){
        String currentDate;
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+2:00"));

        String year = String.valueOf(cal.get(Calendar.YEAR));
        String month = String.valueOf(cal.get(Calendar.MONTH)+1);
        String day = String.valueOf(cal.get(Calendar.DATE));
        switch (month){
            case "1":
                month = "Jan";
                break;
            case "2":
                month = "Feb";
                break;
            case "3":
                month = "Mar";
                break;
            case "4":
                month = "Apr";
                break;
            case "5":
                month = "May";
                break;
            case "6":
                month = "June";
                break;
            case "7":
                month = "July";
                break;
            case "8":
                month = "Aug";
                break;
            case "9":
                month = "Sep";
                break;
            case "10":
                    month = "Oct";
                break;
            case "11":
                month = "Nov";
                break;
            case "12":
                month = "Dec";
                break;
            default:break;
        }
        currentDate = day+"."+month;
        return currentDate;
    }
    //get the time of the send message
    private String getCurrentTime(){
        String currentTime;
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+2:00"));

        String hour;
        if(cal.get(Calendar.AM_PM) == 0)
            hour = String.valueOf(cal.get(Calendar.HOUR));
        else
            hour = String.valueOf(cal.get(Calendar.HOUR)+12);
        String minute = String.valueOf(cal.get(Calendar.MINUTE));
        currentTime = hour+":"+minute;

        return currentTime;
    }
    //get last sentence for every friend from the messagelist
    private void refreshLastSentence(String userId,String friendId,String lastSentence){
        Friend friend = new Friend(friendId,userId,"","","","","","",lastSentence);
        Call<Friend> call = Client.get(getApplicationContext(),false).create(FriendRes.class).updateSentence(friend);
        call.enqueue(new Callback<Friend>() {
            @Override
            public void onResponse(Call<Friend> call, retrofit2.Response<Friend> response) {
                Log.d("refreshLastSentence", lastSentence);
            }

            @Override
            public void onFailure(Call<Friend> call, Throwable t) {

            }
        });
    }
}
