package com.swordrunner.swordrunner.ui.home;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.Utils.WebAddress;
import com.swordrunner.swordrunner.api.Client;
import com.swordrunner.swordrunner.api.service.FriendRes;
import com.swordrunner.swordrunner.api.service.GameRes;
import com.swordrunner.swordrunner.api.service.UserRes;
import com.swordrunner.swordrunner.data.model.Friend;
import com.swordrunner.swordrunner.data.model.Game;
import com.swordrunner.swordrunner.data.model.Home;
import com.swordrunner.swordrunner.data.model.User;
import com.swordrunner.swordrunner.data.model.UserGame;
import com.swordrunner.swordrunner.databinding.FragmentHomeBinding;
import com.swordrunner.swordrunner.ui.ConfirmActivity;
import com.swordrunner.swordrunner.ui.CreateGameActivity;
import com.swordrunner.swordrunner.ui.MapActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;

    private Home homeData = null;
    private List<Game> games = new ArrayList<>();
    private User currentUser;
    private List<Game> gamesFromUser = new ArrayList<>();
    private GamesAdapter gamesAdapter;
    private WebSocket webSocket;
    private String SERVER_PATH = WebAddress.WEB_SOCKET_ADDRESS;
    private static final String TAG = "HomeFragment";
    private BroadcastReceiver broadcastReceiver;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        gamesAdapter = new GamesAdapter(gamesFromUser, this.getContext());
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        binding.homeButtonRun.setOnClickListener(v -> {
            int length = gamesAdapter.getItemCount();
            if(length > 0){
                Intent intent = new Intent(HomeFragment.this.getActivity(), MapActivity.class);
                intent.putExtra("distance_today", homeData.distanceToday);
                intent.putExtra("target_distance", homeData.targetDistance);
                intent.putExtra("days", homeData.days);
                intent.putExtra("coins", homeData.coins);
                startActivity(intent);
                getActivity().finish();
            }else{
                createGame(length);
            }

        });
        binding.listGames.setAdapter(gamesAdapter);
        initItemTouchHelper();
        setHasOptionsMenu(true);
        initiateSocketConnection();
        initGameList();
        //initBroadcast();

        return root;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu,@NotNull MenuInflater inflater) {
        inflater.inflate(R.menu.create_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_menu_create_game:
                int length = gamesAdapter.getItemCount();
                createGame(length);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getHomeData();

        getData(getUserId(), new UserInfoCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                binding.homeTextDaysNum.setText("" + currentUser.getSurvivalDays());
            }

            @Override
            public void onFailure() {

            }
        });
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();
        binding = null;
    }
    private void initBroadcast(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("action.refreshGameList");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals("action.refreshGameList")){
                    initGameList();
                }
            }
        };
        requireActivity().registerReceiver(broadcastReceiver,intentFilter);
    }

    /**
     * init single or multiplayer game
     * @param length
     */
    private void createGame(int length){
        if (length == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.AlertDialog);
            builder.setTitle("You have to create your daily single player game before you start to run.");

            builder.setPositiveButton("Create", (dialog, which) -> {
                Intent intent = new Intent(HomeFragment.this.getActivity(), CreateGameActivity.class);
                intent.putExtra("isSingle", true);
                startActivity(intent);
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        } else {
            Intent intent = new Intent(HomeFragment.this.getActivity(), CreateGameActivity.class);
            intent.putExtra("isSingle", false);
            startActivity(intent);
        }

    }

    /**
     * retrieve home fragment data from server
     */
    private void getHomeData() {
        Call<Home> call = Client.get(getActivity()).create(UserRes.class).getHome();
        call.enqueue(new Callback<Home>() {
            @Override
            public void onResponse(Call<Home> call, Response<Home> response) {
                if (response.code() != 200) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), "Run could not be saved", Snackbar.LENGTH_LONG).show();
                } else {
                    homeData = response.body();
                    binding.homeTextDistance.setText("You have run " + String.format("%.2f", homeData.distance / 1000) + " km");
                    binding.homeTextDaysNum.setText("" + homeData.days);
                    binding.homeTextTime.setText("in " + homeData.period + " h");
                    if (homeData.safehouse) binding.imageViewSafehouse.setVisibility(View.VISIBLE);
                    else binding.imageViewSafehouse.setVisibility(View.GONE);
                }
                getAllFriend();
            }

            @Override
            public void onFailure(Call<Home> call, Throwable t) {
                //Snackbar.make(getActivity().findViewById(android.R.id.content), "Something went wrong", Snackbar.LENGTH_SHORT).show();
                getAllFriend();
            }
        });
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
            getActivity().runOnUiThread(()->{
            });
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            super.onMessage(webSocket, text);
            getActivity().runOnUiThread(()->{
                try {
                    JSONObject jsonObject = new JSONObject(text);
                    String myId = jsonObject.getString("friendGameId");
                    String userId = jsonObject.getString("userGameId");
                    boolean isDelete = jsonObject.getBoolean("deleteGame");
                    boolean stopRun = jsonObject.getBoolean("stopRun");

                    if(getUserId().equals(myId) && getUserId().equals(userId) && !isDelete && !stopRun){

                        Intent intent = new Intent(getContext(), ConfirmActivity.class);
                        intent.putExtra("type", 0);
                        startActivity(intent);
                        getActivity().finish();
                    }else if(getUserId().equals(myId) && !getUserId().equals(userId) && !isDelete && !stopRun){
                        Intent intent = new Intent(getContext(),ConfirmActivity.class);
                        intent.putExtra("type", 1);
                        startActivity(intent);
                        getActivity().finish();
                    }else if(getUserId().equals(myId) && getUserId().equals(userId) && isDelete && !stopRun){
                        Intent intent = new Intent(getContext(),ConfirmActivity.class);
                        intent.putExtra("type", 2);
                        startActivity(intent);
                        getActivity().finish();
                    }else if(getUserId().equals(myId) && getUserId().equals(userId) && !isDelete && stopRun){
                        Intent intent = new Intent(getContext(),ConfirmActivity.class);
                        intent.putExtra("type", 3);
                        startActivity(intent);
                        getActivity().finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }
    }
    //get info of user
    private void getData(String id, UserInfoCallback userInfoCallback) {
        Call<User> call = Client.get(getContext()).create(UserRes.class).get(id);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                userInfoCallback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }

        });
    }
    private void getData(String gameId, String id,Game myGame, QuitGameCallback quitGameCallback) {
        Call<User> call = Client.get(getContext()).create(UserRes.class).get(id);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                quitGameCallback.onSuccess(response.body(), gameId, myGame);
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }

        });
    }
    private void quitGame(Game myGame,String userId){
        List<String> participants = myGame.participants;
        participants.remove(userId);
        myGame.participants = participants;
        Call<Game> call = Client.get(getContext(),false).create(GameRes.class).changeParticipant(myGame);
        call.enqueue(new Callback<Game>() {
            @Override
            public void onResponse(Call<Game> call, Response<Game> response) {
                if(response.code() == 200){
                    Game thisGame = response.body();
                    for(int i = 0; i < thisGame.participants.size(); i++){
                        JSONObject jsonObject = new JSONObject();
                        try {
                            if(!getUserId().equals(thisGame.participants.get(i)))
                            {
                                jsonObject.put("friendGameId",thisGame.participants.get(i));
                                jsonObject.put("userGameId",thisGame.participants.get(i));
                                jsonObject.put("deleteGame",true);
                                jsonObject.put("stopRun",false);
                                webSocket.send(jsonObject.toString());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Game> call, Throwable t) {

            }
        });
    }
    private void quitGameUser(String gameId, User myUser){
        LinkedList<UserGame> myGames = myUser.getGames();
        int judge = -1;
        for(int i = 0; i < myGames.size(); i++){
            if(myGames.get(i).getGameId().equals(gameId))
                judge = i;
        }
        Log.d(TAG, "quitGameUser: "+ myUser.getGames().size());
        if(judge >= 0)
            myGames.remove(judge);

        myUser.setGames(myGames);
        Log.d(TAG, "quitGameUser: "+myUser.getGames().size());
        binding.homeTextMissions.setText(getResources().getQuantityString(R.plurals.game, myUser.getGames().size(),myUser.getGames().size()));
        Call<User> call = Client.get(getContext(),false).create(UserRes.class).createGame(myUser);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.code() == 200){

                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }
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
                        List<Game> thisGame = gamesAdapter.returnGame();
                        String gameId = thisGame.get(position).getId();
                        Game myGame = thisGame.get(position);
                        thisGame.remove(position);
                        gamesAdapter.setLocalDataSet(thisGame);
                        getData(gameId, getUserId(),myGame, new QuitGameCallback() {
                            @Override
                            public void onSuccess(User user, String gameId,Game thisGame) {
                                quitGameUser(gameId,user);
                                quitGame(thisGame,getUserId());
                                List<String> participants = thisGame.participants;
                                participants.remove(getUserId());
                                thisGame.participants = participants;
                                if(thisGame.participants.size() == 1){
                                    getData(thisGame.getId(), thisGame.participants.get(0), thisGame, new QuitGameCallback() {
                                        @Override
                                        public void onSuccess(User user, String gameId, Game secondGame) {
                                            quitGameUser(gameId,user);
                                            quitGame(secondGame,secondGame.participants.get(0));
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
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftActionIcon(R.drawable.ic_action_delete)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(getContext(),R.color.red))
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(binding.listGames);
    }
    private void initGameList(){

        getData(getUserId(), new UserInfoCallback() {
            @Override
            public void onSuccess(User user) {
                int length = gamesAdapter.getItemCount();
                LinkedList<UserGame> myUserGame = user.getGames();
                for(int i = 0; i < myUserGame.size(); i++){
                    String gameId = null;
                    gameId = myUserGame.get(i).getGameId();
                    getGame(gameId,length,i, new GetGameCallback() {
                        @Override
                        public void onSuccess(Game game, int length, int i) {
                            gamesAdapter.addGame(game);
                            if(i == 0 && length !=0)
                            {
                                for(int j = 0; j < length; j++)
                                {
                                    gamesAdapter.removePosition(j);
                                }
                            }
                        }

                        @Override
                        public void onFailure() {

                        }
                    });
                }
                binding.homeTextMissions.setText(getResources().getQuantityString(R.plurals.game, myUserGame.size(), myUserGame.size()));
            }

            @Override
            public void onFailure() {

            }
        });
    }
    private void getGame(String GameId,int length, int i, GetGameCallback getGameCallback){
        Game thisGame = new Game();
        thisGame.setId(GameId);
        Call<Game> call = Client.get(getActivity(),false).create(GameRes.class).searchGame(thisGame);
        call.enqueue(new Callback<Game>() {
            @Override
            public void onResponse(Call<Game> call, Response<Game> response) {
                if(response.code() == 200)
                    getGameCallback.onSuccess(response.body(),length,i);
            }

            @Override
            public void onFailure(Call<Game> call, Throwable t) {
                    getGameCallback.onFailure();
            }
        });
    }


    private void getAllFriend() {

        //String id = getUserId();
        Friend friend = new Friend("", "", "", "", "", "", "", "", "");
        friend.setUserId(getUserId());
        Call<ArrayList<Friend>> call = Client.get(getActivity(), false).create(FriendRes.class).showAllFriends(friend);
        call.enqueue(new Callback<ArrayList<Friend>>() {
            @Override
            public void onResponse(Call<ArrayList<Friend>> call, Response<ArrayList<Friend>> response) {
                if (response.code() == 200) {
                    ArrayList<Friend> friends = response.body();
                    HighscoreAdapter highscoreAdapter = new HighscoreAdapter(friends, homeData, HomeFragment.this.getContext());
                    binding.listHighscore.setAdapter(highscoreAdapter);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Friend>> call, Throwable t) {

            }
        });
    }

    private String getUserId(){
        SharedPreferences pref = getActivity().getSharedPreferences("swordrunnerpref", Context.MODE_PRIVATE);
        String currentId = pref.getString("id","").toString();
        return currentId;
    }
    public interface UserInfoCallback{
        void onSuccess(User user);
        void onFailure();
    }
    public interface GetGameCallback{
        void onSuccess(Game game, int length, int i);
        void onFailure();
    }
    public interface QuitGameCallback{
        void onSuccess(User user, String gameId, Game game);
        void onFailure();
    }
}