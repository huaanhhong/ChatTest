package com.example.huaanhhong.chattest.Linphone;

import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.huaanhhong.chattest.ChatUtils;
import com.example.huaanhhong.chattest.MainActivity;
import com.example.huaanhhong.chattest.R;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallParams;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreListenerBase;
import org.linphone.mediastream.Log;

import java.util.List;

public class CallIncomingActivity extends AppCompatActivity implements LinphoneSliders.LinphoneSliderTriggered{

    private static CallIncomingActivity instance;

    private boolean isActive;
    private float answerX;
    private float declineX;
    private ImageView accept,decline;
    private LinphoneCall mCall;
    private LinphoneCoreListenerBase mListener;

    public static CallIncomingActivity instance() {
        return instance;
    }

    public static boolean isInstanciated() {
        return instance != null;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_incoming);
        android.util.Log.i("CNN","callincoming_oncreate");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // set this flag so this activity will stay in front of the keyguard
        int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
        getWindow().addFlags(flags);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            isActive = pm.isInteractive();
        } else {
            isActive = pm.isScreenOn();
        }

        final int screenWidth = getResources().getDisplayMetrics().widthPixels;

//        acceptUnlock = (LinearLayout) findViewById(R.id.acceptUnlock);
//        declineUnlock = (LinearLayout) findViewById(R.id.declineUnlock);
//
        accept = (ImageView) findViewById(R.id.accept);
        decline = (ImageView) findViewById(R.id.decline);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isActive) {
                    answer();
                } else {
                    decline.setVisibility(View.GONE);
//                    acceptUnlock.setVisibility(View.VISIBLE);
                }
            }
        });
//
        if(!isActive) {
            accept.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    android.util.Log.i("CNN","callincoming_accept");
                    float curX;
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
//                            acceptUnlock.setVisibility(View.VISIBLE);
                            decline.setVisibility(View.GONE);
                            answerX = motionEvent.getX();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            curX = motionEvent.getX();
                            if((answerX - curX) >= 0)
                                view.scrollBy((int) (answerX - curX), view.getScrollY());
                            answerX = curX;
                            if (curX < screenWidth/4) {
                                answer();
                                return true;
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            view.scrollTo(0, view.getScrollY());
                            decline.setVisibility(View.VISIBLE);
//                            acceptUnlock.setVisibility(View.GONE);
                            break;
                    }
                    return true;
                }
            });

            decline.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    android.util.Log.i("CNN","callincoming_decline");
                    float curX;
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
//                            declineUnlock.setVisibility(View.VISIBLE);
                            accept.setVisibility(View.GONE);
                            declineX = motionEvent.getX();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            curX = motionEvent.getX();
                            view.scrollBy((int) (declineX - curX), view.getScrollY());
                            declineX = curX;
                            Log.w(curX);
                            if (curX > (screenWidth/2)){
                                decline();
                                return true;
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            view.scrollTo(0, view.getScrollY());
                            accept.setVisibility(View.VISIBLE);
//                            declineUnlock.setVisibility(View.GONE);
                            break;

                    }
                    return true;
                }
            });
        }
//
        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isActive) {
                    decline();
                } else {
                    accept.setVisibility(View.GONE);
//                    acceptUnlock.setVisibility(View.VISIBLE);
                }
            }
        });


        mListener = new LinphoneCoreListenerBase(){
            @Override
            public void callState(LinphoneCore lc, LinphoneCall call, LinphoneCall.State state, String message) {
                if (call == mCall && LinphoneCall.State.CallEnd == state) {
                    android.util.Log.i("CNN","callincoming_mlisten-calend");
                    finish();
                }
                if (state == LinphoneCall.State.StreamsRunning) {
                    android.util.Log.i("CNN","callincoming_mlisten_streaming");
                    // The following should not be needed except some devices need it (e.g. Galaxy S).
                    LinphoneManager.getLc().enableSpeaker(LinphoneManager.getLc().isSpeakerEnabled());
                }
            }
        };


//        super.onCreate(savedInstanceState);
        instance = this;
    }

    @Override
    protected void onResume() {
        android.util.Log.i("CNN", "callincoming_onresume");
        super.onResume();
        instance = this;
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.addListener(mListener);
        }

        if (LinphoneManager.getLcIfManagerNotDestroyedOrNull() != null) {
            List<LinphoneCall> calls = ChatUtils.getLinphoneCalls(LinphoneManager.getLc());
            for (LinphoneCall call : calls) {
                if (LinphoneCall.State.IncomingReceived == call.getState()) {
                    mCall = call;
                    break;
                }
            }
        }
        if (mCall == null) {
            Log.e("Couldn't find incoming call");
            finish();
            return;
        }
    }

    @Override
    protected void onPause() {
        android.util.Log.i("CNN","callincoming_onpause");
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.removeListener(mListener);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    private void decline() {
        android.util.Log.i("CNN","callincoming_decline");
        LinphoneManager.getLc().terminateCall(mCall);
        finish();
    }

    private void answer() {
        android.util.Log.i("CNN","callincoming_answer");
        LinphoneCallParams params = LinphoneManager.getLc().createCallParams(mCall);

        boolean isLowBandwidthConnection = !ChatUtils.isHighBandwidthConnection(LinphoneService.instance().getApplicationContext());

        if (params != null) {
            params.enableLowBandwidth(isLowBandwidthConnection);
        }else {
            Log.e("Could not create call params for call");
        }

        if (params == null || !LinphoneManager.getInstance().acceptCallWithParams(mCall, params)) {
            // the above method takes care of Samsung Galaxy S
            Toast.makeText(this, R.string.couldnt_accept_call, Toast.LENGTH_LONG).show();
        } else {
            if (!MainActivity.isInstanciated()) {
                return;
            }
            final LinphoneCallParams remoteParams = mCall.getRemoteParams();
            if (remoteParams != null && remoteParams.getVideoEnabled() && LinphonePreferences.instance().shouldAutomaticallyAcceptVideoRequests()) {
                MainActivity.instance().startVideoActivity(mCall);
            } else {
                MainActivity.instance().startIncallActivity(mCall);
            }
        }
    }


    @Override
    public void onLeftHandleTriggered() {

    }

    @Override
    public void onRightHandleTriggered() {

    }
}
