package com.example.myapplication.Models;

public class Chats {
    public String chat ,time ;

    public Chats(){ }

    public Chats(String chat, String time) {
        this.chat = chat;
        this.time = time;
    }

    public String getChat() {
        return chat;
    }

    public void setChat(String chat) {
        this.chat = chat;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
