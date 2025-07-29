package com.chatapp.pingme;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

@IgnoreExtraProperties
public class Messages {

    @PropertyName("messageId")
    private String messageId;
    
    @PropertyName("message")
    private String message;
    
    @PropertyName("senderId")
    private String senderId;
    
    @PropertyName("timestamp")
    private long timestamp;
    
    @PropertyName("currenttime")
    private String currenttime;
    
    @PropertyName("messageType")
    private String messageType; // text, image, file, etc.
    
    @PropertyName("status")
    private String status; // sent, delivered, read
    
    @PropertyName("chatId")
    private String chatId;
    
    @PropertyName("edited")
    private boolean edited;
    
    @PropertyName("editedAt")
    private long editedAt;

    public Messages() {
        // Default constructor required for calls to DataSnapshot.getValue(Messages.class)
    }

    public Messages(String message, String senderId, long timestamp, String currenttime) {
        this.messageId = generateMessageId();
        this.message = message != null ? message : "";
        this.senderId = senderId != null ? senderId : "";
        this.timestamp = timestamp;
        this.currenttime = currenttime != null ? currenttime : "";
        this.messageType = "text";
        this.status = "sent";
        this.chatId = "";
        this.edited = false;
        this.editedAt = 0;
    }

    public Messages(String messageId, String message, String senderId, long timestamp, String currenttime, String chatId) {
        this.messageId = messageId != null ? messageId : generateMessageId();
        this.message = message != null ? message : "";
        this.senderId = senderId != null ? senderId : "";
        this.timestamp = timestamp;
        this.currenttime = currenttime != null ? currenttime : "";
        this.messageType = "text";
        this.status = "sent";
        this.chatId = chatId != null ? chatId : "";
        this.edited = false;
        this.editedAt = 0;
    }

    // Getters
    public String getMessageId() {
        return messageId != null ? messageId : "";
    }

    public String getMessage() {
        return message != null ? message : "";
    }

    public String getSenderId() {
        return senderId != null ? senderId : "";
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getCurrenttime() {
        return currenttime != null ? currenttime : "";
    }

    public String getMessageType() {
        return messageType != null ? messageType : "text";
    }

    public String getStatus() {
        return status != null ? status : "sent";
    }

    public String getChatId() {
        return chatId != null ? chatId : "";
    }

    public boolean isEdited() {
        return edited;
    }

    public long getEditedAt() {
        return editedAt;
    }

    // Setters
    public void setMessageId(String messageId) {
        this.messageId = messageId != null ? messageId : "";
    }

    public void setMessage(String message) {
        this.message = message != null ? message : "";
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId != null ? senderId : "";
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setCurrenttime(String currenttime) {
        this.currenttime = currenttime != null ? currenttime : "";
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType != null ? messageType : "text";
    }

    public void setStatus(String status) {
        this.status = status != null ? status : "sent";
    }

    public void setChatId(String chatId) {
        this.chatId = chatId != null ? chatId : "";
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
        if (edited) {
            this.editedAt = System.currentTimeMillis();
        }
    }

    public void setEditedAt(long editedAt) {
        this.editedAt = editedAt;
    }

    // Utility methods
    private String generateMessageId() {
        return "msg_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    public boolean isTextMessage() {
        return "text".equals(messageType);
    }

    public boolean isImageMessage() {
        return "image".equals(messageType);
    }

    public boolean isFileMessage() {
        return "file".equals(messageType);
    }

    @Override
    public String toString() {
        return "Messages{" +
                "messageId='" + messageId + '\'' +
                ", message='" + message + '\'' +
                ", senderId='" + senderId + '\'' +
                ", timestamp=" + timestamp +
                ", status='" + status + '\'' +
                '}';
    }
}