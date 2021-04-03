package com.fastaccess.permission.base.utils;

import android.content.Context;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.fastaccess.permission.R;

/**
 * Created by Kosh on 20/11/15 10:58 PM. copyrights @ Innov8tif
 */
public class ThemeUtil {

    private ThemeUtil() {}

    @ColorInt
    public static int getPrimaryColor(@NonNull Context context) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, value, true);
        return value.data;
    }

    @ColorInt
    public static int getThemeAccentColor(@NonNull Context context) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        return value.data;
    }

    public static boolean isTextSizeFromResources(@NonNull Context context, int resId) {
        try {
            return context.getResources().getDimension(resId) != 0;
        } catch (Exception ignored) {}
        return false;
    }

}
