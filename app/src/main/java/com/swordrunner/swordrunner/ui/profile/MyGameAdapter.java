package com.swordrunner.swordrunner.ui.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.data.model.Game;
import com.swordrunner.swordrunner.data.model.User;

import java.util.ArrayList;


public class MyGameAdapter extends RecyclerView.Adapter<MyGameAdapter.ViewHolder> {

    private ArrayList<Game> gameList= new ArrayList<>();
    private Context ctx;

    public MyGameAdapter(Context context) {
        ctx = context;
    }

    @NonNull

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mygame, parent, false);
        ViewHolder holder=new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(gameList.get(position).name);
        holder.distance.setText(gameList.get(position).distance + " km");
        holder.days.setText(ctx.getResources().getQuantityString(R.plurals.days, gameList.get(position).progress.days,gameList.get(position).progress.days));
        int length = gameList.get(position).participants.size();
        if(length > 1){
            holder.myImage.setImageResource(R.drawable.multi_player);
        }else{
            holder.myImage.setImageResource(R.drawable.single_player);
        }
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    public void addGame(Game game){
        gameList.add(game);
        notifyDataSetChanged();
    }

    public void removePosition(int i){
        gameList.remove(i);
        notifyDataSetChanged();
    }

    public void setGameList(ArrayList<Game> games){
        this.gameList = games;
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView name, days, distance;
        private ImageView myImage;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);
            name = itemView.findViewById(R.id.tv_Game_Name);
            days = itemView.findViewById(R.id.tv_survival_days);
            distance = itemView.findViewById(R.id.tv_distance);
            myImage = itemView.findViewById(R.id.imageView_mode);
        }
    }



}
