package com.app.ej.cs.ui.permission.callback

import androidx.annotation.ColorInt


/// TODO DONE
interface BaseCallback {
    fun onSkip(permissionName: String)
    fun onNext(permissionName: String)
    fun onStatusBarColorChange(@ColorInt color: Int)
    fun onPermissionRequest(permission: String, canSkip: Boolean)
}

