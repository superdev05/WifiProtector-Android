package com.wifiprotector.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.viewpagerindicator.UnderlinePageIndicator;
import com.wifiprotector.android.Preferences;
import com.wifiprotector.android.adapters.IntroductionFragmentsAdapter;
import com.wifiprotector.android.fragment.introduction.IntroductionFragmentListener;
import com.wifiprotector.android.fragment.introduction.VPNPlanFragment;

import de.blinkt.openvpn.R;

public class IntroductionActivity extends FragmentActivity implements IntroductionFragmentListener {

    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Preferences.introductionDisplayed()) {
            startMainActivity(null);
        } else {
            initView();
        }
    }

    private void startMainActivity(@Nullable VPNPlanFragment.Plan plan) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (plan != null) {
            switch (plan) {
                case LICENSE: {
                    intent.putExtra(MainActivity.EXTRA_ENTER_LICENSE, true);
                    break;
                }
                case BUY: {
                    intent.putExtra(MainActivity.EXTRA_BUY_FULL_VERSION, true);
                    break;
                }
            }
        }

        startActivity(intent);
        finish();
    }

    private void initView() {
        setContentView(R.layout.activity_introduction);

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new IntroductionFragmentsAdapter(getSupportFragmentManager()));

        UnderlinePageIndicator pageIndicator = (UnderlinePageIndicator) findViewById(R.id.indicator);
        pageIndicator.setFades(false);
        pageIndicator.setViewPager(pager);
        pageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == IntroductionFragmentsAdapter.COUNT - 1) {
                    Preferences.introductionDisplayed(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public void onStartClicked(VPNPlanFragment.Plan plan) {
        startMainActivity(plan);
    }

    @Override
    public void onNextClicked() {
        pager.setCurrentItem(pager.getCurrentItem() + 1, true);
    }
}