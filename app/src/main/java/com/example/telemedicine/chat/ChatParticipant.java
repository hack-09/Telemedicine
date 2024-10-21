package com.example.telemedicine.chat;

public class ChatParticipant {
    private String id; // ID of the participant
    private String name; // Name of the participant

    public ChatParticipant(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
