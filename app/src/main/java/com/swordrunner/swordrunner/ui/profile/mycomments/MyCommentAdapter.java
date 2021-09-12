package com.swordrunner.swordrunner.ui.profile.mycomments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.Utils.StartOneActivities;
import com.swordrunner.swordrunner.data.model.Comment;
import com.swordrunner.swordrunner.data.model.User;
import com.swordrunner.swordrunner.ui.profile.OthersProfileActivity;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import de.hdodenhof.circleimageview.CircleImageView;


public class MyCommentAdapter extends RecyclerView.Adapter<MyCommentAdapter.ViewHolder> {

    private LinkedList<Comment> comments=new LinkedList<>();
    private Context context;
    private ArrayList<User> commenters= new ArrayList<>();
    private User user;
    private StartOneActivities startOneActivities;
    private Integer itemCount=0;


    String avatarimgurl=null;

    public MyCommentAdapter(Context context) {
        this.context=context;
        startOneActivities=new StartOneActivities();
    }

    /**
     * TO init essential parameters(exact commenters to a arraylist<User>)
     * @param user
     * @return false:user or user's comments is empty
     */
    public boolean InitMyCommentAdapter(User user){
        if (user == null||user.getComments().isEmpty()) {
            return false;
        }else{
            comments=user.getComments();
            exactCommentersList(user);
            notifyDataSetChanged();
            return true;
        }
    }

    public void setUser(User user) {
        this.user = user;
        notifyDataSetChanged();
    }

    private void exactCommentersList(User user){
        LinkedList<Comment> commentsExacted=null;
        String commenterID;
        String commenterName;
        String commenterAvatarUrl;

        commentsExacted=user.getComments();
        for (int i = 0; i < commentsExacted.size(); i++) {
            commenterID=commentsExacted.get(i).getCommenterID();
            commenterName=commentsExacted.get(i).getCommenter();
            commenterAvatarUrl=commentsExacted.get(i).getUrl();
            commenters.add(new User(commenterID,commenterName,commenterAvatarUrl));

        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.comment.setText(comments.get(position).getCommentContent());
        holder.username.setText(comments.get(position).getCommenter());
        holder.date.setText(dateFormat(comments.get(position).getDate()));
        holder.ratingBar.setRating(comments.get(position).getRate());
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startOneActivities.startProfileActivity(context,commenters.get(position));
            }
        });
        avatarimgurl=comments.get(position).getUrl();
        if (avatarimgurl==null||avatarimgurl.equals(""))
            Glide.with(context)
                    .asBitmap()
                    .load(R.drawable.unknown)
                    .into(holder.avatars);
        else{
            Glide.with(context)
                    .asBitmap()
                    .load(avatarimgurl)
                    .into(holder.avatars);
        }
    }

    @Override
    public int getItemCount() {
        if (itemCount!=0&&itemCount<comments.size()){
            return itemCount;
        }else{
            return comments.size();
        }
    }

    public void setItemCount(Integer sum){
        this.itemCount=sum;
    }

    public void setComments(LinkedList<Comment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView username=null;
        private TextView comment=null;
        private TextView date=null;
        private LinearLayout parent=null;
        private CircleImageView avatars;
        private RatingBar ratingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.vt_username_mycomment);
            comment=itemView.findViewById(R.id.vt_comment);
            date=itemView.findViewById(R.id.vt_date_mycomment);
            avatars=itemView.findViewById(R.id.avatarImg_comment);
            parent=itemView.findViewById(R.id.item_comment_layout);
            ratingBar = itemView.findViewById(R.id.rb_comment);
        }
    }

    public String dateFormat(Date timeofComment){
        Date currentTime=new Date();
        String pattern="yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf1=new SimpleDateFormat(pattern);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        if (timeofComment==null){
            return "1971-01-01";
        }
        long nweek = 1000 * 24 * 60 * 60*7;
        long nday = 1000 * 24 * 60 * 60;
        long nhour = 1000 * 60 * 60;
        long nminute = 1000 * 60;
        long nusecond = 1000;

        // calculate the differ in millisecond
        long diff=currentTime.getTime()-timeofComment.getTime();
        Log.d("cal date", "dateFormat: "
                +"currentTime:"+sdf1.format(currentTime.getTime())
                +" - "
                +"Time of Comment:"+sdf1.format(timeofComment.getTime())
                +" = "
                +diff);
        long week=diff/nweek;
        // calculate the differ in day
        long day = diff %nweek/ nday;
        // calculate the differ in hours
        long hour = diff % nday / nhour;
        // calculate the differ in minutes
        long min = diff % nday % nhour / nminute;
        // calculate the differ in seconds
        long sec = diff % nday % nhour % nminute / nusecond;
        if (week<1){
            if (day<1){
                if (hour<1){
                    if (min<1){
                        return String.valueOf(sec)+" seconds ago";
                    }
                    return String.valueOf(min)+" minutes ago";
                }else {
                    return String.valueOf(hour)+" hours ago";
                }

            }else {
                return (String.valueOf(day)+" days ago");
            }

        }else if (week<4){
            return ( String.valueOf(week) + "weeks ago");

        }
        else {
            return simpleDateFormat.format(timeofComment);

        }
    }
}
