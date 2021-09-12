package com.swordrunner.swordrunner.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.Utils.StartOneActivities;
import com.swordrunner.swordrunner.data.model.Friend;
import com.swordrunner.swordrunner.data.model.Home;
import com.swordrunner.swordrunner.data.model.User;
import com.swordrunner.swordrunner.ui.profile.OthersProfileActivity;

import java.util.List;

public class HighscoreAdapter extends RecyclerView.Adapter<HighscoreAdapter.ViewHolder> {

    private List<Friend> localDataSet;
    private Context ctx;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameView, daysView;
        private final ImageView avatar;
        private final View item;

        public View getItem() {
            return item;
        }

        public TextView getNameView() {
            return nameView;
        }

        public TextView getDaysView() {
            return daysView;
        }

        public ImageView getAvatar() {
            return avatar;
        }

        public ViewHolder(View view) {
            super(view);

            nameView = view.findViewById(R.id.highscore_name);
            daysView = view.findViewById(R.id.highscore_days);
            avatar = view.findViewById(R.id.highscore_avatar);

            item = view;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet Game[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public HighscoreAdapter(List<Friend> dataSet, Home home, Context context) {
        localDataSet = dataSet;
        ctx = context;

        // add self to highscore
        Friend self = new Friend(home.name, home.days, home.points, home.userAvatar);
        localDataSet.add(self);

        // sort by points then days
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            localDataSet.sort((o1, o2) -> {
                int points = Integer.compare(o2.points, o1.points);
                if (points == 0) {
                    return Integer.compare(o2.days, o1.days);
                }
                return points;
            });
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.highscore_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getNameView().setText(localDataSet.get(position).friendName);
        viewHolder.getDaysView().setText(ctx.getResources().getQuantityString(R.plurals.points, localDataSet.get(position).points,localDataSet.get(position).points)
                + " \u2022 "
                + ctx.getResources().getQuantityString(R.plurals.days, localDataSet.get(position).days,localDataSet.get(position).days));

        if (localDataSet.get(position).highlight)
            viewHolder.getNameView().setTextColor(ResourcesCompat.getColor(ctx.getResources(), R.color.red, null));

        if(!localDataSet.get(position).getFriendAvatar().equals("")){
            Glide.with(ctx)
                    .asBitmap()
                    .load(localDataSet.get(position).getFriendAvatar())
                    .into(viewHolder.getAvatar());
        }

        if (!localDataSet.get(position).highlight) {
            viewHolder.getItem().setOnClickListener(v -> {
                String friendID = localDataSet.get(position).getFriendId();
                String friendName = localDataSet.get(position).getFriendName();
                String avatar = localDataSet.get(position).getFriendAvatar();

                User friend = new User();
                friend.setId(friendID);
                friend.setName(friendName);
                friend.setAvatarUrl(avatar);

                Intent intent = new Intent(ctx, OthersProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("user", friend);
                intent.putExtras(bundle);
                ctx.startActivity(intent);
            });
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
