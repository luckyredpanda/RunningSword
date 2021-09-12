package com.swordrunner.swordrunner.ui.chatting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.swordrunner.swordrunner.R;
import com.swordrunner.swordrunner.data.model.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageListAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 0;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 1;

    private Context mContext;
    private ArrayList<JSONObject> mMessageList = new ArrayList<>();

    public MessageListAdapter(Context context, ArrayList<JSONObject> messageList) {
        mContext = context;
        mMessageList = messageList;
        notifyDataSetChanged();
    }
    public void SetList(ArrayList<JSONObject> messageList){
        mMessageList = messageList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        JSONObject message = mMessageList.get(position);
        try {
            if (message.getBoolean("isSent")) {
                // If the current user is the sender of the message
                return VIEW_TYPE_MESSAGE_SENT;
            } else {
                // If some other user sent the message
                return VIEW_TYPE_MESSAGE_RECEIVED;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Inflates the appropriate layout according to the ViewType.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.box_chatting_first, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.box_chatting_second, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        JSONObject message = mMessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message,position);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message,position);
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, dateText;

        public SentMessageHolder(View itemView) {
            super(itemView);

            dateText = (TextView) itemView.findViewById(R.id.text_chat_date_me);
            messageText = (TextView) itemView.findViewById(R.id.text_chat_message_me);
            timeText = (TextView) itemView.findViewById(R.id.text_chat_timestamp_me);
        }

        void bind(JSONObject message, int position) {
            if(position==0){
                //dateText.setVisibility(View.VISIBLE);
            }
            try {
                messageText.setText(message.getString("message"));
                timeText.setText(message.getString("time"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText, dateText;
        CircleImageView profileImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            dateText = (TextView)itemView.findViewById(R.id.text_chat_date_other);
            messageText = (TextView)itemView.findViewById(R.id.text_chat_message_other);
            timeText = (TextView)itemView.findViewById(R.id.text_chat_timestamp_other);
            nameText = (TextView)itemView.findViewById(R.id.text_chat_user_other);
            profileImage = (CircleImageView)itemView.findViewById(R.id.image_chat_profile_other);
        }

        void bind(JSONObject message, int position) {
            if(position==0){
                //dateText.setVisibility(View.VISIBLE);
            }
            try {
                messageText.setText(message.getString("message"));
                timeText.setText(message.getString("time"));
                nameText.setText(message.getString("name"));
                if(!message.getString("avatar").equals("")){
                    Glide.with(mContext)
                            .asBitmap()
                            .load(message.getString("avatar"))
                            .into(profileImage);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}