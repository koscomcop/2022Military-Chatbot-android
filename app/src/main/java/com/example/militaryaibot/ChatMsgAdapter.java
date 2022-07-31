package com.example.militaryaibot;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatMsgAdapter extends RecyclerView.Adapter<ChatMsgAdapter.ChatViewHolder> {

    private List<ChatMessage> chatMessages = new ArrayList<>();

    private boolean chatMe = true;
    private int deviceWidth = 0;
    private int clickedPos = -1;

    public ChatMsgAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public ChatMsgAdapter(List<ChatMessage> chatMessages, int deviceWidth) {
        this.chatMessages = chatMessages;
        this.deviceWidth = deviceWidth;
    }

    //--CREATE SINGLE CHAT_FROM_ME VIEWHOLDER AND RETURN--
    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case ChatType.ChatMe:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_chat_me, parent, false);
                return new ChatMeViewHolder(view);
            case ChatType.ChatYou:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_chat_you, parent, false);
                return new ChatYouViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {

        ChatMessage currentMsg = chatMessages.get(position);

        //--SET MESSAGE INFO--
        if(holder instanceof ChatMeViewHolder){
            ChatMeViewHolder chatMeViewHolder = (ChatMeViewHolder)holder;
            chatMeViewHolder.chatTextMe.setText(currentMsg.getMessage());
            chatMeViewHolder.chatTimeMe.setText(currentMsg.getTime().substring(11, 16));
            if(currentMsg.isFavorited()) chatMeViewHolder.chatFavoriteMe.setVisibility(View.VISIBLE);
        }
        else {
            ChatYouViewHolder chatYouViewHolder = (ChatYouViewHolder) holder;
            chatYouViewHolder.chatTextYou.setText(currentMsg.getMessage());
            chatYouViewHolder.chatTimeYou.setText(currentMsg.getTime().substring(11, 16));
            if(currentMsg.isFavorited()) chatYouViewHolder.chatFavoriteYou.setVisibility(View.VISIBLE);
        }


    }

    @Override
    public void onViewRecycled(@NonNull ChatViewHolder holder) {
        if(holder instanceof ChatMeViewHolder) {
            ChatMeViewHolder chatMeViewHolder = (ChatMeViewHolder)holder;
            chatMeViewHolder.chatFavoriteMe.setVisibility(View.INVISIBLE);
        }
        else {
            ChatYouViewHolder chatYouViewHolder = (ChatYouViewHolder) holder;
            chatYouViewHolder.chatFavoriteYou.setVisibility(View.INVISIBLE);
        }
    }

    //--GET CURRENT POS--
    public int getClickedPos() {
        return clickedPos;
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    //--GET CHAT MESSAGE TYPE : IS CHAT FROM ME?--
    public boolean getIsChatMe(){
        return chatMe;
    }

    @Override
    public int getItemViewType(int position) {
        return chatMessages.get(position).getChatType();
    }

    //--DEFINE VIEWHOLDER INTERFACE
    public class ChatViewHolder extends RecyclerView.ViewHolder {
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    //--DEFINE SINGLE CHAT_FROM_ME HOLDER--
    public class ChatMeViewHolder extends ChatViewHolder implements View.OnCreateContextMenuListener {

        private TextView chatTextMe;
        private TextView chatTimeMe;
        private ImageView chatFavoriteMe;

        public ChatMeViewHolder(@NonNull View itemView) {
            super(itemView);

            chatTextMe = (TextView) itemView.findViewById(R.id.chat_text_me);
            chatTimeMe = (TextView) itemView.findViewById(R.id.chat_time_me);
            chatFavoriteMe = (ImageView) itemView.findViewById(R.id.chat_favorite_me);
            chatFavoriteMe.setVisibility(View.INVISIBLE);

            //--MESSAGE MAX WIDTH
            chatTextMe.setMaxWidth(deviceWidth);

            //--ADD CONTEXT MENU FOR SINGLE CHAT--
            itemView.setOnCreateContextMenuListener(this);

        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuInflater menuInflater = ((Activity) v.getContext()).getMenuInflater();

            if(getIsChatMe()) {
                //--SINGLE CHAT_FROM_ME CONTEXT MENU : DELETE, ADD FAVORITE
                menuInflater.inflate(R.menu.single_chat_me_ctxmenu, menu);
            }

            //--SAVE CLICKED POS--
            clickedPos = getAdapterPosition();

            return;
        }
    }

    //--DEFINE SINGLE CHAT_FROM_YOU HOLDER--
    public class ChatYouViewHolder extends ChatViewHolder implements View.OnCreateContextMenuListener {

        private TextView chatTextYou;
        private TextView chatTimeYou;
        private ImageView chatFavoriteYou;

        public ChatYouViewHolder(@NonNull View itemView) {
            super(itemView);

            chatTextYou = (TextView) itemView.findViewById(R.id.chat_text_you);
            chatTimeYou = (TextView) itemView.findViewById(R.id.chat_time_you);
            chatFavoriteYou = (ImageView) itemView.findViewById(R.id.chat_favorite_you);
            chatFavoriteYou.setVisibility(View.INVISIBLE);

            //--MESSAGE MAX WIDTH
            chatTextYou.setMaxWidth(deviceWidth);

            //--ADD CONTEXT MENU FOR SINGLE CHAT--
            itemView.setOnCreateContextMenuListener(this);

        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuInflater menuInflater = ((Activity) v.getContext()).getMenuInflater();

            if(getIsChatMe()) {
                //--SINGLE CHAT_FROM_ME CONTEXT MENU : DELETE, ADD FAVORITE
                menuInflater.inflate(R.menu.single_chat_me_ctxmenu, menu);
            }

            //--SAVE CLICKED POS--
            clickedPos = getAdapterPosition();

            return;
        }
    }



}
