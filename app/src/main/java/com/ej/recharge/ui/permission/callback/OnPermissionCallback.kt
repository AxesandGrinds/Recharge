package com.ej.recharge.ui.permission.callback


/// TODO DONE
interface OnPermissionCallback {
    fun onPermissionGranted(permissionName: Array<String?>)
    fun onPermissionDeclined(permissionName: Array<String>)
    fun onPermissionPreGranted(permissionsName: String)
    fun onPermissionNeedExplanation(permissionName: String)
    fun onPermissionReallyDeclined(permissionName: String)
    fun onNoPermissionNeeded()
}
