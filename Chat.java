package com.chatapp.pingme;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

@IgnoreExtraProperties
public class Chat {
    
    @PropertyName("chatId")
    private String chatId;
    
    @PropertyName("participants")
    private java.util.List<String> participants;
    
    @PropertyName("lastMessage")
    private String lastMessage;
    
    @PropertyName("lastMessageTimestamp")
    private long lastMessageTimestamp;
    
    @PropertyName("lastMessageSenderId")
    private String lastMessageSenderId;
    
    @PropertyName("unreadCount")
    private java.util.Map<String, Integer> unreadCount;
    
    @PropertyName("createdAt")
    private long createdAt;
    
    @PropertyName("updatedAt")
    private long updatedAt;
    
    @PropertyName("isActive")
    private boolean isActive;

    // Default constructor required for Firestore
    public Chat() {}

    public Chat(String chatId, java.util.List<String> participants) {
        this.chatId = chatId != null ? chatId : "";
        this.participants = participants != null ? participants : new java.util.ArrayList<>();
        this.lastMessage = "";
        this.lastMessageTimestamp = System.currentTimeMillis();
        this.lastMessageSenderId = "";
        this.unreadCount = new java.util.HashMap<>();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isActive = true;
        
        // Initialize unread count for all participants
        for (String participant : this.participants) {
            this.unreadCount.put(participant, 0);
        }
    }

    // Getters
    public String getChatId() {
        return chatId != null ? chatId : "";
    }

    public java.util.List<String> getParticipants() {
        return participants != null ? participants : new java.util.ArrayList<>();
    }

    public String getLastMessage() {
        return lastMessage != null ? lastMessage : "";
    }

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public String getLastMessageSenderId() {
        return lastMessageSenderId != null ? lastMessageSenderId : "";
    }

    public java.util.Map<String, Integer> getUnreadCount() {
        return unreadCount != null ? unreadCount : new java.util.HashMap<>();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public boolean isActive() {
        return isActive;
    }

    // Setters
    public void setChatId(String chatId) {
        this.chatId = chatId != null ? chatId : "";
    }

    public void setParticipants(java.util.List<String> participants) {
        this.participants = participants != null ? participants : new java.util.ArrayList<>();
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage != null ? lastMessage : "";
        this.updatedAt = System.currentTimeMillis();
    }

    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.updatedAt = System.currentTimeMillis();
    }

    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId != null ? lastMessageSenderId : "";
    }

    public void setUnreadCount(java.util.Map<String, Integer> unreadCount) {
        this.unreadCount = unreadCount != null ? unreadCount : new java.util.HashMap<>();
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    // Utility methods
    public void updateLastMessage(String message, String senderId) {
        this.lastMessage = message != null ? message : "";
        this.lastMessageSenderId = senderId != null ? senderId : "";
        this.lastMessageTimestamp = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public void incrementUnreadCount(String userId) {
        if (unreadCount == null) {
            unreadCount = new java.util.HashMap<>();
        }
        int currentCount = unreadCount.getOrDefault(userId, 0);
        unreadCount.put(userId, currentCount + 1);
    }

    public void resetUnreadCount(String userId) {
        if (unreadCount != null) {
            unreadCount.put(userId, 0);
        }
    }

    public int getUnreadCountForUser(String userId) {
        if (unreadCount != null) {
            return unreadCount.getOrDefault(userId, 0);
        }
        return 0;
    }

    public String getOtherParticipant(String currentUserId) {
        if (participants != null) {
            for (String participant : participants) {
                if (!participant.equals(currentUserId)) {
                    return participant;
                }
            }
        }
        return "";
    }

    @Override
    public String toString() {
        return "Chat{" +
                "chatId='" + chatId + '\'' +
                ", participants=" + participants +
                ", lastMessage='" + lastMessage + '\'' +
                ", lastMessageTimestamp=" + lastMessageTimestamp +
                ", isActive=" + isActive +
                '}';
    }
}