package com.example.huaanhhong.chatvn.Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by anhhong on 20/06/2017.
 */

public class APIRetrofit {

    public Retrofit postjson() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://mobisci-lab.com:8090/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit;
    }
}
