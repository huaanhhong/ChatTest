package com.example.huaanhhong.chattest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by huaanhhong on 19/08/2017.
 */

public class ChatUtils {

    public static boolean isHighBandwidthConnection(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected() && isConnectionFast(info.getType(),info.getSubtype()));
    }
    private static boolean isConnectionFast(int type, int subType){
        if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return false;
            }
        }
        //in doubt, assume connection is good.
        return true;
    }

    public static final List<LinphoneCall> getLinphoneCalls(LinphoneCore lc) {
        // return a modifiable list
        return new ArrayList<LinphoneCall>(Arrays.asList(lc.getCalls()));
    }
    public static final List<LinphoneCall> getCallsInState(LinphoneCore lc, Collection<LinphoneCall.State> states) {
        List<LinphoneCall> foundCalls = new ArrayList<LinphoneCall>();
        for (LinphoneCall call : getLinphoneCalls(lc)) {
            if (states.contains(call.getState())) {
                foundCalls.add(call);
            }
        }
        return foundCalls;
    }
    public static boolean isCallEstablished(LinphoneCall call) {
        if (call == null) {
            return false;
        }

        LinphoneCall.State state = call.getState();

        return isCallRunning(call) ||
                state == LinphoneCall.State.Paused ||
                state == LinphoneCall.State.PausedByRemote ||
                state == LinphoneCall.State.Pausing;
    }
    public static boolean isCallRunning(LinphoneCall call)
    {
        if (call == null) {
            return false;
        }

        LinphoneCall.State state = call.getState();

        return state == LinphoneCall.State.Connected ||
                state == LinphoneCall.State.CallUpdating ||
                state == LinphoneCall.State.CallUpdatedByRemote ||
                state == LinphoneCall.State.StreamsRunning ||
                state == LinphoneCall.State.Resuming;
    }
}

