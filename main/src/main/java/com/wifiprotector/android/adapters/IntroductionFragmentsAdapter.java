package com.wifiprotector.android.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.wifiprotector.android.fragment.introduction.AboutVPNFragment;
import com.wifiprotector.android.fragment.introduction.VPNBenefitsFragment;
import com.wifiprotector.android.fragment.introduction.VPNPlanFragment;

public class IntroductionFragmentsAdapter extends FragmentPagerAdapter {

    public static final int COUNT = 3;

    public IntroductionFragmentsAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: {
                return new AboutVPNFragment();
            }
            case 1: {
                return new VPNBenefitsFragment();
            }
            default:
                return new VPNPlanFragment();
        }
    }

    @Override
    public int getCount() {
        return COUNT;
    }
}