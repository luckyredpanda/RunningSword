package com.swordrunner.swordrunner.ui.run;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.data.model.Friend;
import com.swordrunner.swordrunner.data.model.User;
import com.swordrunner.swordrunner.ui.profile.MyGameAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ChooseFriendAdapter extends RecyclerView.Adapter<ChooseFriendAdapter.ViewHolder> {
    private ArrayList<Friend> friends = new ArrayList<>();

    public ChooseFriendAdapter(ArrayList<Friend> friends){
        this.friends = friends;
    }
    public void setFriendList(ArrayList<Friend> friends){
        this.friends = friends;
        notifyDataSetChanged();
    }

    public ArrayList<Friend> getFriends(){
        return this.friends;
    }
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.participant_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(friends.get(position).getFriendName());
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView textView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.set_participants);
        }
    }
}
