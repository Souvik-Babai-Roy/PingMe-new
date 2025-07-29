package com.chatapp.pingme;

public class userprofile {

    private String username;
    private String userUID;

    public userprofile() {
        // Default constructor required for calls to DataSnapshot.getValue(userprofile.class)
    }

    public userprofile(String username, String userUID) {
        this.username = username != null ? username : "";
        this.userUID = userUID != null ? userUID : "";
    }

    public String getUsername() {
        return username != null ? username : "";
    }

    public void setUsername(String username) {
        this.username = username != null ? username : "";
    }

    public String getUserUID() {
        return userUID != null ? userUID : "";
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID != null ? userUID : "";
    }
}