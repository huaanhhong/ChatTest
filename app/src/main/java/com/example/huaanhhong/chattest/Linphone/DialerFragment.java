package com.example.huaanhhong.chattest.Linphone;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.huaanhhong.chattest.MainActivity;
import com.example.huaanhhong.chattest.R;

import org.linphone.core.CallDirection;
import org.linphone.core.LinphoneCallLog;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.mediastream.Log;


public class DialerFragment extends Fragment {

    private static DialerFragment instance;
    private static boolean isCallTransferOngoing = false;
    private Button mBtnCall;
    private EditText mEdtAddress;
    private View.OnClickListener transferListener;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        android.util.Log.i("CNN", "DialerFragment_oncreatview");
        instance = this;
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_dialer, container, false);


        mBtnCall=(Button) view.findViewById(R.id.btn_call);
        mEdtAddress=(EditText) view.findViewById(R.id.edt_mobilecall);
        mBtnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (!LinphoneManager.getInstance().acceptCallIfIncomingPending()) {
                        if (mEdtAddress.getText().length() > 0) {
                            LinphoneManager.getInstance().newOutgoingCall(mEdtAddress);
                        } else {
                            if (getContext().getResources().getBoolean(R.bool.call_last_log_if_adress_is_empty)) {
                                LinphoneCallLog[] logs = LinphoneManager.getLc().getCallLogs();
                                LinphoneCallLog log = null;
                                for (LinphoneCallLog l : logs) {
                                    if (l.getDirection() == CallDirection.Outgoing) {
                                        log = l;
                                        break;
                                    }
                                }
                                if (log == null) {
                                    return;
                                }

                                LinphoneProxyConfig lpc = LinphoneManager.getLc().getDefaultProxyConfig();
                                if (lpc != null && log.getTo().getDomain().equals(lpc.getDomain())) {
                                    mEdtAddress.setText(log.getTo().getUserName());
                                } else {
                                    mEdtAddress.setText(log.getTo().asStringUriOnly());
                                }
                                mEdtAddress.setSelection(mEdtAddress.getText().toString().length());
                                mEdtAddress.setText(log.getTo().getDisplayName());
                            }
                        }
                    }
                } catch (LinphoneCoreException e) {
                    LinphoneManager.getInstance().terminateCall();
//                    onWrongDestinationAddress();
                }
            }
        });


        if (MainActivity.isInstanciated() && LinphoneManager.getLc().getCallsNb() > 0) {
            if (isCallTransferOngoing) {
                android.util.Log.i("CNN", "DialerFragment_oncreatview_iscalltranferongoing");
//                mBtnCall.setBackgroundResource(R.drawable.call_transfer);
            } else {
                android.util.Log.i("CNN", "DialerFragment_oncreatview_clladd");
//                mBtnCall.setBackgroundResource(R.drawable.call_add);
            }
        } else {
            android.util.Log.i("CNN", "DialerFragment_oncreatview_audiostart");
//            mBtnCall.setBackgroundResource(R.drawable.call_audio_start);
        }
        transferListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinphoneCore lc = LinphoneManager.getLc();
                if (lc.getCurrentCall() == null) {
                    return;
                }
                lc.transferCall(lc.getCurrentCall(), mEdtAddress.getText().toString());
                isCallTransferOngoing = false;
                MainActivity.instance().resetClassicMenuLayoutAndGoBackToCallIfStillRunning();
            }
        };

        resetLayout(isCallTransferOngoing);

        return view;

    }
    public static DialerFragment instance() {
        android.util.Log.i("CNN", "DialerFragment_instance");
        return instance;
    }
    public void resetLayout(boolean callTransfer) {
        android.util.Log.i("CNN", "DialerFragment_resetlayout");
        isCallTransferOngoing = callTransfer;
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc == null) {
            return;
        }

        if (lc.getCallsNb() > 0) {
            if (isCallTransferOngoing) {
//                mBtnCall.setBackgroundResource(R.drawable.call_transfer);
                mBtnCall.setOnClickListener(transferListener);
            } else {
//                mBtnCall.setBackgroundResource(R.drawable.call_add);
                mBtnCall.setOnClickListener(transferListener);
            }
//            mAddContact.setEnabled(true);
//            mAddContact.setImageResource(R.drawable.call_alt_back);
//            mAddContact.setOnClickListener(cancelListener);
        } else {
//            mBtnCall.setBackgroundResource(R.drawable.call_audio_start);
//            mAddContact.setEnabled(false);
//            mAddContact.setImageResource(R.drawable.contact_add_button);
//            mAddContact.setOnClickListener(addContactListener);
            enableDisableAddContact();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        resetLayout(isCallTransferOngoing);
    }
    public void enableDisableAddContact() {
        android.util.Log.i("CNN", "DialerFragment_enabledisableaddcontact");
        if(LinphoneManager.getLc().getCallsNb()>0){

        }
//        mAddContact.setEnabled(LinphoneManager.getLc().getCallsNb() > 0 || !mAddress.getText().toString().equals(""));
    }

    public void displayTextInAddressBar(String numberOrSipAddress) {
        android.util.Log.i("CNN", "DialerFragment_displaytextinadressbar");
        mEdtAddress.setText(numberOrSipAddress);
    }

    public void newOutgoingCall(String numberOrSipAddress) {
        android.util.Log.i("CNN", "DialerFragment_newoutgoingcall_string number");
        displayTextInAddressBar(numberOrSipAddress);
        LinphoneManager.getInstance().newOutgoingCall(mEdtAddress);
    }
    public void newOutgoingCall(Intent intent) {

        android.util.Log.i("CNN", "DialerFragment_newoutgoingcall intent");

        if (intent != null && intent.getData() != null) {
            String scheme = intent.getData().getScheme();
            if (scheme.startsWith("imto")) {
                mEdtAddress.setText("sip:" + intent.getData().getLastPathSegment());
            } else if (scheme.startsWith("call") || scheme.startsWith("sip")) {
                mEdtAddress.setText(intent.getData().getSchemeSpecificPart());
            }
            //truong hop nay la goi bang sdt khong dung den
//            } else {
//                Uri contactUri = intent.getData();
//                String address = ContactsManager.getAddressOrNumberForAndroidContact(LinphoneService.instance().getContentResolver(), contactUri);
//                if(address != null) {
//                    mEdtAddress.setText(address);
//                } else {
//                    Log.e("Unknown scheme: ", scheme);
//                    mEdtAddress.setText(intent.getData().getSchemeSpecificPart());
//                }
//            }

//            mEdtAddress.clearDisplayedName();
            intent.setData(null);

            LinphoneManager.getInstance().newOutgoingCall(mEdtAddress);
        }
    }
}
