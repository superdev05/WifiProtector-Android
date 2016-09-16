package com.wifiprotector.android.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.wifiprotector.android.Preferences;
import com.wifiprotector.android.VPNApplication;
import com.wifiprotector.android.adapters.VPNServersAdapter;
import com.wifiprotector.android.api.RegisterUser3Request;
import com.wifiprotector.android.api.RegisterUser3Response;
import com.wifiprotector.android.dialog.AboutDialogFragment;
import com.wifiprotector.android.dialog.ActivationDialogFragment;
import com.wifiprotector.android.dialog.MessageDialogFragment;
import com.wifiprotector.android.dialog.ProgressDialogFragment;
import com.wifiprotector.android.dialog.ReportDialog;
import com.wifiprotector.android.dialog.iap.FullVersionOfferDialog;
import com.wifiprotector.android.dialog.iap.SubscriptionChooserDialogFragment;
import com.wifiprotector.android.dialog.iap.TimeLimitExceededDialog;
import com.wifiprotector.android.iab.IabHelper;
import com.wifiprotector.android.iab.IabResult;
import com.wifiprotector.android.iab.Purchase;
import com.wifiprotector.android.log.SecuredLogManager;
import com.wifiprotector.android.model.VPNCountry;
import com.wifiprotector.android.model.VPNServer;
import com.wifiprotector.android.model.VPNServerList;
import com.wifiprotector.android.model.exception.TimeoutException;
import com.wifiprotector.android.utils.OvpnProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import de.blinkt.openvpn.R;
import de.blinkt.openvpn.VpnProfile;
import hugo.weaving.DebugLog;
import timber.log.Timber;

