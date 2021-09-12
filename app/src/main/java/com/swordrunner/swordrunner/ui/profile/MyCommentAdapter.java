package com.swordrunner.swordrunner.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.data.model.Comment;
import com.swordrunner.swordrunner.data.model.User;
import com.swordrunner.swordrunner.ui.chatting.ChattingActivity;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;


public class MyCommentAdapter extends RecyclerView.Adapter<MyCommentAdapter.ViewHolder> {

    private ArrayList<Comment> comments=new ArrayList<>();
    private Context context;
    private ArrayList<User> users= new ArrayList<>();

    public MyCommentAdapter(Context context) {
        this.context=context;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment,parent,false);
        ViewHolder holder = new ViewHolder(view);
        Log.d("vh","created successfully");
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.comment.setText(comments.get(position).getCommentContent());
        holder.username.setText(comments.get(position).getCommenter());
        holder.date.setText(DateFormatYMD(comments.get(position).date));
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProfileActivity(users.get(position));
            }
        });
        Glide.with(context)
                .asBitmap()
                .load(comments.get(position).url)
                .into(holder.avatars);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView username=null;
        private TextView comment=null;
        private TextView date=null;
        private ConstraintLayout parent=null;
        private CircleImageView avatars;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.vt_username_mycomment);
            comment=itemView.findViewById(R.id.vt_comment);
            date=itemView.findViewById(R.id.vt_date_mycomment);
            avatars=itemView.findViewById(R.id.avatarImg_comment);
            parent=itemView.findViewById(R.id.item_comment_layout);
        }
    }

    public String DateFormatYMD(String date){
        String dateFormat=null;
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        if (date==null){
            return "1971-01-01";
        }
        dateFormat=simpleDateFormat.format(date);

        return dateFormat;
    }

    private void startProfileActivity(User user) {
        Intent intent = new Intent(context, OthersProfileActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", user);
        intent.putExtras(bundle);
        context.startActivity(intent);

    }

}
