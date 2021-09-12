package com.swordrunner.swordrunner.ui.friends;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.swordrunner.swordrunner.Helper.MyButtonClickListener;
import com.swordrunner.swordrunner.Helper.MySwipeHelper;
import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.Utils.WebAddress;
import com.swordrunner.swordrunner.api.Client;
import com.swordrunner.swordrunner.api.service.FriendRes;
import com.swordrunner.swordrunner.api.service.MessageRes;
import com.swordrunner.swordrunner.data.model.Friend;
import com.swordrunner.swordrunner.data.model.Message;
import com.swordrunner.swordrunner.data.model.User;
import com.swordrunner.swordrunner.databinding.FragmentFriendsBinding;
import com.swordrunner.swordrunner.ui.chatting.ChattingActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {

    private FriendsViewModel friendsViewModel;
    private FriendsRevViewAdapter adapter;
    private FragmentFriendsBinding binding;
    private Toolbar toolbar;
    private RecyclerView friendsRecView;
    private ArrayList<Friend> friends = new ArrayList<>();
    private ImageView search;
    private EditText searchText;
    private String searchName;
    private String userName;
    private BroadcastReceiver broadcastReceiver;
    private View root;
    private String id;
    public WebSocket webSocket;
    private String SERVER_PATH = WebAddress.WEB_SOCKET_ADDRESS;
    private static final String TAG = "FriendsFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        friendsViewModel =
                new ViewModelProvider(this).get(FriendsViewModel.class);

        binding = FragmentFriendsBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        initView();
        setHasOptionsMenu(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Friends list");
        id = getUserId();

        initializeFriendList();
        initializeBroadcast();
        initiateSocketConnection();
        initItemTouchHelper();

        return root;
    }
    private void initItemTouchHelper(){
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                switch (direction){
                    case ItemTouchHelper.LEFT:
                        //Toast.makeText(getContext(), "you have clicked position: "+position, Toast.LENGTH_SHORT).show();
                        ArrayList<Friend> thisFriends = adapter.getFriends();
                        String thisId = getUserId();
                        String currentId = thisFriends.get(position).getUserId();
                        String targetId = thisFriends.get(position).getFriendId();
                        thisFriends.remove(position);
                        adapter.setFriends(thisFriends);
                        deleteFriend(currentId,targetId);
                        deleteFriend(targetId,currentId);
                        String totalId = getTotalId(currentId,targetId);
                        deleteMessage(totalId);
                        JSONObject jsonObject = new JSONObject();
                        try {
                            if(thisId.equals(currentId))
                                jsonObject.put("friendId",targetId);
                            else
                                jsonObject.put("friendId",currentId);
                            webSocket.send(jsonObject.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftActionIcon(R.drawable.ic_action_delete)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(getContext(),R.color.red))
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(friendsRecView);
    }
    ////to use broadcast to receive message from other activity
    private void initializeBroadcast(){

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("action.addFriend");
        intentFilter.addAction("action.changeSentence");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals("action.addFriend"))
                {
                    initializeFriendList();
                }else if(action.equals("action.changeSentence")){
                    initializeFriendList();
                }else if(action.equals("action.acceptFriend")){
                    initializeFriendList();
                }else if(action.equals("action.declineFriend")){
                    initializeFriendList();
                }
            }
        };
        getActivity().registerReceiver(broadcastReceiver,intentFilter);
    }
    private void initiateSocketConnection(){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(SERVER_PATH).build();
        webSocket = client.newWebSocket(request, new SocketListener());
    }
    private class SocketListener extends WebSocketListener{
        @Override
        public void onOpen(WebSocket webSocket, okhttp3.Response response) {
            super.onOpen(webSocket, response);
            getActivity().runOnUiThread(()->{
                //Toast.makeText(getContext(), "Friends Connection Successful!", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            super.onMessage(webSocket, text);
            getActivity().runOnUiThread(()->{
                Friend friend = new Friend("","","","","","","","","");
                try {
                    JSONObject jsonObject = new JSONObject(text);
                    friend.setFriendId(jsonObject.getString("friendId"));
                    if(getUserId().equals(friend.getFriendId())){
                        //.makeText(getContext(), "yes", Toast.LENGTH_SHORT).show();
                        initializeFriendList();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            });
        }
    }
    private void initializeFriendList(){
        getAllFriend(new CustomCallback() {
            @Override
            public void onSuccess(ArrayList<Friend> getFriends) {
                getAllUserAsFriend(getFriends, new AsFriendCallback() {
                    @Override
                    public void onSuccess(ArrayList<Friend> friends, ArrayList<Friend> users) {
                        ArrayList<Friend> thisSetterFriends = mergeFriendList(friends,users);
                        initRecyclerView(thisSetterFriends);
                        initSearch(thisSetterFriends);
                        Log.d(TAG, "onSuccess: 1");
                    }

                    @Override
                    public void onNoFriend(ArrayList<Friend> friends) {
                        for(int i = 0; i < friends.size(); i++){
                            friends.get(i).setState(1);
                        }
                        initRecyclerView(friends);
                        initSearch(friends);
                        Log.d(TAG, "onNoFriend: 2");
                    }

                    @Override
                    public void onFailure() {

                    }
                });
            }
            @Override
            public void onFailure() {
                ArrayList<Friend> failureFriend = new ArrayList<>();
                getAllUserAsFriend(failureFriend, new AsFriendCallback() {
                    @Override
                    public void onSuccess(ArrayList<Friend> friends, ArrayList<Friend> users) {
                        for(int i = 0; i < users.size(); i++){
                            users.get(i).setState(2);
                        }
                        initRecyclerView(users);
                        initSearch(users);
                        Log.d(TAG, "onSuccess: 3");
                    }

                    @Override
                    public void onNoFriend(ArrayList<Friend> friends) {
                        initRecyclerView(friends);
                        Log.d(TAG, "onNoFriend: 4");
                    }

                    @Override
                    public void onFailure() {

                    }
                });
            }
        });
    }

    private ArrayList<Friend> mergeFriendList(ArrayList<Friend> friends, ArrayList<Friend> users){
        ArrayList<Friend> thisSetterFriends = friends;
        for(int i = 0; i < friends.size(); i++){
            friends.get(i).setState(1);
        }
        for(int i = 0; i < users.size(); i++){
            users.get(i).setState(2);
        }
        for(int i = 0; i < friends.size(); i++)
            for(int j = 0; j < users.size(); j++)
            {
                if (users.get(j).getUserId().equals(friends.get(i).getFriendId())) {
                    thisSetterFriends.get(i).setState(0);
                }
            }
        for (int i = 0; i < users.size(); i++){
            int count = 0;
            for(int j = 0; j < friends.size(); j++)
            {
                if (friends.get(j).getFriendId().equals(users.get(i).getUserId())) {
                    count++;
                }
            }
            if(count == 0)
            {
                Log.d("count", "mergeFriendList: yes");
                thisSetterFriends.add(users.get(i));
            }
        }
        return  thisSetterFriends;
    }
    private void initSearch(ArrayList<Friend> friends){
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isHave = false;
                int container = -1;
                searchName = searchText.getText().toString();
                //Toast.makeText(getContext(), searchName, Toast.LENGTH_SHORT).show();
                for(int i=0; i<friends.size(); i++){
                    if(searchName.equals(friends.get(i).getFriendName()))
                    {
                        isHave = true;
                        container = i;
                    }
                }
                if(!isHave){
                    for(int i=0; i<friends.size(); i++){
                        if(searchName.equals(friends.get(i).getUserName()))
                        {
                            isHave = true;
                            container = i;
                        }
                    }
                }
                if(isHave){
                    Friend friend = friends.get(container);
                    if(friend.getState() == 0){
                        startChattingActivity(friend);
                    }else if(friend.getState() == 1){
                        Snackbar.make(root, "Please wait to be added as friend", Snackbar.LENGTH_SHORT).show();
                    }else{
                        Snackbar.make(root, "Please add as a friend first", Snackbar.LENGTH_SHORT).show();
                    }
                }
                else {
                    //Toast.makeText(getContext(), "Please type right name", Toast.LENGTH_SHORT).show();
                    Snackbar.make(root, "Please type right name", Snackbar.LENGTH_SHORT).show();
                    searchText.setText("");
                }
            }
        });
    }
    private void initView(){
        friendsRecView = binding.usersRecView;
        search = binding.include2.searchImage;
        searchText = binding.include2.searchEditText;
    }

    private void initRecyclerView(ArrayList<Friend> thisFriends){
        adapter = new FriendsRevViewAdapter(getContext());
        adapter.setFriends(thisFriends);
        adapter.setWebSocket(webSocket);
        friendsRecView.setAdapter(adapter);
        friendsRecView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
    }

    //initial menu in toolbar
    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu,@NotNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.friend_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.friend_menu_add:
                startAddFriendActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void startAddFriendActivity(){
        Intent intent = new Intent(getContext(),AddFriendActivity.class);
        startActivity(intent);
    }

    private void startChattingActivity(Friend friend) {
        Intent intent = new Intent(getContext(), ChattingActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("friend",friend);
        intent.putExtras(bundle);
        startActivity(intent);

    }

    private String getUserId(){

        SharedPreferences pref = getContext().getSharedPreferences("swordrunnerpref", Context.MODE_PRIVATE);
        String currentId = pref.getString("id","").toString();
        return currentId;
    }
    private void deleteMessage(String totalId){
        Message message = new Message("","","","","","",false);
        message.setTotalId(totalId);
        Call<Message> call = Client.get(getContext(),false).create(MessageRes.class).deleteMessage(message);
        call.enqueue(new Callback<Message>() {
            @Override
            public void onResponse(Call<Message> call, Response<Message> response) {
                //Toast.makeText(getContext(), "delete success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Message> call, Throwable t) {

            }
        });
    }
    private void deleteFriend(String friendId, String userId){
        Friend friend = new Friend(friendId,userId,"","","","","","","");
        Call<Friend> call = Client.get(getContext(),false).create(FriendRes.class).deleteFriend(friend);
        call.enqueue(new Callback<Friend>() {
            @Override
            public void onResponse(Call<Friend> call, Response<Friend> response) {
                //Toast.makeText(getContext(), "delete success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Friend> call, Throwable t) {

            }
        });
    }

    //get totalId with currentId and friendId
    private String getTotalId(String currentId, String friendId){
        String totalId = null;
        int compare = currentId.compareTo(friendId);
        if(compare > 0){
            //that is, initCurrentId > initFriendId
            totalId = friendId + currentId;
        }else{
            // initCurrentId < initFriendId
            totalId = currentId + friendId;
        }
        return totalId;
    }

    private void getAllFriend(CustomCallback customCallback){

        //String id = getUserId();
        Friend friend = new Friend("",id,"","","","","","","");
        Call<ArrayList<Friend>> call = Client.get(getContext(),false).create(FriendRes.class).showAllFriends(friend);
        call.enqueue(new Callback<ArrayList<Friend>>() {
            @Override
            public void onResponse(Call<ArrayList<Friend>> call, Response<ArrayList<Friend>> response) {
                if(response.code() == 200){
                    ArrayList<Friend> friends = response.body();
                    customCallback.onSuccess(friends);
                }else if(response.code() == 403){
                    customCallback.onFailure();
                }
            }
            @Override
            public void onFailure(Call<ArrayList<Friend>> call, Throwable t) {
            }
        });
    }

    private void getAllUserAsFriend(ArrayList<Friend> friendUsers,AsFriendCallback asFriendCallback) {
        Friend friend = new Friend(id,"","","","","","","","");
        Call<ArrayList<Friend>> call = Client.get(getContext(),false).create(FriendRes.class).showUserAsFriend(friend);
        call.enqueue(new Callback<ArrayList<Friend>>() {
            @Override
            public void onResponse(Call<ArrayList<Friend>> call, Response<ArrayList<Friend>> response) {
                if(response.code() == 200){
                    asFriendCallback.onSuccess(friendUsers,response.body());
                }else if(response.code() == 403){
                    asFriendCallback.onNoFriend(friendUsers);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Friend>> call, Throwable t) {

            }
        });
    }

    public interface CustomCallback{
        void onSuccess(ArrayList<Friend> friends);
        void onFailure();
    }

    public interface CheckCallback{
        void onSuccess(boolean isFriendBoth, int position);
        void onFailure();
    }

    public interface AsFriendCallback{
        void onSuccess(ArrayList<Friend> friends, ArrayList<Friend> users);
        void onNoFriend(ArrayList<Friend> friends);
        void onFailure();
    }

}