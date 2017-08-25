package com.example.huaanhhong.chattest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.huaanhhong.chattest.Linphone.CallActivity;
import com.example.huaanhhong.chattest.Linphone.CallIncomingActivity;
import com.example.huaanhhong.chattest.Linphone.CallOutgoingActivity;
import com.example.huaanhhong.chattest.Linphone.DialerFragment;
import com.example.huaanhhong.chattest.Linphone.LinphoneManager;
import com.example.huaanhhong.chattest.Linphone.LinphonePreferences;
import com.example.huaanhhong.chattest.Linphone.LinphoneService;

import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreListenerBase;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.PayloadType;
import org.linphone.core.Reason;
import org.linphone.mediastream.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private static String TAG = "MainActivity";
    private TabLayout tabLayout = null;
    private ViewPager viewPager;
    public static String STR_CHAT_FRAGMENT = "CHAT";
    public static String STR_CONTACT_FRAGMENT = "CONTACT";
    public static String STR_CALL_FRAGMENT = "CALL";
    public static String STR_GROUP_FRAGMENT = "GROUP";
    public static String STR_SETTING_FRAGMENT = "SETTING";


    private ViewPagerAdapter adapter;


    private static MainActivity instance;
    public static final boolean isInstanciated() {

        android.util.Log.i("CNN", "LinphoneActivity_isInstanciated");
        return instance != null;
    }

    public static final MainActivity instance() {
        android.util.Log.i("CNN", "LinphoneActivity_instance");
        if (instance != null)
            return instance;
        throw new RuntimeException("LinphoneActivity not instantiated yet");
    }

    private OrientationEventListener mOrientationHelper;
    private static final int CALL_ACTIVITY = 19;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 201;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO_INCOMING_CALL = 203;
    private LinphoneCoreListenerBase mListener;
    private boolean permissionAsked = false;
    private boolean newProxyConfig;
    private DialerFragment dialerFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!LinphoneManager.isInstanciated()) {
//            Log.e("No service running: avoid crash by starting the launch", this.getClass().getName());
            finish();
            startActivity(getIntent().setClass(MainActivity.this, WelcomeActivity.class));
            return;
        }

        try {
            setting();
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
        }

