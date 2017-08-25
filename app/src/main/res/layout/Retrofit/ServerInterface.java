package com.example.huaanhhong.chatvn.Retrofit;

import com.google.gson.JsonElement;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by anhhong on 20/06/2017.
 */

public interface ServerInterface {

    @POST("api/signup")
    Call<JsonElement> postsignup(@Body com.example.huaanhhong.chatvn.Retrofit.TokenRequest tokenRequest);
    @POST("api/signin")
    Call<JsonElement> postsignin(@Body com.example.huaanhhong.chatvn.Retrofit.TokenRequest tokenRequest);
    @POST("api/update_profile")
    Call<JsonElement> postupdate(@Body com.example.huaanhhong.chatvn.Retrofit.TokenRequest tokenRequest);
}
