package com.wifiprotector.android.activities;

import android.os.Bundle;

import com.wifiprotector.android.fragment.SettingsFragment;
import com.wifiprotector.android.iab.Purchase;
import com.wifiprotector.android.iab.SkuDetails;

import de.blinkt.openvpn.R;

public class SettingsActivity extends BaseBillingActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        showSettingsFragment();
    }

    @Override
    protected void onInventoryQueryFinished() {
        super.onInventoryQueryFinished();
        showSettingsFragment();
    }

    private void showSettingsFragment() {
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public Purchase getSubscriptionPurchase() {
        return subscriptionPurchase;
    }

    public SkuDetails getMonthlySubscriptionDetails() {
        return monthlySubscriptionDetails;
    }

    public SkuDetails getYearlySubscriptionDetails() {
        return yearlySubscriptionDetails;
    }
}
