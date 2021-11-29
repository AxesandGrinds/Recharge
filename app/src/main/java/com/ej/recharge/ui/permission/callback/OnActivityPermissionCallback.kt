package com.ej.recharge.ui.permission.callback


/// TODO DONE
interface OnActivityPermissionCallback {
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    )

    fun onActivityForResult(requestCode: Int)
}
