package com.swordrunner.swordrunner.ui.friends;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.api.Client;
import com.swordrunner.swordrunner.api.service.FriendRes;
import com.swordrunner.swordrunner.api.service.UserRes;
import com.swordrunner.swordrunner.data.model.Friend;
import com.swordrunner.swordrunner.data.model.User;
import com.swordrunner.swordrunner.ui.chatting.ChattingActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.WebSocket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendsRevViewAdapter extends RecyclerView.Adapter{

    private static final int VIEW_TYPE_MESSAGE = 0;
    private static final int VIEW_TYPE_WAIT = 1;
    private static final int VIEW_TYPE_ACCEPT = 2;

    private ArrayList<Friend> friends = new ArrayList<>();
    private WebSocket webSocket;

    private Context context;

   public void setFriends(ArrayList<Friend> friends){
       this.friends = friends;
       notifyDataSetChanged();
   }
   public ArrayList<Friend> getFriends(){
       return this.friends;
   }
   public void setWebSocket(WebSocket webSocket){
       this.webSocket = webSocket;
   }

    public FriendsRevViewAdapter(Context context) {
        this.context = context;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == VIEW_TYPE_ACCEPT){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_friend_accept,parent,false);
            return  new AcceptViewHolder(view);
        }else if(viewType == VIEW_TYPE_MESSAGE){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_friends,parent,false);
            return new MessageViewHolder(view);
        }else if(viewType == VIEW_TYPE_WAIT){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_friend_wait,parent,false);
            return new WaitViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, int position) {

       Friend friend = friends.get(position);
       switch (holder.getItemViewType()){
           case VIEW_TYPE_MESSAGE:
               ((MessageViewHolder) holder).bind(friend,position);
               break;
           case VIEW_TYPE_WAIT:
                ((WaitViewHolder) holder).bind(friend,position);
                break;
           case VIEW_TYPE_ACCEPT:
               ((AcceptViewHolder) holder).bind(friend,position);
               break;
           default:break;

       }

    }

    @Override
    public int getItemViewType(int position) {
       Friend friend = friends.get(position);
       if(friend.getState() == 0){
           return VIEW_TYPE_MESSAGE;
       }else if(friend.getState() == 1){
           return VIEW_TYPE_WAIT;
       }else if (friend.getState() == 2){
           return VIEW_TYPE_ACCEPT;
       }
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        private TextView userName, countingDays, lastSentence;
        private CardView parent;
        private CircleImageView avatars;
        public MessageViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            lastSentence = itemView.findViewById(R.id.lastSentence);
            avatars = itemView.findViewById(R.id.avatarImg);
            parent = itemView.findViewById(R.id.cardParent);
        }
        void bind(Friend friend, int position){
            if(friend.getLastSentence()==null)
                lastSentence.setText("No unread words");
            else
                lastSentence.setText(friend.getLastSentence());
            userName.setText(friend.getFriendName());
            parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(context,"UserID:"+users.get(position).getId()+" Selected",Toast.LENGTH_SHORT).show();
                    startChattingActivity(friend);
                }
            });
            if(!friend.getFriendAvatar().equals("")){
                Glide.with(context)
                        .asBitmap()
                        .load(friend.getFriendAvatar())
                        .into(avatars);
            }
        }
    }
    public class AcceptViewHolder extends RecyclerView.ViewHolder{
        private TextView userName;
        private CardView parent;
        private ImageButton acceptButton, declineButton;
        private CircleImageView avatars;
        public AcceptViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userNameAccept);
            acceptButton = itemView.findViewById(R.id.acceptFriend);
            declineButton = itemView.findViewById(R.id.declineFriend);
            parent = itemView.findViewById(R.id.cardParentAccept);
            avatars = itemView.findViewById(R.id.avatarImgAccept);
        }
        void bind(Friend friend, int position){
            parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context, "Please click right", Toast.LENGTH_SHORT).show();
                }
            });
            userName.setText(friend.getUserName());
            acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Friend addFriend = new Friend("","","","","","","","","");
                    addFriend.setUserName(friend.getFriendName());
                    addFriend.setUserEmail(friend.getFriendEmail());
                    addFriend.setUserAvatar(friend.getFriendAvatar());
                    addFriend.setFriendEmail(friend.getUserEmail());
                    addFriend.setFriendAvatar(friend.getUserAvatar());
                    addFriend.setFriendName(friend.getUserName());
                    addFriend.setUserId(friend.getFriendId());
                    addFriend.setFriendId(friend.getUserId());
                    addFriend.setLastSentence(friend.getLastSentence());
                    addFriend.setState(0);
                    addOneFriend(addFriend);
                    friends.remove(position);
                    friends.add(position,addFriend);
                    setFriends(friends);
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("friendId",friend.getUserId());
                        webSocket.send(jsonObject.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            declineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    friends.remove(friend);
                    setFriends(friends);
                    deleteFriend(friend.getFriendId(),friend.getUserId());
                    deleteFriend(friend.getUserId(),friend.getFriendId());
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("friendId",friend.getUserId());
                        webSocket.send(jsonObject.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            if(!friend.getUserAvatar().equals("")){
                Glide.with(context)
                        .asBitmap()
                        .load(friend.getUserAvatar())
                        .into(avatars);
            }
        }
    }
    public class WaitViewHolder extends RecyclerView.ViewHolder{
        private TextView userName;
        private CardView parent;
        private CircleImageView avatars;
        public WaitViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userNameWait);
            avatars = itemView.findViewById(R.id.avatarImgWait);
            parent = itemView.findViewById(R.id.cardParentWait);
        }
        void bind(Friend friend, int position){
            userName.setText(friend.getFriendName());
            if(!friend.getFriendAvatar().equals("")){
                Glide.with(context)
                        .asBitmap()
                        .load(friend.getFriendAvatar())
                        .into(avatars);
            }
        }
    }
    private void startChattingActivity(Friend friend) {
       Intent intent = new Intent(context,ChattingActivity.class);
       Bundle bundle = new Bundle();
       bundle.putSerializable("friend",friend);
       intent.putExtras(bundle);
       context.startActivity(intent);

    }
    private void deleteFriend(String friendId, String userId){
        Friend friend = new Friend(friendId,userId,"","","","","","","");
        Call<Friend> call = Client.get(context,false).create(FriendRes.class).deleteFriend(friend);
        call.enqueue(new Callback<Friend>() {
            @Override
            public void onResponse(Call<Friend> call, Response<Friend> response) {

            }
            @Override
            public void onFailure(Call<Friend> call, Throwable t) {

            }
        });
    }
    private void addOneFriend(Friend friend){
        Call<Friend> call = Client.get(context,false).create(FriendRes.class).add(friend);
        call.enqueue(new Callback<Friend>() {
            @Override
            public void onResponse(Call<Friend> call, Response<Friend> response) {
                if(response.code()==200){

                }
            }

            @Override
            public void onFailure(Call<Friend> call, Throwable t) {
                Toast.makeText(context, "Adding process went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
