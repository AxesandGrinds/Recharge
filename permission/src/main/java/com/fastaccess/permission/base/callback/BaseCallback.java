package com.fastaccess.permission.base.callback;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

public interface BaseCallback {

    void onSkip(@NonNull String permissionName);

    void onNext(@NonNull String permissionName);

    void onStatusBarColorChange(@ColorInt int color);

    void onPermissionRequest(@NonNull String permission, boolean canSkip);
}
