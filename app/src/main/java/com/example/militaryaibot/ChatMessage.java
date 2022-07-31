package com.example.militaryaibot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

public class ChatMessage implements Comparable<ChatMessage> {

    private String message;
    private ChatUser from;
    private String time;
    private int seenCount;
    private String id;
    private boolean favorited = false;

    public ChatMessage(String message, ChatUser from, String time, int seenCount) {
        this.message = message;
        this.from = from;
        this.time = time;
        this.seenCount = seenCount;

        this.id = time.replaceAll("[ !@#$%^&*(),.?\\\":{}|<>]", "");
    }
    public ChatMessage(String id, String message, ChatUser from, String time, int seenCount, boolean favorited) {
        this.message = message;
        this.from = from;
        this.time = time;
        this.seenCount = seenCount;
        this.favorited = favorited;
        this.id = id;
    }
    private int chatType = ChatType.ChatMe;

    public int getChatType() {
        return chatType;
    }
    public void setChatType(int chatType) {
        this.chatType = chatType;
    }

    public String getMessage() {
        return message;
    }

    public ChatUser getFrom() {
        return from;
    }

    public String getTime() {
        return time;
    }

    public long getSeenCount() {
        return seenCount;
    }

    public String getId() { return id; }

    public boolean isFavorited() {
        return favorited;
    }

    //--UPDATE IS CHAT FAVORITED AND RETURN WHETHER DATA ACTUALLY CHANGED--
    public boolean setFavorite(boolean favorite) {
        this.favorited = favorite;

        return (this.favorited || favorite);
    }

    @Override
    public int compareTo(ChatMessage chatMessage)
    {
        return this.id.compareTo(chatMessage.getId());
    }
}