//        DialerFragment dialerFrgment=new DialerFragment();
//        FragmentManager fragmentManager=getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
//        fragmentTransaction.replace(R.id.frame_call,dialerFrgment);
//        fragmentTransaction.addToBackStack(null);
//        fragmentTransaction.commit();

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(5);
        initTab();

        instance = this;

        mListener = new LinphoneCoreListenerBase(){


            @Override
            public void authInfoRequested(LinphoneCore lc, String realm, String username, String domain) {
                //authInfoPassword = displayWrongPasswordDialog(username, realm, domain);
                //authInfoPassword.show();
            }

            @Override
            public void registrationState(LinphoneCore lc, LinphoneProxyConfig proxy, LinphoneCore.RegistrationState state, String smessage) {
                if (state.equals(LinphoneCore.RegistrationState.RegistrationCleared)) {
                    if (lc != null) {
                        LinphoneAuthInfo authInfo = lc.findAuthInfo(proxy.getIdentity(), proxy.getRealm(), proxy.getDomain());
                        if (authInfo != null)
                            lc.removeAuthInfo(authInfo);
                    }
                }
//                refreshAccounts();
                android.util.Log.i("CNN", "LinphoneActivity_langngheregisterstate");
                if(state.equals(LinphoneCore.RegistrationState.RegistrationFailed) && newProxyConfig) {
                    newProxyConfig = false;
                    if (proxy.getError() == Reason.BadCredentials) {
                        //displayCustomToast(getString(R.string.error_bad_credentials), Toast.LENGTH_LONG);
                    }
                    if (proxy.getError() == Reason.Unauthorized) {
                        displayCustomToast(getString(R.string.error_unauthorized), Toast.LENGTH_LONG);
                    }
                    if (proxy.getError() == Reason.IOError) {
                        displayCustomToast(getString(R.string.error_io_error), Toast.LENGTH_LONG);
                    }
                }
            }

            @Override
            public void callState(LinphoneCore lc, LinphoneCall call, LinphoneCall.State state, String message) {
                android.util.Log.i("CNN", "LinphoneActivity_lang nghe callstate");
                if (state == LinphoneCall.State.IncomingReceived) {
                    if (getPackageManager().checkPermission(android.Manifest.permission.RECORD_AUDIO, getPackageName()) == PackageManager.PERMISSION_GRANTED || LinphonePreferences.instance().audioPermAsked()) {
                        startActivity(new Intent(MainActivity.instance(), CallIncomingActivity.class));
                    } else {
                        checkAndRequestPermission(android.Manifest.permission.RECORD_AUDIO, PERMISSIONS_REQUEST_RECORD_AUDIO_INCOMING_CALL);
                    }
                } else if (state == LinphoneCall.State.OutgoingInit || state == LinphoneCall.State.OutgoingProgress) {
                    if (getPackageManager().checkPermission(android.Manifest.permission.RECORD_AUDIO, getPackageName()) == PackageManager.PERMISSION_GRANTED || LinphonePreferences.instance().audioPermAsked()) {
                        startActivity(new Intent(MainActivity.instance(), CallOutgoingActivity.class));
                    } else {
                        checkAndRequestPermission(android.Manifest.permission.RECORD_AUDIO, PERMISSIONS_REQUEST_RECORD_AUDIO);
                    }
                } else if (state == LinphoneCall.State.CallEnd || state == LinphoneCall.State.Error || state == LinphoneCall.State.CallReleased) {
                    // Convert LinphoneCore message for internalization
                    if (message != null && call.getErrorInfo().getReason() == Reason.Declined) {
                        displayCustomToast(getString(R.string.error_call_declined), Toast.LENGTH_SHORT);
                    } else if (message != null && call.getErrorInfo().getReason() == Reason.NotFound) {
                        displayCustomToast(getString(R.string.error_user_not_found), Toast.LENGTH_SHORT);
                    } else if (message != null && call.getErrorInfo().getReason() == Reason.Media) {
                        displayCustomToast(getString(R.string.error_incompatible_media), Toast.LENGTH_SHORT);
                    } else if (message != null && state == LinphoneCall.State.Error) {
                        displayCustomToast(getString(R.string.error_unknown) + " - " + message, Toast.LENGTH_SHORT);
                    }
                    resetClassicMenuLayoutAndGoBackToCallIfStillRunning();
                }

                int missedCalls = LinphoneManager.getLc().getMissedCallsCount();
//                displayMissedCalls(missedCalls);
            }
        };

        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            android.util.Log.i("CNN", "LinphoneActivity_add listen");
            lc.addListener(mListener);
        }

        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                rotation = 0;
                break;
            case Surface.ROTATION_90:
                rotation = 90;
                break;
            case Surface.ROTATION_180:
                rotation = 180;
                break;
            case Surface.ROTATION_270:
                rotation = 270;
                break;
        }

        LinphoneManager.getLc().setDeviceRotation(rotation);
        mAlwaysChangingPhoneAngle = rotation;

    }

    public void isNewProxyConfig(){
        newProxyConfig = true;
        android.util.Log.i("CNN", "LinphoneActivity_isNewProxyConfig");
    }
    @SuppressLint("SimpleDateFormat")
    private String secondsToDisplayableString(int secs) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.set(0, 0, 0, 0, 0, secs);
        return dateFormat.format(cal.getTime());
    }

    public void resetClassicMenuLayoutAndGoBackToCallIfStillRunning() {
        android.util.Log.i("CNN", "LinphoneActivity_resetClassicMenuLayoutAndBackToCallIfStillRunning");
        //cv nay nham thiet ke lai giao dien cho phu hop tinh trang cuoc goi
        if (dialerFragment != null) {
            ((DialerFragment) dialerFragment).resetLayout(false);
        }

        if (LinphoneManager.isInstanciated() && LinphoneManager.getLc().getCallsNb() > 0) {
            LinphoneCall call = LinphoneManager.getLc().getCalls()[0];
            if (call.getState() == LinphoneCall.State.IncomingReceived) {
                startActivity(new Intent(MainActivity.this, CallIncomingActivity.class));
            } else if (call.getCurrentParamsCopy().getVideoEnabled()) {
                startVideoActivity(call);
            } else {
                startIncallActivity(call);
            }
        }
    }
    public void startVideoActivity(LinphoneCall currentCall) {
        android.util.Log.i("CNN", "LinphoneActivity_startVideoActivity");
        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra("VideoEnabled", true);
        startOrientationSensor();
        startActivityForResult(intent, CALL_ACTIVITY);
    }
    public void startIncallActivity(LinphoneCall currentCall) {
        android.util.Log.i("CNN", "LinphoneActivity_startIncallActivity");
        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra("VideoEnabled", false);
        startOrientationSensor();
        startActivityForResult(intent, CALL_ACTIVITY);
    }

    /**
     * Register a sensor to track phoneOrientation changes
     */
    private synchronized void startOrientationSensor() {
        android.util.Log.i("CNN", "LinphoneActivity_startOrientationSensor");
        if (mOrientationHelper == null) {
            mOrientationHelper = new LocalOrientationEventListener(this);
        }
        mOrientationHelper.enable();
    }

    private int mAlwaysChangingPhoneAngle = -1;

    private class LocalOrientationEventListener extends OrientationEventListener {

        public LocalOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(final int o) {
            android.util.Log.i("CNN", "LinphoneActivity_LocalOrientationChanged");
            if (o == OrientationEventListener.ORIENTATION_UNKNOWN) {
                return;
            }

            int degrees = 270;
            if (o < 45 || o > 315)
                degrees = 0;
            else if (o < 135)
                degrees = 90;
            else if (o < 225)
                degrees = 180;

            if (mAlwaysChangingPhoneAngle == degrees) {
                return;
            }
            mAlwaysChangingPhoneAngle = degrees;

            Log.d("Phone orientation changed to ", degrees);
            int rotation = (360 - degrees) % 360;
            LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
            if (lc != null) {
                lc.setDeviceRotation(rotation);
                LinphoneCall currentCall = lc.getCurrentCall();
                if (currentCall != null && currentCall.cameraEnabled() && currentCall.getCurrentParamsCopy().getVideoEnabled()) {
                    lc.updateCall(currentCall, null);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        android.util.Log.i("CNN", "LinphoneActivity_onActivityResult");
//        if (resultCode == Activity.RESULT_FIRST_USER && requestCode == SETTINGS_ACTIVITY) {
//            if (data.getExtras().getBoolean("Exit", false)) {
//                quit();
//            } else {
//                FragmentsAvailable newFragment = (FragmentsAvailable) data.getExtras().getSerializable("FragmentToDisplay");
//                changeCurrentFragment(newFragment, null, true);
//                selectMenu(newFragment);
//            }
         if (resultCode == Activity.RESULT_FIRST_USER && requestCode == CALL_ACTIVITY) {
            getIntent().putExtra("PreviousActivity", CALL_ACTIVITY);
             //cong doan nay cho biet co dang tranfer trong cuoc goi hay khogn
            boolean callTransfer = data == null ? false : data.getBooleanExtra("Transfer", false);
//            boolean chat = data == null ? false : data.getBooleanExtra("chat", false);
//            if(chat){
//                displayChatList();
//            }
            if (LinphoneManager.getLc().getCallsNb() > 0) {
//                initInCallMenuLayout(callTransfer);
                //cv nay lam dua ve fragment dialer
            } else {
                resetClassicMenuLayoutAndGoBackToCallIfStillRunning();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    @Override
    protected void onPause() {
        android.util.Log.i("CNN", "LinphoneActivity_pause");
        getIntent().putExtra("PreviousActivity", 0);
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
        android.util.Log.i("CNN", "LinphoneActivity_onResume");

        if (!LinphoneService.isReady())  {
            startService(new Intent(Intent.ACTION_MAIN).setClass(this, LinphoneService.class));
        }

//        if (getPackageManager().checkPermission(Manifest.permission.READ_CONTACTS, getPackageName()) == PackageManager.PERMISSION_GRANTED && !fetchedContactsOnce) {
//            ContactsManager.getInstance().enableContactsAccess();
//            ContactsManager.getInstance().fetchContacts();
//            fetchedContactsOnce = true;
//        } else {
//            checkAndRequestPermission(Manifest.permission.READ_CONTACTS, PERMISSIONS_REQUEST_READ_CONTACTS);
//        }
//
//        updateMissedChatCount();
//
//        displayMissedCalls(LinphoneManager.getLc().getMissedCallsCount());
//
//        LinphoneManager.getInstance().changeStatusToOnline();

        if (getIntent().getIntExtra("PreviousActivity", 0) != CALL_ACTIVITY){
            if (LinphoneManager.getLc().getCalls().length > 0) {
                LinphoneCall call = LinphoneManager.getLc().getCalls()[0];
                LinphoneCall.State callState = call.getState();
                if (callState == LinphoneCall.State.IncomingReceived) {
                    if (getPackageManager().checkPermission(Manifest.permission.RECORD_AUDIO, getPackageName()) == PackageManager.PERMISSION_GRANTED || LinphonePreferences.instance().audioPermAsked()) {
                        startActivity(new Intent(this, CallIncomingActivity.class));
                    } else {
                        checkAndRequestPermission(Manifest.permission.RECORD_AUDIO, PERMISSIONS_REQUEST_RECORD_AUDIO_INCOMING_CALL);
                    }
                } else if (callState == LinphoneCall.State.OutgoingInit || callState == LinphoneCall.State.OutgoingProgress || callState == LinphoneCall.State.OutgoingRinging) {
                    if (getPackageManager().checkPermission(Manifest.permission.RECORD_AUDIO, getPackageName()) == PackageManager.PERMISSION_GRANTED || LinphonePreferences.instance().audioPermAsked()) {
                        startActivity(new Intent(this, CallOutgoingActivity.class));
                    } else {
                        checkAndRequestPermission(Manifest.permission.RECORD_AUDIO, PERMISSIONS_REQUEST_RECORD_AUDIO);
                    }
                } else {
                    if (call.getCurrentParamsCopy().getVideoEnabled()) {
                        startVideoActivity(call);
                    } else {
                        startIncallActivity(call);
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {

        android.util.Log.i("CNN", "LinphoneActivity_onDetroy");
        if (mOrientationHelper != null) {
            mOrientationHelper.disable();
            mOrientationHelper = null;
        }

        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.removeListener(mListener);
        }

        instance = null;
        super.onDestroy();

//        unbindDrawables(findViewById(R.id.topLayout));
        System.gc();
    }



//    private void unbindDrawables(View view) {
//        android.util.Log.i("CNN", "LinphoneActivity_unbindDrawables");
//        if (view != null && view.getBackground() != null) {
//            view.getBackground().setCallback(null);
//        }
//        if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
//            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
//                unbindDrawables(((ViewGroup) view).getChildAt(i));
//            }
//            ((ViewGroup) view).removeAllViews();
//        }
//    }
    public void checkAndRequestPermission(String permission, int result) {
        android.util.Log.i("CNN", "LinphoneActivity_checkAndRequestPermission");
        if (getPackageManager().checkPermission(permission, getPackageName()) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,permission) && !permissionAsked) {
                permissionAsked = true;
                if(LinphonePreferences.instance().shouldInitiateVideoCall() ||
                        LinphonePreferences.instance().shouldAutomaticallyAcceptVideoRequests()) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, permission}, result);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{permission}, result);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        android.util.Log.i("CNN", "LinphoneActivity_onRequestPemissionResult");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_RECORD_AUDIO:
                startActivity(new Intent(this, CallOutgoingActivity.class));
                LinphonePreferences.instance().neverAskAudioPerm();
                break;
            case PERMISSIONS_REQUEST_RECORD_AUDIO_INCOMING_CALL:
                startActivity(new Intent(this, CallIncomingActivity.class));
                LinphonePreferences.instance().neverAskAudioPerm();
                break;
        }
        permissionAsked = false;
    }

    public void displayCustomToast(final String message, final int duration) {
        android.util.Log.i("CNN", "LinphoneActivity_displayCustomToast");
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast, (ViewGroup) findViewById(R.id.toastRoot));

        TextView toastText = (TextView) layout.findViewById(R.id.toastMessage);
        toastText.setText(message);

        final Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(duration);
        toast.setView(layout);
        toast.show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        android.util.Log.i("CNN", "LinphoneActivity_onNewIntent");

        Bundle extras = intent.getExtras();
        if (extras != null && extras.getBoolean("GoToChat", false)) {
//            LinphoneService.instance().removeMessageNotification();
            String sipUri = extras.getString("ChatContactSipUri");

        } else if (extras != null && extras.getBoolean("Notification", false)) {
            if (LinphoneManager.getLc().getCallsNb() > 0) {
                LinphoneCall call = LinphoneManager.getLc().getCalls()[0];
                if (call.getCurrentParamsCopy().getVideoEnabled()) {
                    startVideoActivity(call);
                } else {
                    startIncallActivity(call);
                }
            }
        } else {
            if (dialerFragment != null) {
                if (extras != null && extras.containsKey("SipUriOrNumber")) {
                    if (getResources().getBoolean(R.bool.automatically_start_intercepted_outgoing_gsm_call)) {
                        ((DialerFragment) dialerFragment).newOutgoingCall(extras.getString("SipUriOrNumber"));
                    } else {
                        ((DialerFragment) dialerFragment).displayTextInAddressBar(extras.getString("SipUriOrNumber"));
                    }
                } else {
                    ((DialerFragment) dialerFragment).newOutgoingCall(intent);
                }
            }
            if (LinphoneManager.getLc().getCalls().length > 0) {
                LinphoneCall calls[] = LinphoneManager.getLc().getCalls();
                if (calls.length > 0) {
                    LinphoneCall call = calls[0];

                    if (call != null && call.getState() != LinphoneCall.State.IncomingReceived) {
                        if (call.getCurrentParamsCopy().getVideoEnabled()) {
                            startVideoActivity(call);
                        } else {
                            startIncallActivity(call);
                        }
                    }
                }

                // If a call is ringing, start incomingcallactivity
                Collection<LinphoneCall.State> incoming = new ArrayList<LinphoneCall.State>();
                incoming.add(LinphoneCall.State.IncomingReceived);
                if (ChatUtils.getCallsInState(LinphoneManager.getLc(), incoming).size() > 0) {
                    if (CallActivity.isInstanciated()) {
                        CallActivity.instance().startIncomingCallActivity();
                    } else {
                        if (getPackageManager().checkPermission(Manifest.permission.RECORD_AUDIO, getPackageName()) == PackageManager.PERMISSION_GRANTED || LinphonePreferences.instance().audioPermAsked()) {
                            startActivity(new Intent(this, CallIncomingActivity.class));
                        } else {
                            checkAndRequestPermission(Manifest.permission.RECORD_AUDIO, PERMISSIONS_REQUEST_RECORD_AUDIO_INCOMING_CALL);
                        }
                    }
                }
            }
        }
    }

    private void initTab() {
        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorIndivateTab));
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
    }


    private void setupTabIcons() {
        int[] tabIcons = {
                R.drawable.chat,
                R.drawable.call

        };

        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);

    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new ChatFragment(), STR_CHAT_FRAGMENT);
        adapter.addFrag(new DialerFragment(), STR_CALL_FRAGMENT);


        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if (adapter.getItem(position) instanceof ChatFragment) {

                } else if (adapter.getItem(position) instanceof DialerFragment){

                }  else {

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * Adapter hien thi tab
     */
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {

            // return null to display only the icon
            return null;
        }
    }


    private void setting() throws LinphoneCoreException {
        LinphonePreferences.instance().setInitiateVideoCall(true);
        LinphonePreferences.instance().setAutomaticallyAcceptVideoRequests(true);
//        LinphonePreferences.instance().isEchoCancellationEnabled();
        LinphonePreferences.instance().setMediaEncryption(LinphoneCore.MediaEncryption.ZRTP);
        LinphonePreferences.instance().setStunServer("104.198.213.170");
        LinphonePreferences.instance().setIceEnabled(true);
        LinphonePreferences.instance().setTurnEnabled(true);
        LinphonePreferences.instance().useIpv6(true);
//        LinphonePreferences.instance().setUpnpEnabled(true);
//        LinphonePreferences.instance().useRandomPort1(true);


        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        for (final PayloadType pt : lc.getAudioCodecs()) {
			/* Special case */
            if (pt.getMime().equals("mpeg4-generic")) {
                if (android.os.Build.VERSION.SDK_INT < 16) {
					/* Make sure AAC is disabled */
                    try {
                        lc.enablePayloadType(pt, false);
                    } catch (LinphoneCoreException e) {
                        Log.e(e);
                    }
                    continue;
                }
                LinphoneManager.getLcIfManagerNotDestroyedOrNull().enablePayloadType(pt,true);
                if (lc.payloadTypeIsVbr(pt)) {
                    lc.setPayloadTypeBitrate(pt, LinphonePreferences.instance().getCodecBitrateLimit());
                }
                lc.isPayloadTypeEnabled(pt);
            }
        }
    }

}
