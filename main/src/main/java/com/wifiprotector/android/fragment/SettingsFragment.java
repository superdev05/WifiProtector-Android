package com.wifiprotector.android.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.view.View;

import com.wifiprotector.android.Preferences;
import com.wifiprotector.android.activities.SettingsActivity;
import com.wifiprotector.android.iab.Purchase;
import com.wifiprotector.android.iab.SkuDetails;

import de.blinkt.openvpn.R;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof SettingsActivity)) {
            throw new RuntimeException("SettingsFragment must be attached to SettingsActivity");
        }
    }

    private SettingsActivity getSettingsActivity() {
        return (SettingsActivity) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateSubscriptionInfoView();
        initLicenceView();
    }

    private void initLicenceView() {
        PreferenceCategory licenceCategory = (PreferenceCategory) findPreference("category_licence");
        Preference licencePreference = findPreference("licence");

        if (Preferences.WifiProtector.hasLicenceKey()) {
            licencePreference.setTitle(getString(R.string.licence_key));
            licencePreference.setSummary(Preferences.WifiProtector.licenceKey());
        } else {
            getPreferenceScreen().removePreference(licenceCategory);
        }
    }

    private void updateSubscriptionInfoView() {
        PreferenceCategory subscriptionCategory = (PreferenceCategory) findPreference("category_subscription");
        Preference subscriptionPreference = findPreference("subscription");

        Purchase subscriptionPurchase = getSettingsActivity().getSubscriptionPurchase();
        if (subscriptionPurchase != null) {
            SkuDetails monthlySubscriptionDetails = getSettingsActivity().getMonthlySubscriptionDetails();
            SkuDetails yearlySubscriptionDetails = getSettingsActivity().getYearlySubscriptionDetails();

            String title = "";
            String desc = "";
            if (subscriptionPurchase.getSku().equals(SettingsActivity.SKU_FULL_VERSION_MONTHLY)) {
                title = monthlySubscriptionDetails.getTitle();
                desc = monthlySubscriptionDetails.getDescription();
            } else if (subscriptionPurchase.getSku().equals(SettingsActivity.SKU_FULL_VERSION_YEARLY)) {
                title = yearlySubscriptionDetails.getTitle();
                desc = yearlySubscriptionDetails.getDescription();
            }

            if (subscriptionPurchase.getPurchaseState() == 1) {
                title += " (" + getString(R.string.subscription_cancelled) + ")";
            }

            subscriptionPreference.setTitle(title);
            subscriptionPreference.setSummary(desc);
        } else {
            getPreferenceScreen().removePreference(subscriptionCategory);
        }
    }
}
