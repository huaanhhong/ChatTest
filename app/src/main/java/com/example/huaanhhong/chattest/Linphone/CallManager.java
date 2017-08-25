package com.example.huaanhhong.chattest.Linphone;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallParams;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.mediastream.Log;

/**
 * Created by huaanhhong on 19/08/2017.
 */

public class CallManager {

    private static CallManager instance;

    private CallManager() {}

    public static final synchronized CallManager getInstance() {
        android.util.Log.i("CNN", "CallManagerment_instance");
        if (instance == null) instance = new CallManager();
        return instance;
    }
    private BandwidthManager bm() {
        android.util.Log.i("CNN", "CallManagerment_BandwidthManager");
        return BandwidthManager.getInstance();
    }

    public void inviteAddress(LinphoneAddress lAddress, boolean videoEnabled, boolean lowBandwidth) throws LinphoneCoreException {

        android.util.Log.i("CNN", "CallManagerment_invitAddress");
        LinphoneCore lc = LinphoneManager.getLc();

        LinphoneCallParams params = lc.createCallParams(null);
        bm().updateWithProfileSettings(lc, params);

        if (videoEnabled && params.getVideoEnabled()) {
            params.setVideoEnabled(true);
        } else {
            params.setVideoEnabled(false);
        }

        if (lowBandwidth) {
            params.enableLowBandwidth(true);
            Log.d("Low bandwidth enabled in call params");
        }

        lc.inviteAddressWithParams(lAddress, params);
    }
    boolean reinviteWithVideo() {
        android.util.Log.i("CNN", "CallManagerment_renviteWithVideo");
        LinphoneCore lc =  LinphoneManager.getLc();
        LinphoneCall lCall = lc.getCurrentCall();
        if (lCall == null) {
            Log.e("Trying to reinviteWithVideo while not in call: doing nothing");
            return false;
        }
        LinphoneCallParams params = lCall.getCurrentParamsCopy();

        if (params.getVideoEnabled()) return false;


        // Check if video possible regarding bandwidth limitations
        bm().updateWithProfileSettings(lc, params);

        // Abort if not enough bandwidth...
        if (!params.getVideoEnabled()) {
            return false;
        }

        // Not yet in video call: try to re-invite with video
        lc.updateCall(lCall, params);
        return true;
    }

    public void updateCall() {
        android.util.Log.i("CNN", "CallManagerment_updateCall");
        LinphoneCore lc = LinphoneManager.getLc();
        LinphoneCall lCall = lc.getCurrentCall();
        if (lCall == null) {
            Log.e("Trying to updateCall while not in call: doing nothing");
            return;
        }
        LinphoneCallParams params = lCall.getCurrentParamsCopy();
        bm().updateWithProfileSettings(lc, params);
        lc.updateCall(lCall, null);
    }

}
