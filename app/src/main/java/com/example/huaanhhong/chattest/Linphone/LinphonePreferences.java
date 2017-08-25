package com.example.huaanhhong.chattest.Linphone;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;


import com.example.huaanhhong.chattest.R;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneNatPolicy;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.LpConfig;
import org.linphone.core.TunnelConfig;
import org.linphone.mediastream.Log;

/**
 * Created by huaanhhong on 16/08/2017.
 */

public class LinphonePreferences {

    private static LinphonePreferences instance;
    private static final int LINPHONE_CORE_RANDOM_PORT = -1;
    private Context mContext;

    public static final synchronized LinphonePreferences instance() {
        android.util.Log.i("CNN", "linphonePreferences_instance");
        if (instance == null) {
            instance = new LinphonePreferences();
        }
        return instance;
    }
    private LinphonePreferences() {

    }

    public void setContext(Context c) {
        android.util.Log.i("CNN", "linphonePreferences_setContext");
        mContext = c;
    }
    private String getString(int key) {
        android.util.Log.i("CNN", "linphonePreferences_getString");
        if (mContext == null && LinphoneManager.isInstanciated()) {
            mContext = LinphoneManager.getInstance().getContext();
        }

        return mContext.getString(key);
    }

    private LinphoneCore getLc() {
        android.util.Log.i("CNN", "linphonePreferences_getLC");
        if (!LinphoneManager.isInstanciated())
            return null;

        return LinphoneManager.getLcIfManagerNotDestroyedOrNull();
    }
    /** cong viec nay nham tao noi de luu trang thai, cau hinh**/
    public LpConfig getConfig() {
        android.util.Log.i("CNN", "linphonePreferences_getConfig");
        LinphoneCore lc = getLc();
        if (lc != null) {
            return lc.getConfig();
        }

        if (!LinphoneManager.isInstanciated()) {
            Log.w("LinphoneManager not instanciated yet...");
            return LinphoneCoreFactory.instance().createLpConfig(mContext.getFilesDir().getAbsolutePath() + "/.linphonerc");
        }

        return LinphoneCoreFactory.instance().createLpConfig(LinphoneManager.getInstance().mLinphoneConfigFile);
    }
    public void removePreviousVersionAuthInfoRemoval() {
        android.util.Log.i("CNN", "linphonePreferences_removePreviousVersionAuthInfoRemoval");
        getConfig().setBool("sip", "store_auth_info", true);
    }
    // App settings
    public boolean isFirstLaunch() {
        android.util.Log.i("CNN", "linphonePreferences_boolisFirstLaunch");
        return getConfig().getBool("app", "first_launch", true);
    }

    public void firstLaunchSuccessful() {
        android.util.Log.i("CNN", "linphonePreferences_firstLaunchSuccessful");
        getConfig().setBool("app", "first_launch", false);
    }
    public boolean isFirstComed() {
        android.util.Log.i("CNN", "linphonePreferences_boolisFirstLaunch");
        return getConfig().getBool("app", "first_comed", false);
    }

    public void firstComedSuccessful() {
        android.util.Log.i("CNN", "linphonePreferences_firstLaunchSuccessful");
        getConfig().setBool("app", "first_comed", true);
    }

    // Accounts settings
    private LinphoneProxyConfig getProxyConfig(int n) {
        android.util.Log.i("CNN", "linphonePreferences_getProxyConfig");
        LinphoneProxyConfig[] prxCfgs = getLc().getProxyConfigList();
        if (n < 0 || n >= prxCfgs.length)
            return null;
        return prxCfgs[n];
    }

