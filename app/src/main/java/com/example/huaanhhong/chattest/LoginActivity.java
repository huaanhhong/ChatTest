package com.example.huaanhhong.chattest;

import android.Manifest;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.huaanhhong.chattest.Linphone.EchoCancellerCalibrationFragment;
import com.example.huaanhhong.chattest.Linphone.LinphoneManager;
import com.example.huaanhhong.chattest.Linphone.LinphonePreferences;
import com.example.huaanhhong.chattest.Linphone.LinphoneService;
import com.example.huaanhhong.chattest.Retrofit.APIRetrofit;
import com.example.huaanhhong.chattest.Retrofit.ServerInterface;
import com.example.huaanhhong.chattest.Retrofit.TokenRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.json.JSONException;
import org.json.JSONObject;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListenerBase;
import org.linphone.core.LinphoneProxyConfig;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity implements OnClickListener {


    @BindView(R.id.edt_email)
    EditText mEdtEmail;
    @BindView(R.id.edt_sigup_password)
    EditText mEdtPassword;
    @BindView(R.id.edt_sigup_confirm)
    EditText mEdtConfirmPassword;
    @BindView(R.id.btn_sigup)
    Button mBtnSignup;
    @BindView(R.id.btn_signin)
    Button mBtnSignin;

    private static LoginActivity instance;

    /**
     * String name pass, boolean......
     */
    String mEmail, mPass, mConfirmPass,mMobile;
    private boolean accountLinphoneCreated = false,newAccount = false;
    private Handler mHandle;

    /**Linphone
     * */
    private LinphoneCoreListenerBase mListener;
    private LinphoneAddress address;
    private LinphonePreferences mPrefs;
    private ProgressDialog progress;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 201;

    /**
     * firebase
     */
    private FirebaseAuth mFirebaseAuth;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseDatabase mFirebaseDatabase;
    private static final int RC_SIGN_IN = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        /** khoi dong firebase
         * */
        mFirebaseAuth= FirebaseAuth.getInstance();
        mFirebaseDatabase= FirebaseDatabase.getInstance();
        mFirebaseAuth.addAuthStateListener(listenAuthState);


        /** click ui
         * */
        mBtnSignup.setOnClickListener(this);
        mBtnSignin.setOnClickListener(this);

        /**
         * Linphone
         * */
        mPrefs = LinphonePreferences.instance();
        if (!LinphoneService.isReady())  {
            Log.i("CNN","ASSISTANCE XET SERVIC");
            startService(new Intent(Intent.ACTION_MAIN).setClass(this, LinphoneService.class));
        }
        mListener = new LinphoneCoreListenerBase(){

            @Override
            public void registrationState(LinphoneCore lc, LinphoneProxyConfig cfg, LinphoneCore.RegistrationState state, String smessage) {
                Log.i("CNN", "AssistantActivity_registersrationState");
                if(accountLinphoneCreated && !newAccount){
                    if(address != null && address.asString().equals(cfg.getAddress().asString()) ) {
                        if (state == LinphoneCore.RegistrationState.RegistrationOk) {
                            if(progress != null)
                                progress.dismiss();
                             Toast.makeText(LoginActivity.this,"Dang ki thanh cong",Toast.LENGTH_LONG).show();
                            if (LinphoneManager.getLc().getDefaultProxyConfig() != null) {
                                launchEchoCancellerCalibration(true);
                            }
                        } else if (state == LinphoneCore.RegistrationState.RegistrationFailed) {
                            if(progress != null)
                                progress.dismiss();
                        } else if(!(state == LinphoneCore.RegistrationState.RegistrationProgress)) {
                            if(progress != null)
                                progress.dismiss();
                        }
                    }
                }
            }
        };
        instance = this;
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.addListener(mListener);
        }
      if(mPrefs.isFirstComed()){
          Intent intent=new Intent(LoginActivity.this,MainActivity.class);
          startActivity(intent);
          finish();
      }

    }
    public static LoginActivity instance() {
        Log.i("CNN", "AssistantActivity_instance");
        return instance;
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.btn_sigup:
                    mEmail = mEdtEmail.getText().toString();
                    mPass = mEdtPassword.getText().toString();
                    mConfirmPass = mEdtConfirmPassword.getText().toString();
                    if (mEmail == null || mPass == null||mConfirmPass==null) {
                        Toast.makeText(LoginActivity.this, this.getString(R.string.thieuthongtin), Toast.LENGTH_LONG).show();
                    }
                    else if(!mPass.equals(mConfirmPass)) {
                        Toast.makeText(LoginActivity.this, this.getString(R.string.saimatkhau), Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        new signupByEmail().execute(mEmail,mPass);
                    }
                    break;
                case R.id.btn_signin:
                    mEmail = mEdtEmail.getText().toString();
                    mPass = mEdtPassword.getText().toString();
                    if (mEmail == null || mPass == null) {
                        Toast.makeText(LoginActivity.this, this.getString(R.string.thieuthongtin), Toast.LENGTH_LONG).show();
                    }
                    else {
                        new loginByEmail().execute(mEmail,mPass);
                    }

                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /** Sign up bang email,GG,facebook --qua trinh nay dien ra la mot asysntask, bao gom:
     * firebase
     * retrofit webservice
     * linphone
     * thong tin bao gom : email,password, mobilevausername la mot so random
     * /////////////////////////////////////////////////////////////////// */

    /**Firebase*/
    private ProgressDialog progressDialog;
    class signupByEmail extends AsyncTask<String,Void,Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog=new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage(getString(R.string.dialogchodangki));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            mFirebaseAuth.createUserWithEmailAndPassword(strings[0],strings[1])
                    .addOnCompleteListener(listenCompleteSignup);
            return null;
        }
    }
    // Lang nghe dang ki firebase co thanh cong hay khong
    private OnCompleteListener listenCompleteSignup=new OnCompleteListener() {
        @Override
        public void onComplete(@NonNull Task task) {

            try {

                if(task.isComplete()&&task.isSuccessful()){
                    //saveData();
                    //tiep tuc sign up webservice
                    signupWebservice();
                }
                else {
                    Toast.makeText(LoginActivity.this, getString(R.string.err),Toast.LENGTH_LONG).show();
                    Log.i("CNN","DA CO LOI", task.getException());
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("SIGNUP",e.toString());

            }
        }
    };
    ///Login firebase/////
    class loginByEmail extends AsyncTask<String,Void,Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog=new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage(getString(R.string.thongbaodangnhap));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        @Override
        protected Boolean doInBackground(String... voids) {

            mFirebaseAuth.signInWithEmailAndPassword(voids[0],voids[1])
                    .addOnCompleteListener(completesigin);

            return null;
        }
    }
    ////Lang nghe dang nhap firebase co thanh cong hay khong////////
    private OnCompleteListener completesigin=new OnCompleteListener() {
        @Override
        public void onComplete(@NonNull Task task) {

            try {

                if(task.isComplete()&&task.isSuccessful()){
                    Log.i("CNN","Dang nhap firebase thanh cong");
                     //saveData();
                    //Lognin firebase
                    LogninWebservice();
                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "da co loi xay ra",Toast.LENGTH_LONG).show();
                    Log.i("CNN","DA CO LOI", task.getException());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("LOGNIN",e.toString());
            }
        }
    };
    ////save data len firebase sao khi dang ki,hoac dang nhap thanh cong
    private void saveData() {
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        User newuser=new User();
        newuser.setUserId(user.getUid());
        newuser.setUserName(user.getDisplayName()==null?user.getEmail():user.getDisplayName());
        newuser.setUserEmail(user.getEmail());
        newuser.setUrlAvatar(user.getPhotoUrl()==null? "":user.getPhotoUrl().toString());
        newuser.setIsOnline(true);

        Gson gson=new Gson();
        mFirebaseDatabase.getReference("ListUser").child(newuser.getUserId())
                .setValue(gson.toJson(newuser))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                   Log.i("CNN","Luu thong tin user len firebase thanh cong");
                    }
                })
        ;
    }

     //-END.
    //ket thuc CV tren firebase////////////////

   /** signup tren Webservice */////////////////////
    private void signupWebservice() {
        TokenRequest tokensignup=new TokenRequest();
        tokensignup.setPassword(mPass);
        UUID myuuid = UUID.randomUUID();
        long highbits = myuuid.getMostSignificantBits();
            //        long lowbits = myuuid.getLeastSignificantBits();
        mMobile= "+84"+(String.valueOf(highbits)).substring(1,8);
        tokensignup.setProfile(new TokenRequest.Profile(mMobile,mEmail));
        tokensignup.setUsername(mMobile);

        ServerInterface serverSignup=new APIRetrofit().postjson().create(ServerInterface.class);
        Call<JsonElement> call= serverSignup.postsignup(tokensignup);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                Log.i("CNN",response.body().toString());
                try {
                    JSONObject data = new JSONObject(response.body().toString());
                    String err = data.getString("err_code");

                    if (err.equals("0")) {
                        //show dialog thong bao check mail de tiep tuc login
                        mHandle=new Handler();
                        showdialogcheckmail();
                        LogninWebservice();
                    }
                    else {
                        LogninWebservice();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {

            }
        });
    }
    /** nhac nho check mail
     * */
    private void showdialogcheckmail(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mHandle.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(progressDialog!=null){
                            progressDialog.dismiss();
                        }
                        dialogCheckmail();
                    }
                }, 50000);
            }
        });
    }
    private Runnable onPlay=new Runnable() {
        @Override
        public void run() {

            }
    };
    /** dialog nhac nho check mail */
    private void dialogCheckmail() {
        progressDialog=new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage(getString(R.string.messageNhaccheckmail));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
  /** LOgin Webservice de lay token *///////////////////
  int mCount=0;
    private void LogninWebservice() {
        Log.i("CNN","login lan "+mCount);
        TokenRequest token = new TokenRequest();
        token.setUsername(mEmail);
        token.setPassword(mPass);
        ServerInterface serverinterface = new APIRetrofit().postjson().create(ServerInterface.class);
        Call<JsonElement> call = serverinterface.postsignin(token);
        call.enqueue(new Callback<JsonElement>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                Log.i("CNN", response.body().toString());
                Log.i("CNN",(response.body().toString()).substring(56,60));
                try {
                    JSONObject data = new JSONObject(response.body().toString());
                    String err = data.getString("err_code");
                    JSONObject object = data.getJSONObject("data");
//                    Log.i("CNN","conftokennnnnnnnnn"+conf_token+" "+mobile);
                    if(((response.body().toString()).substring(56,60)).equals("null")) {
                        Log.i("CNN","CONF TOKEN NULL");
                        //tiep tuc cho checkmail
                        mHandle=new Handler();
                        mHandle.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(mCount==11){
                                    dimiss();
                                    LogninWebservice();
                                }
                                else if(mCount<11){
                                    LogninWebservice();
                                }
                                mCount++;
                            }
                        },40000);
                    }
                    JSONObject objectprofile = object.getJSONObject("profile");
                    String conf_token = object.getString("conf_token");
                    String mobile=objectprofile.getString("mobile");
                    if(!err.equals("0")){
                       dimiss();
                    }
                    else if(conf_token!=null){
                        if (progressDialog!=null){
                        progressDialog.dismiss();}
                        saveData();
                        /** tiep tuc signupLinphone */
                        LinphoneAddress.TransportType transport;
                        transport = LinphoneAddress.TransportType.LinphoneTransportUdp;
                        String tokenf=conf_token.substring(0,25);
                        genericLogIn(mobile,tokenf,null,"104.198.213.170",transport);
                        Log.i("CNN",tokenf);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {

            }
        });
    }
    //dimiss
    private void dimiss(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(progressDialog!=null) {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this,getString(R.string.err),Toast.LENGTH_SHORT).show();
                }
    }});}
     //-END.
    /////////ket thuc signup tren webservice//////////
    /**
     * Tien hanh signup vao linphone
     * */
    public void genericLogIn(String username, String password, String displayName, String domain, LinphoneAddress.TransportType transport) {
        Log.i("CNN", "AssistantActivity_genericLogIn");
        if(accountLinphoneCreated) {
            retryLogin(username, password, displayName, domain, transport);
        } else {
            logIn(username, password, displayName, domain, transport, false);
        }
    }
    private void logIn(String username, String password, String displayName, String domain, LinphoneAddress.TransportType transport, boolean sendEcCalibrationResult) {
        Log.i("CNN", "AssistantActivity_logIn");
        saveCreatedAccount(username, password, displayName, domain, transport);
    }
    public void retryLogin(String username, String password, String displayName, String domain, LinphoneAddress.TransportType transport) {
        Log.i("CNN", "AssistantActivity_retrylogIn");
        accountLinphoneCreated = false;
        saveCreatedAccount(username, password, displayName, domain, transport);
    }
    public void saveCreatedAccount(String username, String password, String displayName, String domain, LinphoneAddress.TransportType transport) {
        Log.i("CNN", "AssistantActivity_saveCreatedAccount");
        if (accountLinphoneCreated)
            return;

        if(username.startsWith("sip:")) {
            username = username.substring(4);
        }

        if (username.contains("@"))
            username = username.split("@")[0];

        if(domain.startsWith("sip:")) {
            domain = domain.substring(4);
        }

        String identity = "sip:" + username + "@" + domain;
        try {
            address = LinphoneCoreFactory.instance().createLinphoneAddress(identity);
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
        }

        if(address != null && displayName != null && !displayName.equals("")){
            address.setDisplayName(displayName);
        }

        boolean isMainAccountLinphoneDotOrg = domain.equals(getString(R.string.default_domain));
        LinphonePreferences.AccountBuilder builder = new LinphonePreferences.AccountBuilder(LinphoneManager.getLc())
                .setUsername(username)
                .setDomain(domain)
                .setDisplayName(displayName)
                .setPassword(password);

        if (isMainAccountLinphoneDotOrg) {
            if (getResources().getBoolean(R.bool.disable_all_security_features_for_markets)) {
                builder.setProxy(domain)
                        .setTransport(LinphoneAddress.TransportType.LinphoneTransportTcp);
            }
            else {
                builder.setProxy(domain)
                        .setTransport(LinphoneAddress.TransportType.LinphoneTransportTls);
            }

            builder.setExpires("604800")
                    .setAvpfEnabled(true)
                    .setAvpfRRInterval(3)
                    .setQualityReportingCollector("sip:voip-metrics@sip.linphone.org")
                    .setQualityReportingEnabled(true)
                    .setQualityReportingInterval(180)
                    .setRealm("sip.linphone.org")
                    .setNoDefault(false);


            mPrefs.setStunServer(getString(R.string.default_stun));
            mPrefs.setIceEnabled(true);
        } else {
            String forcedProxy = "";
            if (!TextUtils.isEmpty(forcedProxy)) {
                builder.setProxy(forcedProxy)
                        .setOutboundProxyEnabled(true)
                        .setAvpfRRInterval(5);
            }

            if(transport != null) {
                builder.setTransport(transport);
            }
        }

//        if (getResources().getBoolean(R.bool.enable_push_id)) {
//            String regId = mPrefs.getPushNotificationRegistrationID();
//            String appId = getString(R.string.push_sender_id);
//            if (regId != null && mPrefs.isPushNotificationEnabled()) {
//                String contactInfos = "app-id=" + appId + ";pn-type=google;pn-tok=" + regId;
//                builder.setContactParameters(contactInfos);
//            }
//        }

        try {
            builder.saveNewAccount();
            if(!newAccount) {
                displayRegistrationInProgressDialog();
            }
            accountLinphoneCreated = true;
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
        }
    }
    //show dialog register linphone
    public void displayRegistrationInProgressDialog(){
        Log.i("CNN", "AssistantActivity_displayRegistrationInProgressDialog");
        if(LinphoneManager.getLc().isNetworkReachable()) {
            progress = ProgressDialog.show(this,null,null);
            Drawable d = new ColorDrawable(getResources().getColor(R.color.dialog));
            d.setAlpha(200);
            progress.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            progress.getWindow().setBackgroundDrawable(d);
            progress.setContentView(R.layout.progress_dialog);
            progress.show();

        }
        else{
            Log.i("CNN","khong co mang");
        }
    }
    public void checkAndRequestAudioPermission() {
        Log.i("CNN", "AssistantActivity_checkAndRequestAudioPermission");
        if (getPackageManager().checkPermission(Manifest.permission.RECORD_AUDIO, getPackageName()) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.i("CNN", "AssistantActivity_onRequestPermissionResult");
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (getPackageManager().checkPermission(Manifest.permission.RECORD_AUDIO, getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                launchEchoCancellerCalibration(true);
            } else {
                success();
            }
        } else {
            success();
        }
    }

    private void launchEchoCancellerCalibration(boolean sendEcCalibrationResult) {
        Log.i("CNN", "AssistantActivity_launchEchoCancellerCalibration");
        if (getPackageManager().checkPermission(Manifest.permission.RECORD_AUDIO, getPackageName()) == PackageManager.PERMISSION_GRANTED) {
            boolean needsEchoCalibration = LinphoneManager.getLc().needsEchoCalibration();
            if (needsEchoCalibration && mPrefs.isFirstLaunch()) {
                EchoCancellerCalibrationFragment fragment = new EchoCancellerCalibrationFragment();
                fragment.enableEcCalibrationResultSending(sendEcCalibrationResult);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.framelayout, fragment);
                transaction.commitAllowingStateLoss();
            } else {

                success();
            }
        } else {
            checkAndRequestAudioPermission();
        }
    }
    public void isEchoCalibrationFinished() {
        Log.i("CNN", "AssistantActivity_isEchoCalibrationFinished");
        success();
    }
    public void success() {
        Log.i("CNN", "AssistantActivity_success");
        mPrefs.firstLaunchSuccessful();
        mPrefs.firstComedSuccessful();
        Intent intent=new Intent(LoginActivity.this,MainActivity.class);
        startActivity(intent);
        finish();

    }
    ////////////////////KET THUC LOGIN BANG LINPHONE//////////////////////////////////

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("CNN", "AssistantActivity_onResume");
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.addListener(mListener);
            Log.i("CNN", "ON RESUME ASSISTANT ACTIVITY");
        }
    }

    @Override
    protected void onPause() {
        Log.i("CNN", "AssistantActivity_onPause");
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.removeListener(mListener);
        }

        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirebaseAuth.removeAuthStateListener(listenAuthState);
    }

    /**Lang nghe AuthFirebase
     * */
    private FirebaseAuth.AuthStateListener listenAuthState=new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

        }
    };

}

