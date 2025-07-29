package com.chatapp.pingme;

public class firebasemodel {

    private String name;
    private String image;
    private String uid;
    private String status;
    private String email;

    public firebasemodel() {
        // Default constructor required for calls to DataSnapshot.getValue(firebasemodel.class)
    }

    public firebasemodel(String name, String image, String uid, String status) {
        this.name = name != null ? name : "";
        this.image = image != null ? image : "";
        this.uid = uid != null ? uid : "";
        this.status = status != null ? status : "Offline";
    }

    public firebasemodel(String name, String image, String uid, String status, String email) {
        this.name = name != null ? name : "";
        this.image = image != null ? image : "";
        this.uid = uid != null ? uid : "";
        this.status = status != null ? status : "Offline";
        this.email = email != null ? email : "";
    }

    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name != null ? name : "";
    }

    public String getImage() {
        return image != null ? image : "";
    }

    public void setImage(String image) {
        this.image = image != null ? image : "";
    }

    public String getUid() {
        return uid != null ? uid : "";
    }

    public void setUid(String uid) {
        this.uid = uid != null ? uid : "";
    }

    public String getStatus() {
        return status != null ? status : "Offline";
    }

    public void setStatus(String status) {
        this.status = status != null ? status : "Offline";
    }

    public String getEmail() {
        return email != null ? email : "";
    }

    public void setEmail(String email) {
        this.email = email != null ? email : "";
    }
}