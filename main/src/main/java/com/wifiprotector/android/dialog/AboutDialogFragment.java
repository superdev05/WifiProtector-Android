package com.wifiprotector.android.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import de.blinkt.openvpn.BuildConfig;
import de.blinkt.openvpn.R;

public class AboutDialogFragment extends DialogFragment {
    public static final String TAG = AboutDialogFragment.class.getName();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.about_dialog, null);
        ((TextView) view.findViewById(R.id.version)).setText(BuildConfig.VERSION_NAME);

        return new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.about))
                .setView(view)
                .create();
    }
}
