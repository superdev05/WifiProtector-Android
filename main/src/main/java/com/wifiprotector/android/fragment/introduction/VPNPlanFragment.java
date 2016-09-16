package com.wifiprotector.android.fragment.introduction;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import de.blinkt.openvpn.R;

public class VPNPlanFragment extends Fragment {
    public static final CharSequence TAG = VPNPlanFragment.class.getName();

    public enum Plan {
        FREE, LICENSE, BUY
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vpn_plan, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        getView().findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton licenseButton = (RadioButton) getView().findViewById(R.id.radio_license);
                RadioButton buyButton = (RadioButton) getView().findViewById(R.id.radio_buy);

                Plan plan = Plan.FREE;
                if (licenseButton.isChecked()) {
                    plan = Plan.LICENSE;
                } else if (buyButton.isChecked()) {
                    plan = Plan.BUY;
                }

                ((IntroductionFragmentListener) getActivity()).onStartClicked(plan);
            }
        });
    }
}
