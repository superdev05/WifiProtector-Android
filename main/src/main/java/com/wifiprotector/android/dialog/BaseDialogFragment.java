package com.wifiprotector.android.dialog;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public abstract class BaseDialogFragment extends DialogFragment {

    public void showAllowingStateLoss(FragmentManager manager) {
        showAllowingStateLoss(manager, null);
    }

    public void showAllowingStateLoss(FragmentManager manager, String tag) {
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commitAllowingStateLoss();
    }
}
