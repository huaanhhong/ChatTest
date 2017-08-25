package com.example.huaanhhong.chatvn.Retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by anhhong on 20/06/2017.
 */

public class TokenRequest {

    @SerializedName("username")
    @Expose
    private String mUsername;
    @SerializedName("password")
    @Expose
    private String mPassword;
    @SerializedName("profile")
    @Expose
    private Profile mProfile;

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        this.mUsername = username;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        this.mPassword = password;
    }

    public Profile getProfile() {
        return mProfile;
    }

    public void setProfile(Profile profile) {
        this.mProfile = profile;
    }

    public static class Profile {

        @SerializedName("mobile")
        @Expose
        private String mMobile;
        @SerializedName("email")
        @Expose
        private String mEmail;

        public Profile(String mMobile, String mEmail) {
            this.mMobile = mMobile;
            this.mEmail = mEmail;
        }

        public String getMobile() {
            return mMobile;
        }

        public void setMobile(String mobile) {
            this.mMobile = mobile;
        }

        public String getEmail() {
            return mEmail;
        }

        public void setEmail(String email) {
            this.mEmail = email;
        }

    }
}
