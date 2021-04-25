package com.app.ej.cs.ui.account

import android.Manifest
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.viewpager.widget.ViewPager
import com.app.ej.cs.R
import com.app.ej.cs.ui.permission.activity.BasePermissionActivity
import com.app.ej.cs.ui.permission.model.PermissionModel
import com.app.ej.cs.ui.permission.model.PermissionModelBuilder
import com.droidman.ktoasty.KToasty
import java.util.*


class RegisterActivity : BasePermissionActivity() {

    protected override fun permissions(): List<PermissionModel> {

        val permissions: MutableList<PermissionModel> = ArrayList<PermissionModel>()

        permissions.add(
            PermissionModelBuilder.withContext(this)
                .withTitle("STORAGE")
                .withCanSkip(false)
                .withPermissionName(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withMessage(
                    "Recharge App needs your permission to read images of recharge codes and also to store your information locally on your phone."
                )
                .withExplanationMessage("We need this permission to save your information locally on your phone," +
                        " and for when you take pictures of recharge codes outside of Recharge App.")
                .withFontType("my_font.ttf")
                .withLayoutColorRes(R.color.dark_blue)
                .withImageResourceId(R.drawable.folder)
                .withPreviousIcon(R.drawable.ic_transparent_icon_60)
                .build()

        )

        permissions.add(
                PermissionModelBuilder.withContext(this)
                        .withTitle("LOCATION ACCESS")
                        .withCanSkip(false)
                        .withPermissionName(Manifest.permission.ACCESS_FINE_LOCATION)
                        .withMessage(
                                "Location is requested only at registration. " +
                                        "It is only used for providing personalization for ads."
                        )
                        .withExplanationMessage(
                                "We need this permission to access your location for" +
                                        " ads personalization."
                        )
                        .withFontType("my_font.ttf")
                    .withLayoutColorRes(R.color.green)
                    .withImageResourceId(R.drawable.map)
                        .build()
        )

        permissions.add(
                PermissionModelBuilder.withContext(this)
                        .withTitle("READ CONTACTS")
                        .withCanSkip(false)
                        .withPermissionName(Manifest.permission.READ_CONTACTS)
                        .withMessage(
                                "Read Contacts is used to allow you to add friends from " +
                                        "your contact list so no need to copy and paste numbers."
                        )
                        .withExplanationMessage(
                                "We need this permission to allow you to add friends from contact list. " +
                                        "Without this permission, you will need to add friends by copy and paste."
                        )
                        .withFontType("my_font.ttf")
                        .withLayoutColorRes(R.color.black)
                    .withImageResourceId(R.drawable.friends)
                        .build()
        )

        permissions.add(
            PermissionModelBuilder.withContext(this)
                .withTitle("CALL PHONE")
                .withCanSkip(false)
                .withPermissionName(Manifest.permission.CALL_PHONE)
                .withMessage(
                    "Recharge App knows all your USSD codes and would like " +
                            "to automatically launch USSD codes when you request it."
                )
                .withExplanationMessage(
                    "We need this permission to automatically launch your " +
                            "USSD codes when you request such in app."
                )
                .withFontType("my_font.ttf")
                .withLayoutColorRes(R.color.purple_500)
                .withImageResourceId(R.drawable.dial_pad)
                .build()
        )

        return permissions
    }

    protected override fun theme(): Int {
        return R.style.AppTheme_NoActionBar
    }

    protected override fun onIntroFinished() {

        KToasty.info(this, "Intro Finished", Toast.LENGTH_SHORT).show()

        Log.e("onIntroFinished", "Intro has finished")

        val intent = Intent(this, RegisterDetailsActivity::class.java)
        startActivity(intent)
        finish()

    }

    protected override fun pagerTransformer(): ViewPager.PageTransformer? {
        return null
    }

    protected override fun backPressIsEnabled(): Boolean {
        return false
    }

    protected override fun permissionIsPermanentlyDenied(permissionName: String) {

        Log.e("DANGER",
            "Permission ( $permissionName ) is permanentlyDenied and can only be granted via settings screen")

    }

    protected override fun onUserDeclinePermission(permissionName: String) {
        Log.w(
            "Warning",
            """Permission ( $permissionName ) is skipped you can request it again by calling doing such
                     if (permissionHelper.isExplanationNeeded(permissionName)) {
                            permissionHelper.requestAfterExplanation(permissionName);
                        }
                        if (permissionHelper.isPermissionPermanentlyDenied(permissionName)) {
                            /** read {@link #permissionIsPermanentlyDenied(String)} **/
                        }"""
        )
    }

    companion object {

        fun startActivity(context: Context) {
            context.startActivity(Intent(context, RegisterActivity::class.java))
        }

    }

}