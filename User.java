package com.chatapp.pingme;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

@IgnoreExtraProperties
public class User {
    
    @PropertyName("name")
    private String name;
    
    @PropertyName("email")
    private String email;
    
    @PropertyName("image")
    private String image;
    
    @PropertyName("uid")
    private String uid;
    
    @PropertyName("status")
    private String status;
    
    @PropertyName("lastSeen")
    private long lastSeen;
    
    @PropertyName("isOnline")
    private boolean isOnline;
    
    @PropertyName("createdAt")
    private long createdAt;
    
    @PropertyName("bio")
    private String bio;

    // Default constructor required for Firestore
    public User() {}

    public User(String name, String email, String image, String uid) {
        this.name = name != null ? name.trim() : "";
        this.email = email != null ? email.trim() : "";
        this.image = image != null ? image : "";
        this.uid = uid != null ? uid : "";
        this.status = "Online";
        this.isOnline = true;
        this.lastSeen = System.currentTimeMillis();
        this.createdAt = System.currentTimeMillis();
        this.bio = "";
    }

    // Getters
    public String getName() {
        return name != null ? name : "";
    }

    public String getEmail() {
        return email != null ? email : "";
    }

    public String getImage() {
        return image != null ? image : "";
    }

    public String getUid() {
        return uid != null ? uid : "";
    }

    public String getStatus() {
        return status != null ? status : "Offline";
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getBio() {
        return bio != null ? bio : "";
    }

    // Setters
    public void setName(String name) {
        this.name = name != null ? name.trim() : "";
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim() : "";
    }

    public void setImage(String image) {
        this.image = image != null ? image : "";
    }

    public void setUid(String uid) {
        this.uid = uid != null ? uid : "";
    }

    public void setStatus(String status) {
        this.status = status != null ? status : "Offline";
        this.isOnline = "Online".equals(status);
        this.lastSeen = System.currentTimeMillis();
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public void setOnline(boolean online) {
        this.isOnline = online;
        this.status = online ? "Online" : "Offline";
        this.lastSeen = System.currentTimeMillis();
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setBio(String bio) {
        this.bio = bio != null ? bio : "";
    }

    // Utility methods
    public boolean isValid() {
        return uid != null && !uid.isEmpty() && 
               name != null && !name.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", uid='" + uid + '\'' +
                ", status='" + status + '\'' +
                ", isOnline=" + isOnline +
                '}';
    }
}