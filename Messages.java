package com.chatapp.pingme;

public class Messages {

    private String message;
    private String senderId;
    private long timestamp;
    private String currenttime;

    public Messages() {
        // Default constructor required for calls to DataSnapshot.getValue(Messages.class)
    }

    public Messages(String message, String senderId, long timestamp, String currenttime) {
        this.message = message != null ? message : "";
        this.senderId = senderId != null ? senderId : "";
        this.timestamp = timestamp;
        this.currenttime = currenttime != null ? currenttime : "";
    }

    public String getMessage() {
        return message != null ? message : "";
    }

    public void setMessage(String message) {
        this.message = message != null ? message : "";
    }

    public String getSenderId() {
        return senderId != null ? senderId : "";
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId != null ? senderId : "";
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCurrenttime() {
        return currenttime != null ? currenttime : "";
    }

    public void setCurrenttime(String currenttime) {
        this.currenttime = currenttime != null ? currenttime : "";
    }
}