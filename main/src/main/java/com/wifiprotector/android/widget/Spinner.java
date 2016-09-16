package com.wifiprotector.android.widget;

import android.content.Context;
import android.util.AttributeSet;

public class Spinner extends android.widget.Spinner {

    private int lastSelected = 0;

    public Spinner(Context context) {
        super(context);
    }

    public Spinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Spinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (this.lastSelected == this.getSelectedItemPosition()
                && getOnItemSelectedListener() != null) {
            getOnItemSelectedListener().onItemSelected(this, getSelectedView(),
                    this.getSelectedItemPosition(), getSelectedItemId());
        }

        if (!changed) {
            lastSelected = this.getSelectedItemPosition();
        }

        super.onLayout(changed, l, t, r, b);
    }
}
