package com.example.huaanhhong.chattest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;


import com.example.huaanhhong.chattest.Linphone.LinphoneService;

import static android.content.Intent.ACTION_MAIN;

public class WelcomeActivity extends AppCompatActivity {

    private Handler mHandler;
    private ServiceWaitThread mThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mHandler = new Handler();

        if (LinphoneService.isReady()) {
            onServiceReady();
            android.util.Log.i("CNN", "launcher service ready");
        } else {
            // start linphone as background
            android.util.Log.i("CNN", "launcher servie not ready");
            startService(new Intent(ACTION_MAIN).setClass(this, LinphoneService.class));
            mThread = new ServiceWaitThread();
            mThread.start();
        }
    }

    protected void onServiceReady() {
        final Class<? extends Activity> classToStart;

            classToStart = LoginActivity.class;
            android.util.Log.i("CNN", "launcher_onservice ready linphone");

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent().setClass(WelcomeActivity.this, classToStart).setData(getIntent().getData()));
                finish();
            }
        }, 1000);
    }


    private class ServiceWaitThread extends Thread {
        public void run() {
            while (!LinphoneService.isReady()) {
                try {
                    sleep(30);
                } catch (InterruptedException e) {
                    throw new RuntimeException("waiting thread sleep() has been interrupted");
                }
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onServiceReady();
                }
            });
            mThread = null;
        }
    }
}
