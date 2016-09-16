package com.wifiprotector.android.dialog.iap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;

import de.blinkt.openvpn.R;

public class TimeLimitExceededDialog extends SubscriptionChooserDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.vpn_is_off))
                .setView(buildView(R.layout.dialog_time_limit_exceeded)).create();
    }
}
