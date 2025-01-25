package com.example.communityforum;

public class User {
    private String userId;
    private String userName;
    private String email;
    private String profilePictureUrl;

    public User()
    {}


    public User(String userId, String userName, String email, String profilePictureUrl) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.profilePictureUrl = profilePictureUrl;
    }

    public User(String userId, String userName, String email) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.profilePictureUrl = null;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;

    }
}