package com.example.huaanhhong.chattest.Linphone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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

public class CallOutgoingActivity extends AppCompatActivity implements View.OnClickListener {

    private static CallOutgoingActivity instance;

    private ImageView contactPicture, micro, speaker, hangUp;
    private LinphoneCall mCall;
    private LinphoneCoreListenerBase mListener;
    private boolean isMicMuted, isSpeakerEnabled;

    public static CallOutgoingActivity instance() {
        return instance;
    }

    public static boolean isInstanciated() {
        return instance != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_outgoing);

        android.util.Log.i("CNN","calloutgoing_oncreate");
        isMicMuted = false;
        isSpeakerEnabled = false;

        micro = (ImageView) findViewById(R.id.micro);
        micro.setOnClickListener(this);
        speaker = (ImageView) findViewById(R.id.speaker);
        speaker.setOnClickListener(this);

        hangUp = (ImageView) findViewById(R.id.outgoing_hang_up);
        hangUp.setOnClickListener(this);

        mListener = new LinphoneCoreListenerBase(){
            @Override
            public void callState(LinphoneCore lc, LinphoneCall call, LinphoneCall.State state, String message) {
                if (LinphoneManager.getLc().getCallsNb() == 0) {
                    finish();
                    return;
                }
                if (call == mCall && LinphoneCall.State.CallEnd == state) {
                    Toast.makeText(CallOutgoingActivity.this,"Decline!",Toast.LENGTH_LONG).show();
                    finish();
                }

                if (call == mCall && (LinphoneCall.State.Connected == state)){
                    if (!MainActivity.isInstanciated()) {
                        return;
                    }
                    final LinphoneCallParams remoteParams = mCall.getRemoteParams();
                    if (remoteParams != null && remoteParams.getVideoEnabled() && LinphonePreferences.instance().shouldAutomaticallyAcceptVideoRequests()) {
                        MainActivity.instance().startVideoActivity(mCall);
                        android.util.Log.i("CNN","calloutgoing_startvideoActivity");
                    } else {
                        MainActivity.instance().startIncallActivity(mCall);
                        android.util.Log.i("CNN","calloutgoing_startaudioActivity");
                    }
                    finish();
                    return;
                }
            }
        };
        instance = this;

    }

    @Override
    protected void onResume() {
        super.onResume();
        android.util.Log.i("CNN","calloutgoing_onresume");
        instance = this;
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.addListener(mListener);
        }
        mCall=null;

        // Only one call ringing at a time is allowed
        if (LinphoneManager.getLcIfManagerNotDestroyedOrNull() != null) {
            List<LinphoneCall> calls = ChatUtils.getLinphoneCalls(LinphoneManager.getLc());
            for (LinphoneCall call : calls) {
                LinphoneCall.State cstate = call.getState();
                if (LinphoneCall.State.OutgoingInit == call.getState() || LinphoneCall.State.OutgoingProgress == call.getState() || LinphoneCall.State.OutgoingRinging == call.getState() || LinphoneCall.State.OutgoingEarlyMedia == call.getState()) {
                    mCall = call;
                    break;
                }
                if (LinphoneCall.State.StreamsRunning == cstate) {
                    if (!MainActivity.isInstanciated()) {
                        return;
                    }
                    MainActivity.instance().startIncallActivity(mCall);
                    finish();
                    return;
                }
            }

            if (mCall == null) {
                Log.e("Couldn't find outgoing call");
                finish();
                return;
            }
        }
    }

    @Override
    protected void onPause() {
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
    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.micro) {
            isMicMuted = !isMicMuted;
            android.util.Log.i("CNN","calloutgoing_click micro "+isMicMuted);
            if (isMicMuted) {
                micro.setImageResource(R.drawable.micro_selected);
            } else {
                micro.setImageResource(R.drawable.micro_default);
            }
            LinphoneManager.getLc().muteMic(isMicMuted);
        }
        if (id == R.id.speaker) {
            isSpeakerEnabled = !isSpeakerEnabled;
            android.util.Log.i("CNN","calloutgoing_click_speaker "+isSpeakerEnabled);
            if (isSpeakerEnabled) {
                speaker.setImageResource(R.drawable.speaker_selected);
            } else {
                speaker.setImageResource(R.drawable.speaker_default);
            }
            LinphoneManager.getLc().enableSpeaker(isSpeakerEnabled);
        }
        if (id == R.id.outgoing_hang_up) {
            decline();
        }
    }
        private void decline() {
            LinphoneManager.getLc().terminateCall(mCall);
        }
}
