package com.swordrunner.swordrunner.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.Utils.UserOwnInfo;
import com.swordrunner.swordrunner.Utils.WebAddress;
import com.swordrunner.swordrunner.api.Client;
import com.swordrunner.swordrunner.api.service.FriendRes;
import com.swordrunner.swordrunner.api.service.GameRes;
import com.swordrunner.swordrunner.api.service.UserRes;
import com.swordrunner.swordrunner.data.model.Friend;
import com.swordrunner.swordrunner.data.model.Game;
import com.swordrunner.swordrunner.data.model.GenericResponse;
import com.swordrunner.swordrunner.data.model.User;
import com.swordrunner.swordrunner.data.model.UserGame;
import com.swordrunner.swordrunner.databinding.ActivityCreateGameBinding;
import com.swordrunner.swordrunner.ui.friends.AddFriendActivity;
import com.swordrunner.swordrunner.ui.friends.FriendsFragment;
import com.swordrunner.swordrunner.ui.run.ChooseFriendAdapter;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;

import androidx.core.content.ContextCompat;
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

public class CreateGameActivity extends AppCompatActivity {

    private ActivityCreateGameBinding binding;
    private Game game = new Game();

    private ArrayList<Friend> friends = new ArrayList<>();
    private ArrayList<Friend> participantFriend = new ArrayList<>();
    private ArrayList<String> friendsNames = new ArrayList<>();
    private RecyclerView participantRecyclerView;
    private ChooseFriendAdapter adapter;
    private WebSocket webSocket;
    private String SERVER_PATH = WebAddress.WEB_SOCKET_ADDRESS;
    private static final String TAG = "CreateGameActivity";
    private boolean isSingle = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupActionBar();

        binding = ActivityCreateGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        participantRecyclerView = findViewById(R.id.recycler_new_game);

        adapter = new ChooseFriendAdapter(participantFriend);
        participantRecyclerView.setAdapter(adapter);
        participantRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        initItemTouchHelper();
        isSingle = getIntent().getBooleanExtra("isSingle",isSingle);
        if (isSingle){
            binding.layoutChoose.setVisibility(View.GONE);
            binding.editName.setText("SinglePlay");
            binding.editName.setEnabled(false);
        }



