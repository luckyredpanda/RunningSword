package com.swordrunner.swordrunner.ui.profile;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.api.Client;
import com.swordrunner.swordrunner.api.service.GameRes;
import com.swordrunner.swordrunner.api.service.UserRes;
import com.swordrunner.swordrunner.data.model.Game;
import com.swordrunner.swordrunner.data.model.User;
import com.swordrunner.swordrunner.data.model.UserGame;
import com.swordrunner.swordrunner.databinding.ActivityMyGamelistBinding;
import com.swordrunner.swordrunner.ui.home.HomeFragment;

import java.util.ArrayList;
import java.util.LinkedList;

public class MyGamelistActivity extends AppCompatActivity {
    private ActivityMyGamelistBinding binding;
    private ArrayList<Game> gameList = new ArrayList<>();
    private MyGameAdapter gameAdapter;
    private String userId = null;
    RecyclerView mygameRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_my_gamelist);
        gameAdapter = new MyGameAdapter(this);
        binding=ActivityMyGamelistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Game list");

        userId = getIntent().getStringExtra("userId");
        mygameRecyclerView=findViewById(R.id.mygameRecyclerView);

        mygameRecyclerView.setAdapter(gameAdapter);
        mygameRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        initGameList();
    }
    private void initGameList(){
        gameAdapter.setGameList(gameList);
        getData(userId, new ListUserInfoCallback() {
            @Override
            public void onSuccess(User user) {
                int length = gameAdapter.getItemCount();
                LinkedList<UserGame> myUserGame = user.getGames();
                for(int i = 0; i < myUserGame.size(); i++){
                    String gameId = null;
                    gameId = myUserGame.get(i).getGameId();
                    getGame(gameId,length,i, new ListGetGameCallback() {
                        @Override
                        public void onSuccess(Game game, int length, int i) {
                            gameAdapter.addGame(game);
                            if(i == 0 && length !=0)
                            {
                                for(int j = 0; j < length; j++)
                                {
                                    gameAdapter.removePosition(j);
                                }
                            }
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
    }
    private void getData(String id, ListUserInfoCallback userInfoCallback) {
        Call<User> call = Client.get(getApplicationContext()).create(UserRes.class).get(id);
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
    private void getGame(String GameId,int length, int i, ListGetGameCallback getGameCallback){
        Game thisGame = new Game();
        thisGame.setId(GameId);
        Call<Game> call = Client.get(getApplicationContext(),false).create(GameRes.class).searchGame(thisGame);
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

    public interface ListUserInfoCallback{
        void onSuccess(User user);
        void onFailure();
    }
    public interface ListGetGameCallback{
        void onSuccess(Game game, int length, int i);
        void onFailure();
    }


}