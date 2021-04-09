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

//        // Dark Blue, Purple, Light Blue
//        permissions.add(
//            PermissionModelBuilder.withContext(this)
//                .withTitle("STORAGE")
//                .withCanSkip(false)
//                .withPermissionName(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                .withMessage(
//                    "Recharge App lets you pay bills without internet access." +
//                            " For this reason, we have to store your information locally in your phone."
//                )
//                .withExplanationMessage("We need this permission to save your data locally on your phone.")
//                .withFontType("my_font.ttf")
//                .withLayoutColorRes(R.color.dark_blue)
////                .withLayoutColorRes(R.color.blue)
////                .withLayoutColorRes(R.color.light_grey)
//                //.withImageResourceId(R.drawable.permission_two)
//                .withImageResourceId(R.drawable.folder)
//                .build()
//        )

        // Dark Blue, Purple, Light Blue
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
//                .withLayoutColorRes(R.color.blue)
//                .withLayoutColorRes(R.color.light_grey)
                //.withImageResourceId(R.drawable.permission_two)
                .withImageResourceId(R.drawable.folder)
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
//                    .withLayoutColorRes(R.color.colorAccent3)
                    .withLayoutColorRes(R.color.green)
//                    .withImageResourceId(R.drawable.permission_one)
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
//                    .withImageResourceId(R.drawable.permission_one)
                    .withImageResourceId(R.drawable.friends)
                        .build()
        )

//        permissions.add(PermissionModelBuilder.withContext(this)
//                .withTitle("ACCESS WIFI STATE")
//                .withCanSkip(false)
//                .withPermissionName(Manifest.permission.ACCESS_WIFI_STATE)
//                .withMessage("Recharge App needs to know if you have wifi or not in order to load " +
//                        "information locally or from the internet if available.")
//                .withExplanationMessage("We need this permission to know whether to load data locally.")
//                .withFontType("my_font.ttf")
//                .withLayoutColorRes(R.color.colorAccent)
//                .withImageResourceId(R.drawable.permission_two)
//                .build());

//        permissions.add(PermissionModelBuilder.withContext(this)
//                .withTitle("ACCESS NETWORK STATE")
//                .withCanSkip(false)
//                .withPermissionName(Manifest.permission.ACCESS_NETWORK_STATE)
//                .withMessage("Recharge App needs to know if you have internet from your phone or not in order to load " +
//                        "information locally or from the internet if available.")
//                .withExplanationMessage("We need this permission to know whether to load data locally.")
//                .withFontType("my_font.ttf")
//                .withLayoutColorRes(R.color.colorAccent)
//                .withImageResourceId(R.drawable.permission_two)
//                .build());

//        permissions.add(PermissionModelBuilder.withContext(this)
//                .withTitle("INTERNET")
//                .withCanSkip(false)
//                .withPermissionName(Manifest.permission.INTERNET)
//                .withMessage("Recharge App needs to use internet to update data whenever " +
//                        "you choose to update your information.")
//                .withExplanationMessage("We need this permission to know whether to load data locally.")
//                .withFontType("my_font.ttf")
//                .withLayoutColorRes(R.color.colorAccent)
//                .withImageResourceId(R.drawable.permission_two)
//                .build());

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
//                .withLayoutColorRes(R.color.green)
                .withLayoutColorRes(R.color.purple_500)
//                .withLayoutColorRes(R.color.yellow)
//                .withImageResourceId(R.drawable.permission_three)
                .withImageResourceId(R.drawable.dial_pad)
                .build()
        )

//        permissions.add(PermissionModelBuilder.withContext(this)
//                .withTitle("SEND SMS")
//                .withCanSkip(false)
//                .withPermissionName(Manifest.permission.SEND_SMS)
//                .withMessage("Recharge App knows all 3-5 digit bank codes in Nigeria. " +
//                        "Sometimes your bank or telecom may need SMS instead of USSD codes.")
//                .withExplanationMessage("We need this permission to send SMS for your request.")
//                .withFontType("my_font.ttf")
//                .withLayoutColorRes(R.color.colorAccent3)
//                .withImageResourceId(R.drawable.permission_one)
//                .build());

//                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.ACCESS_WIFI_STATE,
//                Manifest.permission.CHANGE_WIFI_STATE,
//                Manifest.permission.ACCESS_NETWORK_STATE,
//                Manifest.permission.INTERNET,
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION,
//                Manifest.permission.READ_PHONE_STATE,
//                Manifest.permission.CALL_PHONE,

//                Manifest.permission.READ_CONTACTS,
//                Manifest.permission.SEND_SMS,
//                Manifest.permission.READ_SMS,
//                Manifest.permission.RECEIVE_SMS,
//                Manifest.permission.CAMERA
        return permissions
    }

    protected override fun theme(): Int {
        return R.style.AppTheme_NoActionBar
    }

    protected override fun onIntroFinished() {

        KToasty.info(this, "Intro Finished", Toast.LENGTH_SHORT).show()

        Log.i("onIntroFinished", "Intro has finished")
        // do whatever you like!
        val intent = Intent(this, RegisterDetailsActivity::class.java)
        startActivity(intent)
        finish()
    }

    protected override fun pagerTransformer(): ViewPager.PageTransformer? {
        return null //use default
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