        binding.gameTextPeriod.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateGameActivity.this,R.style.AlertDialog);
            builder.setTitle("Set period")
                    .setItems(R.array.periods, (dialog, which) -> {
                        String selection = getResources().getStringArray(R.array.periods)[which];
                        binding.gameTextPeriod.setText(selection);
                        //game.period = selection;
                    });
            builder.create().show();
        });

        binding.gameTextDistance.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateGameActivity.this,R.style.AlertDialog);
            builder.setTitle("Set distance")
                    .setItems(R.array.distances, (dialog, which) -> {
                        String selection = getResources().getStringArray(R.array.distances)[which];
                        binding.gameTextDistance.setText(selection);
                        game.distance = Integer.parseInt(selection.split(" ")[0]);
                    });
            builder.create().show();
        });
        getAllFriend();
        initAddFriend();
        initiateSocketConnection();


    }

    private void initAddFriend(){
        binding.imageViewMode.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateGameActivity.this,R.style.AlertDialog);
            builder.setTitle("Invite friends")
                    .setItems(friendsNames.toArray(new String[0]), (dialog, which) -> {
                        boolean judge = true;
                        for(int i = 0; i < game.participants.size(); i++){
                            if(game.participants.get(i).equals(friends.get(which).getFriendId())){
                                judge = false;
                            }
                        }
                        if(judge)
                        {
                            //game.participants.add(friends.get(which).getFriendId());
                            getData(friends.get(which).getFriendId(), new FriendGameCallback() {
                                @Override
                                public void onSuccess(User user) {
                                    int gameNum = user.getGames().size();
                                    Log.d(TAG, "gameNum: "+gameNum);
                                    if(gameNum < 2){
                                        ArrayList<Friend> myFriends = adapter.getFriends();
                                        myFriends.add(friends.get(which));
                                        adapter.setFriendList(myFriends);
                                    }else{
                                        Snackbar.make(findViewById(android.R.id.content), "This friend already has two games", Snackbar.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onFailure() {

                                }
                            });

                        }
                        else
                            Toast.makeText(this, "You have chosen this friend", Toast.LENGTH_SHORT).show();
                    });
            builder.create().show();
        });
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
            runOnUiThread(()->{

             });
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            super.onMessage(webSocket, text);
        }
    }

    /**
     * connect game to user
     * @param gameId
     * @param myUser
     */
    private void storeGame(String gameId, User myUser){
        LinkedList<UserGame> games = myUser.getGames();
        UserGame game = new UserGame(gameId);
        game.isSingle = isSingle;
        if(games == null)
            games = new LinkedList<>();

        games.addFirst(game);
        myUser.setGames(games);
        Call<User> call = Client.get(this,false).create(UserRes.class).createGame(myUser);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.code() == 200){
                    //Toast.makeText(CreateGameActivity.this, "Create Game Success", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });

    }

    /**
     * to remove friends
     */
    private void initItemTouchHelper(){
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull @NotNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                switch (direction){
                    case ItemTouchHelper.LEFT:
                        participantFriend.remove(position);
                        adapter.setFriendList(participantFriend);
                        initAddFriend();
                        if (participantFriend.size() == 0)
                            binding.imageView2.setImageResource(R.drawable.single_player);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftActionIcon(R.drawable.ic_action_delete)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.red))
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(participantRecyclerView);
    }

    private void setupActionBar() {
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle("Create Game");
        }
    }

    /**
     * create game on server
     * @param gameCallback
     */
    private void sendGameCreate(GameCallback gameCallback) {
        Call<Game> call = Client.get(this).create(GameRes.class).create(game);
        call.enqueue(new Callback<Game>() {
            @Override
            public void onResponse(Call<Game> call, Response<Game> response) {
                if (response.code() != 200) {
                    gameCallback.onFailure();
                    Snackbar.make(findViewById(android.R.id.content), "Game could not be saved", Snackbar.LENGTH_LONG).show();
                } else {
                    gameCallback.onSuccess(response.body());
                }
            }

            @Override
            public void onFailure(Call<Game> call, Throwable t) {
                gameCallback.onFailure();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_game_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_game_create:
                ArrayList<Friend> friends = adapter.getFriends();
                //game.period = "0 Days";
                for(int i = 0; i < friends.size(); i++ ){
                    game.participants.add(friends.get(i).getFriendId());
                }
                if(binding.editName.getText().toString().isEmpty())
                    Snackbar.make(findViewById(android.R.id.content), "Please enter a Game Name", Snackbar.LENGTH_LONG).show();
                else if(game.participants.size() == 0 && !isSingle)
                    Snackbar.make(findViewById(android.R.id.content), "Please choose at least one friend", Snackbar.LENGTH_LONG).show();

                else{
                    game.participants.add(getUserId());
                    game.name = binding.editName.getText().toString();
                    getData(getUserId(), new FriendGameCallback() {
                        @Override
                        public void onSuccess(User user) {
                            if(user.getGames().size() < 2 )
                            {
                                sendGameCreate(new GameCallback() {
                                    @Override
                                    public void onSuccess(Game thisGame) {
                                        Log.d(TAG, "gameId: "+thisGame.id);
                                        for(int i = 0; i < game.participants.size(); i++){
                                            getData(game.participants.get(i), new FriendGameCallback() {
                                                @Override
                                                public void onSuccess(User user) {
                                                    JSONObject jsonObject = new JSONObject();
                                                    try {
                                                        jsonObject.put("userGameId",getUserId());
                                                        jsonObject.put("friendGameId",user.getId());
                                                        jsonObject.put("deleteGame",false);
                                                        jsonObject.put("stopRun",false);
                                                        webSocket.send(jsonObject.toString());
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    storeGame(thisGame.id,user);
                                                }
                                                @Override
                                                public void onFailure() {

                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onFailure() {

                                    }
                                });
                                finish();
                            }
                            else {
                                Snackbar.make(findViewById(android.R.id.content), "You cannot have more than two games simultaneously", Snackbar.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure() {

                        }
                    });

                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //get info of friend user
    private void getData(String id, FriendGameCallback friendGameCallback) {
        Call<User> call = Client.get(getApplicationContext()).create(UserRes.class).get(id);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                friendGameCallback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }

        });
    }

    /**
     * get user's friends to be able to add to game
     */
    private void getAllFriend() {

        //String id = getUserId();
        Friend friend = new Friend("", "", "", "", "", "", "", "", "");
        friend.setUserId(getUserId());
        Call<ArrayList<Friend>> call = Client.get(this, false).create(FriendRes.class).showAllFriends(friend);
        call.enqueue(new Callback<ArrayList<Friend>>() {
            @Override
            public void onResponse(Call<ArrayList<Friend>> call, Response<ArrayList<Friend>> response) {
                if (response.code() == 200) {
                    friends = response.body();
                    for (Friend f: friends) {
                        friendsNames.add(f.getFriendName());
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Friend>> call, Throwable t) {

            }
        });
    }

    private String getUserId(){
        SharedPreferences pref = getSharedPreferences("swordrunnerpref", Context.MODE_PRIVATE);
        String currentId = pref.getString("id","").toString();
        return currentId;
    }
    public interface GameCallback{
        void onSuccess(Game thisGame);
        void onFailure();
    }
    public interface FriendGameCallback{
        void onSuccess(User user);
        void onFailure();
    }
}