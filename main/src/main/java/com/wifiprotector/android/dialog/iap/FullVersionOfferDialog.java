package com.wifiprotector.android.dialog.iap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;

import de.blinkt.openvpn.R;

public class FullVersionOfferDialog extends SubscriptionChooserDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.upgrade_to_full_version))
                .setView(buildView(R.layout.dialog_full_version_offer))
                .setNeutralButton(getString(R.string.not_now), null).create();
    }
}
