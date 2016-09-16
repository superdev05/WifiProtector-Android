package com.wifiprotector.android.activities;

import android.content.Intent;
import android.os.Bundle;

import com.wifiprotector.android.Preferences;
import com.wifiprotector.android.VPNApplication;
import com.wifiprotector.android.iab.IabHelper;
import com.wifiprotector.android.iab.IabResult;
import com.wifiprotector.android.iab.Inventory;
import com.wifiprotector.android.iab.Purchase;
import com.wifiprotector.android.iab.SkuDetails;
import com.wifiprotector.android.log.SecuredLogManager;

import java.util.LinkedList;
import java.util.List;

import de.blinkt.openvpn.BuildConfig;
import hugo.weaving.DebugLog;
import timber.log.Timber;

public abstract class BaseBillingActivity extends BaseActivity {
    public static final String SKU_FULL_VERSION_MONTHLY = "full_version_monthly_live";
    public static final String SKU_FULL_VERSION_YEARLY = "full_version_yearly_live";

    protected IabHelper iabHelper;
    protected Purchase subscriptionPurchase;
    protected SkuDetails monthlySubscriptionDetails;
    protected SkuDetails yearlySubscriptionDetails;

    IabHelper.QueryInventoryFinishedListener gotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (iabHelper == null) {
                return;
            }

            if (result == null || inventory == null)
                return;

            if (result.isFailure()) {
                SecuredLogManager.getLogManager().writeLog(6, "WFProctector", "Failed to query inventory: " + result);
                return;
            }

            monthlySubscriptionDetails = inventory.getSkuDetails(SKU_FULL_VERSION_MONTHLY);
            yearlySubscriptionDetails = inventory.getSkuDetails(SKU_FULL_VERSION_YEARLY);
            SecuredLogManager.getLogManager().writeLog(3, "WFProctector", "Query inventory was successful.");

            final boolean hasMonthlySubscription = inventory.hasPurchase(SKU_FULL_VERSION_MONTHLY);
            if (hasMonthlySubscription) {
                subscriptionPurchase = inventory.getPurchase(SKU_FULL_VERSION_MONTHLY);
            }

            final boolean hasYearlySubscription = inventory.hasPurchase(SKU_FULL_VERSION_YEARLY);
            if (hasYearlySubscription) {
                subscriptionPurchase = inventory.getPurchase(SKU_FULL_VERSION_YEARLY);
            }

            Preferences.Subscription.isActive(hasMonthlySubscription || hasYearlySubscription);

            onInventoryQueryFinished();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Preferences.WifiProtector.hasLicenceKey()) {
            initInAppBilling();
        }
    }

    private void initInAppBilling() {
        iabHelper = new IabHelper(this, VPNApplication.getPublicLicenceKey());
        iabHelper.enableDebugLogging(BuildConfig.DEBUG);

        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    SecuredLogManager.getLogManager().writeLog(3, "WFProctector", "Problem setting up In-app Billing: " + result);
                    return;
                }

                List<String> skus = new LinkedList<String>();
                skus.add(SKU_FULL_VERSION_MONTHLY);
                skus.add(SKU_FULL_VERSION_YEARLY);
                iabHelper.queryInventoryAsync(true, skus, gotInventoryListener);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (iabHelper == null) {
            return;
        }

        if (!iabHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (iabHelper != null) {
            iabHelper.dispose();
        }
        iabHelper = null;
    }

    @DebugLog
    protected void onFullVersionRegistered() {
        invalidateOptionsMenu();
    }

    protected void onInventoryQueryFinished() {
    }
}
