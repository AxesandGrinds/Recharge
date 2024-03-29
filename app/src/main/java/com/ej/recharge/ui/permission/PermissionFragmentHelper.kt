package com.ej.recharge.ui.permission

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.ej.recharge.ui.permission.callback.OnActivityPermissionCallback
import com.ej.recharge.ui.permission.callback.OnPermissionCallback
import com.ej.recharge.ui.permission.model.PermissionModel
import java.util.*


class PermissionFragmentHelper : OnActivityPermissionCallback {
    private val permissionCallback: OnPermissionCallback
    private val context: Fragment
    private var forceAccepting = false
    private var skipExplanation = false

    private constructor(context: Fragment) {
        this.context = context
        if (context is OnPermissionCallback) {
            permissionCallback = context as OnPermissionCallback
        } else {
            throw IllegalArgumentException("Fragment must implement (OnPermissionCallback)")
        }
    }

    private constructor(context: Fragment, permissionCallback: OnPermissionCallback) {
        this.context = context
        this.permissionCallback = permissionCallback
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if (verifyPermissions(grantResults)) {
                permissionCallback.onPermissionGranted(permissions)
            } else {
                val declinedPermissions = declinedPermissions(context, permissions)
                val deniedPermissionsLength: MutableList<Boolean> = ArrayList() //needed
                for (permissionName: String? in declinedPermissions) {
                    if (permissionName != null && !isExplanationNeeded(permissionName)) {
                        permissionCallback.onPermissionReallyDeclined(permissionName)
                        deniedPermissionsLength.add(false)
                    }
                }
                if (deniedPermissionsLength.size == 0) {
                    if (forceAccepting) {
                        requestAfterExplanation(declinedPermissions)
                        return
                    }
                    permissionCallback.onPermissionDeclined(declinedPermissions)
                }
            }
        }
    }

    /**
     * used only for [Manifest.permission.SYSTEM_ALERT_WINDOW]
     */
    override fun onActivityForResult(requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
                if (isSystemAlertGranted) {
                    permissionCallback.onPermissionGranted(arrayOf(Manifest.permission.SYSTEM_ALERT_WINDOW))
                } else {
                    permissionCallback.onPermissionDeclined(arrayOf(Manifest.permission.SYSTEM_ALERT_WINDOW))
                }
            }
        } else {
            permissionCallback.onPermissionPreGranted(Manifest.permission.SYSTEM_ALERT_WINDOW)
        }
    }

    /**
     * force the user to accept the permission. it won't work if the user ever thick-ed the "don't show again"
     */
    fun setForceAccepting(forceAccepting: Boolean): PermissionFragmentHelper {
        this.forceAccepting = forceAccepting
        return this
    }

    /**
     * @param permissionName
     * (it can be one of these types (String), (String[])
     */
    fun request(permissionName: Any): PermissionFragmentHelper {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionName is String) {
                handleSingle(permissionName)
            }
            else { //if (permissionName is Array<String?>) {

                handleMulti(permissionName as Array<String?>)
            }