public class MainActivity extends BaseVpnActivity implements
        ActivationDialogFragment.ActivationListener,
        SubscriptionChooserDialogFragment.SubscribeListener {

    private static final int PURCHASE_REQUEST_CODE = 10;

    public static String EXTRA_BUY_FULL_VERSION = "extraBuyFullVersion";
    public static String EXTRA_FREE_TIME_EXCEEDED = "extraFreeTimeExceeded";
    public static String EXTRA_ENTER_LICENSE = "extraEnterLicense";

    private View noInternetView;
    private View autoSelectionView;
    private Button actionButton;
    private Spinner serversSpinner;
    private VPNServersAdapter serversAdapter;
    private ViewAnimator spinnerAnimator;
    private ViewAnimator lockAnimator;

    private VpnProfile vpnProfile;

    private List<VPNCountry> vpnCountries = new ArrayList<VPNCountry>();

    private VPNServerList servers = new VPNServerList();
    private ListIterator<VPNServer> serversIterator;
    private VPNServer currentServer;
    private VPNCountry currentCountry = new VPNCountry();
    private FullVersionOfferDialog fullVersionOfferDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setElevation(0);

        onNewIntent(getIntent());

        SecuredLogManager.getLogManager().setMainActivity(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null && isFreeUser()) {
            if (extras.getBoolean(EXTRA_BUY_FULL_VERSION)) {
                showFullVersionOfferDialog();
            } else if (extras.getBoolean(EXTRA_ENTER_LICENSE)) {
                showActivationDialog();
            } else if (extras.getBoolean(EXTRA_FREE_TIME_EXCEEDED)) {
                stopVpn();
                showTimeLimitExceededDialog();
            }
        }
    }

    private void showFullVersionOfferDialog() {
        fullVersionOfferDialog = new FullVersionOfferDialog();
        fullVersionOfferDialog.showAllowingStateLoss(getSupportFragmentManager());
    }

    private void showFullVersionPurchasedDialog() {
        MessageDialogFragment.newInstance(getString(R.string.activated),
                getString(R.string.account_activated)).showAllowingStateLoss(getSupportFragmentManager());
    }

    private void showActivationDialog() {
        new ActivationDialogFragment().showAllowingStateLoss(getSupportFragmentManager());
    }

    private void showAboutDialog() {
        new AboutDialogFragment().show(getSupportFragmentManager(), AboutDialogFragment.TAG);
    }

    private void showConnectionTimeoutToast() {
        String text;
        if (isAutoSelection()) {
            text = getString(R.string.servers_are_not_responding_toast);
        } else {
            text = getString(R.string.server_is_not_responding_toast);
        }

        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SecuredLogManager.getLogManager().writeLog(3, "WFProctector", "WIFI_PROTECTOR STARTED");
        if (Preferences.WifiProtector.hasLicenceKey() && Preferences.WifiProtector.licenceKeyExpired()) {
            onWifiProtectorLicenceCodeExpired();
            SecuredLogManager.getLogManager().writeLog(3, "WFProctector", "WIFI_PROTECTOR LICENCE CODE EXPIRED");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.test_consume_iap).setVisible(VPNApplication.isDebug());
        menu.findItem(R.id.enter_licence).setVisible(isFreeUser());
        menu.findItem(R.id.test_consume_iap).setVisible(false);
        menu.findItem(R.id.settings).setVisible(!isFreeUser());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.enter_licence: {
                showActivationDialog();
                return true;
            }
            case R.id.settings: {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            case R.id.test_consume_iap: {
                consumeFullVersion();
                return true;
            }
            case R.id.support: {
                openSupportPage();
                return true;
            }
            case R.id.about: {
                showAboutDialog();
                return true;
            }
            case R.id.sendLog:{
                //SecuredLogManager.getLogManager().sendLog();
                showReportDialog();
                return true;
            }
            case R.id.buyLicense:{
                showFullVersionOfferDialog();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showReportDialog(){
        ReportDialog dialog = new ReportDialog(this);
        dialog.setActivity(this);
        dialog.setVPNCountries(vpnCountries);
        dialog.show();
    }

    private void openSupportPage() {
        //String url = getString(R.string.support_url);
        String url = getString(R.string.support_url_mobile);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);

    }

    /**
     * Used only in Stage builds for testing purposes
     */
    private void consumeFullVersion() {
        Preferences.Subscription.isActive(false);
        Preferences.WifiProtector.clearLicenceKey();

        if (subscriptionPurchase != null) {
            iabHelper.consumeAsync(subscriptionPurchase, new IabHelper.OnConsumeFinishedListener() {
                @Override
                public void onConsumeFinished(Purchase purchase, IabResult result) {
                    SecuredLogManager.getLogManager().writeLog(3, "WFProctector", "Premium consumed");
                    subscriptionPurchase = null;
                    invalidateOptionsMenu();
                }
            });
        }
    }

    private int nSelectedPosition = 0;
    @Override
    protected void initView() {
        setContentView(R.layout.activity_main);

        serversSpinner = (Spinner) findViewById(R.id.servers);
        serversAdapter = new VPNServersAdapter(this, vpnCountries);
        serversSpinner.setAdapter(serversAdapter);
        serversSpinner.setOnItemSelectedListener(m_serverSelectedListener);

        lockAnimator = (ViewAnimator) findViewById(R.id.lock_animator);
        spinnerAnimator = (ViewAnimator) findViewById(R.id.spinner_animator);
        noInternetView = findViewById(R.id.no_internet);
        autoSelectionView = findViewById(R.id.auto_selection_description);

        actionButton = (Button) findViewById(R.id.connect_disconnect);
        actionButton.setOnClickListener(m_actionButtonClickListener);

        findViewById(R.id.refresh).setOnClickListener(m_RefreshClickListener);
        updateView();
    }

    private void actionConnect(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isConnected()) {
                    onDisconnect();
                } else {
                    onConnect();
                }
            }
        });

    }

    @DebugLog
    private void connect() throws Exception {
        currentServer = serversIterator.next();
        String strVPN = Preferences.WifiProtector.vpnProfile();

        SecuredLogManager.getLogManager().writeLog(7, "VPN_PROFILE", "PROFILE_START\n");
        SecuredLogManager.getLogManager().writeLog(7, "VPN_PROFILE", strVPN);
        SecuredLogManager.getLogManager().writeLog(7, "VPN_PROFILE", "PROFILE_END\n");
        vpnProfile = OvpnProfile.create(Preferences.WifiProtector.vpnProfile(), currentServer);

        SecuredLogManager.getLogManager().writeLog(3, "WFProctector", "WIFI_PROTECTOR STARTING VPN...");
        asyncStartVpn(vpnProfile);
    }

    @Override
    protected void onStatusUpdated() {
        updateView();
    }

    private void updateView() {
        serversAdapter.notifyDataSetChanged();
        serversSpinner.setEnabled(true);

        switch (getStatus()) {
            case SERVERS_INITIALIZING: {
                spinnerAnimator.setDisplayedChild(0);

                actionButton.setText(R.string.updating_servers);
                actionButton.setEnabled(false);

                updateAutoSelectionView(false);

                break;
            }
            case WP_INITIALIZED: {
                actionButton.setVisibility(View.VISIBLE);

                break;
            }
            case SERVERS_INITIALIZED:
            case VPN_NOT_CONNECTED: {
                if (isFreeUser()) {
                    serversSpinner.setSelection(VPNServersAdapter.AUTO_SELECTION_POSITION);
                }

                spinnerAnimator.setDisplayedChild(1);
                lockAnimator.setDisplayedChild(0);

                actionButton.setVisibility(View.VISIBLE);
                actionButton.setText(R.string.connect);
                actionButton.setEnabled(isOnline() && !serversAdapter.isEmpty());

                updateAutoSelectionView(true);

                break;
            }
            case VPN_CONNECTING: {
                spinnerAnimator.setDisplayedChild(0);

                actionButton.setText(R.string.state_connecting);
                actionButton.setEnabled(false);

                updateAutoSelectionView(false);

                break;
            }
            case VPN_CONNECTED: {
                spinnerAnimator.setDisplayedChild(1);
                lockAnimator.setDisplayedChild(1);

                serversSpinner.setEnabled(false);

                actionButton.setText(R.string.disconnect);
                actionButton.setEnabled(true);

                updateAutoSelectionView(false);

                break;
            }
            default: {
                spinnerAnimator.setDisplayedChild(0);
                lockAnimator.setDisplayedChild(0);

                actionButton.setVisibility(View.GONE);
                updateAutoSelectionView(false);
            }
        }
    }

    private void updateAutoSelectionView(boolean canShow) {
        if (canShow && isAutoSelectionSelected()) {
            autoSelectionView.setVisibility(View.VISIBLE);
        } else {
            autoSelectionView.setVisibility(View.GONE);
        }
    }

    private boolean isAutoSelectionSelected() {
        VPNCountry country = (VPNCountry) serversSpinner.getSelectedItem();
        return country != null && country.isAutoSelection();
    }

    private VPNServerList getAllServers() {
        VPNServerList servers = new VPNServerList();
        for (VPNCountry country : vpnCountries) {
            servers.addAll(country.getServers());
        }
        servers.sortByPriority();

        return servers;
    }

    private VPNCountry getSelectedCountry() {
        return (VPNCountry) serversSpinner.getSelectedItem();
    }

    private void updateSpinner(VPNCountry country) {
        serversSpinner.setSelection(vpnCountries.indexOf(country) + 1, true);
    }

    @Override
    public void onActivate(String key) {
        showProgressDialog();
        spiceManager.execute(new RegisterUser3Request(key, true), new RequestListener<RegisterUser3Response>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                MessageDialogFragment.newInstance(getString(R.string.error), getString(R.string.unknown_error)).showAllowingStateLoss(getSupportFragmentManager());
            }

            @Override
            public void onRequestSuccess(RegisterUser3Response response) {
                dismissProgressDialog();
                String title = null;
                String message = null;

                switch (response.getRegisterStatus()) {
                    case LICENCE_EXPIRED:
                        title = getString(R.string.error);
                        message = getString(R.string.licence_expired);
                        break;
                    case KEY_NOT_VALID:
                        title = getString(R.string.error);
                        message = getString(R.string.licence_not_valid);
                        break;
                    case LICENCE_VALID:
                        title = getString(R.string.activated);
                        message = getString(R.string.account_activated);
                        onFullVersionRegistered();
                        break;
                }

                MessageDialogFragment.newInstance(title, message).showAllowingStateLoss(getSupportFragmentManager());
            }
        });
    }

    @Override
    protected void onFullVersionRegistered() {
        super.onFullVersionRegistered();

        SecuredLogManager.getLogManager().writeLog(3, "WFProctector", "WIFI_PROTECTOR FULL Version Registered!");
        invalidateOptionsMenu();
        reinitWifiProtector();
    }

    @Override
    protected void onWifiProtectorLicenceCodeExpired() {
        super.onWifiProtectorLicenceCodeExpired();

        MessageDialogFragment.newInstance(getString(R.string.licence_key_expired),
                getString(R.string.licence_key_expired_desc)).showAllowingStateLoss(getSupportFragmentManager());
    }

    private void showProgressDialog() {
        new ProgressDialogFragment().showAllowingStateLoss(getSupportFragmentManager());
    }

    private void dismissProgressDialog() {
        ProgressDialogFragment.dismiss(getSupportFragmentManager());
    }

    @Override
    public void onSubscriptionChosen(String sku) {
        iabHelper.launchPurchaseFlow(this, sku, IabHelper.ITEM_TYPE_SUBS, PURCHASE_REQUEST_CODE, new IabHelper.OnIabPurchaseFinishedListener() {

            @Override
            public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                if (result.isFailure()) {
                    SecuredLogManager.getLogManager().writeLog(4, "WFProctector", "Error purchasing: " + result);
                } else if (purchase.getSku().equals(SKU_FULL_VERSION_MONTHLY) || purchase.getSku().equals(SKU_FULL_VERSION_YEARLY)) {
                    subscriptionPurchase = purchase;
                    Preferences.Subscription.isActive(true);

                    if (fullVersionOfferDialog != null) {
                        fullVersionOfferDialog.dismiss();
                    }
                    showFullVersionPurchasedDialog();

                    onFullVersionRegistered();
                }
            }
        }, null);
    }

    @Override
    protected void onConnectivityChanged() {
        if (isOnline()) {
            noInternetView.setVisibility(View.GONE);
            actionButton.setEnabled(true);
        } else {
            noInternetView.setVisibility(View.VISIBLE);
            actionButton.setEnabled(false);
        }
    }

    @Override
    protected void onWifiProtectorServersUpdated(VPNServerList servers) {
        super.onWifiProtectorServersUpdated(servers);

        if (servers.isEmpty()) {
            showRefreshView(true);
        } else {
            showRefreshView(false);

            updateCountries(servers);
            updateCurrentServers();
        }
    }

    private void updateCountries(VPNServerList servers) {
        vpnCountries.clear();
        vpnCountries.addAll(servers.getServersGroupedByCountry());
        serversAdapter.notifyDataSetChanged();
    }

    private void updateCurrentServers() {
        VPNServerList servers;
        if (isAutoSelection()) {
            servers = getAllServers();
        } else {
            servers = getSelectedCountry().getServers();
        }

        this.servers.clear();
        this.servers.addAll(servers);
        serversIterator = this.servers.listIterator();

        SecuredLogManager.getLogManager().writeLog(4, "WFProctector", String.format("Selected servers: %s", this.servers.toString()));
    }

    private void showRefreshView(boolean show) {
        ((ViewAnimator) findViewById(R.id.main_animator)).setDisplayedChild(show ? 1 : 0);
    }

    @DebugLog
    public void onVpnConnectionTimeout(TimeoutException e) {
        SecuredLogManager.getLogManager().writeLog(6, "WFProctector", String.format("Vpn profile: %s", vpnProfile.getConfigFile(this, false)));
        SecuredLogManager.getLogManager().writeLog(6, "WFProctector", e.getMessage());

        try {
            connect();
        } catch (Exception e2) { // no more servers
            showConnectionTimeoutToast();
            stopVpn();
        }
    }

    @Override
    public void onVpnConnected() {
        super.onVpnConnected();

        if(currentCountry.isAutoSelection())
            updateSpinner(VPNCountry.findByServer(vpnCountries, currentServer));
        else
            updateSpinner(currentCountry);

        SecuredLogManager.getLogManager().writeLog(4, "WFProctector", String.format("Connected: %s", currentServer));
    }

    @Override
    public void onVpnConnected(String ip) {
        super.onVpnConnected();

    }

    @Override
    public void onVpnNotConnected(TimeoutException e) {
        if (e != null) {
            e.setServer(currentServer);
            onVpnConnectionTimeout(e);
        } else {
            stopVpn();

            if (isAutoSelection()) {
                updateWifiProtectorServers();
            }
        }
    }

    private void onConnect() {
        if (isTimeLimitExceeded()) {
            showTimeLimitExceededDialog();
            return;
        }
        if (serversSpinner.getSelectedItem() == null) {
            return;
        }

        updateCurrentServers();

        try {
            onVpnConnecting();
            connect();
        } catch (Exception e) {
            onVpnNotConnected(null);
            SecuredLogManager.getLogManager().writeLog(6, "WFProctector", e.getMessage());
        }
    }

    private boolean isAutoSelection() {
        VPNCountry country = getSelectedCountry();
        return country == null || country.isAutoSelection();
    }

    private void onDisconnect() {
        stopVpn();
    }

    private void onRefresh() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (getAllServers().isEmpty()) {
                    initWifiProtector();
                } else {
                    updateWifiProtectorServers();
                }
            }
        });

    }

    //Nigel
    public void onBackPressed() {
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }

    public void showToast(boolean bSuccess) {
        Toast confirmToast;

        if (bSuccess) {
            confirmToast = Toast.makeText(this, "Log files were sent successfully!", Toast.LENGTH_LONG);
        }else{
            confirmToast = Toast.makeText(this, "Failed to send log files!", Toast.LENGTH_LONG);
        }

        confirmToast.setGravity(Gravity.CENTER , 0, 0);
        confirmToast.show();
    }

    public Handler m_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0 )
                showToast(true);
            else
                showToast(false);
        }
    };


    //click listeners
    View.OnClickListener m_actionButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            actionConnect();
        }
    };

    View.OnClickListener m_RefreshClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onRefresh();
        }
    };

    AdapterView.OnItemSelectedListener m_serverSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            nSelectedPosition = position;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!isConnected()) {
                        VPNCountry country = (VPNCountry) serversSpinner.getSelectedItem();
                        if (isFreeUser() && nSelectedPosition != Spinner.INVALID_POSITION && !country.isAutoSelection()) {
                            showFullVersionOfferDialog();
                            serversSpinner.setSelection(VPNServersAdapter.AUTO_SELECTION_POSITION);
                        }
                        currentCountry = country;
                    }
                    updateView();
                }
            });
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
}