    private LinphoneAuthInfo getAuthInfo(int n) {
        android.util.Log.i("CNN", "linphonePreferences_getAuthInfo");
        LinphoneProxyConfig prxCfg = getProxyConfig(n);
        try {
            LinphoneAddress addr = LinphoneCoreFactory.instance().createLinphoneAddress(prxCfg.getIdentity());
            LinphoneAuthInfo authInfo = getLc().findAuthInfo(addr.getUserName(), null, addr.getDomain());
            return authInfo;
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Removes a authInfo from the core and returns a copy of it.
     * Useful to edit a authInfo (you should call saveAuthInfo after the modifications to save them).
     */
    private LinphoneAuthInfo getClonedAuthInfo(int n) {
        android.util.Log.i("CNN", "linphonePreferences_getCLoneAuthInfo");
        LinphoneAuthInfo authInfo = getAuthInfo(n);
        if (authInfo == null)
            return null;

        LinphoneAuthInfo cloneAuthInfo = authInfo.clone();
        getLc().removeAuthInfo(authInfo);
        return cloneAuthInfo;
    }

    /**
     * Saves a authInfo into the core.
     * Useful to save the changes made to a cloned authInfo.
     */
    private void saveAuthInfo(LinphoneAuthInfo authInfo) {
        android.util.Log.i("CNN", "linphonePreferences_saveAuthInfo");
        getLc().addAuthInfo(authInfo);
    }

    public static class AccountBuilder {

        private LinphoneCore lc;
        private String tempUsername;
        private String tempDisplayName;
        private String tempUserId;
        private String tempPassword;
        private String tempDomain;
        private String tempProxy;
        private String tempRealm;
        private boolean tempOutboundProxy;
        private String tempContactsParams;
        private String tempExpire;
        private LinphoneAddress.TransportType tempTransport;
        private boolean tempAvpfEnabled = false;
        private int tempAvpfRRInterval = 0;
        private String tempQualityReportingCollector;
        private boolean tempQualityReportingEnabled = false;
        private int tempQualityReportingInterval = 0;
        private boolean tempEnabled = true;
        private boolean tempNoDefault = false;


        public AccountBuilder(LinphoneCore lc)
        {
            android.util.Log.i("CNN", "linphonePreferences_accounBuilder");
            this.lc = lc;
        }

        public AccountBuilder setTransport(LinphoneAddress.TransportType transport) {
            android.util.Log.i("CNN", "linphonePreferences_accounBuilder_settransport");
            tempTransport = transport;
            return this;
        }

        public AccountBuilder setUsername(String username) {
            android.util.Log.i("CNN", "linphonePreferences_accounBuilder_setusername");
            tempUsername = username;
            return this;
        }

        public AccountBuilder setDisplayName(String displayName) {
            android.util.Log.i("CNN", "linphonePreferences_accounBuilder_setdisplayname");
            tempDisplayName = displayName;
            return this;
        }

        public AccountBuilder setPassword(String password) {
            android.util.Log.i("CNN", "linphonePreferences_accounBuilder_setPassword");
            tempPassword = password;
            return this;
        }

        public AccountBuilder setDomain(String domain) {
            android.util.Log.i("CNN", "linphonePreferences_accounBuilder_setDomain");
            tempDomain = domain;
            return this;
        }

        public AccountBuilder setProxy(String proxy) {
            android.util.Log.i("CNN", "linphonePreferences_accounBuilder_setProxy");
            tempProxy = proxy;
            return this;
        }

        public AccountBuilder setOutboundProxyEnabled(boolean enabled) {
            android.util.Log.i("CNN", "linphonePreferences_accounBuilder_setOutboundProxyEnable");
            tempOutboundProxy = enabled;
            return this;
        }

        public AccountBuilder setContactParameters(String contactParams) {
            android.util.Log.i("CNN", "linphonePreferences_accounBuilder_setCOntactParamemeters "+contactParams );
            tempContactsParams = contactParams;
            return this;
        }

        public AccountBuilder setExpires(String expire) {
            android.util.Log.i("CNN", "linphonePreferences_accounBuilder_setExpire");
            tempExpire = expire;
            return this;
        }

        public AccountBuilder setUserId(String userId) {
            android.util.Log.i("CNN", "linphonePreferences_accounBuilder_setUserId");
            tempUserId = userId;
            return this;
        }

        public AccountBuilder setAvpfEnabled(boolean enable) {
            android.util.Log.i("CNN", "linphonePreferences_accounBuilder_setAvpfEnabled");
            tempAvpfEnabled = enable;
            return this;
        }

        public AccountBuilder setAvpfRRInterval(int interval) {
            android.util.Log.i("CNN", "linphonePreferences_accounBuilder_setAvpfRRInterval");
            tempAvpfRRInterval = interval;
            return this;
        }

        public AccountBuilder setRealm(String realm) {
            android.util.Log.i("CNN", "linphonePreferences_accounBuilder_setRealm");
            tempRealm = realm;
            return this;
        }

        public AccountBuilder setQualityReportingCollector(String collector) {
            android.util.Log.i("CNN", "linphonePreferences_accounBuilder_setQualityReportCllector");
            tempQualityReportingCollector = collector;
            return this;
        }

        public AccountBuilder setQualityReportingEnabled(boolean enable) {
            android.util.Log.i("CNN", "linphonePreferences_accounBuilder_setQualityReportingENable");
            tempQualityReportingEnabled = enable;
            return this;
        }

        public AccountBuilder setQualityReportingInterval(int interval) {
            android.util.Log.i("CNN", "linphonePreferences_accounBuilder_setQualityReportingInerval");
            tempQualityReportingInterval = interval;
            return this;
        }

        public AccountBuilder setEnabled(boolean enable) {
            android.util.Log.i("CNN", "linphonePreferences_accounBuilder_setEnable");
            tempEnabled = enable;
            return this;
        }

        public AccountBuilder setNoDefault(boolean yesno) {
            android.util.Log.i("CNN", "linphonePreferences_accounBuilder_setNoDefault");
            tempNoDefault = yesno;
            return this;
        }

        /**
         * Creates a new account
         * @throws LinphoneCoreException
         */
        public void saveNewAccount() throws LinphoneCoreException {
            android.util.Log.i("CNN", "linphonePreferences_saveNewAccount");

            if (tempUsername == null || tempUsername.length() < 1 || tempDomain == null || tempDomain.length() < 1) {
                Log.w("Skipping account save: username or domain not provided");
                return;
            }

            String identity = "sip:" + tempUsername + "@" + tempDomain;
            String proxy = "sip:";
            if (tempProxy == null) {
                proxy += tempDomain;
            } else {
                if (!tempProxy.startsWith("sip:") && !tempProxy.startsWith("<sip:")
                        && !tempProxy.startsWith("sips:") && !tempProxy.startsWith("<sips:")) {
                    proxy += tempProxy;
                } else {
                    proxy = tempProxy;
                }
            }
            LinphoneAddress proxyAddr = LinphoneCoreFactory.instance().createLinphoneAddress(proxy);
            LinphoneAddress identityAddr = LinphoneCoreFactory.instance().createLinphoneAddress(identity);
            android.util.Log.i("CNN", "linphonePreferences_saveNewAccount "+proxyAddr+" "+identityAddr);

            if (tempDisplayName != null) {
                identityAddr.setDisplayName(tempDisplayName);
            }

            if (tempTransport != null) {
                proxyAddr.setTransport(tempTransport);
            }

            String route = tempOutboundProxy ? proxyAddr.asStringUriOnly() : null;

            LinphoneProxyConfig prxCfg = lc.createProxyConfig(identityAddr.asString(), proxyAddr.asStringUriOnly(), route, tempEnabled);
            android.util.Log.i("CNN", "linphonePreferences_saveNewAccount"+ " "+prxCfg);
            if (tempContactsParams != null)
                prxCfg.setContactUriParameters(tempContactsParams);
            if (tempExpire != null) {
                try {
                    prxCfg.setExpires(Integer.parseInt(tempExpire));
                } catch (NumberFormatException nfe) { }
            }

            prxCfg.enableAvpf(tempAvpfEnabled);
            prxCfg.setAvpfRRInterval(tempAvpfRRInterval);
            prxCfg.enableQualityReporting(tempQualityReportingEnabled);
            prxCfg.setQualityReportingCollector(tempQualityReportingCollector);
            prxCfg.setQualityReportingInterval(tempQualityReportingInterval);

            if(tempRealm != null)
                prxCfg.setRealm(tempRealm);

            LinphoneAuthInfo authInfo = LinphoneCoreFactory.instance().createAuthInfo(tempUsername, tempUserId, tempPassword, null, null, tempDomain);
            android.util.Log.i("CNN", "linphonePreferences_saveNewAccount authINfo"+authInfo);
            lc.addProxyConfig(prxCfg);
            lc.addAuthInfo(authInfo);

            if (!tempNoDefault)
                lc.setDefaultProxyConfig(prxCfg);
        }
    }
    ///xong accountbuilder

    public void setAccountTransport(int n, String transport) {
        android.util.Log.i("CNN", "linphonePreferences_setAccountTranspot");
        LinphoneProxyConfig proxyConfig = getProxyConfig(n);

        if (proxyConfig != null && transport != null) {
            LinphoneAddress proxyAddr;
            try {
                proxyAddr = LinphoneCoreFactory.instance().createLinphoneAddress(proxyConfig.getProxy());
                int port = 0;
                if (transport.equals(getString(R.string.pref_transport_udp_key))) {
                    proxyAddr.setTransport(LinphoneAddress.TransportType.LinphoneTransportUdp);
                } else if (transport.equals(getString(R.string.pref_transport_tcp_key))) {
                    proxyAddr.setTransport(LinphoneAddress.TransportType.LinphoneTransportTcp);
                } else if (transport.equals(getString(R.string.pref_transport_tls_key))) {
                    proxyAddr.setTransport(LinphoneAddress.TransportType.LinphoneTransportTls);
                    port = 5223;
                }

                /* 3G mobile firewall might block random TLS port, so we force use of 5223.
                 * However we must NOT use this port when changing to TCP/UDP because otherwise
                  * REGISTER (and everything actually) will fail...
                  * */
                if ("sip.linphone.org".equals(proxyConfig.getDomain())) {
                    proxyAddr.setPort(port);
                }

                LinphoneProxyConfig prxCfg = getProxyConfig(n);
                prxCfg.edit();
                prxCfg.setProxy(proxyAddr.asStringUriOnly());
                prxCfg.done();

                if (isAccountOutboundProxySet(n)) {
                    setAccountOutboundProxyEnabled(n, true);
                }
            } catch (LinphoneCoreException e) {
                e.printStackTrace();
            }
        }
    }

    public LinphoneAddress.TransportType getAccountTransport(int n) {
        android.util.Log.i("CNN", "linphonePreferences_getAccountTranspot");
        LinphoneAddress.TransportType transport = null;
        LinphoneProxyConfig proxyConfig = getProxyConfig(n);

        if (proxyConfig != null) {
            LinphoneAddress proxyAddr;
            try {
                proxyAddr = LinphoneCoreFactory.instance().createLinphoneAddress(proxyConfig.getProxy());
                transport = proxyAddr.getTransport();
            } catch (LinphoneCoreException e) {
                e.printStackTrace();
            }
        }

        return transport;
    }

    public String getAccountTransportKey(int n) {
        android.util.Log.i("CNN", "linphonePreferences_getAccountTranspot int "+n);
        LinphoneAddress.TransportType transport = getAccountTransport(n);
        String key = getString(R.string.pref_transport_udp_key);

        if (transport != null && transport == LinphoneAddress.TransportType.LinphoneTransportTcp)
            key = getString(R.string.pref_transport_tcp_key);
        else if (transport != null && transport == LinphoneAddress.TransportType.LinphoneTransportTls)
            key = getString(R.string.pref_transport_tls_key);

        return key;
    }

    public String getAccountTransportString(int n) {
        android.util.Log.i("CNN", "linphonePreferences_getAccountTranspotString");
        LinphoneAddress.TransportType transport = getAccountTransport(n);

        if (transport != null && transport == LinphoneAddress.TransportType.LinphoneTransportTcp)
            return getString(R.string.pref_transport_tcp);
        else if (transport != null && transport == LinphoneAddress.TransportType.LinphoneTransportTls)
            return getString(R.string.pref_transport_tls);

        return getString(R.string.pref_transport_udp);
    }

    public void setAccountUsername(int n, String username) {
        android.util.Log.i("CNN", "linphonePreferences_seAcountUsername");
        String identity = "sip:" + username + "@" + getAccountDomain(n);
        LinphoneAuthInfo info = getClonedAuthInfo(n); // Get the auth info before editing the proxy config to ensure to get the correct auth info
        try {
            LinphoneProxyConfig prxCfg = getProxyConfig(n);
            prxCfg.edit();
            prxCfg.setIdentity(identity);
            prxCfg.done();

            if(info != null) {
                info.setUsername(username);
                saveAuthInfo(info);
            }
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
        }
    }

    public String getAccountUsername(int n) {
        android.util.Log.i("CNN", "linphonePreferences_getAccountUsername");
        LinphoneAuthInfo authInfo = getAuthInfo(n);
        return authInfo == null ? null : authInfo.getUsername();
    }

    public void setAccountDisplayName(int n, String displayName) {
        android.util.Log.i("CNN", "linphonePreferences_setAccountDisplayName");
        try {
            LinphoneProxyConfig prxCfg = getProxyConfig(n);
            LinphoneAddress addr = LinphoneCoreFactory.instance().createLinphoneAddress(prxCfg.getIdentity());
            addr.setDisplayName(displayName);
            prxCfg.edit();
            prxCfg.setIdentity(addr.asString());
            prxCfg.done();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getAccountDisplayName(int n) {
        android.util.Log.i("CNN", "linphonePreferences_getAccountDIsplayname");
        LinphoneAddress addr = getProxyConfig(n).getAddress();
        if(addr != null) {
            return addr.getDisplayName();
        }
        return null;
    }

    public void setAccountUserId(int n, String userId) {
        android.util.Log.i("CNN", "linphonePreferences_setAccountUserId");
        LinphoneAuthInfo info = getClonedAuthInfo(n);
        if(info != null) {
            info.setUserId(userId);
            saveAuthInfo(info);
        }
    }

    public String getAccountUserId(int n) {
        android.util.Log.i("CNN", "linphonePreferences_getAccountUserID");
        LinphoneAuthInfo authInfo = getAuthInfo(n);
        return authInfo == null ? null : authInfo.getUserId();
    }

    public void setAccountPassword(int n, String password) {
        android.util.Log.i("CNN", "linphonePreferences_setAccountPassword");
        if(getAccountDomain(n) != null && getAccountUsername(n) != null) {
            LinphoneAuthInfo authInfo = LinphoneCoreFactory.instance().createAuthInfo(getAccountUsername(n), null, password, null, null, getAccountDomain(n));
            LinphoneManager.getLc().addAuthInfo(authInfo);
        }
    }

    public String getAccountPassword(int n) {
        android.util.Log.i("CNN", "linphonePreferences_getAccountPassword");

        LinphoneAuthInfo authInfo = getAuthInfo(n);
        return authInfo == null ? null : authInfo.getPassword();
    }
    public void setAccountDomain(int n, String domain) {
        android.util.Log.i("CNN", "linphonePreferences_setAccountDomain");
        String identity = "sip:" + getAccountUsername(n) + "@" + domain;

        try {
            LinphoneAuthInfo authInfo = getClonedAuthInfo(n);
            if(authInfo != null) {
                authInfo.setDomain(domain);
                saveAuthInfo(authInfo);
            }

            LinphoneProxyConfig prxCfg = getProxyConfig(n);
            prxCfg.edit();
            prxCfg.setIdentity(identity);
            prxCfg.done();
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
        }
    }

    public String getAccountDomain(int n) {
        android.util.Log.i("CNN", "linphonePreferences_getAccountDomain");
        return getProxyConfig(n).getDomain();
    }

    public void setAccountProxy(int n, String proxy) {
        android.util.Log.i("CNN", "linphonePreferences_setAccountProxy");
        if (proxy == null || proxy.length() <= 0) {
            proxy = getAccountDomain(n);
        }

        if (!proxy.contains("sip:")) {
            proxy = "sip:" + proxy;
        }

        try {
            LinphoneAddress proxyAddr = LinphoneCoreFactory.instance().createLinphoneAddress(proxy);
            if (!proxy.contains("transport=")) {
                proxyAddr.setTransport(getAccountTransport(n));
            }

            LinphoneProxyConfig prxCfg = getProxyConfig(n);
            prxCfg.edit();
            prxCfg.setProxy(proxyAddr.asStringUriOnly());
            prxCfg.done();

            if (isAccountOutboundProxySet(n)) {
                setAccountOutboundProxyEnabled(n, true);
            }
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
        }
    }

    public String getAccountProxy(int n) {
        android.util.Log.i("CNN", "linphonePreferences_getAccountProxy");
        String proxy = getProxyConfig(n).getProxy();
        return proxy;
    }


    public void setAccountOutboundProxyEnabled(int n, boolean enabled) {
        android.util.Log.i("CNN", "linphonePreferences_setAccountOutboundProxyEnabled");
        try {
            LinphoneProxyConfig prxCfg = getProxyConfig(n);
            prxCfg.edit();
            if (enabled) {
                String route = prxCfg.getProxy();
                prxCfg.setRoute(route);
            } else {
                prxCfg.setRoute(null);
            }
            prxCfg.done();
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
        }
    }

    public boolean isAccountOutboundProxySet(int n) {
        android.util.Log.i("CNN", "linphonePreferences_boolisAccountOutboundProxySet");
        return getProxyConfig(n).getRoute() != null;
    }

    public void setAccountContactParameters(int n, String contactParams) {
        android.util.Log.i("CNN", "linphonePreferences_setAccountCOntactParameters");
        LinphoneProxyConfig prxCfg = getProxyConfig(n);
        prxCfg.edit();
        prxCfg.setContactUriParameters(contactParams);
        prxCfg.done();
    }

    public String getExpires(int n) {
        android.util.Log.i("CNN", "linphonePreferences_stringgetExpires");
        return String.valueOf(getProxyConfig(n).getExpires());
    }

    public void setExpires(int n, String expire) {
        android.util.Log.i("CNN", "linphonePreferences_setExpires");
        try {
            LinphoneProxyConfig prxCfg = getProxyConfig(n);
            prxCfg.edit();
            prxCfg.setExpires(Integer.parseInt(expire));
            prxCfg.done();
        } catch (NumberFormatException nfe) { }
    }

    public String getPrefix(int n) {
        android.util.Log.i("CNN", "linphonePreferences_stringgetPrefix");
        return getProxyConfig(n).getDialPrefix();
    }

    public void setPrefix(int n, String prefix) {
        android.util.Log.i("CNN", "linphonePreferences_setPrefix");
        LinphoneProxyConfig prxCfg = getProxyConfig(n);
        prxCfg.edit();
        prxCfg.setDialPrefix(prefix);
        prxCfg.done();
    }

    public boolean avpfEnabled(int n) {
        android.util.Log.i("CNN", "linphonePreferences_boolavpfEnabled");
        return getProxyConfig(n).avpfEnabled();
    }

    public void enableAvpf(int n, boolean enable) {
        android.util.Log.i("CNN", "linphonePreferences_enableAvpf");
        LinphoneProxyConfig prxCfg = getProxyConfig(n);
        prxCfg.edit();
        prxCfg.enableAvpf(enable);
        prxCfg.done();
    }

    public String getAvpfRRInterval(int n) {
        android.util.Log.i("CNN", "linphonePreferences_stringgetAvpfRRInterval");
        return String.valueOf(getProxyConfig(n).getAvpfRRInterval());
    }

    public void setAvpfRRInterval(int n, String interval) {
        android.util.Log.i("CNN", "linphonePreferences_setAvpfRRInterval");
        try {
            LinphoneProxyConfig prxCfg = getProxyConfig(n);
            prxCfg.edit();
            prxCfg.setAvpfRRInterval(Integer.parseInt(interval));
            prxCfg.done();
        } catch (NumberFormatException nfe) { }
    }

    public boolean getReplacePlusByZeroZero(int n) {
        android.util.Log.i("CNN", "linphonePreferences_boolgetREplacePlusByZeroZero");
        return getProxyConfig(n).getDialEscapePlus();
    }

    public void setReplacePlusByZeroZero(int n, boolean replace) {
        android.util.Log.i("CNN", "linphonePreferences_setReplacesPlusByZeroZero");
        LinphoneProxyConfig prxCfg = getProxyConfig(n);
        prxCfg.edit();
        prxCfg.setDialEscapePlus(replace);
        prxCfg.done();
    }

    public void setDefaultAccount(int accountIndex) {
        android.util.Log.i("CNN", "linphonePreferences_setDefaultAccount");
        LinphoneProxyConfig[] prxCfgs = getLc().getProxyConfigList();
        if (accountIndex >= 0 && accountIndex < prxCfgs.length)
            getLc().setDefaultProxyConfig(prxCfgs[accountIndex]);
    }

    public int getDefaultAccountIndex() {
        android.util.Log.i("CNN", "linphonePreferences_intgetDefaultAccountIndex");
        LinphoneProxyConfig defaultPrxCfg = getLc().getDefaultProxyConfig();
        if (defaultPrxCfg == null)
            return -1;

        LinphoneProxyConfig[] prxCfgs = getLc().getProxyConfigList();
        for (int i = 0; i < prxCfgs.length; i++) {
            if (defaultPrxCfg.getIdentity().equals(prxCfgs[i].getIdentity())) {
                return i;
            }
        }
        return -1;
    }

    public int getAccountCount() {
        android.util.Log.i("CNN", "linphonePreferences_intgetAccountCount");
        if (getLc() == null || getLc().getProxyConfigList() == null)
            return 0;

        return getLc().getProxyConfigList().length;
    }

    public void setAccountEnabled(int n, boolean enabled) {
        android.util.Log.i("CNN", "linphonePreferences_setAccountEnabled");
        LinphoneProxyConfig prxCfg = getProxyConfig(n);
        prxCfg.edit();
        prxCfg.enableRegister(enabled);
        prxCfg.done();

        // If default proxy config is disabled, try to set another one as default proxy
        if (!enabled && getLc().getDefaultProxyConfig().getIdentity().equals(prxCfg.getIdentity())) {
            int count = getLc().getProxyConfigList().length;
            if (count > 1) {
                for (int i = 0; i < count; i++) {
                    if (isAccountEnabled(i)) {
                        getLc().setDefaultProxyConfig(getProxyConfig(i));
                        break;
                    }
                }
            }
        }
    }

    public boolean isAccountEnabled(int n) {
        android.util.Log.i("CNN", "linphonePreferences_boolisAccountEnabled");
        return getProxyConfig(n).registerEnabled();
    }

    public void resetDefaultProxyConfig(){
        android.util.Log.i("CNN", "linphonePreferences_resetDefaultProxyCOnfig");
        int count = getLc().getProxyConfigList().length;
        for (int i = 0; i < count; i++) {
            if (isAccountEnabled(i)) {
                getLc().setDefaultProxyConfig(getProxyConfig(i));
                break;
            }
        }

        if(getLc().getDefaultProxyConfig() == null){
            getLc().setDefaultProxyConfig(getProxyConfig(0));
        }
    }

    public void deleteAccount(int n) {
        android.util.Log.i("CNN", "linphonePreferences_deleteAccount");
        final LinphoneProxyConfig proxyCfg = getProxyConfig(n);
        if (proxyCfg != null)
            getLc().removeProxyConfig(proxyCfg);
        if (getLc().getProxyConfigList().length != 0) {
            resetDefaultProxyConfig();
        } else {
            getLc().setDefaultProxyConfig(null);
        }
        getLc().refreshRegisters();
    }
    // End of accounts settings


        // Advanced settings
    public void setDebugEnabled(boolean enabled) {
        android.util.Log.i("CNN", "linphonePreferences_setDebugEnabled");
        getConfig().setBool("app", "debug", enabled);
        LinphoneCoreFactory.instance().enableLogCollection(enabled);
        LinphoneCoreFactory.instance().setDebugMode(enabled, getString(R.string.app_name));
    }

    public boolean isDebugEnabled()
    {android.util.Log.i("CNN", "linphonePreferences_boolisDebugEnabled");
        return getConfig().getBool("app", "debug", false);
    }
    public void setBackgroundModeEnabled(boolean enabled) {
        android.util.Log.i("CNN", "linphonePreferences_setBackgroundModeEnabled");
        getConfig().setBool("app", "background_mode", enabled);
    }

    public boolean isBackgroundModeEnabled() {
        android.util.Log.i("CNN", "linphonePreferences_boolisBackgroundModleEnabled");
        return getConfig().getBool("app", "background_mode", true);
    }

    public void setAnimationsEnabled(boolean enabled) {
        android.util.Log.i("CNN", "linphonePreferences_setAnimationsEnabled");
        getConfig().setBool("app", "animations", enabled);
    }

    public boolean areAnimationsEnabled() {
        android.util.Log.i("CNN", "linphonePreferences_boolareAnimationEnabled");
        return getConfig().getBool("app", "animations", false);
    }
    public boolean isAutoStartEnabled() {
        android.util.Log.i("CNN", "linphonePreferences_boolisAutoStartEnabled");
        return getConfig().getBool("app", "auto_start", false);
    }

    public void setAutoStart(boolean autoStartEnabled) {
        android.util.Log.i("CNN", "linphonePreferences_setAutoStart");
        getConfig().setBool("app", "auto_start", autoStartEnabled);
    }

    public String getSharingPictureServerUrl() {
        android.util.Log.i("CNN", "linphonePreferences_stringgetSharingPictureServerUrl");
        return getConfig().getString("app", "sharing_server", null);
    }

    public void setRemoteProvisioningUrl(String url) {
        android.util.Log.i("CNN", "linphonePreferences_setRemoteProvisioningUrl");
        if (url != null && url.length() == 0) {
            url = null;
        }
        getLc().setProvisioningUri(url);
    }

    public String getRemoteProvisioningUrl() {
        android.util.Log.i("CNN", "linphonePreferences_stringgetRemoteProvisioningUrl");
        return getLc().getProvisioningUri();
    }


    public void setSharingPictureServerUrl(String url) {
        android.util.Log.i("CNN", "linphonePreferences_setSharingPictureServerUrl");
        getConfig().setString("app", "sharing_server", url);
    }
    //Tunnel setting
    private TunnelConfig tunnelConfig = null;

    public TunnelConfig getTunnelConfig() {
        android.util.Log.i("CNN", "linphonePreferences_getTUnneCOnfig");
        if(getLc().isTunnelAvailable()) {
            if(tunnelConfig == null) {
                TunnelConfig servers[] = getLc().tunnelGetServers();
                if(servers.length > 0) {
                    tunnelConfig = servers[0];
                } else {
                    tunnelConfig = LinphoneCoreFactory.instance().createTunnelConfig();
                }
            }
            return tunnelConfig;
        } else {
            return null;
        }
    }
    public String getTunnelMode() {
        android.util.Log.i("CNN", "linphonePreferences_stringgetTunneMode");
        return getConfig().getString("app", "tunnel", null);
    }

    public void setTunnelMode(String mode) {
        android.util.Log.i("CNN", "linphonePreferences_setTUnneMode");
        getConfig().setString("app", "tunnel", mode);
        LinphoneManager.getInstance().initTunnelFromConf();
    }
    //Audio setting
    public void setEchoCancellation(boolean enable) {
        android.util.Log.i("CNN", "linphonePreferences_setEchoCanclellation");
        getLc().enableEchoCancellation(enable);
    }

    public boolean isEchoCancellationEnabled() {
        android.util.Log.i("CNN", "linphonePreferences_boolisEchoCanclellationEnabled");
        return getLc().isEchoCancellationEnabled();
    }
    public boolean isEchoConfigurationUpdated() {
        android.util.Log.i("CNN", "linphonePreferences_boolisEchoconfigurationUpdated");
        return getConfig().getBool("app", "ec_updated", false);
    }
                   //echo tieng vong
    public void echoConfigurationUpdated() {
        android.util.Log.i("CNN", "linphonePreferences_echoConfigurationUpdated");
        getConfig().setBool("app", "ec_updated", true);
    }
    // Video settings
    public boolean useFrontCam() {
        android.util.Log.i("CNN", "linphonePreferences_booluseFRontCam");
        return getConfig().getBool("app", "front_camera_default", true);
    }
    public void setFrontCamAsDefault(boolean frontcam) {
        android.util.Log.i("CNN", "linphonePreferences_setFrontCamDeFAULT");
        getConfig().setBool("app", "front_camera_default", frontcam);
    }
    // Network settings
    public void setWifiOnlyEnabled(Boolean enable) {
        android.util.Log.i("CNN", "linphonePreferences_setWifiOnlyEnabled");
        getConfig().setBool("app", "wifi_only", enable);
    }

    public boolean isWifiOnlyEnabled() {
        android.util.Log.i("CNN", "linphonePreferences_boolisWifiOnLyEnabled");
        return getConfig().getBool("app", "wifi_only", false);
    }
    public void useRandomPort(boolean enabled) {
        android.util.Log.i("CNN", "linphonePreferences_useRandomPort");
        useRandomPort(enabled, true);
    }
    public String getStunServer() {
        android.util.Log.i("CNN", "linphonePreferences_stringgetStunserver");
        return getLc().getStunServer();
    }

    public void setStunServer(String stun) {
        android.util.Log.i("CNN", "linphonePreferences_setStunServer");
        getLc().setStunServer(stun);
    }
    public void setIceEnabled(boolean enabled) {
        android.util.Log.i("CNN", "linphonePreferences_setIceEnabled");
        if (enabled) {
            getLc().setFirewallPolicy(LinphoneCore.FirewallPolicy.UseIce);
        } else {
            String stun = getStunServer();
            if (stun != null && stun.length() > 0) {
                getLc().setFirewallPolicy(LinphoneCore.FirewallPolicy.UseStun);
            } else {
                getLc().setFirewallPolicy(LinphoneCore.FirewallPolicy.NoFirewall);
            }
        }
    }

    public void useRandomPort(boolean enabled, boolean apply) {
        android.util.Log.i("CNN", "linphonePreferences_useRandomPort");
        getConfig().setBool("app", "random_port", enabled);
        if (apply) {
            if (enabled) {
                setSipPort(LINPHONE_CORE_RANDOM_PORT);
            } else {
                setSipPort(5060);
            }
        }
    }
    public void setSipPort(int port) {
        android.util.Log.i("CNN", "linphonePreferences_setSipPort");
        LinphoneCore.Transports transports = getLc().getSignalingTransportPorts();
        transports.udp = port;
        transports.tcp = port;
        transports.tls = LINPHONE_CORE_RANDOM_PORT;
        getLc().setSignalingTransportPorts(transports);
    }


    //////////////////////MOi Them/////////////////////////////////
    public boolean isVideoEnabled() {
        android.util.Log.i("CNN", "linphonePreferences_boolisVideoEnabled");
        return getLc().isVideoSupported() && getLc().isVideoEnabled();
    }
    public boolean shouldInitiateVideoCall() {
        android.util.Log.i("CNN", "linphonePreferences_boolshouldInitiaVideoCall");
        return getLc().getVideoAutoInitiatePolicy();
    }

    public void setInitiateVideoCall(boolean initiate) {
        android.util.Log.i("CNN", "linphonePreferences_seInitiaatteVIdeoCall "+initiate);
        getLc().setVideoPolicy(initiate, shouldAutomaticallyAcceptVideoRequests());
    }

    public boolean shouldAutomaticallyAcceptVideoRequests() {
        android.util.Log.i("CNN", "linphonePreferences_boolshouldAutomicticalAcceptVideoRequests");
        return getLc().getVideoAutoAcceptPolicy();
    }
    public void neverAskAudioPerm()
    {     android.util.Log.i("CNN", "linphonePreferences_neverAskAudioPerm");
        getConfig().setBool("app", "audio_perm", true);
    }
    public Boolean audioPermAsked(){
        android.util.Log.i("CNN", "linphonePreferences_boolaudioPermAsked");
        return getConfig().getBool("app", "audio_perm", false);
    }
    public Boolean cameraPermAsked(){
        android.util.Log.i("CNN", "linphonePreferences_boolcameraPermAsked");
        return getConfig().getBool("app", "camera_perm", false);
    }
    public void neverAskCameraPerm(){
        android.util.Log.i("CNN", "linphonePreferencesneverAskCameraPerm");
        getConfig().setBool("app", "camera_perm", true);
    }
    public String getRingtone(String defaultRingtone) {
        android.util.Log.i("CNN", "linphonePreferences_stringgetRIngtone");
        String ringtone = getConfig().getString("app", "ringtone", defaultRingtone);
        if (ringtone == null || ringtone.length() == 0)
            ringtone = defaultRingtone;
        return ringtone;
    }

    public void setRingtone(String ringtonePath) {
        android.util.Log.i("CNN", "linphonePreferences_setRIngtone");
        getConfig().setString("app", "ringtone", ringtonePath);

    }
    public boolean isDeviceRingtoneEnabled() {
        int readExternalStorage = mContext.getPackageManager().checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, mContext.getPackageName());
        return getConfig().getBool("app", "device_ringtone", true) && readExternalStorage == PackageManager.PERMISSION_GRANTED;
    }

    public void enableDeviceRingtone(boolean enable) {
        getConfig().setBool("app", "device_ringtone", enable);
    }

    public boolean isAutoAnswerEnabled() {
        return getConfig().getBool("app", "auto_answer", false);
    }

    public void enableAutoAnswer(boolean enable) {
        getConfig().setBool("app", "auto_answer", enable);
    }

    public void setAutoAnswerTime(int time) {
        getConfig().setInt("app", "auto_answer_delay", time);
    }

    public int getAutoAnswerTime() {
        return getConfig().getInt("app", "auto_answer_delay", 0);
    }
    public boolean firstTimeAskingForPermission(String permission) {
        return firstTimeAskingForPermission(permission, true);
    }

    public boolean firstTimeAskingForPermission(String permission, boolean toggle) {
        boolean firstTime = getConfig().getBool("app", permission, true);
        if (toggle) {
            permissionHasBeenAsked(permission);
        }
        return firstTime;
    }
    public void permissionHasBeenAsked(String permission) {
        getConfig().setBool("app", permission, false);
    }
    public boolean isDozeModeEnabled() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return false;
        return getConfig().getBool("app", "doze_mode", true);
    }

    public void enableDozeMode(boolean enable) {
        getConfig().setBool("app", "doze_mode", enable);
    }

    public String getXmlrpcUrl(){
        return getConfig().getString("assistant", "xmlrpc_url", null);
    }

    public void setXmlrpcUrl(String url){
        getConfig().setString("assistant", "xmlrpc_url", url);
    }


    public boolean isProvisioningLoginViewEnabled() {

        return (getConfig() != null) ? getConfig().getBool("app", "show_login_view", false) : false;
    }

    public void disableProvisioningLoginView() {
        if (isProvisioningLoginViewEnabled()) { // Only do it if it was previously enabled
            getConfig().setBool("app", "show_login_view", false);
        } else {
            Log.w("Remote provisioning login view wasn't enabled, ignoring");
        }
    }
    public void setAutomaticallyAcceptVideoRequests(boolean accept) {
        getLc().setVideoPolicy(shouldInitiateVideoCall(), accept);
    }

//network
    public void setMediaEncryption(LinphoneCore.MediaEncryption menc) {
        if (menc == null)
            return;

        getLc().setMediaEncryption(menc);
    }
    public void setTurnEnabled(boolean enabled) {
        LinphoneNatPolicy nat = getOrCreateNatPolicy();
        nat.enableTurn(enabled);
        getLc().setNatPolicy(nat);
    }
    private LinphoneNatPolicy getOrCreateNatPolicy() {
        LinphoneNatPolicy nat = getLc().getNatPolicy();
        if (nat == null) {
            nat = getLc().createNatPolicy();
        }
        return nat;
    }
    public void useIpv6(Boolean enable) {
        getLc().enableIpv6(enable);
    }
    public void setUpnpEnabled(boolean enabled) {
        LinphoneNatPolicy nat = getOrCreateNatPolicy();
        nat.enableUpnp(enabled);
        getLc().setNatPolicy(nat);
    }
    public void useRandomPort1(boolean enabled) {
        useRandomPort2(enabled, true);
    }

    public void useRandomPort2(boolean enabled, boolean apply) {
        getConfig().setBool("app", "random_port", enabled);
        if (apply) {
            if (enabled) {
                setSipPort1(LINPHONE_CORE_RANDOM_PORT);
            } else {
                setSipPort1(5060);
            }
        }
    }

    public void setSipPort1(int port) {
        LinphoneCore.Transports transports = getLc().getSignalingTransportPorts();
        transports.udp = port;
        transports.tcp = port;
        transports.tls = LINPHONE_CORE_RANDOM_PORT;
        getLc().setSignalingTransportPorts(transports);
    }


    //audio
    public int getCodecBitrateLimit() {
        return getConfig().getInt("audio", "codec_bitrate_limit", 36);
    }
}


