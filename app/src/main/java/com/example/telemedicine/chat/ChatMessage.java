package com.example.telemedicine.chat;

public class ChatMessage {
    private String messageId;
    private String senderId;
    private String message;
    private long timestamp;

    public ChatMessage() { }

    public ChatMessage(String messageId, String message, String senderId, long timestamp) {
        this.messageId = messageId;
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    public String getSenderId() {
        return senderId;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

