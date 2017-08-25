package com.example.huaanhhong.chattest.Linphone;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;


import com.example.huaanhhong.chattest.R;

import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.LpConfig;

/**
 * Created by huaanhhong on 16/08/2017.
 */

public class PreferencesMigrator {

    private LinphonePreferences mNewPrefs;
    private SharedPreferences mOldPrefs;
    private Resources mResources;

    public PreferencesMigrator(Context context) {
        android.util.Log.i("CNN", "PreferenceMigrator_context");
        mNewPrefs = LinphonePreferences.instance();
        mResources = context.getResources();
        mOldPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isEchoMigratioNeeded() {
        android.util.Log.i("CNN", "PreferenceMigrator_isEchoMigraionNeeded");
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc == null) {
            return false;
        }

        if (mNewPrefs.isEchoConfigurationUpdated()) {
            return false;
        }

        return (!lc.needsEchoCalibration() && mNewPrefs.isEchoCancellationEnabled());
    }

    public void doEchoMigration() {
        android.util.Log.i("CNN", "PreferenceMigrator_doEchoMIgration");
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc == null) {
            return;
        }

        if (!lc.needsEchoCalibration()) {
            mNewPrefs.setEchoCancellation(false);
        }
    }

    public boolean isMigrationNeeded() {
        android.util.Log.i("CNN", "PreferenceMigrator_boolisMigrationNeeded");
        int accountNumber = mOldPrefs.getInt(getString(R.string.pref_extra_accounts), -1);
        return accountNumber != -1;
    }

    public void doMigration() {
        android.util.Log.i("CNN", "PreferenceMigrator_doMigraion");
        mNewPrefs.firstLaunchSuccessful(); // If migration is needed, it is safe to assume Linphone has already been started once.
        mNewPrefs.removePreviousVersionAuthInfoRemoval(); // Remove flag in linphonerc asking core not to store auths infos

        mNewPrefs.setFrontCamAsDefault(getPrefBoolean(R.string.pref_video_use_front_camera_key, true));
        mNewPrefs.setWifiOnlyEnabled(getPrefBoolean(R.string.pref_wifi_only_key, false));
        mNewPrefs.useRandomPort(getPrefBoolean(R.string.pref_transport_use_random_ports_key, true), false);
//        mNewPrefs.setPushNotificationEnabled(getPrefBoolean(R.string.pref_push_notification_key, false));
//        mNewPrefs.setPushNotificationRegistrationID(getPrefString(R.string.push_reg_id_key, null));
        mNewPrefs.setDebugEnabled(getPrefBoolean(R.string.pref_debug_key, false));
        mNewPrefs.setBackgroundModeEnabled(getPrefBoolean(R.string.pref_background_mode_key, true));
        mNewPrefs.setAnimationsEnabled(getPrefBoolean(R.string.pref_animation_enable_key, false));
        mNewPrefs.setAutoStart(getPrefBoolean(R.string.pref_autostart_key, false));
        mNewPrefs.setSharingPictureServerUrl(getPrefString(R.string.pref_image_sharing_server_key, null));
        mNewPrefs.setRemoteProvisioningUrl(getPrefString(R.string.pref_remote_provisioning_key, null));

        doAccountsMigration();
        deleteAllOldPreferences();
    }

    public void migrateRemoteProvisioningUriIfNeeded() {
        android.util.Log.i("CNN", "PreferenceMigrator_migraRemoteProvisingUriNeeded");
        String oldUri = mNewPrefs.getConfig().getString("app", "remote_provisioning", null);
        String currentUri = mNewPrefs.getRemoteProvisioningUrl();
        if (oldUri != null && oldUri.length() > 0 && currentUri == null) {
            mNewPrefs.setRemoteProvisioningUrl(oldUri);
            mNewPrefs.getConfig().setString("app", "remote_provisioning", null);
            mNewPrefs.getConfig().sync();
        }
    }

    public void migrateSharingServerUrlIfNeeded() {
        android.util.Log.i("CNN", "PreferenceMigrator_migrateSharingServerUrlIfNeeded");
        String currentUrl = mNewPrefs.getConfig().getString("app", "sharing_server", null);
        if (currentUrl == null || currentUrl.equals("https://www.linphone.org:444/upload.php")) {
            mNewPrefs.setSharingPictureServerUrl("https://www.linphone.org:444/lft.php");
            mNewPrefs.getConfig().sync();
        }
    }

    private void doAccountsMigration() {
        android.util.Log.i("CNN", "PreferenceMigrator_doAccountsMigration");
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        lc.clearAuthInfos();
        lc.clearProxyConfigs();

        for (int i = 0; i < mOldPrefs.getInt(getString(R.string.pref_extra_accounts), 1); i++) {
            doAccountMigration(i, i == getPrefInt(R.string.pref_default_account_key, 0));
        }
    }

    private void doAccountMigration(int index, boolean isDefaultAccount) {
        android.util.Log.i("CNN", "PreferenceMigrator_doAccounMIgration");
        String key = index == 0 ? "" : String.valueOf(index);

        String username = getPrefString(getString(R.string.pref_username_key) + key, null);
        String userid = getPrefString(getString(R.string.pref_auth_userid_key) + key, null);
        String password = getPrefString(getString(R.string.pref_passwd_key) + key, null);
        String domain = getPrefString(getString(R.string.pref_domain_key) + key, null);
        if (username != null && username.length() > 0 && password != null) {
            String proxy = getPrefString(getString(R.string.pref_proxy_key) + key, null);
            String expire = getPrefString(R.string.pref_expire_key, null);

            LinphonePreferences.AccountBuilder builder = new LinphonePreferences.AccountBuilder(LinphoneManager.getLc())
                    .setUsername(username)
                    .setUserId(userid)
                    .setDomain(domain)
                    .setPassword(password)
                    .setProxy(proxy)
                    .setExpires(expire);

            if (getPrefBoolean(getString(R.string.pref_enable_outbound_proxy_key) + key, false)) {
                builder.setOutboundProxyEnabled(true);
            }
//            if (mResources.getBoolean(R.bool.enable_push_id)) {
//                String regId = mNewPrefs.getPushNotificationRegistrationID();
//                String appId = getString(R.string.push_sender_id);
//                if (regId != null && mNewPrefs.isPushNotificationEnabled()) {
//                    String contactInfos = "app-id=" + appId + ";pn-type=google;pn-tok=" + regId;
//                    builder.setContactParameters(contactInfos);
//                }
//            }

            try {
                builder.saveNewAccount();
            } catch (LinphoneCoreException e) {
                e.printStackTrace();
            }

            if (isDefaultAccount) {
                mNewPrefs.setDefaultAccount(index);
            }
        }
    }

    private void deleteAllOldPreferences() {
        android.util.Log.i("CNN", "PreferenceMigrator_deleteAllOLdPreferences");
        SharedPreferences.Editor editor = mOldPrefs.edit();
        editor.clear();
        editor.commit();
    }

    private String getString(int key) {
        return mResources.getString(key);
    }
    private boolean getPrefBoolean(int key, boolean defaultValue) {
        android.util.Log.i("CNN", "PreferenceMigrator_boolgetPrefBooleankeyint");
        return mOldPrefs.getBoolean(mResources.getString(key), defaultValue);
    }
    private boolean getPrefBoolean(String key, boolean defaultValue) {
        android.util.Log.i("CNN", "PreferenceMigrator_boolgetPreBooleankeystring");
        return mOldPrefs.getBoolean(key, defaultValue);
    }
    private String getPrefString(int key, String defaultValue) {
        android.util.Log.i("CNN", "PreferenceMigrator_stringgetPrefStringintkey");
        return mOldPrefs.getString(mResources.getString(key), defaultValue);
    }
    private int getPrefInt(int key, int defaultValue) {
        android.util.Log.i("CNN", "PreferenceMigrator_intgetPrefIninkey");
        return mOldPrefs.getInt(mResources.getString(key), defaultValue);
    }
    private String getPrefString(String key, String defaultValue) {
        android.util.Log.i("CNN", "PreferenceMigrator_stringgetPrefStringkeystring");
        return mOldPrefs.getString(key, defaultValue);
    }

}

