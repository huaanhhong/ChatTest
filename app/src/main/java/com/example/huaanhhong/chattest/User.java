package com.example.huaanhhong.chattest;

/**
 * Created by huaanhhong on 03/08/2017.
 */

public class User {

    private String mUserId;
    private String mUserName;
    private String mUserEmail;
    private String mUrlAvatar;
    private boolean mIsOnline;

    public User(String mUserId, String mUserName, String mUserEmail, String mUrlAvatar, boolean mIsOnline) {
        this.mUserId = mUserId;
        this.mUserName = mUserName;
        this.mUserEmail = mUserEmail;
        this.mUrlAvatar = mUrlAvatar;
        this.mIsOnline = mIsOnline;
    }

    public User() {
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String mUserName) {
        this.mUserName = mUserName;
    }

    public String getUserEmail() {
        return mUserEmail;
    }

    public void setUserEmail(String mUserEmail) {
        this.mUserEmail = mUserEmail;
    }

    public String getUrlAvatar() {
        return mUrlAvatar;
    }

    public void setUrlAvatar(String mUrlAvatar) {
        this.mUrlAvatar = mUrlAvatar;
    }

    public boolean isIsOnline() {
        return mIsOnline;
    }

    public void setIsOnline(boolean mIsOnline) {
        this.mIsOnline = mIsOnline;
    }
}
