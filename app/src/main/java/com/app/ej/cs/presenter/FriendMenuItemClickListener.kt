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
import com.app.ej.cs.repository.entity.Friend
import com.app.ej.cs.repository.entity.User
import com.app.ej.cs.utils.PhoneUtil

class FriendMenuItemClickListener(
    private val context: Context,
    private val fragment: Fragment,
    private val activity: Activity,
    private val modelList: MutableList<User>,
    private val friend: Friend,
)
    : PopupMenu.OnMenuItemClickListener {


    private val PERMISSION_READ_PHONE_STATE = 18

    override fun onMenuItemClick(item: MenuItem): Boolean {

        if (

            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
            == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED) {

            val phoneUtil: PhoneUtil = PhoneUtil()

            if (item.groupId == R.id.fmenu) {

                return when (item.itemId) {

                    R.id.check_account_number -> {
                        phoneUtil.showAccountNumber(context, friend)
                        true
                    }
                    R.id.airtime_phone_transfer -> {
                        phoneUtil.amountAirtimeTransfer(context, fragment, activity, modelList, friend)
                        true
                    }
                    R.id.airtime_bank_topup_transfer -> {
                        phoneUtil.amountBankTopupTransfer(context, fragment, activity, modelList, friend)
                        true
                    }
                    R.id.bank_transfer -> {
                        phoneUtil.amountChoiceBankTransfer(context, fragment, activity, modelList, friend)
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
