package com.swordrunner.swordrunner.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.data.model.Game;

import java.util.Date;
import java.util.List;

public class GamesAdapter extends RecyclerView.Adapter<GamesAdapter.ViewHolder> {

    private List<Game> localDataSet;

    public void setLocalDataSet(List<Game> localDataSet){
        this.localDataSet = localDataSet;
        notifyDataSetChanged();
    }
    public void addGame(Game game){
        localDataSet.add(game);
        notifyDataSetChanged();
    }
    public List<Game> returnGame(){
        return localDataSet;
    }
    public void removePosition(int i){
        localDataSet.remove(i);
        notifyDataSetChanged();
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView, participantsView, timeView, distanceView;

        public TextView getParticipantsView() {
            return participantsView;
        }

        public TextView getTimeView() {
            return timeView;
        }

        public TextView getDistanceView() {
            return distanceView;
        }

        public ViewHolder(View view) {
            super(view);

            titleView = (TextView) view.findViewById(R.id.game_title);
            participantsView = (TextView) view.findViewById(R.id.game_participants);
            timeView = (TextView) view.findViewById(R.id.game_time);
            distanceView = (TextView) view.findViewById(R.id.game_distance);
        }

        public TextView getTitleView() {
            return titleView;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet Game[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public GamesAdapter(List<Game> dataSet, Context context) {
        localDataSet = dataSet;
        ctx = context;
    }
    public GamesAdapter() {
    }

    private Context ctx;

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.games_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getTitleView().setText(localDataSet.get(position).name);
        viewHolder.getTimeView().setText(ctx.getResources().getQuantityString(R.plurals.days, localDataSet.get(position).progress.days,localDataSet.get(position).progress.days));
        viewHolder.getParticipantsView().setText(localDataSet.get(position).participants.size() + " participants");
        viewHolder.getDistanceView().setText(localDataSet.get(position).progress.distance + " / " + localDataSet.get(position).distance);

        if (localDataSet.get(position).participants.size() < 2) {
            viewHolder.getParticipantsView().setVisibility(View.GONE);
            viewHolder.getTimeView().setVisibility(View.GONE);
        } else {
            viewHolder.getParticipantsView().setVisibility(View.VISIBLE);
            viewHolder.getTimeView().setVisibility(View.VISIBLE);
        }

        if (twoDaysAgo(localDataSet.get(position).progress.lastDay)) {
            viewHolder.getDistanceView().setText("Game Over");
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    /**
     * returns if date (last game update) was more than two days ago.
     * @param fromDate
     * @return
     */
    private boolean twoDaysAgo(Date fromDate)
    {
        Date toDate = new Date();
        return (int)( (toDate.getTime() - fromDate.getTime()) / (1000 * 60 * 60 * 24)) >= 2;
    }
}
