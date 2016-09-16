package com.wifiprotector.android.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import de.blinkt.openvpn.R;

public class ProgressDialogFragment extends BaseDialogFragment {
    private static final String TAG = "ProgressDialog";
    private static final String ARG_MESSAGE = "message";

    public ProgressDialogFragment() {
    }

    public ProgressDialogFragment newInstance(String message) {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.activating));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public static void dismiss(FragmentManager fragmentManager) {
        DialogFragment frag = (DialogFragment) fragmentManager.findFragmentByTag(TAG);
        if (frag != null) {
            frag.dismissAllowingStateLoss();
        }
    }

    @Override
    public void showAllowingStateLoss(FragmentManager manager) {
        super.showAllowingStateLoss(manager, TAG);
    }
}
