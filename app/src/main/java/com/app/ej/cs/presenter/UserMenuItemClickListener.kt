package com.app.ej.cs.presenter

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager

import android.view.MenuItem
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.app.ej.cs.R
import com.app.ej.cs.repository.entity.User
import com.app.ej.cs.utils.PhoneUtil

class UserMenuItemClickListener(
    private val context: Context,
    private val fragment: Fragment,
    private val activity: Activity,
    private val user: User,
    private val pAccount: String,
)
    : PopupMenu.OnMenuItemClickListener {


    private val PERMISSION_READ_PHONE_STATE = 18

    override fun onMenuItemClick(item: MenuItem): Boolean {

        if (

            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
            == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED) {

            val phoneUtil: PhoneUtil = PhoneUtil()

            if (item.groupId == R.id.user_menu) {

                return when (item.itemId) {

                    R.id.airtime_balance -> {
                        phoneUtil.checkAirtimeBalance(context, fragment, activity, user, pAccount)
                        true
                    }
                    R.id.data_balance -> {
                        phoneUtil.checkDataBalance(context, fragment, activity, user, pAccount)
                        true
                    }
                    R.id.bank_recharge -> {
                        phoneUtil.bankChoiceRecharge(context, fragment, activity, user, pAccount)
                        true
                    }
                    R.id.code_recharge -> {
                        phoneUtil.codeRecharge(context, activity, user, pAccount)
                        true
                    }
                    R.id.buy_data -> {
                        phoneUtil.dataRechargeDialog(activity, user, pAccount)
                        true
                    }
                    R.id.dstv_bill -> {
                        phoneUtil. payDSTVOrGoTVBill(context, fragment, activity, user, pAccount)
                        true
                    }
                    R.id.electricity_bill -> {
                        phoneUtil.payElectricityBill(context, fragment, activity, user, pAccount)
                        true
                    }

                    else -> false

                }

            }

        }
        else {

            ActivityCompat.requestPermissions(
                activity, arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE),
                PERMISSION_READ_PHONE_STATE
            )

        }

        return false

    }

}
