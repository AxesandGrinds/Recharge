package com.fastaccess.permission.base.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

public class DisabledPager extends ViewPager {

    public DisabledPager(Context context) {
        super(context);
    }

    public DisabledPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override public boolean onInterceptTouchEvent(MotionEvent event) {
        return false;
    }
}