//            else {
//                throw IllegalArgumentException(
//                    "Permissions can only be one of these types (String) or (String[])" +
//                            ". given type is " + permissionName.javaClass.simpleName
//                )
//            }
        } else {
            permissionCallback.onNoPermissionNeeded()
        }
        return this
    }

    /**
     * used only for [Manifest.permission.SYSTEM_ALERT_WINDOW]
     */
    fun requestSystemAlertPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if (!isSystemAlertGranted) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse(
                            "package:" + context.requireContext().getPackageName()
                        )
                    )
                    context.startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
                } else {
                    permissionCallback.onPermissionPreGranted(Manifest.permission.SYSTEM_ALERT_WINDOW)
                }
            } catch (ignored: Exception) {
            }
        } else {
            permissionCallback.onPermissionPreGranted(Manifest.permission.SYSTEM_ALERT_WINDOW)
        }
    }

    /**
     * internal usage.
     */
    private fun handleSingle(permissionName: String) {
        if (permissionExists(permissionName)) { // android M throws exception when requesting
            // run time permission that does not exists in AndroidManifest.
            if (!permissionName.equals(
                    Manifest.permission.SYSTEM_ALERT_WINDOW,
                    ignoreCase = true
                )
            ) {
                if (isPermissionDeclined(permissionName)) {
                    if (isExplanationNeeded(permissionName) && !skipExplanation) {
                        permissionCallback.onPermissionNeedExplanation(permissionName)
                    } else {
                        context.requestPermissions(arrayOf(permissionName), REQUEST_PERMISSIONS)
                    }
                } else {
                    permissionCallback.onPermissionPreGranted(permissionName)
                }
            } else {
                requestSystemAlertPermission()
            }
        } else {
            permissionCallback.onPermissionDeclined(arrayOf(permissionName))
        }
    }

    /**
     * internal usage.
     */
    private fun handleMulti(permissionNames: Array<String?>) {
        val permissions = declinedPermissionsAsList(context, permissionNames)
        if (permissions.isEmpty()) {
            permissionCallback.onPermissionGranted(permissionNames)
            return
        }
        val hasAlertWindowPermission = permissions.contains(Manifest.permission.SYSTEM_ALERT_WINDOW)
        if (hasAlertWindowPermission) {
            val index = permissions.indexOf(Manifest.permission.SYSTEM_ALERT_WINDOW)
            permissions.removeAt(index)
        }
        context.requestPermissions(permissions.toTypedArray(), REQUEST_PERMISSIONS)
    }

    /**
     * to be called when explanation is presented to the user
     */
    fun requestAfterExplanation(permissionName: String) {
        if (isPermissionDeclined(permissionName)) {
            context.requestPermissions(arrayOf(permissionName), REQUEST_PERMISSIONS)
        } else {
            permissionCallback.onPermissionPreGranted(permissionName)
        }
    }

    /**
     * to be called when explanation is presented to the user
     */
    fun requestAfterExplanation(permissions: Array<String>) {
        var permissions = permissions
        val permissionsToRequest = ArrayList<String>()
        for (permissionName: String in permissions) {
            if (isPermissionDeclined(permissionName)) {
                permissionsToRequest.add(permissionName) // add permission to request
            } else {
                permissionCallback.onPermissionPreGranted(permissionName) // do not request, since it is already granted
            }
        }
        if (permissionsToRequest.isEmpty()) return
        permissions = permissionsToRequest.toTypedArray()
        context.requestPermissions(permissions, REQUEST_PERMISSIONS)
    }

    /**
     * return true if permission is declined, false otherwise.
     */
    fun isPermissionDeclined(permissionsName: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            (context.context)!!,
            permissionsName
        ) != PackageManager.PERMISSION_GRANTED
    }

    /**
     * return true if permission is granted, false otherwise.
     */
    fun isPermissionGranted(permissionsName: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            (context.context)!!,
            permissionsName
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * @return true if explanation needed.
     */
    fun isExplanationNeeded(permissionName: String): Boolean {
        return context.shouldShowRequestPermissionRationale(permissionName)
    }

    /**
     * @return true if the permission is patently denied by the user and only can be granted via settings Screen
     *
     *
     * consider using [PermissionFragmentHelper.openSettingsScreen]} to open settings screen
     */
    fun isPermissionPermanentlyDenied(permission: String): Boolean {
        return isPermissionDeclined(permission) && !isExplanationNeeded(permission)
    }

    /**
     * internal usage.
     */
    private fun verifyPermissions(grantResults: IntArray): Boolean {
        if (grantResults.size < 1) {
            return false
        }
        for (result: Int in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    /**
     * @return true if permission exists in the manifest, false otherwise.
     */
    fun permissionExists(permissionName: String): Boolean {
        try {
            val context = context.context
            val packageInfo = context!!.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_PERMISSIONS
            )
            if (packageInfo.requestedPermissions != null) {
                for (p: String in packageInfo.requestedPermissions) {
                    if ((p == permissionName)) {
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * @return true if [Manifest.permission.SYSTEM_ALERT_WINDOW] is granted
     */
    val isSystemAlertGranted: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context.context)
            } else true
        }

    /**
     * open android settings screen for the specific package name
     */
    fun openSettingsScreen() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.parse("package:" + context.requireContext().packageName)
        intent.data = uri
        context.startActivity(intent)
    }

    fun setSkipExplanation(skipExplanation: Boolean) {
        this.skipExplanation = skipExplanation
    }

    companion object {
        private val OVERLAY_PERMISSION_REQ_CODE = 2
        private val REQUEST_PERMISSIONS = 1
        fun getInstance(context: Fragment): PermissionFragmentHelper {
            return PermissionFragmentHelper(context)
        }

        fun getInstance(
            context: Fragment,
            permissionCallback: OnPermissionCallback
        ): PermissionFragmentHelper {
            return PermissionFragmentHelper(context, permissionCallback)
        }

        /**
         * be aware as it might return null (do check if the returned result is not null!)
         *
         *
         * can be used outside of activity.
         */
        fun declinedPermission(context: Fragment, permissions: Array<String>): String? {
            for (permission: String in permissions) {
                if (isPermissionDeclined(context, permission)) {
                    return permission
                }
            }
            return null
        }

        /**
         * @return list of permissions that the user declined or not yet granted.
         */
        fun declinedPermissions(context: Fragment, permissions: Array<String?>): Array<String> {
            val permissionsNeeded: MutableList<String> = ArrayList()
            for (permission: String? in permissions) {
                if (permission?.let { isPermissionDeclined(context, it) } == true && permissionExists(
                        context,
                        permission
                    )
                ) {
                    permissionsNeeded.add(permission)
                }
            }
            return permissionsNeeded.toTypedArray()
        }

        fun declinedPermissionsAsList(context: Fragment, permissions: Array<String?>): MutableList<String> {
            val permissionsNeeded: MutableList<String> = ArrayList()
            for (permission: String? in permissions) {
                if (permission?.let { isPermissionDeclined(context, it) } == true && permissionExists(
                        context,
                        permission
                    )
                ) {
                    permissionsNeeded.add(permission)
                }
            }
            return permissionsNeeded
        }

        /**
         * return true if permission is granted, false otherwise.
         *
         *
         * can be used outside of activity.
         */
        fun isPermissionGranted(context: Fragment, permission: String): Boolean {
            return ActivityCompat.checkSelfPermission(
                (context.context)!!,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }

        /**
         * return true if permission is declined, false otherwise.
         *
         *
         * can be used outside of activity.
         */
        fun isPermissionDeclined(context: Fragment, permission: String): Boolean {
            return ActivityCompat.checkSelfPermission(
                (context.context)!!,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        }

        /**
         * @return true if explanation needed.
         */
        fun isExplanationNeeded(context: Fragment, permissionName: String): Boolean {
            return context.shouldShowRequestPermissionRationale(permissionName)
        }

        /**
         * @return true if the permission is patently denied by the user and only can be granted via settings Screen
         *
         *
         * consider using [PermissionFragmentHelper.openSettingsScreen] to open settings screen
         */
        fun isPermissionPermanentlyDenied(context: Fragment, permission: String): Boolean {
            return isPermissionDeclined(context, permission) && !isExplanationNeeded(
                context,
                permission
            )
        }

        /**
         * open android settings screen for your app.
         */
        fun openSettingsScreen(context: Fragment) {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.parse("package:" + context.requireContext().packageName)
            intent.data = uri
            context.startActivity(intent)
        }

        /**
         * @return true if permission exists in the manifest, false otherwise.
         */
        fun permissionExists(context: Fragment, permissionName: String): Boolean {
            try {
                val ctx = context.context
                val packageInfo = ctx!!.packageManager.getPackageInfo(
                    ctx.packageName,
                    PackageManager.GET_PERMISSIONS
                )
                if (packageInfo.requestedPermissions != null) {
                    for (p: String in packageInfo.requestedPermissions) {
                        if ((p == permissionName)) {
                            return true
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }

        /**
         * @return true if [Manifest.permission.SYSTEM_ALERT_WINDOW] is granted
         */
        fun isSystemAlertGranted(context: Fragment): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context.context)
            } else true
        }

        fun removeGrantedPermissions(context: Fragment, models: MutableList<PermissionModel>) {
            val granted: MutableList<PermissionModel> = ArrayList<PermissionModel>()
            for (permissionModel: PermissionModel in models) {
                if (permissionModel.permissionName
                        .equals(Manifest.permission.SYSTEM_ALERT_WINDOW, true)
                ) {
                    if (isSystemAlertGranted(context)) {
                        granted.add(permissionModel)
                    }
                } else if (permissionModel.permissionName?.let { isPermissionGranted(context, it) } == true) {
                    granted.add(permissionModel)
                }
            }
            if (!granted.isEmpty()) {
                models.removeAll(granted)
            }
        }
    }
}
