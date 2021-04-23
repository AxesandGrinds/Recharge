package com.app.ej.cs.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.*
import android.content.Context.CLIPBOARD_SERVICE
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.Parcelable
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.app.ej.cs.R
import com.app.ej.cs.repository.entity.Friend
import com.app.ej.cs.repository.entity.User
import com.app.ej.cs.service.USSDService
import com.app.ej.cs.ui.CodeInputActivity
import com.app.ej.cs.ui.DataBuyForFriendDialog
import com.app.ej.cs.ui.DataRechargeDialog
import com.app.ej.cs.ui.DataTransferDialog
import com.droidman.ktoasty.KToasty
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.coroutines.sendSuspend
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.google.android.material.textfield.TextInputEditText
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PhoneUtil {

    private val PERMISSION_READ_PHONE_STATE = 18
    private val PERMISSION_CALL_PHONE = 19
    private val TAG = "ATTENTION ATTENTION"

    private val simSlotName = arrayOf(
        "extra_asus_dial_use_dualsim",
        "com.android.phone.extra.slot",
        "slot",
        "simslot",
        "sim_slot",
        "subscription",
        "Subscription",
        "phone",
        "com.android.phone.DialingMode",
        "simSlot",
        "slot_id",
        "simId",
        "simnum",
        "phone_type",
        "slotId",
        "slotIdx")

    fun isDualSim(context: Context): Boolean {

        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        var simCount: Int

        when {

            Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1 -> {

                val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

                val activeSubscriptionInfoList = subscriptionManager.activeSubscriptionInfoList
                simCount = activeSubscriptionInfoList.size

            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {

                simCount = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {

                    telephonyManager.phoneCount

                }
                else {

                    telephonyManager.activeModemCount

                }

            }
            else -> {

                simCount = 1

            }

        }

        return simCount > 1

    }


    fun getDetails(data: Intent?, context: Context): Array<String> {


        val contactUri: Uri? = data!!.data
        val projection: Array<String> = arrayOf<String>(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
        )

        var person: Array<String>

        val uri2 = Uri.withAppendedPath(data.data, ContactsContract.Contacts.Data.CONTENT_DIRECTORY)
        Log.e("ATTENTION ATTENTION", "onActivityResult all uri data $uri2")
        Log.e("ATTENTION ATTENTION", "onActivityResult all contactUri data ${contactUri?.userInfo}")

        val people: Cursor? = context.contentResolver
            .query((contactUri)!!, null, null, null, null)

        if (people!!.moveToFirst()) {

            // Retrieve the name and number from the respective columns
            val indexName: Int = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val indexNumber: Int = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val name: String = people.getString(indexName)
            val number: String = people.getString(indexNumber)
            person = arrayOf<String>(name, number)

            Log.e("ATTENTION ATTENTION", "Cursor is not empty. Inside if (people!!.moveToFirst())")

            val contactList = "name: $name phone number: $number"

            Log.e("ATTENTION ATTENTION", contactList)

        }
        else {
            person = arrayOf<String>("name", "000")

            Log.e("ATTENTION ATTENTION", "Cursor is empty.")
        }


        people.close()

        return person
    }












    fun sendToSim(
        context: Context,
        intent: Intent,
        simNumber: Int,
    ) {

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("com.android.phone.force.slot", true)
        intent.putExtra("Cdma_Supp", true)
        //Add all slots here, according to device.. (different device require different key so put all together)

        for (s in simSlotName) intent.putExtra(s, simNumber) //0 or 1 according to sim.......

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

            @SuppressLint("MissingPermission")
            val list = telecomManager.callCapablePhoneAccounts

            val localSubscriptionManager =
                context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

            @SuppressLint("MissingPermission")
            val activeSubscriptionInfoList = localSubscriptionManager.activeSubscriptionInfoList

            for (phoneAccountHandle in list) {

                if (simNumber == 0) {

                    if (phoneAccountHandle.id.contains(activeSubscriptionInfoList[simNumber].iccId)) {

                        Log.e("ATTENTION ATTENTION", "Does This Ever Run?")

                        intent.putExtra(
                            "android.telecom.extra.PHONE_ACCOUNT_HANDLE",
                            phoneAccountHandle as Parcelable
                        )

                        return

                    }

                }
                else if (simNumber == 1 && activeSubscriptionInfoList.size > 1) {

                    if (phoneAccountHandle.id.contains(activeSubscriptionInfoList[simNumber].iccId)) {

                        Log.e("ATTENTION ATTENTION", "Does This Ever Run?")

                        intent.putExtra(
                            "android.telecom.extra.PHONE_ACCOUNT_HANDLE",
                            phoneAccountHandle as Parcelable
                        )

                        return

                    }



                    return

                }
                else if (simNumber == 1 && activeSubscriptionInfoList.size == 1) {

                    if (phoneAccountHandle.id.contains(activeSubscriptionInfoList[0].iccId)) {

                        Log.e("ATTENTION ATTENTION", "Does This Ever Run?")

                        intent.putExtra(
                            "android.telecom.extra.PHONE_ACCOUNT_HANDLE",
                            phoneAccountHandle as Parcelable
                        )

                        return

                    }

                    return

                }
            }

        }

    }



    fun simpleRecharge(
        context: Context,
        activity: Activity,
        pAccount: String,
        rechargeNumber: String,
        ussdStarter: String
    ) {

        val ussdCode: String = "*" + ussdStarter + "*" + rechargeNumber + Uri.encode("#")

        val intent: Intent = Intent("android.intent.action.CALL", Uri.parse("tel:$ussdCode"))

        if (pAccount == "1") {
            sendToSim(context, intent, 0)
        }
        else {
            sendToSim(context, intent, 1)
        }

        intent.setPackage("com.android.server.telecom")

        val util: Util = Util()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            CoroutineScope(Dispatchers.IO).launch {

                val result = (activity as FragmentActivity).permissionsBuilder(
                    Manifest.permission.CALL_PHONE,
                ).build().sendSuspend()

                if (result.allGranted()) { // All the permissions are granted.

                    withContext(Dispatchers.Main) {

                        val msg: String = "Recharge Requested."
                        util.onShowMessageSuccess(msg, context)
                        Log.e(TAG, msg)

                        context.startActivity(intent)

                    }

                }
                else {

                    withContext(Dispatchers.Main) {

                        val msg = "You have denied some " +
                                "permissions permanently. Functions will not work without these permission. " +
                                "Please grant the permissions for this app in your phone's settings."
                        util.onShowMessage(msg, context)
                        Log.e(TAG, msg)

                    }

                }

            }

        }
        else {

            val msg: String = "Recharge Requested."
            util.onShowMessageSuccess(msg, context)
            Log.e(TAG, msg)

            context.startActivity(intent)


        }



    }



    fun payElectricityBill(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        user: User,
        pAccount: String,
    ) {

        val meterNumberChoiceBuilder: AlertDialog.Builder = AlertDialog.Builder(
            context,
            R.style.MyDialogTheme
        )
        meterNumberChoiceBuilder.setTitle("Choose your Meter Number")

        val pMeterNumbers = mutableListOf<String>()

        if (user.meterNumber1 != null && user.meterNumber1 != "") {

            pMeterNumbers.add(user.meterNumber1!!)

        }
        if (user.meterNumber2 != null && user.meterNumber2 != "") {

            pMeterNumbers.add(user.meterNumber2!!)

        }
        if (user.meterNumber3 != null && user.meterNumber3 != "") {

            pMeterNumbers.add(user.meterNumber3!!)

        }

        val util: Util = Util()

        meterNumberChoiceBuilder.setSingleChoiceItems(pMeterNumbers.toTypedArray(), -1) {

                dialogInterface, i ->

            dialogInterface.dismiss()

            val meterNumber: String = pMeterNumbers[i]

            val ussdCode: String = "*389*8*" + meterNumber + Uri.encode("#")

            val intent: Intent = Intent("android.intent.action.CALL", Uri.parse("tel:$ussdCode"))

            if (pAccount == "1") {
                sendToSim(context, intent, 0)
            }
            else {
                sendToSim(context, intent, 1)
            }

            intent.setPackage("com.android.server.telecom")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                CoroutineScope(Dispatchers.IO).launch {

                    val result = fragment.permissionsBuilder(
                        Manifest.permission.CALL_PHONE,
                    ).build().sendSuspend()

                    if (result.allGranted()) { // All the permissions are granted.

                        withContext(Dispatchers.Main) {

                            val msg: String = "Electricity Bill Payment requested."
                            util.onShowMessageSuccess(msg, context)
                            Log.e(TAG, msg)

                            context.startActivity(intent)

                        }

                    }
                    else {

                        withContext(Dispatchers.Main) {

                            val msg = "You have denied some " +
                                    "permissions permanently. Functions will not work without these permission. " +
                                    "Please grant the permissions for this app in your phone's settings."
                            util.onShowMessage(msg, context)
                            Log.e(TAG, msg)

                        }

                    }

                }

            }
            else {

                val msg: String = "Electricity Bill Payment requested."
                util.onShowMessageSuccess(msg, context)
                Log.e(TAG, msg)

                context.startActivity(intent)

            }

        }

        meterNumberChoiceBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }

        meterNumberChoiceBuilder.show()

    }

    fun payDSTVOrGoTVBill(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        user: User,
        pAccount: String,
    ) {


        val smartCardNumberChoiceBuilder: AlertDialog.Builder = AlertDialog.Builder(
            context,
            R.style.MyDialogTheme
        )
        smartCardNumberChoiceBuilder.setTitle("Choose your Smart Card Number")

        val pSmartCardNumbers = mutableListOf<String>()

        if (user.smartCardNumber1 != null && user.smartCardNumber1 != "") {

            pSmartCardNumbers.add(user.smartCardNumber1!!)

        }
        if (user.smartCardNumber2 != null && user.smartCardNumber2 != "") {

            pSmartCardNumbers.add(user.smartCardNumber2!!)

        }
        if (user.smartCardNumber3 != null && user.smartCardNumber3 != "") {

            pSmartCardNumbers.add(user.smartCardNumber3!!)

        }
        if (user.smartCardNumber4 != null && user.smartCardNumber4 != "") {

            pSmartCardNumbers.add(user.smartCardNumber4!!)

        }

        val util: Util = Util()

        smartCardNumberChoiceBuilder.setSingleChoiceItems(pSmartCardNumbers.toTypedArray(), -1) {

                dialogInterface, i ->

            dialogInterface.dismiss()

            val smartCardNumber: String = pSmartCardNumbers[i]

            val ussdCode: String = "*389*9*" + smartCardNumber + Uri.encode("#")

            val intent: Intent = Intent("android.intent.action.CALL", Uri.parse("tel:$ussdCode"))

            if (pAccount == "1") {
                sendToSim(context, intent, 0)
            }
            else {
                sendToSim(context, intent, 1)
            }

            intent.setPackage("com.android.server.telecom")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                CoroutineScope(Dispatchers.IO).launch {

                    val result = fragment.permissionsBuilder(
                        Manifest.permission.CALL_PHONE,
                    ).build().sendSuspend()

                    if (result.allGranted()) { // All the permissions are granted.

                        withContext(Dispatchers.Main) {

                            val msg: String = "TV Service Bill Payment requested."
                            util.onShowMessageSuccess(msg, context)
                            Log.e(TAG, msg)

                            context.startActivity(intent)

                        }

                    }
                    else {

                        withContext(Dispatchers.Main) {

                            val msg: String = "You have denied some " +
                                    "permissions permanently. Functions will not work without these permission. " +
                                    "Please grant the permissions for this app in your phone's settings."
                            util.onShowMessage(msg, context)
                            Log.e(TAG, msg)

                        }

                    }

                }

            }
            else {

                val msg: String = "TV Service Bill Payment requested."
                util.onShowMessageSuccess(msg, context)
                Log.e(TAG, msg)

                context.startActivity(intent)

            }


        }

        smartCardNumberChoiceBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }

        smartCardNumberChoiceBuilder.show()

    }


    private fun heritageBankPayBill(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        tvServiceSelection: String,
        smartCardStr: String,
        smartCardStr2: String,
        amount: String,

        ) {
        val smartCardNumber: String = if (pAccount == "1") {
            smartCardStr
        } else {
            smartCardStr2
        }

        var ussdCode: String = "*389*9*" + smartCardNumber + Uri.encode("#")

        when (tvServiceSelection) {

            "DSTV" -> {
                ussdCode = "*322*030*1099*" + amount + Uri.encode("#")
                Log.e(TAG, "$tvServiceSelection bill payment of $amount was successful.")
            }
            "GOTV" -> {
                ussdCode = "*322*030*1088*" + amount + Uri.encode("#")
                Log.e(TAG, "$tvServiceSelection bill payment of $amount was successful.")
            }
            "StarTimes" -> {
                ussdCode = "*322*030*1098*" + amount + Uri.encode("#")
                Log.e(TAG, "$tvServiceSelection bill payment of $amount was successful.")
            }
            else -> {
                Toasty.error(
                    context,
                    "Error paying DSTV, GOTV or StarTimes Bill with $amount",
                    Toasty.LENGTH_LONG,
                    true
                ).show()
                Log.e(TAG, "Error paying DSTV, GOTV or StarTimes Bill with $amount")
            }

        }

        val util: Util = Util()

        val intent: Intent = Intent("android.intent.action.CALL", Uri.parse("tel:$ussdCode"))

        if (pAccount == "1") {
            sendToSim(context, intent, 0)
        }
        else {
            sendToSim(context, intent, 1)
        }

        intent.setPackage("com.android.server.telecom")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            CoroutineScope(Dispatchers.IO).launch {

                val result = fragment.permissionsBuilder(
                    Manifest.permission.CALL_PHONE,
                ).build().sendSuspend()

                if (result.allGranted()) { // All the permissions are granted.

                    withContext(Dispatchers.Main) {

                        val msg: String = "$tvServiceSelection bill payment of $amount was successful."
                        util.onShowMessageSuccess(msg, context)
                        Log.e(TAG, msg)

                        context.startActivity(intent)

                    }

                }
                else {

                    withContext(Dispatchers.Main) {

                        val msg: String = "You have denied some " +
                                "permissions permanently. Functions will not work without these permission. " +
                                "Please grant the permissions for this app in your phone's settings."
                        util.onShowMessage(msg, context)
                        Log.e(TAG, msg)

                    }

                }

            }

        }
        else {

            val msg: String = "$tvServiceSelection bill payment of $amount was successful."
            util.onShowMessageSuccess(msg, context)
            Log.e(TAG, msg)

            context.startActivity(intent)

        }


    }

    fun heritageBankDSTVorGOTVBill(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        smartCardStr: String,
        smartCardStr2: String,
    ) {

        val tv_bill = arrayOf(
            "DSTV",
            "GOTV",
            "StarTimes"
        )

        val statementBuilder = AlertDialog.Builder(context, R.style.MyDialogTheme)
        statementBuilder.setTitle("Confirm")
        statementBuilder.setMessage(
            "Unlike other banks, Heritage Bank needs you to input your " +
                    "Smart Card Number during the bill payment process. " +
                    "Please have your SmartCard Number in hand. Thank you."
        )

        statementBuilder.setPositiveButton("Ok") { dialog, which ->

            dialog.dismiss()

            val selectTVServiceBuilder = AlertDialog.Builder(context, R.style.MyDialogTheme)
            selectTVServiceBuilder.setTitle("Select TV Service")
            selectTVServiceBuilder.setSingleChoiceItems(tv_bill, -1) { dialog2, i ->

                dialog2.dismiss()

                val tvServiceSelection = tv_bill[i]

                val amountBuilder: AlertDialog.Builder = AlertDialog.Builder(
                    context,
                    R.style.MyDialogTheme
                )

                amountBuilder.setTitle("Amount")
                val amountViewInflated: View =
                    LayoutInflater.from(context).inflate(R.layout.input_amount, null)

                val amountInput = amountViewInflated.findViewById<View>(R.id.input_amount) as TextInputEditText

                amountBuilder.setView(amountViewInflated)

                amountBuilder.setPositiveButton("Ok") { dialog, which ->

                        dialog.dismiss()

                        heritageBankPayBill(
                            context,
                            fragment,
                            activity,
                            pAccount,
                            tvServiceSelection,
                            smartCardStr,
                            smartCardStr2,
                            amountInput.text.toString(),
                        )

                    }

                amountBuilder.setNegativeButton("Cancel") { dialog, which ->
                    dialog.cancel()
                }
                amountBuilder.show()

            }

            val mDialog = selectTVServiceBuilder.create()
            mDialog.show()

        }

        statementBuilder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }

        statementBuilder.create().show()

    }


    /// TODO Not Needed because no fab?
    fun setupShare(
        context: Context,
        goingTo: Int,
        network: String,
        phone: String,
        pin: String,
    ) {

        val amountBuilder: AlertDialog.Builder = AlertDialog.Builder(context, R.style.MyDialogTheme)
        amountBuilder.setTitle("Amount")

        val amountViewInflated: View = LayoutInflater.from(context).inflate(
            R.layout.input_amount,
            null
        )

        val amountInput = amountViewInflated.findViewById<View>(R.id.input_amount) as TextInputEditText

        amountBuilder.setView(amountViewInflated)

        amountBuilder.setPositiveButton(android.R.string.ok) { dialog, which ->

            dialog.dismiss()

            val amountInputStr: String = amountInput.text.toString()

            when (goingTo) {

//                R.id.fab_friend_phone1 -> if (network1Str == networkF1Str) {
//                    share("1", networkF1Str, phoneF1Str, amountInputStr, pin1Str)
//                }
//                else if (network2Str == networkF1Str) {
//                    share("2", networkF1Str, phoneF1Str, amountInputStr, pin2Str)
//                }


            }

        }
        amountBuilder.setNegativeButton(android.R.string.cancel,
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        amountBuilder.show()
    }


    private fun simpleShare(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        rechargeCode: String,
        phone: String,
        amount: String?,
        password: String,
    ) {


        val util: Util = Util()

        if (amount != null) {

            val ussdCode =
                "*" + rechargeCode + "*" + phone + "*" + amount + "*" + password + Uri.encode("#")

            val intent: Intent = Intent("android.intent.action.CALL", Uri.parse("tel:$ussdCode"))

            if (pAccount == "1") {
                sendToSim(context, intent, 0)
            }
            else {
                sendToSim(context, intent, 1)
            }

            intent.setPackage("com.android.server.telecom")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {CoroutineScope(Dispatchers.IO).launch {

                val result = fragment.permissionsBuilder(
                    Manifest.permission.CALL_PHONE,
                ).build().sendSuspend()

                if (result.allGranted()) { // All the permissions are granted.

                    withContext(Dispatchers.Main) {

                        val msg: String = "$amount sent to $phone. Completed!"
                        util.onShowMessageSuccess(msg, context)
                        Log.e(TAG, msg)

                        context.startActivity(intent)

                    }

                }
                else {

                    withContext(Dispatchers.Main) {

                        val msg: String = "You have denied some " +
                                "permissions permanently. Functions will not work without these permission. " +
                                "Please grant the permissions for this app in your phone's settings."
                        util.onShowMessage(msg, context)
                        Log.e(TAG, msg)

                    }

                }

            }


            }
            else {

                val msg: String = "$amount sent to $phone. Completed!"
                util.onShowMessageSuccess(msg, context)
                Log.e(TAG, msg)

                context.startActivity(intent)

            }


        }
        else {

            val msg: String = "Error has occurred. Please try again."
            util.onShowErrorMessage(msg, context)
            Log.e(TAG, msg)

        }

    }


    // TODO Use with future update
    //2u 08012345678 500 2314 to 432
    fun smsShareAuto(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        rechargeCode: String,
        phone: String,
        amount: String,
        password: String,
    ) {

        val util: Util = Util()

        val message = "2U $phone $amount $password"

        val SENT = "SMS_SENT"
        val DELIVERED = "SMS_DELIVERED"

        var smsManager: SmsManager
        val sentPI: PendingIntent = PendingIntent
            .getBroadcast(context, 0, Intent(SENT), 0)

        val deliveredPI: PendingIntent = PendingIntent
            .getBroadcast(context, 0, Intent(DELIVERED), 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            //SubscriptionManager localSubscriptionManager = SubscriptionManager.from(context);
            val localSubscriptionManager =
                context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                CoroutineScope(Dispatchers.IO).launch {

                    val result = fragment.permissionsBuilder(
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_PHONE_STATE,
                    ).build().sendSuspend()

                    if (result.allGranted()) { // All the permissions are granted.

                        withContext(Dispatchers.Main) {

                            var simCardsLength = localSubscriptionManager.activeSubscriptionInfoCount

                            if (simCardsLength == 0) {
                                simCardsLength = localSubscriptionManager.activeSubscriptionInfoList.size
                                Log.e(
                                    "ATTENTION ATTENTION",
                                    "getActiveSubscriptionInfoList().size(): " + localSubscriptionManager.activeSubscriptionInfoList.size
                                )
                            }

                            if (simCardsLength == 0) {
                                val manager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                                simCardsLength = manager.phoneCount
                                Log.e("ATTENTION ATTENTION", "simCardsLength: $simCardsLength")
                            }

                            if (simCardsLength > 1) {

                                val localList = localSubscriptionManager.activeSubscriptionInfoList
                                val simInfo1 = localList[0] as SubscriptionInfo
                                val simInfo2 = localList[1] as SubscriptionInfo
                                val sim1 = simInfo1.subscriptionId
                                val sim2 = simInfo2.subscriptionId

                                smsManager = if (pAccount == "1") {
                                    SmsManager.getSmsManagerForSubscriptionId(sim1)
                                }
                                else {
                                    SmsManager.getSmsManagerForSubscriptionId(sim2)
                                }

                                smsManager.sendTextMessage(
                                    rechargeCode,
                                    null,
                                    message,
                                    sentPI,
                                    deliveredPI
                                )

                                val msg = "$amount sent to $phone. Completed! Press send to complete."
                                util.onShowMessageSuccess(msg, context)
                                Log.e(TAG, msg)

                            }
                            else if (simCardsLength == 1) {

                                val localList = localSubscriptionManager.activeSubscriptionInfoList
                                val simInfo1 = localList[0] as SubscriptionInfo
                                val sim1 = simInfo1.subscriptionId

                                if (pAccount == "1") {

                                    smsManager = SmsManager.getSmsManagerForSubscriptionId(sim1)
                                    smsManager.sendTextMessage(
                                        rechargeCode,
                                        null,
                                        message,
                                        sentPI,
                                        deliveredPI
                                    ) // TODO Use with future update

                                    val msg = "$amount sent to $phone. Completed! Press send to complete."
                                    util.onShowMessageSuccess(msg, context)
                                    Log.e(TAG, msg)

                                }
                                else {

                                    val msg = "You only have one sim card. Please update " +
                                            "your settings with appropriate info and " +
                                            "set \"phone 1\" to the correct number."
                                    util.onShowErrorMessage(msg, context)
                                    Log.e(TAG, msg)


                                }

                            }
                            else {

                            }

                        }

                    }
                    else {

                        withContext(Dispatchers.Main) {

                            val msg = "You have denied some " +
                                    "permissions permanently. Functions will not work without these permission. " +
                                    "Please grant the permissions for this app in your phone's settings."
                            util.onShowMessage(msg, context)
                            Log.e(TAG, msg)

                        }

                    }

                }

            }
            else {

                var simCardsLength = localSubscriptionManager.activeSubscriptionInfoCount

                if (simCardsLength == 0) {
                    simCardsLength = localSubscriptionManager.activeSubscriptionInfoList.size
                    Log.e(
                        "ATTENTION ATTENTION",
                        "getActiveSubscriptionInfoList().size(): " + localSubscriptionManager.activeSubscriptionInfoList.size
                    )
                }

                if (simCardsLength == 0) {
                    val manager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    simCardsLength = manager.phoneCount
                    Log.e("ATTENTION ATTENTION", "simCardsLength: $simCardsLength")
                }

                if (simCardsLength > 1) {

                    val localList = localSubscriptionManager.activeSubscriptionInfoList
                    val simInfo1 = localList[0] as SubscriptionInfo
                    val simInfo2 = localList[1] as SubscriptionInfo
                    val sim1 = simInfo1.subscriptionId
                    val sim2 = simInfo2.subscriptionId

                    smsManager = if (pAccount == "1") {
                        SmsManager.getSmsManagerForSubscriptionId(sim1)
                    }
                    else {
                        SmsManager.getSmsManagerForSubscriptionId(sim2)
                    }

                    smsManager.sendTextMessage(rechargeCode, null, message, sentPI, deliveredPI)

                    val msg = "$amount sent to $phone. Completed! Press send to complete."
                    util.onShowMessageSuccess(msg, context)
                    Log.e(TAG, msg)

                }
                else if (simCardsLength == 1) {

                    val localList = localSubscriptionManager.activeSubscriptionInfoList
                    val simInfo1 = localList[0] as SubscriptionInfo
                    val sim1 = simInfo1.subscriptionId

                    if (pAccount == "1") {

                        smsManager = SmsManager.getSmsManagerForSubscriptionId(sim1)
                        smsManager.sendTextMessage(
                            rechargeCode,
                            null,
                            message,
                            sentPI,
                            deliveredPI
                        ) // TODO Use with future update

                        val msg = "$amount sent to $phone. Completed! Press send to complete."
                        util.onShowMessageSuccess(msg, context)
                        Log.e(TAG, msg)

                    }
                    else {

                        val msg = "You only have one sim card. Please update " +
                                "your settings with appropriate info and " +
                                "set \"phone 1\" to the correct number."
                        util.onShowErrorMessage(msg, context)
                        Log.e(TAG, msg)


                    }

                }

            }


        }
        else {

            if (pAccount == "1") {
                SimUtil.sendSMS(context, 0, rechargeCode, null, message, sentPI, deliveredPI)
            }
            else {
                SimUtil.sendSMS(context, 1, rechargeCode, null, message, sentPI, deliveredPI)
            }

            val msg = "$amount sent to $phone. Completed! Press send to complete."
            util.onShowMessageSuccess(msg, context)
            Log.e(TAG, msg)

        }

    }

    // TODO Updated with SMS Intent: Fixed temporally for SMS Policy
    //2u 08012345678 500 2314 to 432
    private fun smsShare(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        rechargeCode: String,
        phone: String,
        amount: String,
        password: String,
    ) {

        val util: Util = Util()

        val message = "2U $phone $amount $password"
        
        val sendSmsIntent = Intent(Intent.ACTION_VIEW)
        sendSmsIntent.data = Uri.parse("sms:$rechargeCode")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            //SubscriptionManager localSubscriptionManager = SubscriptionManager.from(context);
            val localSubscriptionManager =
                context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

            CoroutineScope(Dispatchers.IO).launch {

                val result = fragment.permissionsBuilder(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CALL_PHONE,
                ).build().sendSuspend()

                if (result.allGranted()) { // All the permissions are granted.

                    withContext(Dispatchers.Main) {

                        var simCardsLength = localSubscriptionManager.activeSubscriptionInfoCount

                        if (simCardsLength == 0) {

                            simCardsLength =
                                localSubscriptionManager.activeSubscriptionInfoList.size
                            Log.e(
                                "ATTENTION ATTENTION",
                                "getActiveSubscriptionInfoList().size(): " +
                                        localSubscriptionManager.activeSubscriptionInfoList.size
                            )

                        }
                        if (simCardsLength == 0) {

                            val manager =
                                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                            simCardsLength = manager.phoneCount
                            Log.e("ATTENTION ATTENTION", "simCardsLength: $simCardsLength")

                        }
                        if (simCardsLength > 1) {

                            if (pAccount == "1") sendToSim(context, sendSmsIntent, 0)
                            else sendToSim(context, sendSmsIntent, 1)

                            sendSmsIntent.putExtra("sms_body", message)
                            val msg = "$amount sent to $phone. Completed!"
                            util.onShowMessageSuccess(msg, context)

                            //startActivity(sendSmsIntent);
                            activity.startActivityForResult(
                                Intent.createChooser(
                                    sendSmsIntent,
                                    context.getString(R.string.choose_messenger_instructions)
                                ), 5
                            )

                        } else if (simCardsLength == 1) {

                            if (pAccount == "1") {

                                sendToSim(context, sendSmsIntent, 0)
                                sendSmsIntent.putExtra("sms_body", message)
                                val msg = "$amount sent to $phone. Completed!"

                                util.onShowMessageSuccess(msg, context)
                                Log.e(TAG, msg)

                                //startActivity(sendSmsIntent);
                                activity.startActivityForResult(
                                    Intent.createChooser(
                                        sendSmsIntent,
                                        context.getString(R.string.choose_messenger_instructions)
                                    ), 5
                                )

                            } else {
                                val msg = "You only have one sim card. Please update " +
                                        "your settings with appropriate info and " +
                                        "set \"phone 1\" to the correct number."
                                util.onShowErrorMessage(msg, context)
                                Log.e(TAG, msg)
                            }

                        }

                    }

                }
                else {

                    withContext(Dispatchers.Main) {
                        
                        val msg = "You have denied some " +
                                "permissions permanently. Functions will not work without these permission. " +
                                "Please grant the permissions for this app in your phone's settings."
                        util.onShowMessage(msg, context)
                        Log.e(TAG, msg)

                    }

                }

            }

        }
        else {

            if (pAccount == "1") sendToSim(context, sendSmsIntent, 0)
            else sendToSim(context, sendSmsIntent, 1)

            sendSmsIntent.putExtra("sms_body", message)
            val msg = "$amount sent to $phone. Completed!"
            util.onShowMessageSuccess(msg, context)
            Log.e(TAG, msg)

            activity.startActivityForResult(
                Intent.createChooser(
                    sendSmsIntent,
                    context.getString(R.string.choose_messenger_instructions)
                ), 5
            )

        }

    }


    // MTN      https://www.mtnonline.com/products-services/value-added-services/share-and-sell
    // Glo      https://www.gloworld.com/ng/personal/voice-sms/easyshare/
    // Etisalat https://www.85kobo.com/how-to-recharge-etisalat-airtime/
    // Etisalat https://www.value2sms.com/index.php/gsm-codes.html
    // Airtel   https://gmposts.com/transfer-credit-one-line-another-mtn-glo-airtel-etisalat-network/
    // Airtel   https://www.legit.ng/1112865-how-transfer-airtime-airtel.html
    // Visafone https://allpromonigeria.blogspot.com/2017/06/codes-to-share-and-transfer-airtime-in.html
    fun share(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        network: String,
        phoneNumber: String,
        amount: String,
        password: String,
    ) {

        val util: Util = Util()

        when (network) {

            "Airtel" -> {

                val rechargeCode = "432"
                util.rechargeCodeTemp = rechargeCode
                util.pAccountTemp = pAccount
                util.phoneNumberTemp = phoneNumber
                util.amountTemp = amount
                util.passwordTemp = password

                smsShare(
                    context,
                    fragment,
                    activity,
                    pAccount,
                    rechargeCode,
                    phoneNumber,
                    amount,
                    password
                )
                Log.e(TAG, "$network: from key to rechargeBool")

            }
            "Etisalat(9Mobile)" -> {

                val rechargeCode = "100"

                simpleShare(
                    context,
                    fragment,
                    activity,
                    pAccount,
                    rechargeCode,
                    phoneNumber,
                    amount,
                    password
                )

                Log.e(TAG, "$network: from key to rechargeBool")

            }
            "Glo Mobile" -> {

                val rechargeCode = "131"

                simpleShare(
                    context,
                    fragment,
                    activity,
                    pAccount,
                    rechargeCode,
                    phoneNumber,
                    amount,
                    password
                )

                Log.e(TAG, "$network: from key to rechargeBool")

            }
            "MTN Nigeria" -> {

                val rechargeCode = "600"

                simpleShare(
                    context,
                    fragment,
                    activity,
                    pAccount,
                    rechargeCode,
                    phoneNumber,
                    amount,
                    password
                )

                Log.e(TAG, "$network: from key to rechargeBool")

            }
            "Visafone" -> {

                val rechargeCode = "447"

                simpleShare(
                    context,
                    fragment,
                    activity,
                    pAccount,
                    rechargeCode,
                    phoneNumber,
                    amount,
                    password
                )

                Log.e(TAG, "$network: from key to rechargeBool")

            }
            else -> {

                KToasty.error(
                    context, "$amount not sent to $phoneNumber. Error!",
                    Toast.LENGTH_LONG, true
                ).show()

                println("$network: error sharing credit!")

            }

        }

    }


    fun amountChoiceBankTransfer(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        modelList: MutableList<User>,
        friend: Friend,
    ) {

        val accountChoiceBuilder: AlertDialog.Builder = AlertDialog.Builder(
            context,
            R.style.MyDialogTheme
        )

        accountChoiceBuilder.setTitle("Choose the phone and bank you want to transfer from")

        val pAccountType = mutableListOf<String>()
        val pAccountList = mutableListOf<String>()

        val banksList1 = mutableListOf<String>()
        val banksList2 = mutableListOf<String>()

        if (modelList[0].phone != null && modelList[0].phone!!.trim() != "") {

            if (modelList[0].bank1 != null && modelList[0].bank1 != "Choose Bank") {
                banksList1.add(modelList[0].bank1!!)
            }

            if (modelList[0].bank2 != null && modelList[0].bank2 != "Choose Bank") {
                banksList1.add(modelList[0].bank2!!)
            }

            if (modelList[0].bank3 != null && modelList[0].bank3 != "Choose Bank") {
                banksList1.add(modelList[0].bank3!!)
            }

            if (modelList[0].bank4 != null && modelList[0].bank4 != "Choose Bank") {
                banksList1.add(modelList[0].bank4!!)
            }

            pAccountType.add(modelList[0].phone!!)
            pAccountList.add("1")

        }
        if (modelList.size > 1 && modelList[1].phone != null && modelList[1].phone != "") {

            if (modelList[1].bank1 != null && modelList[1].bank1 != "Choose Bank") {
                banksList2.add(modelList[1].bank1!!)
            }

            if (modelList[1].bank2 != null && modelList[1].bank2 != "Choose Bank") {
                banksList2.add(modelList[1].bank2!!)
            }

            if (modelList[1].bank3 != null && modelList[1].bank3 != "Choose Bank") {
                banksList2.add(modelList[1].bank3!!)
            }

            if (modelList[1].bank4 != null && modelList[1].bank4 != "Choose Bank") {
                banksList2.add(modelList[1].bank4!!)
            }

            pAccountType.add(modelList[1].phone!!)
            pAccountList.add("2")

        }

        var banks1: String = ""
        var banks2: String = ""

        for ((i, bank) in banksList1.withIndex()) {

            banks1 = when (i) {
                0 -> {
                    bank
                }
                banksList1.size - 1 -> {
                    "$banks1, $bank."
                }
                else -> {
                    "$banks1, $bank"
                }
            }

        }

        for ((i, bank) in banksList2.withIndex()) {

            banks2 = when (i) {
                0 -> {
                    bank
                }
                banksList2.size - 1 -> {
                    "$banks2, $bank."
                }
                else -> {
                    "$banks2, $bank"
                }
            }

        }

        var banksAndAccounts1: String = ""
        var banksAndAccounts2: String = ""

        val banksAndAccountsList = mutableListOf<String>()

        banksAndAccounts1 = "${pAccountType[0]} with banks: $banks1"
        banksAndAccountsList.add(banksAndAccounts1)

        if (pAccountList.size > 1 && banksList2.size > 0) {
            banksAndAccounts2 = "${pAccountType[1]} with banks: $banks2"
            banksAndAccountsList.add(banksAndAccounts2)
        }

        val friendBankChoiceBuilder: AlertDialog.Builder = AlertDialog.Builder(
            context,
            R.style.MyDialogTheme)

        friendBankChoiceBuilder.setTitle("Choose your friend's account")

        val amountBuilder: AlertDialog.Builder = AlertDialog.Builder(context, R.style.MyDialogTheme)

        amountBuilder.setTitle("Amount")

        val amountViewInflated: View = LayoutInflater.from(context).inflate(
            R.layout.input_amount,
            null)

        val amountInput = amountViewInflated.findViewById<View>(R.id.input_amount) as TextInputEditText
        amountBuilder.setView(amountViewInflated)

        if (banksAndAccountsList.size == 1) {

            val user: User = modelList[0]
            val pAccount: String = pAccountList[0]

            val name: String = if (friend.name == null || friend.name!!.trim() == "") {
                "Unknown Name"
            }
            else {
                friend.name!!
            }

            val banks: MutableList<String> = mutableListOf<String>()
            val accountNumbers: MutableList<String> = mutableListOf<String>()
            val banksAndAccountNumbers: MutableList<String> = mutableListOf<String>()

            if ((friend.bank1 != null && friend.bank1 != "Choose Bank") &&
                (friend.accountNumber1 != null && friend.accountNumber1 != "")) {
                banks.add(friend.bank1!!)
                accountNumbers.add(friend.accountNumber1!!)
                banksAndAccountNumbers.add("${friend.accountNumber1}: ${friend.bank1}")
            }

            if ((friend.bank2 != null && friend.bank2 != "Choose Bank") &&
                (friend.accountNumber2 != null && friend.accountNumber2 != "")) {
                banks.add(friend.bank2!!)
                accountNumbers.add(friend.accountNumber2!!)
                banksAndAccountNumbers.add("${friend.accountNumber2}: ${friend.bank2}")
            }

            if ((friend.bank3 != null && friend.bank3 != "Choose Bank") &&
                (friend.accountNumber3 != null && friend.accountNumber3 != "")) {
                banks.add(friend.bank3!!)
                accountNumbers.add(friend.accountNumber3!!)
                banksAndAccountNumbers.add("${friend.accountNumber1}: ${friend.bank1}")
            }

            if ((friend.bank4 != null && friend.bank4 != "Choose Bank") &&
                (friend.accountNumber4 != null && friend.accountNumber4 != "")) {
                banks.add(friend.bank4!!)
                accountNumbers.add(friend.accountNumber4!!)
                banksAndAccountNumbers.add("${friend.accountNumber1}: ${friend.bank1}")
            }

            Log.e("ATTENTION ATTENTION", "Choose your friend's account")
            Log.e("ATTENTION ATTENTION", "\n\n\nFriend's account numbers: ${banksAndAccountNumbers.toString()}")


            if (banksAndAccountNumbers.size == 1) {

                val bank: String = banks[0]
                val accountNumber: String = accountNumbers[0]

                amountBuilder.setPositiveButton(android.R.string.ok) { dialog, which ->

                    dialog.dismiss()

                    val amountInputStr: String = amountInput.text.toString()

                    bankChoiceTransfer(
                        context, fragment, activity, user, pAccount,
                        name, accountNumber, amountInputStr, bank
                    )

                }

                amountBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                amountBuilder.show()

            }
            else {

                friendBankChoiceBuilder.setSingleChoiceItems(banksAndAccountNumbers.toTypedArray(), -1) {

                        dialogInterface, i ->

                    dialogInterface.dismiss()

                    val bank: String = banks[i]
                    val accountNumber: String = accountNumbers[i]

                    amountBuilder.setPositiveButton(android.R.string.ok) { dialog, which ->

                        dialog.dismiss()

                        val amountInputStr: String = amountInput.text.toString()

                        bankChoiceTransfer(
                            context, fragment, activity, user, pAccount,
                            name, accountNumber, amountInputStr, bank
                        )

                    }

                    amountBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                    amountBuilder.show()

                }

                friendBankChoiceBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                friendBankChoiceBuilder.show()

            }

        }
        else {

            accountChoiceBuilder.setSingleChoiceItems(banksAndAccountsList.toTypedArray(), -1) {

                    dialogInterface, i ->

                dialogInterface.dismiss()

                val user: User = modelList[i]
                val pAccount: String = pAccountList[i]

                val name: String = if (friend.name == null || friend.name!!.trim() == "") {
                    "Unknown Name"
                }
                else {
                    friend.name!!
                }

                val banks: MutableList<String> = mutableListOf<String>()
                val accountNumbers: MutableList<String> = mutableListOf<String>()
                val banksAndAccountNumbers: MutableList<String> = mutableListOf<String>()

                if ((friend.bank1 != null && friend.bank1 != "Choose Bank") &&
                    (friend.accountNumber1 != null && friend.accountNumber1 != "")) {
                    banks.add(friend.bank1!!)
                    accountNumbers.add(friend.accountNumber1!!)
                    banksAndAccountNumbers.add("${friend.accountNumber1}: ${friend.bank1}")
                }

                if ((friend.bank2 != null && friend.bank2 != "Choose Bank") &&
                    (friend.accountNumber2 != null && friend.accountNumber2 != "")) {
                    banks.add(friend.bank2!!)
                    accountNumbers.add(friend.accountNumber2!!)
                    banksAndAccountNumbers.add("${friend.accountNumber2}: ${friend.bank2}")
                }

                if ((friend.bank3 != null && friend.bank3 != "Choose Bank") &&
                    (friend.accountNumber3 != null && friend.accountNumber3 != "")) {
                    banks.add(friend.bank3!!)
                    accountNumbers.add(friend.accountNumber3!!)
                    banksAndAccountNumbers.add("${friend.accountNumber1}: ${friend.bank1}")
                }

                if ((friend.bank4 != null && friend.bank4 != "Choose Bank") &&
                    (friend.accountNumber4 != null && friend.accountNumber4 != "")) {
                    banks.add(friend.bank4!!)
                    accountNumbers.add(friend.accountNumber4!!)
                    banksAndAccountNumbers.add("${friend.accountNumber1}: ${friend.bank1}")
                }

                Log.e("ATTENTION ATTENTION", "Choose your friend's account")
                Log.e("ATTENTION ATTENTION", "\n\n\nFriend's account numbers: ${banksAndAccountNumbers.toString()}")

                if (banksAndAccountNumbers.size == 1) {

                    val bank: String = banks[0]
                    val accountNumber: String = accountNumbers[0]

                    amountBuilder.setPositiveButton(android.R.string.ok) { dialog, which ->

                        dialog.dismiss()

                        val amountInputStr: String = amountInput.text.toString()

                        bankChoiceTransfer(
                            context, fragment, activity, user, pAccount,
                            name, accountNumber, amountInputStr, bank
                        )

                    }

                    amountBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                    amountBuilder.show()

                }
                else {

                    friendBankChoiceBuilder.setSingleChoiceItems(banksAndAccountNumbers.toTypedArray(), -1) {

                            dialogInterface, i ->

                        dialogInterface.dismiss()

                        val bank: String = banks[i]
                        val accountNumber: String = accountNumbers[i]

                        amountBuilder.setPositiveButton(android.R.string.ok) { dialog, which ->

                            dialog.dismiss()

                            val amountInputStr: String = amountInput.text.toString()

                            bankChoiceTransfer(
                                context, fragment, activity, user, pAccount,
                                name, accountNumber, amountInputStr, bank
                            )

                        }

                        amountBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                        amountBuilder.show()

                    }

                    friendBankChoiceBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                    friendBankChoiceBuilder.show()

                }

            }

            accountChoiceBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
            accountChoiceBuilder.show()

        }

    }


    // https://www.codingdemos.com/android-alertdialog-single-choice/
    // https://android--code.blogspot.com/2015/08/android-alertdialog-multichoice.html
    private fun bankChoiceTransfer(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        model: User,
        pAccount: String,
        name: String,
        amount: String,
        accountNumber: String,
        bank: String,
    ) {

        val bankChoiceBuilder: AlertDialog.Builder = AlertDialog.Builder(
            context,
            R.style.MyDialogTheme
        )
        bankChoiceBuilder.setTitle("Choose your Bank")

        val pBanks = mutableListOf<String>()

        if (model.bank1 != null && model.bank1 != "Choose Bank") {

            pBanks.add(model.bank1!!)

        }
        if (model.bank2 != null && model.bank2 != "Choose Bank") {

            pBanks.add(model.bank2!!)

        }
        if (model.bank3 != null && model.bank3 != "Choose Bank") {

            pBanks.add(model.bank3!!)

        }
        if (model.bank4 != null && model.bank4 != "Choose Bank") {

            pBanks.add(model.bank4!!)

        }

//        if (pAccount == "1") {
//            pBanks = p1Banks
//        } else {
//            pBanks = p2Banks
//        }

        bankChoiceBuilder.setSingleChoiceItems(pBanks.toTypedArray(), -1)
            { dialogInterface, i ->

                dialogInterface.dismiss()

                if (pBanks.contains(bank)) {
                    
                    when (pBanks[i]) {

                        "Access Bank" -> {

                            val code = "901"
                            val ext = "1"

                            commonExtBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                ext,
                                amount,
                                accountNumber
                            )

                            KToasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "Diamond Bank" -> {

                            val code = "426"

                            commonBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                amount,
                                accountNumber
                            )

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "EcoBank" -> {

                            val code = "326"
                            val ext = "1"

                            commonExtBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                ext,
                                amount,
                                accountNumber
                            )

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "FCMB Bank" -> {

                            val code = "329"

                            commonBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                amount,
                                accountNumber
                            )

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "Fidelity Bank" -> {

                            val code = "770"

                            commonBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                accountNumber,
                                amount
                            ) // reversed

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "First Bank" -> {

                            val code = "894"

                            commonBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                amount,
                                accountNumber
                            )

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "GTBank" -> {

                            val code = "737"
                            val ext = "1"

                            commonExtBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                ext,
                                amount,
                                accountNumber
                            )

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "Heritage Bank" -> {

                            val code = "322*030"

                            commonBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                accountNumber,
                                amount
                            ) // reverse

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "Jaiz Bank" -> {

                            val code = "389*301"

                            commonBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                accountNumber,
                                amount
                            ) // reverse

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "Keystone Bank" -> {

                            val code = "7111"
                            commonBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                amount,
                                accountNumber
                            )
                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "Polaris Bank" -> {

                            val code = "833"

                            commonBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                amount,
                                accountNumber
                            )

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "Stanbic IBTC Bank" -> {

                            val code = "909"
                            val ext = "11"

                            commonExtBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                ext,
                                amount,
                                accountNumber
                            )

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "Sterling Bank" -> {

                            val code = "822"
                            val ext = "4"

                            commonExtBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                ext,
                                amount,
                                accountNumber
                            )

                            Toasty.success(
                                context,
                                "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG,
                                true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "UBA Bank" -> {

                            val code = "919"
                            val ext = "3"

                            commonExtBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                ext,
                                accountNumber,
                                amount
                            ) // reverse

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "Union Bank" -> {

                            val code = "826"
                            val ext = "1"

                            commonExtBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                ext,
                                amount,
                                accountNumber
                            )

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "Unity Bank" -> {

                            val code = "7799"
                            val ext = "1"

                            commonExtBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                ext,
                                accountNumber,
                                amount
                            ) // reverse

                            Toasty.success(
                                context,
                                "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG,
                                true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "Wema Bank" -> {

                            val code = "945"

                            commonBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                accountNumber,
                                amount
                            ) // reverse

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "Zenith Bank" -> {

                            val code = "966"

                            commonBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                amount,
                                accountNumber
                            )

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        else -> {

                            Toasty.error(
                                context,
                                "Error making bank transfer! Please check account settings in Edit Tab.",
                                Toasty.LENGTH_LONG,
                                true
                            ).show()

                            println("$bank: Error making bank transfer! Please check account settings in Edit Tab.")

                        }
                    }
                }

                // TODO Done Outside bank
                else {

                    when (pBanks[i]) {

                        "Access Bank" -> {

                            val code = "901"
                            val ext = "2"

                            commonExtBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                ext,
                                amount,
                                accountNumber
                            )

                            Toasty.success(
                                context,
                                "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG,
                                true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "Diamond Bank" -> {

                            val code = "426"
                            commonBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                amount,
                                accountNumber
                            )

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "EcoBank" -> {

                            val code = "326"
                            val ext = "1"

                            commonExtBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                ext,
                                amount,
                                accountNumber
                            )

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "FCMB Bank" -> {

                            val code = "329"

                            commonBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                amount,
                                accountNumber
                            )

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "Fidelity Bank" -> {

                            val code = "770"

                            commonBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                accountNumber,
                                amount
                            ) // reversed

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "First Bank" -> {

                            val code = "894"

                            commonBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                amount,
                                accountNumber
                            )

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "GTBank" -> {

                            val code = "737"
                            val ext = "2"

                            commonExtBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                ext,
                                amount,
                                accountNumber
                            )

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "Heritage Bank" -> {

                            val code = "322*030"

                            commonBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                accountNumber,
                                amount
                            ) // reverse

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "Jaiz Bank" -> {

                            val code = "389*301"

                            commonBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                accountNumber,
                                amount
                            ) // reverse

                            Toasty.success(
                                context,
                                "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG,
                                true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "Keystone Bank" -> {

                            val code = "7111"

                            commonBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                amount,
                                accountNumber
                            )

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "Polaris Bank" -> {

                            val code = "833"
                            commonBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                amount,
                                accountNumber
                            )

                            Toasty.success(
                                context,
                                "Transferring $amount to $name with $bank. Follow prompts.",
                                Toasty.LENGTH_LONG,
                                true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "Stanbic IBTC Bank" -> {

                            val code = "909"
                            val ext = "22"

                            commonExtBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                ext,
                                amount,
                                accountNumber
                            )

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "Sterling Bank" -> {

                            val code = "822"
                            val ext = "5"

                            commonExtBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                ext,
                                amount,
                                accountNumber
                            )

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "UBA Bank" -> {

                            val code = "919"
                            val ext = "4"

                            commonExtBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                ext,
                                accountNumber,
                                amount
                            ) // reverse

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "Union Bank" -> {

                            val code = "826"
                            val ext = "2"

                            commonExtBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                ext,
                                amount,
                                accountNumber
                            )

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "Unity Bank" -> {

                            val code = "7799"
                            val ext = "2"

                            commonExtBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                ext,
                                accountNumber,
                                amount
                            ) // reverse

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "Wema Bank" -> {

                            val code = "945"

                            commonBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                accountNumber,
                                amount
                            ) // reverse

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        "Zenith Bank" -> {

                            val code = "966"

                            commonBankTransfer(
                                context,
                                fragment,
                                activity,
                                pAccount,
                                code,
                                amount,
                                accountNumber
                            )

                            Toasty.success(
                                context, "Transferring $amount to $name with $bank complete.",
                                Toasty.LENGTH_LONG, true
                            ).show()

                            Log.e(TAG, "Transferring $amount to $name with $bank complete.")

                        }
                        else -> {

                            Toasty.error(
                                context,
                                "Error making bank transfer! Please check account settings in Edit Tab.",
                                Toasty.LENGTH_LONG,
                                true
                            ).show()

                            println("$bank: Error making bank transfer! Please check account settings in Edit Tab.")

                        }
                    }

                }

            }

        bankChoiceBuilder.setNegativeButton(android.R.string.cancel)
            { dialog, which ->
                dialog.cancel()
        }

        bankChoiceBuilder.show()

    }


    private fun commonBankTransfer(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        code: String,
        amount: String,
        accountNumber: String
    ) {

        val ussdCode = "*" + code + "*" + amount + "*" + accountNumber + Uri.encode("#")

        val intent: Intent = Intent("android.intent.action.CALL", Uri.parse("tel:$ussdCode"))

        if (pAccount == "1") {
            sendToSim(context, intent, 0)
        }
        else {
            sendToSim(context, intent, 1)
        }

        /*if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {

            TelecomManager telecomManager = (TelecomManager) context.getSystemService(TelecomManager.class);

            //intent.setPackage(telecomManager.getDefaultDialerPackage());
            intent.setPackage("com.android.server.telecom");
        }
        else {

            PackageManager packageManager = context.getPackageManager();
            List activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

            for(int j = 0 ; j < activities.size() ; j++)
            {
                if(activities.get(j).toString().toLowerCase().contains("com.android.phone"))
                {
                    intent.setPackage("com.android.phone");
                }
                else if(activities.get(j).toString().toLowerCase().contains("call"))
                {
                    String pack = (activities.get(j).toString().split("[ ]")[1].split("[/]")[0]);

                    intent.setPackage(pack);
                }
            }
        }*/
        
        intent.setPackage("com.android.server.telecom")

        val util: Util = Util()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            CoroutineScope(Dispatchers.IO).launch {

                val result = fragment.permissionsBuilder(
                    Manifest.permission.CALL_PHONE,
                ).build().sendSuspend()

                if (result.allGranted()) { // All the permissions are granted.

                    withContext(Dispatchers.Main) {

                        val msg: String = "Bank transfer requested."
                        //util.onShowMessageSuccess(msg, context)
                        Log.e(TAG, msg)

                        context.startActivity(intent)

                    }

                }
                else {

                    withContext(Dispatchers.Main) {

                        val msg = "You have denied some " +
                                "permissions permanently. Functions will not work without these permission. " +
                                "Please grant the permissions for this app in your phone's settings."
                        util.onShowMessage(msg, context)
                        Log.e(TAG, msg)

                    }

                }

            }

        }
        else {

            val msg: String = "Bank transfer requested."
            //util.onShowMessageSuccess(msg, context)
            Log.e(TAG, msg)

            context.startActivity(intent)

        }


        
    }

    private fun commonExtBankTransfer(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        code: String,
        ext: String,
        amount: String,
        accountNumber: String
    ) {

        val ussdCode = "*" + code + "*" + ext + "*" + amount + "*" + accountNumber + Uri.encode("#")

        val intent: Intent = Intent("android.intent.action.CALL", Uri.parse("tel:$ussdCode"))

        if (pAccount == "1") {
            sendToSim(context, intent, 0)
        }
        else {
            sendToSim(context, intent, 1)
        }

        /*if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {

            TelecomManager telecomManager = (TelecomManager) context.getSystemService(TelecomManager.class);

            //intent.setPackage(telecomManager.getDefaultDialerPackage());
            intent.setPackage("com.android.server.telecom");
        }
        else {

            PackageManager packageManager = context.getPackageManager();
            List activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

            for(int j = 0 ; j < activities.size() ; j++)
            {
                if(activities.get(j).toString().toLowerCase().contains("com.android.phone"))
                {
                    intent.setPackage("com.android.phone");
                }
                else if(activities.get(j).toString().toLowerCase().contains("call"))
                {
                    String pack = (activities.get(j).toString().split("[ ]")[1].split("[/]")[0]);

                    intent.setPackage(pack);
                }
            }
        }*/

        intent.setPackage("com.android.server.telecom")
        
        val util: Util = Util()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            CoroutineScope(Dispatchers.IO).launch {

                val result = fragment.permissionsBuilder(
                    Manifest.permission.CALL_PHONE,
                ).build().sendSuspend()

                if (result.allGranted()) { // All the permissions are granted.

                    withContext(Dispatchers.Main) {

                        val msg: String = "Bank transfer requested."
//                        util.onShowMessageSuccess(msg, context)
                        Log.e(TAG, msg)

                        context.startActivity(intent)

                    }

                }
                else {

                    withContext(Dispatchers.Main) {

                        val msg = "You have denied some " +
                                "permissions permanently. Functions will not work without these permission. " +
                                "Please grant the permissions for this app in your phone's settings."
                        util.onShowMessage(msg, context)
                        Log.e(TAG, msg)

                    }

                }

            }

        }
        else {

            val msg: String = "Bank transfer requested."
//            util.onShowMessageSuccess(msg, context)
            Log.e(TAG, msg)

            context.startActivity(intent)

        }



    }

    fun showAccountNumber(
        context: Context,
        friend: Friend,
    ) {

        var accountNumberAndBank: String = ""

        var accountNumbers: MutableList<String> = mutableListOf<String>()
        var accountNumberAndBanks: MutableList<String> = mutableListOf<String>()

        if (friend.accountNumber1 != null &&
            friend.accountNumber1 != "" &&
            friend.bank1 != null &&
            friend.bank1 != "Choose Bank")
            {

            accountNumbers.add(friend.accountNumber1!!)
            accountNumberAndBanks.add("${friend.accountNumber1}: ${friend.bank1}")

        }
        if (friend.accountNumber2 != null &&
            friend.accountNumber2 != "" &&
            friend.bank2 != null &&
            friend.bank2 != "Choose Bank")
            {

            accountNumbers.add(friend.accountNumber2!!)
            accountNumberAndBanks.add("${friend.accountNumber2}: ${friend.bank2}")

        }
        if (friend.accountNumber3 != null &&
            friend.accountNumber3 != "" &&
            friend.bank3 != null &&
            friend.bank3 != "Choose Bank") {

            accountNumbers.add(friend.accountNumber3!!)
            accountNumberAndBanks.add("${friend.accountNumber3}: ${friend.bank3}")

        }
        if (friend.accountNumber4 != null &&
            friend.accountNumber4 != "" &&
            friend.bank4 != null &&
            friend.bank4 != "Choose Bank") {

//            accountNumber += ""
            accountNumbers.add(friend.accountNumber4!!)
            accountNumberAndBanks.add("${friend.accountNumber4}: ${friend.bank4}")

        }

        for (i in 0 until accountNumberAndBanks.size) {

            accountNumberAndBank = when (i) {
                0 -> {

                    accountNumberAndBanks[i]

                }
                accountNumberAndBanks.size - 1 -> {

                    "$accountNumberAndBank, ${accountNumberAndBanks[i]}."

                }
                else -> {

                    "$accountNumberAndBank, ${accountNumberAndBanks[i]}, "

                }
            }

        }

        if (accountNumberAndBank == "") {
            accountNumberAndBank = "You have not set an account number for this friend."
        }

        val dialog = AlertDialog.Builder(context, R.style.MyDialogTheme)

        var name: String = ""
        var title: String = "Account number(s):"

        if (friend.name != null || friend.name!!.trim() != "") {
            name = friend.name!! + " "
        }

        title = name + title
        dialog.setTitle(title)
        dialog.setMessage(accountNumberAndBank)

        dialog.setPositiveButton(android.R.string.ok) { dialog, which: Int -> dialog.cancel() }

//        dialog.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
        dialog.show()

    }

    fun dataBuyForFriend(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        modelList: MutableList<User>,
        friend: Friend,
    ) {

        val name: String = if (friend.name == null || friend.name!!.trim() == "") {
            "Unknown Name"
        }
        else {
            friend.name!!
        }

        val networks: MutableList<String> = mutableListOf<String>()
        val phones: MutableList<String> = mutableListOf<String>()
        val phonesAndNetworks: MutableList<String> = mutableListOf<String>()

        if ((friend.network1 != null && friend.network1 != "Choose Network") &&
            (friend.phone1 != null && friend.phone1!!.trim() != "")) {
            networks.add(friend.network1!!)
            phones.add(friend.phone1!!)
            phonesAndNetworks.add("${friend.phone1}: ${friend.network1}")
        }

        if ((friend.network2 != null && friend.network2 != "Choose Network") &&
            (friend.phone2 != null && friend.phone2!!.trim() != "")) {
            networks.add(friend.network2!!)
            phones.add(friend.phone2!!)
            phonesAndNetworks.add("${friend.phone2}: ${friend.network2}")
        }

        if ((friend.network3 != null && friend.network3 != "Choose Network") &&
            (friend.phone3 != null && friend.phone3!!.trim() != "")) {
            networks.add(friend.network3!!)
            phones.add(friend.phone3!!)
            phonesAndNetworks.add("${friend.phone3}: ${friend.network3}")
        }

        val friendPhoneChoiceBuilder: AlertDialog.Builder = AlertDialog.Builder(context, R.style.MyDialogTheme)

        friendPhoneChoiceBuilder.setTitle("Choose your friend's phone number")

        val accountChoiceBuilder: AlertDialog.Builder = AlertDialog.Builder(context, R.style.MyDialogTheme)

        accountChoiceBuilder.setTitle("Choose phone number to transfer data from.")

        if (phonesAndNetworks.size == 1) {

            val network: String = networks[0]
            val phone: String = phones[0]

            val pAccountType = mutableListOf<String>()
            val pAccountList = mutableListOf<String>()

            if (modelList[0].phone != null && modelList[0].phone!!.trim() != "") {

                val pAccount1 =
                    if (modelList[0].network != null && modelList[0].network != "Choose Network") {
                        "${modelList[0].phone!!}: ${modelList[0].network}"
                    }
                    else {
                        modelList[0].phone!!
                    }

                pAccountType.add(pAccount1)
                pAccountList.add("1")

            }
            if (modelList.size > 1 && modelList[1].phone != null && modelList[1].phone != "") {

                val pAccount2 =
                    if (modelList[1].network != null && modelList[1].network != "Choose Network") {
                        "${modelList[1].phone!!}: ${modelList[1].network}"
                    }
                    else {
                        modelList[1].phone!!
                    }

                pAccountType.add(pAccount2)
                pAccountList.add("2")

            }

            if (pAccountType.size == 1) {

                var user: User = modelList[0]
                val pAccount: String = pAccountList[0]

                if (network != user.network) {

                    val util: Util = Util()
                    val message: String = "You can only transfer airtime within the same network."
                    util.onShowErrorMessage(message, context)
                }
                else {

                    when (network) {

                        "Airtel" -> {

                            // TODO Done
                            runAirtelDataBuyForFriend(
                                activity,
                                user, pAccount,
                                name, phone)

                        }
                        "Etisalat(9Mobile)" -> {

                            // TODO Done
                            run9MobileDataBuyForFriend(
                                activity,
                                user, pAccount,
                                name, phone)

                        }
                        "Glo Mobile" -> {

                            // TODO Done
                            runGloDataBuyForFriend(
                                activity,
                                user, pAccount,
                                name, phone)

                        }
                        "MTN Nigeria" -> {

                            // TODO Done
                            runMTNDataBuyForFriend(
                                activity,
                                user, pAccount,
                                name, phone)

                        }
                        "Visafone" -> {



                        }
                        else -> {

                        }

                    }

                }

            }
            else {

                accountChoiceBuilder.setSingleChoiceItems(pAccountType.toTypedArray(), -1) {

                        dialogInterface, i ->

                    dialogInterface.dismiss()

                    var user: User = modelList[i]
                    val pAccount: String = pAccountList[i]

                    if (network != user.network) {

                        val util: Util = Util()
                        val message: String = "You can only transfer airtime within the same network."
                        util.onShowErrorMessage(message, context)
                    }
                    else {

                        when (network) {

                            "Airtel" -> {

                                // TODO Done
                                runAirtelDataBuyForFriend(
                                    activity,
                                    user, pAccount,
                                    name, phone)

                            }
                            "Etisalat(9Mobile)" -> {

                                // TODO Done
                                run9MobileDataBuyForFriend(
                                    activity,
                                    user, pAccount,
                                    name, phone)

                            }
                            "Glo Mobile" -> {

                                // TODO Done
                                runGloDataBuyForFriend(
                                    activity,
                                    user, pAccount,
                                    name, phone)

                            }
                            "MTN Nigeria" -> {

                                // TODO Done
                                runMTNDataBuyForFriend(
                                    activity,
                                    user, pAccount,
                                    name, phone)

                            }
                            "Visafone" -> {



                            }
                            else -> {

                            }

                        }

                    }

                }

                accountChoiceBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                accountChoiceBuilder.show()

            }

        }
        else {

            Log.e("ATTENTION ATTENTION", "Choose your friend's phone number")
            Log.e("ATTENTION ATTENTION", "\n\n\nFriend's Phone Numbers: ${phonesAndNetworks.toString()}")

            friendPhoneChoiceBuilder.setSingleChoiceItems(phonesAndNetworks.toTypedArray(), -1) {

                    dialogInterface, i ->

                dialogInterface.dismiss()

                val network: String = networks[i]
                val phone: String = phones[i]

                val pAccountType = mutableListOf<String>()
                val pAccountList = mutableListOf<String>()

                if (modelList[0].phone != null && modelList[0].phone!!.trim() != "") {

                    val pAccount1 =
                        if (modelList[0].network != null && modelList[0].network != "Choose Network") {
                            "${modelList[0].phone!!}: ${modelList[0].network}"
                        }
                        else {
                            modelList[0].phone!!
                        }

                    pAccountType.add(pAccount1)
                    pAccountList.add("1")

                }
                if (modelList.size > 1 && modelList[1].phone != null && modelList[1].phone != "") {

                    val pAccount2 =
                        if (modelList[1].network != null && modelList[1].network != "Choose Network") {
                            "${modelList[1].phone!!}: ${modelList[1].network}"
                        }
                        else {
                            modelList[1].phone!!
                        }

                    pAccountType.add(pAccount2)
                    pAccountList.add("2")

                }

                if (pAccountType.size == 1) {

                    var user: User = modelList[0]
                    val pAccount: String = pAccountList[0]

                    if (network != user.network) {

                        val util: Util = Util()
                        val message: String = "You can only transfer airtime within the same network."
                        util.onShowErrorMessage(message, context)
                    }
                    else {

                        when (network) {

                            "Airtel" -> {

                                // TODO Done
                                runAirtelDataBuyForFriend(
                                    activity,
                                    user, pAccount,
                                    name, phone)

                            }
                            "Etisalat(9Mobile)" -> {

                                // TODO Done
                                run9MobileDataBuyForFriend(
                                    activity,
                                    user, pAccount,
                                    name, phone)

                            }
                            "Glo Mobile" -> {

                                // TODO Done
                                runGloDataBuyForFriend(
                                    activity,
                                    user, pAccount,
                                    name, phone)

                            }
                            "MTN Nigeria" -> {

                                // TODO Done
                                runMTNDataBuyForFriend(
                                    activity,
                                    user, pAccount,
                                    name, phone)

                            }
                            "Visafone" -> {



                            }
                            else -> {

                            }

                        }

                    }

                }
                else {

                    accountChoiceBuilder.setSingleChoiceItems(pAccountType.toTypedArray(), -1) {

                            dialogInterface, i ->

                        dialogInterface.dismiss()

                        var user: User = modelList[i]
                        val pAccount: String = pAccountList[i]

                        if (network != user.network) {

                            val util: Util = Util()
                            val message: String = "You can only transfer airtime within the same network."
                            util.onShowErrorMessage(message, context)
                        }
                        else {

                            when (network) {

                                "Airtel" -> {

                                    // TODO Done
                                    runAirtelDataBuyForFriend(
                                        activity,
                                        user, pAccount,
                                        name, phone)

                                }
                                "Etisalat(9Mobile)" -> {

                                    // TODO Done
                                    run9MobileDataBuyForFriend(
                                        activity,
                                        user, pAccount,
                                        name, phone)

                                }
                                "Glo Mobile" -> {

                                    // TODO Done
                                    runGloDataBuyForFriend(
                                        activity,
                                        user, pAccount,
                                        name, phone)

                                }
                                "MTN Nigeria" -> {

                                    // TODO Done
                                    runMTNDataBuyForFriend(
                                        activity,
                                        user, pAccount,
                                        name, phone)

                                }
                                "Visafone" -> {



                                }
                                else -> {

                                }

                            }

                        }

                    }

                    accountChoiceBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                    accountChoiceBuilder.show()

                }


            }

            friendPhoneChoiceBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
            friendPhoneChoiceBuilder.show()

        }

    }

    fun dataTransfer(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        modelList: MutableList<User>,
        friend: Friend,
    ) {

        val name: String = if (friend.name == null || friend.name!!.trim() == "") {
            "Unknown Name"
        }
        else {
            friend.name!!
        }

        val networks: MutableList<String> = mutableListOf<String>()
        val phones: MutableList<String> = mutableListOf<String>()
        val phonesAndNetworks: MutableList<String> = mutableListOf<String>()

        if ((friend.network1 != null && friend.network1 != "Choose Network") &&
            (friend.phone1 != null && friend.phone1!!.trim() != "")) {
            networks.add(friend.network1!!)
            phones.add(friend.phone1!!)
            phonesAndNetworks.add("${friend.phone1}: ${friend.network1}")
        }

        if ((friend.network2 != null && friend.network2 != "Choose Network") &&
            (friend.phone2 != null && friend.phone2!!.trim() != "")) {
            networks.add(friend.network2!!)
            phones.add(friend.phone2!!)
            phonesAndNetworks.add("${friend.phone2}: ${friend.network2}")
        }

        if ((friend.network3 != null && friend.network3 != "Choose Network") &&
            (friend.phone3 != null && friend.phone3!!.trim() != "")) {
            networks.add(friend.network3!!)
            phones.add(friend.phone3!!)
            phonesAndNetworks.add("${friend.phone3}: ${friend.network3}")
        }

        val friendPhoneChoiceBuilder: AlertDialog.Builder = AlertDialog.Builder(
            context,
            R.style.MyDialogTheme
        )

        friendPhoneChoiceBuilder.setTitle("Choose your friend's phone number")

        Log.e("ATTENTION ATTENTION", "Choose your friend's phone number")
        Log.e("ATTENTION ATTENTION", "\n\n\nFriend's Phone Numbers: ${phonesAndNetworks.toString()}")

        val accountChoiceBuilder: AlertDialog.Builder = AlertDialog.Builder(
            context,
            R.style.MyDialogTheme)

        accountChoiceBuilder.setTitle("Choose phone number to transfer data from.")

        if (phonesAndNetworks.size == 1) {

            val network: String = networks[0]
            val phone: String = phones[0]

            val pAccountType = mutableListOf<String>()
            val pAccountList = mutableListOf<String>()

            if (modelList[0].phone != null && modelList[0].phone!!.trim() != "") {

                val pAccount1 =
                    if (modelList[0].network != null && modelList[0].network != "Choose Network") {
                        "${modelList[0].phone!!}: ${modelList[0].network}"
                    }
                    else {
                        modelList[0].phone!!
                    }

                pAccountType.add(pAccount1)
                pAccountList.add("1")

            }
            if (modelList.size > 1 && modelList[1].phone != null && modelList[1].phone != "") {

                val pAccount2 =
                    if (modelList[1].network != null && modelList[1].network != "Choose Network") {
                        "${modelList[1].phone!!}: ${modelList[1].network}"
                    }
                    else {
                        modelList[1].phone!!
                    }

                pAccountType.add(pAccount2)
                pAccountList.add("2")

            }

            if (pAccountType.size == 1) {

                val user: User = modelList[0]
                val pAccount: String = pAccountList[0]

                if (network != user.network) {

                    val util: Util = Util()
                    val message: String = "You can only transfer airtime within the same network."
                    util.onShowErrorMessage(message, context)

                }
                else {

                    when (network) {

                        "Airtel" -> {

                            // TODO Done
                            runAirtelDataTransfer(
                                activity,
                                user, pAccount,
                                name, phone)

                        }
                        "Etisalat(9Mobile)" -> {

                            // TODO Done
                            runEtisalatDataTransfer(
                                activity,
                                user, pAccount,
                                name, phone)

                        }
                        "Glo Mobile" -> {

                            // TODO Not Supported
                            runGloDataTransfer(
                                context,
                                activity,
                                user, pAccount,
                                name, phone)

                        }
                        "MTN Nigeria" -> {

                            // TODO Done
                            runMTNDataTransfer(
                                activity,
                                user, pAccount,
                                name, phone)

                        }
                        "Visafone" -> {

                        }
                        else -> {

                        }

                    }

                }

            }
            else {

                accountChoiceBuilder.setSingleChoiceItems(pAccountType.toTypedArray(), -1) {

                        dialogInterface, i ->

                    dialogInterface.dismiss()

                    val user: User = modelList[i]
                    val pAccount: String = pAccountList[i]

                    if (network != user.network) {

                        val util: Util = Util()
                        val message: String = "You can only transfer airtime within the same network."
                        util.onShowErrorMessage(message, context)
                    }
                    else {

                        when (network) {

                            "Airtel" -> {

                                // TODO Done
                                runAirtelDataTransfer(
                                    activity,
                                    user, pAccount,
                                    name, phone)

                            }
                            "Etisalat(9Mobile)" -> {

                                // TODO Done
                                runEtisalatDataTransfer(
                                    activity,
                                    user, pAccount,
                                    name, phone)

                            }
                            "Glo Mobile" -> {

                                // TODO Not Supported
                                runGloDataTransfer(
                                    context,
                                    activity,
                                    user, pAccount,
                                    name, phone)

                            }
                            "MTN Nigeria" -> {

                                // TODO Done
                                runMTNDataTransfer(
                                    activity,
                                    user, pAccount,
                                    name, phone)

                            }
                            "Visafone" -> {

                            }
                            else -> {

                            }

                        }

                    }

                }

                accountChoiceBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                accountChoiceBuilder.show()

            }

        }
        else {

            friendPhoneChoiceBuilder.setSingleChoiceItems(phonesAndNetworks.toTypedArray(), -1) {

                    dialogInterface, i ->

                dialogInterface.dismiss()

                val network: String = networks[i]
                val phone: String = phones[i]

                val pAccountType = mutableListOf<String>()
                val pAccountList = mutableListOf<String>()

                if (modelList[0].phone != null && modelList[0].phone!!.trim() != "") {

                    val pAccount1 =
                        if (modelList[0].network != null && modelList[0].network != "Choose Network") {
                            "${modelList[0].phone!!}: ${modelList[0].network}"
                        }
                        else {
                            modelList[0].phone!!
                        }

                    pAccountType.add(pAccount1)
                    pAccountList.add("1")

                }
                if (modelList.size > 1 && modelList[1].phone != null && modelList[1].phone != "") {

                    val pAccount2 =
                        if (modelList[1].network != null && modelList[1].network != "Choose Network") {
                            "${modelList[1].phone!!}: ${modelList[1].network}"
                        }
                        else {
                            modelList[1].phone!!
                        }

                    pAccountType.add(pAccount2)
                    pAccountList.add("2")

                }

                if (pAccountType.size == 1) {

                    var user: User = modelList[0]
                    val pAccount: String = pAccountList[0]

                    if (network != user.network) {

                        val util: Util = Util()
                        val message: String = "You can only transfer airtime within the same network."
                        util.onShowErrorMessage(message, context)

                    }
                    else {

                        when (network) {

                            "Airtel" -> {

                                // TODO Done
                                runAirtelDataTransfer(
                                    activity,
                                    user, pAccount,
                                    name, phone)

                            }
                            "Etisalat(9Mobile)" -> {

                                // TODO Done
                                runEtisalatDataTransfer(
                                    activity,
                                    user, pAccount,
                                    name, phone)

                            }
                            "Glo Mobile" -> {

                                // TODO Not Supported
                                runGloDataTransfer(
                                    context,
                                    activity,
                                    user, pAccount,
                                    name, phone)

                            }
                            "MTN Nigeria" -> {

                                // TODO Done
                                runMTNDataTransfer(
                                    activity,
                                    user, pAccount,
                                    name, phone)

                            }
                            "Visafone" -> {

                            }
                            else -> {

                            }

                        }

                    }

                }
                else {

                    accountChoiceBuilder.setSingleChoiceItems(pAccountType.toTypedArray(), -1) {

                            dialogInterface, i ->

                        dialogInterface.dismiss()

                        var user: User = modelList[i]
                        val pAccount: String = pAccountList[i]

                        if (network != user.network) {

                            val util: Util = Util()
                            val message: String = "You can only transfer airtime within the same network."
                            util.onShowErrorMessage(message, context)
                        }
                        else {

                            when (network) {

                                "Airtel" -> {

                                    // TODO Done
                                    runAirtelDataTransfer(
                                        activity,
                                        user, pAccount,
                                        name, phone)

                                }
                                "Etisalat(9Mobile)" -> {

                                    // TODO Done
                                    runEtisalatDataTransfer(
                                        activity,
                                        user, pAccount,
                                        name, phone)

                                }
                                "Glo Mobile" -> {

                                    // TODO Not Supported
                                    runGloDataTransfer(
                                        context,
                                        activity,
                                        user, pAccount,
                                        name, phone)

                                }
                                "MTN Nigeria" -> {

                                    // TODO Done
                                    runMTNDataTransfer(
                                        activity,
                                        user, pAccount,
                                        name, phone)

                                }
                                "Visafone" -> {

                                }
                                else -> {

                                }

                            }

                        }

                    }

                    accountChoiceBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                    accountChoiceBuilder.show()

                }

            }

            friendPhoneChoiceBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
            friendPhoneChoiceBuilder.show()

        }

    }

    private fun runAirtelDataTransfer(
        activity: Activity,
        user: User,
        pAccount: String,
        name: String,
        phoneNumber: String)
    {

        val whichSimCard: Int = pAccount.toInt()
        val network: String? = user.network
        val pin: String? = user.pin

        val fragment: DataTransferDialog = DataTransferDialog.newInstance(network, whichSimCard, name, phoneNumber, pin)

        try {

            fragment.show(
                (activity as AppCompatActivity).supportFragmentManager,
                "DataTransferDialog")

        }
        catch (e: NullPointerException) {

            e.printStackTrace()

        }


    }

    private fun runEtisalatDataTransfer(
        activity: Activity,
        user: User,
        pAccount: String,
        name: String,
        phoneNumber: String
    ) {

        val whichSimCard: Int = pAccount.toInt()
        val network: String? = user.network
        val pin: String? = user.pin

        val fragment: DataTransferDialog = DataTransferDialog.newInstance(network, whichSimCard, name, phoneNumber, pin)

        try {

            fragment.show(
                (activity as AppCompatActivity).supportFragmentManager,
                "DataTransferDialog")

        }
        catch (e: NullPointerException) {

            e.printStackTrace()

        }

    }

    private fun runGloDataTransfer(
        context: Context,
        activity: Activity,
        user: User,
        pAccount: String,
        name: String,
        phoneNumber: String
    ) {


//        val util: Util = Util()
//        val message: String = "You can only transfer airtime within the same network."
//        util.onShowErrorMessage(message, context)

        val  builder: AlertDialog.Builder = AlertDialog.Builder(context)

        builder
            .setTitle("Glo does not offer direct data transfer.")
            .setMessage("You are still able to share data on Glo via USSD codes but that is not available on Recharge App.")
            .setCancelable(false)
            .setPositiveButton("Ok") { dialog, id ->

                dialog.dismiss()

            }

        val  alert: AlertDialog = builder.create()

        alert.show()

//        val whichSimCard: Int = pAccount.toInt()
//        val network: String? = user.network
//        val pin: String? = user.pin
//
//        val fragment: DataTransferDialog = DataTransferDialog.newInstance(network, whichSimCard, name, phoneNumber, pin)
//
//        try {
//
//            fragment.show(
//                (activity as AppCompatActivity).supportFragmentManager,
//                "DataTransferDialog")
//
//        }
//        catch (e: NullPointerException) {
//
//            e.printStackTrace()
//
//        }

    }

    private fun runMTNDataTransfer(
        activity: Activity,
        user: User,
        pAccount: String,
        name: String,
        phoneNumber: String
    ) {

        val whichSimCard: Int = pAccount.toInt()
        val network: String? = user.network
        val pin: String? = user.pin

        val fragment: DataTransferDialog = DataTransferDialog.newInstance(network, whichSimCard, name, phoneNumber, pin)

        try {

            fragment.show(
                (activity as AppCompatActivity).supportFragmentManager,
                "DataTransferDialog")

        }
        catch (e: NullPointerException) {

            e.printStackTrace()

        }

    }

    private fun runMTNDataBuyForFriend(
        activity: Activity,
        user: User,
        pAccount: String,
        name: String,
        phoneNumber: String
    ) {

        val whichSimCard: Int = pAccount.toInt()
        val network: String? = user.network

        val fragment: DataBuyForFriendDialog = DataBuyForFriendDialog.newInstance(network, whichSimCard, name, phoneNumber)

        try {

            fragment.show(
                (activity as AppCompatActivity).supportFragmentManager,
                "DataRechargeDialog")

        }
        catch (e: NullPointerException) {

            e.printStackTrace()

        }

    }

    private fun run9MobileDataBuyForFriend(
        activity: Activity,
        user: User,
        pAccount: String,
        name: String,
        phoneNumber: String
    ) {

        val whichSimCard: Int = pAccount.toInt()
        val network: String? = user.network

        val fragment: DataBuyForFriendDialog = DataBuyForFriendDialog.newInstance(network, whichSimCard, name, phoneNumber)

        try {

            fragment.show(
                (activity as AppCompatActivity).supportFragmentManager,
                "DataRechargeDialog")

        }
        catch (e: NullPointerException) {

            e.printStackTrace()

        }

    }

    private fun runAirtelDataBuyForFriend(
        activity: Activity,
        user: User,
        pAccount: String,
        name: String,
        phoneNumber: String
    ) {

        val whichSimCard: Int = pAccount.toInt()
        val network: String? = user.network

        val fragment: DataBuyForFriendDialog = DataBuyForFriendDialog.newInstance(network, whichSimCard, name, phoneNumber)

        try {

            fragment.show(
                (activity as AppCompatActivity).supportFragmentManager,
                "DataRechargeDialog")

        }
        catch (e: NullPointerException) {

            e.printStackTrace()

        }

    }

    private fun runGloDataBuyForFriend(
        activity: Activity,
        user: User,
        pAccount: String,
        name: String,
        phoneNumber: String
    ) {

        val whichSimCard: Int = pAccount.toInt()
        val network: String? = user.network

        val fragment: DataBuyForFriendDialog = DataBuyForFriendDialog.newInstance(network, whichSimCard, name, phoneNumber)

        try {

            fragment.show(
                (activity as AppCompatActivity).supportFragmentManager,
                "DataRechargeDialog")

        }
        catch (e: NullPointerException) {

            e.printStackTrace()

        }

    }

    fun buyFriendDataMTNUSSD(

        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        code: String,
        dataFull: String,
        contactNumber: String,

        ) {

        var bundleCode: String =

            when (dataFull.toInt()) {
                30 -> {
                    "131*7*2*1*1"
                }
                100 -> {
                    "131*7*2*1*2"
                }
                750 -> {
                    "131*7*2*2*2"
                }
                1500 -> {
                    "131*7*2*3*1"
                }
                4500 -> {
                    "131*7*2*3*3"
                }
                12000 -> {
                    "131*7*2*3*5"
                }
                20000 -> {
                    "131*7*2*3*6"
                }
                100000 -> {
                    "131*7*2*4*1"
                }
                400000 -> {
                    "131*7*2*5*1"
                }
                else -> {
                    ""
                }
            }

//        bundleCode += "*1"

        Log.e("ATTENTION ATTENTION", "MTN Data Code bundleCode: $bundleCode, dataFull: $dataFull")

//        val ussdCode = "*" + bundleCode + "*" + contactNumber + Uri.encode("#")
        val ussdCode = "*" + bundleCode + Uri.encode("#")

        val intent: Intent = Intent("android.intent.action.CALL", Uri.parse("tel:$ussdCode"))

        if (pAccount == "1") {
            sendToSim(context, intent, 0)
        }
        else {
            sendToSim(context, intent, 1)
        }

//        var ussdService: USSDService
//
//        val connection = object: ServiceConnection {
//
//            override fun onServiceDisconnected(name: ComponentName?) {}
//
//            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//
//                Log.e("ATTENTION ATTENTION", "Connecting service")
//
//                val binder = service as USSDService.USSDServiceBinder
//
//                ussdService = binder.getService()
//
//            }
//
//        }
//
//        val ussdServiceIntent: Intent = Intent(context, USSDService::class.java)
//            .also {
//
//                it.putExtra("phoneNumber", contactNumber)
////                context.bindService(it, connection, Context.BIND_AUTO_CREATE)
//                context.startService(it)
//            }


        val myClipboard: ClipboardManager? = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
        val myClip: ClipData? = ClipData.newPlainText("text", contactNumber)
        if (myClip != null) {
            myClipboard?.setPrimaryClip(myClip)
        }

        intent.setPackage("com.android.server.telecom")

        val util: Util = Util()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            CoroutineScope(Dispatchers.IO).launch {

                val result = fragment.permissionsBuilder(
                    Manifest.permission.CALL_PHONE,
                ).build().sendSuspend()

                if (result.allGranted()) { // All the permissions are granted.

                    withContext(Dispatchers.Main) {

                        val dialogBuilder = AlertDialog.Builder(context)

                        dialogBuilder.setMessage("Paste number at the end after you proceed.")
                            .setCancelable(true)
                            .setPositiveButton("Proceed", DialogInterface.OnClickListener {

                                    dialog, id ->

                                dialog.cancel()

                                val msg: String = "Data transfer requested."
                                util.onShowMessageSuccess(msg, context)
                                Log.e(TAG, msg)

                                context.startActivity(intent)

                            })
                            .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                                    dialog, id -> dialog.cancel() })

                        val alert = dialogBuilder.create()
                        alert.setTitle("Phone number copied to clipboard.")
                        alert.show()

                    }

                }
                else {

                    withContext(Dispatchers.Main) {

                        val msg = "You have denied some " +
                                "permissions permanently. Functions will not work without these permission. " +
                                "Please grant the permissions for this app in your phone's settings."
                        util.onShowMessage(msg, context)
                        Log.e(TAG, msg)

                    }

                }

            }

        }
        else {

            val dialogBuilder = AlertDialog.Builder(context)

            dialogBuilder.setMessage("Paste number at the end after you proceed.")
                .setCancelable(true)
                .setPositiveButton("Proceed", DialogInterface.OnClickListener {

                        dialog, id ->

                    dialog.cancel()

                    val msg: String = "Data transfer requested."
                    util.onShowMessageSuccess(msg, context)
                    Log.e(TAG, msg)

                    context.startActivity(intent)

                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                        dialog, id -> dialog.cancel() })

            val alert = dialogBuilder.create()
            alert.setTitle("Phone number copied to clipboard.")
            alert.show()

        }

    }

    fun transferDataMTNUSSD(

        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        dataFull: String,
        contactNumber: String,

        ) {

        val requestCode  = 131

        val code: String =

            when (dataFull) {
                "50" -> {
                    "50"
                }
                "100" -> {
                    "100"
                }
                "200" -> {
                    "200"
                }
                "500" -> {
                    "500"
                }
                else -> {
                    ""
                }
            }

        Log.e("ATTENTION ATTENTION", "DataFull: $dataFull")

        val ussdCode = "*" + requestCode + "*" + contactNumber + "*" + code + Uri.encode("#")

        val intent: Intent = Intent("android.intent.action.CALL", Uri.parse("tel:$ussdCode"))

        if (pAccount == "1") {
            sendToSim(context, intent, 0)
        }
        else {
            sendToSim(context, intent, 1)
        }

        intent.setPackage("com.android.server.telecom")

        val util: Util = Util()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            CoroutineScope(Dispatchers.IO).launch {

                val result = fragment.permissionsBuilder(
                    Manifest.permission.CALL_PHONE,
                ).build().sendSuspend()

                if (result.allGranted()) {

                    withContext(Dispatchers.Main) {

                        val msg: String = "Data transfer requested."
                        util.onShowMessageSuccess(msg, context)
                        Log.e(TAG, msg)

                        context.startActivity(intent)

                    }

                }
                else {

                    withContext(Dispatchers.Main) {

                        val msg = "You have denied some " +
                                "permissions permanently. Functions will not work without these permission. " +
                                "Please grant the permissions for this app in your phone's settings."
                        util.onShowMessage(msg, context)
                        Log.e(TAG, msg)

                    }

                }

            }

        }
        else {

            val msg: String = "Data transfer requested."
            util.onShowMessageSuccess(msg, context)
            Log.e(TAG, msg)

            context.startActivity(intent)

        }

    }

    fun buyFriendData9MobileUSSD(

        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        code: String,
        dataFull: String,
        contactNumber: String,

        ) {

        var bundleCode: String = "229" +

            when (dataFull.toInt()) {

                500 -> {
                    "*2*11"
                }
                1000 -> {
                    "*2*22"
                }
                2500 -> {
                    "*2*44"
                }
                5000 -> {
                    "*2*33"
                }
                11500 -> {
                    "*2*55"
                }
                10 -> {
                    "*3*8"
                }
                40000 -> {
                    "*3*1"
                }
                150000 -> {
                    "*2*10"
                }
                500000 -> {
                    "*2*12"
                }
                30000 -> {
                    "*5*1"
                }
                60000 -> {
                    "*5*2"
                }
                100000 -> {
                    "*4*5"
                }
                120000 -> {
                    "*5*3"
                }
                else -> {
                    ""
                }

            }

//        bundleCode += "*1"

        Log.e("ATTENTION ATTENTION", "MTN Data Code bundleCode: $bundleCode, dataFull: $dataFull")

        val ussdCode = "*" + bundleCode + "*" + contactNumber + Uri.encode("#")
//        val ussdCode = "*" + bundleCode + Uri.encode("#")

        val intent: Intent = Intent("android.intent.action.CALL", Uri.parse("tel:$ussdCode"))

        if (pAccount == "1") {
            sendToSim(context, intent, 0)
        }
        else {
            sendToSim(context, intent, 1)
        }

        intent.setPackage("com.android.server.telecom")

        val util: Util = Util()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            CoroutineScope(Dispatchers.IO).launch {

                val result = fragment.permissionsBuilder(
                    Manifest.permission.CALL_PHONE,
                ).build().sendSuspend()

                if (result.allGranted()) { // All the permissions are granted.

                    withContext(Dispatchers.Main) {

                        val msg: String = "Data transfer requested."
                        util.onShowMessageSuccess(msg, context)
                        Log.e(TAG, msg)

                        context.startActivity(intent)

                    }

                }
                else {

                    withContext(Dispatchers.Main) {

                        val msg = "You have denied some " +
                                "permissions permanently. Functions will not work without these permission. " +
                                "Please grant the permissions for this app in your phone's settings."
                        util.onShowMessage(msg, context)
                        Log.e(TAG, msg)

                    }

                }

            }

        }
        else {

            val msg: String = "Data transfer requested."
            util.onShowMessageSuccess(msg, context)
            Log.e(TAG, msg)

            context.startActivity(intent)

        }

    }

    fun transferData9MobileUSSD(

        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        dataFull: String,
        pin: String?,
        contactNumber: String,

        ) {

        val requestCode  = 229

        val code: String =

            when (dataFull) {
                "50" -> {
                    "50"
                }
                "100" -> {
                    "100"
                }
                "200" -> {
                    "200"
                }
                "500" -> {
                    "500"
                }
                "1000" -> {
                    "1000"
                }
                else -> {
                    ""
                }
            }

        if (pin == null) {

            val dialogBuilder = AlertDialog.Builder(context)

            dialogBuilder.setMessage("You need to set your pin in Edit Tab in order to transfer data on 9Mobile.")
                .setCancelable(true)
                .setPositiveButton("Ok", DialogInterface.OnClickListener {

                        dialog, id ->

                    dialog.cancel()

                })

            val alert = dialogBuilder.create()
            alert.setTitle("Pin required.")
            alert.show()


        }
        else {

            Log.e("ATTENTION ATTENTION", "DataFull: $dataFull")

            val ussdCode = "*" + requestCode + "*" + pin + "*" + code+ "*"+ contactNumber + Uri.encode("#")

            val intent: Intent = Intent("android.intent.action.CALL", Uri.parse("tel:$ussdCode"))

            if (pAccount == "1") {
                sendToSim(context, intent, 0)
            }
            else {
                sendToSim(context, intent, 1)
            }

            intent.setPackage("com.android.server.telecom")

            val util: Util = Util()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                CoroutineScope(Dispatchers.IO).launch {

                    val result = fragment.permissionsBuilder(
                        Manifest.permission.CALL_PHONE,
                    ).build().sendSuspend()

                    if (result.allGranted()) {

                        withContext(Dispatchers.Main) {

                            val msg: String = "Data transfer requested."
                            util.onShowMessageSuccess(msg, context)
                            Log.e(TAG, msg)

                            context.startActivity(intent)

                        }

                    }
                    else {

                        withContext(Dispatchers.Main) {

                            val msg = "You have denied some " +
                                    "permissions permanently. Functions will not work without these permission. " +
                                    "Please grant the permissions for this app in your phone's settings."
                            util.onShowMessage(msg, context)
                            Log.e(TAG, msg)

                        }

                    }

                }

            }
            else {

                val msg: String = "Data transfer requested."
                util.onShowMessageSuccess(msg, context)
                Log.e(TAG, msg)

                context.startActivity(intent)

            }

        }

    }



    fun transferDataAirtelUSSD(

        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        dataFull: String,
        contactNumber: String,

        ) {

        val requestCode  = "141*"

        val code: String = requestCode +

            when (dataFull) {
                "10" -> {
                    "712*11"
                }
                "25" -> {
                    "712*9"
                }
                "60" -> {
                    "712*4"
                }
                else -> {
                    ""
                }
            }

        Log.e("ATTENTION ATTENTION", "DataFull: $dataFull")

        val ussdCode = "*" + code + "*" + contactNumber + Uri.encode("#")

        val intent: Intent = Intent("android.intent.action.CALL", Uri.parse("tel:$ussdCode"))

        if (pAccount == "1") {
            sendToSim(context, intent, 0)
        }
        else {
            sendToSim(context, intent, 1)
        }

        intent.setPackage("com.android.server.telecom")

        val util: Util = Util()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            CoroutineScope(Dispatchers.IO).launch {

                val result = fragment.permissionsBuilder(
                    Manifest.permission.CALL_PHONE,
                ).build().sendSuspend()

                if (result.allGranted()) {

                    withContext(Dispatchers.Main) {

                        val msg: String = "Data transfer requested."
                        util.onShowMessageSuccess(msg, context)
                        Log.e(TAG, msg)

                        context.startActivity(intent)

                    }

                }
                else {

                    withContext(Dispatchers.Main) {

                        val msg = "You have denied some " +
                                "permissions permanently. Functions will not work without these permission. " +
                                "Please grant the permissions for this app in your phone's settings."
                        util.onShowMessage(msg, context)
                        Log.e(TAG, msg)

                    }

                }

            }

        }
        else {

            val msg: String = "Data transfer requested."
            util.onShowMessageSuccess(msg, context)
            Log.e(TAG, msg)

            context.startActivity(intent)

        }

    }



    fun buyFriendDataGloUSSD(

        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        code: String,
        fullData: String,
        contactNumber: String,

        ) {

        var bundleCode: String = "127*127" +

                when (fullData.toInt()) {

                    30 -> {
                        "14"
                    }
                    70 -> {
                        "51"
                    }
                    200 -> {
                        "56"
                    }
                    1600 -> {
                        "57"
                    }
                    3200 -> {
                        "53"
                    }
                    7500 -> {
                        "55"
                    }
                    10000 -> {
                        "58"
                    }
                    12000 -> {
                        "54"
                    }
                    16000 -> {
                        "59"
                    }
                    24000 -> {
                        "2"
                    }
                    32000 -> {
                        "1"
                    }
                    46000 -> {
                        "11"
                    }
                    60000 -> {
                        "12"
                    }
                    90000 -> {
                        "13"
                    }
                    else -> {
                        ""
                    }

                }


        Log.e("ATTENTION ATTENTION", "Glo Data Code bundleCode: $bundleCode, dataFull: $fullData")

        val ussdCode = "*" + bundleCode + "*" + contactNumber + Uri.encode("#")

        val intent: Intent = Intent("android.intent.action.CALL", Uri.parse("tel:$ussdCode"))

        if (pAccount == "1") {
            sendToSim(context, intent, 0)
        }
        else {
            sendToSim(context, intent, 1)
        }

        intent.setPackage("com.android.server.telecom")

        val util: Util = Util()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            CoroutineScope(Dispatchers.IO).launch {

                val result = fragment.permissionsBuilder(
                    Manifest.permission.CALL_PHONE,
                ).build().sendSuspend()

                if (result.allGranted()) {

                    withContext(Dispatchers.Main) {

                        val msg: String = "Data transfer requested."
                        util.onShowMessageSuccess(msg, context)
                        Log.e(TAG, msg)

                        context.startActivity(intent)

                    }

                }
                else {

                    withContext(Dispatchers.Main) {

                        val msg = "You have denied some " +
                                "permissions permanently. Functions will not work without these permission. " +
                                "Please grant the permissions for this app in your phone's settings."
                        util.onShowMessage(msg, context)
                        Log.e(TAG, msg)

                    }

                }

            }

        }
        else {

            val msg: String = "Data transfer requested."
            util.onShowMessageSuccess(msg, context)
            Log.e(TAG, msg)

            context.startActivity(intent)

        }

    }

    fun buyFriendDataAirtelUSSD(

        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        validityOption: String,
        value: String,
        contactNumber: String,

        ) {

        var bundleCode: String = "141*6*2*" +

                when (value.toInt()) {

                    1 -> {
                        "1"
                    }
                    2 -> {
                        "2"
                    }
                    3 -> {
                        "3"
                    }
                    4 -> {
                        "4"
                    }
                    else -> {
                        ""
                    }

                }

        Log.e("ATTENTION ATTENTION", "Airtel Data Valid BundleCode: $validityOption, value: $value")

        val ussdCode = "*" + bundleCode + Uri.encode("#")

        val intent: Intent = Intent("android.intent.action.CALL", Uri.parse("tel:$ussdCode"))

        if (pAccount == "1") {
            sendToSim(context, intent, 0)
        }
        else {
            sendToSim(context, intent, 1)
        }

        val myClipboard: ClipboardManager? = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
        val myClip: ClipData? = ClipData.newPlainText("text", contactNumber)
        if (myClip != null) {
            myClipboard?.setPrimaryClip(myClip)
        }

        intent.setPackage("com.android.server.telecom")

        val util: Util = Util()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            CoroutineScope(Dispatchers.IO).launch {

                val result = fragment.permissionsBuilder(
                    Manifest.permission.CALL_PHONE,
                ).build().sendSuspend()

                if (result.allGranted()) { // All the permissions are granted.

                    withContext(Dispatchers.Main) {

                        val dialogBuilder = AlertDialog.Builder(context)

                        dialogBuilder.setMessage("Paste number at the end after you proceed.")
                            .setCancelable(true)
                            .setPositiveButton("Proceed", DialogInterface.OnClickListener {

                                    dialog, id ->

                                dialog.cancel()

                                val msg: String = "Data transfer requested."
                                util.onShowMessageSuccess(msg, context)
                                Log.e(TAG, msg)

                                context.startActivity(intent)

                            })
                            .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                                    dialog, id -> dialog.cancel() })

                        val alert = dialogBuilder.create()
                        alert.setTitle("Phone number copied to clipboard.")
                        alert.show()

                    }

                }
                else {

                    withContext(Dispatchers.Main) {

                        val msg = "You have denied some " +
                                "permissions permanently. Functions will not work without these permission. " +
                                "Please grant the permissions for this app in your phone's settings."
                        util.onShowMessage(msg, context)
                        Log.e(TAG, msg)

                    }

                }

            }

        }
        else {

            val dialogBuilder = AlertDialog.Builder(context)

            dialogBuilder.setMessage("Paste number at the end after you proceed.")
                .setCancelable(true)
                .setPositiveButton("Proceed", DialogInterface.OnClickListener {

                        dialog, id ->

                    dialog.cancel()

                    val msg: String = "Data transfer requested."
                    util.onShowMessageSuccess(msg, context)
                    Log.e(TAG, msg)

                    context.startActivity(intent)

                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                        dialog, id -> dialog.cancel() })

            val alert = dialogBuilder.create()
            alert.setTitle("Phone number copied to clipboard.")
            alert.show()

        }

    }

    fun giftDataGloUSSD(

        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        code: String,
        contactNumber: String,

        ) {

        val requestCode  = 131

        val ussdCode = "*" + requestCode + "*" + code + "*" + contactNumber + Uri.encode("#")

        val intent: Intent = Intent("android.intent.action.CALL", Uri.parse("tel:$ussdCode"))

        if (pAccount == "1") {
            sendToSim(context, intent, 0)
        }
        else {
            sendToSim(context, intent, 1)
        }

        intent.setPackage("com.android.server.telecom")

        val util: Util = Util()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            CoroutineScope(Dispatchers.IO).launch {

                val result = fragment.permissionsBuilder(
                    Manifest.permission.CALL_PHONE,
                ).build().sendSuspend()

                if (result.allGranted()) {

                    withContext(Dispatchers.Main) {

                        val msg: String = "Data transfer requested."
                        util.onShowMessageSuccess(msg, context)
                        Log.e(TAG, msg)

                        context.startActivity(intent)

                    }

                }
                else {

                    withContext(Dispatchers.Main) {

                        val msg = "You have denied some " +
                                "permissions permanently. Functions will not work without these permission. " +
                                "Please grant the permissions for this app in your phone's settings."
                        util.onShowMessage(msg, context)
                        Log.e(TAG, msg)

                    }

                }

            }

        }
        else {

            val msg: String = "Data transfer requested."
            util.onShowMessageSuccess(msg, context)
            Log.e(TAG, msg)

            context.startActivity(intent)

        }

    }

    fun amountAirtimeTransfer(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        modelList: MutableList<User>,
        friend: Friend,
    ) {

        val name: String = if (friend.name == null || friend.name!!.trim() == "") {
            "Unknown Name"
        }
        else {
            friend.name!!
        }

        val networks: MutableList<String> = mutableListOf<String>()
        val phones: MutableList<String> = mutableListOf<String>()
        val phonesAndNetworks: MutableList<String> = mutableListOf<String>()

        if ((friend.network1 != null && friend.network1 != "Choose Network") &&
            (friend.phone1 != null && friend.phone1!!.trim() != "")) {
            networks.add(friend.network1!!)
            phones.add(friend.phone1!!)
            phonesAndNetworks.add("${friend.phone1}: ${friend.network1}")
        }

        if ((friend.network2 != null && friend.network2 != "Choose Network") &&
            (friend.phone2 != null && friend.phone2!!.trim() != "")) {
            networks.add(friend.network2!!)
            phones.add(friend.phone2!!)
            phonesAndNetworks.add("${friend.phone2}: ${friend.network2}")
        }

        if ((friend.network3 != null && friend.network3 != "Choose Network") &&
            (friend.phone3 != null && friend.phone3!!.trim() != "")) {
            networks.add(friend.network3!!)
            phones.add(friend.phone3!!)
            phonesAndNetworks.add("${friend.phone3}: ${friend.network3}")
        }


        val pAccountType = mutableListOf<String>()
        val pAccountList = mutableListOf<String>()

        if (modelList[0].phone != null && modelList[0].phone!!.trim() != "") {

            val pAccount1 =
                if (modelList[0].network != null && modelList[0].network != "Choose Network") {
                    "${modelList[0].phone!!}: ${modelList[0].network}"
                }
                else {
                    modelList[0].phone!!
                }

            pAccountType.add(pAccount1)
            pAccountList.add("1")

        }
        if (modelList.size > 1 && modelList[1].phone != null && modelList[1].phone != "") {

            val pAccount2 =
                if (modelList[1].network != null && modelList[1].network != "Choose Network") {
                    "${modelList[1].phone!!}: ${modelList[1].network}"
                }
                else {
                    modelList[1].phone!!
                }

            pAccountType.add(pAccount2)
            pAccountList.add("2")

        }

        val accountChoiceBuilder: AlertDialog.Builder = AlertDialog.Builder(
            context,
            R.style.MyDialogTheme
        )

        accountChoiceBuilder.setTitle("Choose phone number to transfer data from.")

        val friendPhoneChoiceBuilder: AlertDialog.Builder =
            AlertDialog.Builder(context, R.style.MyDialogTheme)

        friendPhoneChoiceBuilder.setTitle("Choose your friend's phone number")

        val amountBuilder: AlertDialog.Builder = AlertDialog.Builder(
            context,
            R.style.MyDialogTheme
        )

        amountBuilder.setTitle("Amount")

        val amountViewInflated: View = LayoutInflater.from(context).inflate(R.layout.input_amount, null)

        val amountInput = amountViewInflated.findViewById<View>(R.id.input_amount) as TextInputEditText

        amountBuilder.setView(amountViewInflated)

        if (phonesAndNetworks.size == 1) {

            val network: String = networks[0]
            val phone: String = phones[0]

            if (pAccountType.size == 1) {

                var user: User = modelList[0]
                val pAccount: String = pAccountList[0]

                amountBuilder.setPositiveButton(android.R.string.ok) {

                        dialog, which ->

                    dialog.dismiss()

                    val amountInputStr: String = amountInput.text.toString()

                    airtimeTransfer(
                        context, fragment, activity, modelList, pAccount,
                        name, network, phone, amountInputStr)

                }

                amountBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                amountBuilder.show()

            }
            else {

                accountChoiceBuilder.setSingleChoiceItems(pAccountType.toTypedArray(), -1) {

                        dialogInterface, i ->

                    dialogInterface.dismiss()

                    var user: User = modelList[i]
                    val pAccount: String = pAccountList[i]

                    amountBuilder.setPositiveButton(android.R.string.ok) {

                            dialog, which ->

                        dialog.dismiss()

                        val amountInputStr: String = amountInput.text.toString()

                        airtimeTransfer(
                            context, fragment, activity, modelList, pAccount,
                            name, network, phone, amountInputStr
                        )

                    }

                    amountBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                    amountBuilder.show()

                }

                accountChoiceBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                accountChoiceBuilder.show()

            }

        }
        else {

            Log.e("ATTENTION ATTENTION", "Choose your friend's phone number")
            Log.e("ATTENTION ATTENTION", "\n\n\nFriend's Phone Numbers: ${phonesAndNetworks.toString()}")

            friendPhoneChoiceBuilder.setSingleChoiceItems(phonesAndNetworks.toTypedArray(), -1) {

                    dialogInterface, i ->

                dialogInterface.dismiss()

                val network: String = networks[i]
                val phone: String = phones[i]

                if (pAccountType.size == 1) {

                    var user: User = modelList[0]
                    val pAccount: String = pAccountList[0]

                    amountBuilder.setPositiveButton(android.R.string.ok) {

                            dialog, which ->

                        dialog.dismiss()

                        val amountInputStr: String = amountInput.text.toString()

                        airtimeTransfer(
                            context, fragment, activity, modelList, pAccount,
                            name, network, phone, amountInputStr
                        )

                    }

                    amountBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                    amountBuilder.show()

                }
                else {

                    accountChoiceBuilder.setSingleChoiceItems(pAccountType.toTypedArray(), -1) {

                            dialogInterface, i ->

                        dialogInterface.dismiss()

                        var user: User = modelList[i]
                        val pAccount: String = pAccountList[i]

                        amountBuilder.setPositiveButton(android.R.string.ok) {

                                dialog, which ->

                            dialog.dismiss()

                            val amountInputStr: String = amountInput.text.toString()

                            airtimeTransfer(
                                context, fragment, activity, modelList, pAccount,
                                name, network, phone, amountInputStr
                            )

                        }

                        amountBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                        amountBuilder.show()

                    }

                    accountChoiceBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                    accountChoiceBuilder.show()

                }

            }

            friendPhoneChoiceBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
            friendPhoneChoiceBuilder.show()

        }



    }

    // TODO Use with future update
    //2u 08012345678 500 2314 to 432
    fun smsAirtimeTransferAuto(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        code: String,
        phone: String,
        amount: String,
        pin: String
    ) {

        val util: Util = Util()

        val message = "2U $phone $amount $pin"

        val SENT = "SMS_SENT"
        val DELIVERED = "SMS_DELIVERED"

        var smsManager: SmsManager

        val sentPI: PendingIntent = PendingIntent.getBroadcast(context, 0, Intent(SENT), 0)

        val deliveredPI: PendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(DELIVERED),
            0
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            //SubscriptionManager localSubscriptionManager = SubscriptionManager.from(context);
            val localSubscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

            CoroutineScope(Dispatchers.IO).launch {

                val result = fragment.permissionsBuilder(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CALL_PHONE,
                ).build().sendSuspend()

                if (result.allGranted()) { // All the permissions are granted.

                    withContext(Dispatchers.Main) {

                        var simCardsLength = localSubscriptionManager.activeSubscriptionInfoCount

                        if (simCardsLength == 0) {

                            simCardsLength =
                                localSubscriptionManager.activeSubscriptionInfoList.size

                            Log.e(
                                "ATTENTION ATTENTION",
                                "getActiveSubscriptionInfoList().size(): " +
                                        localSubscriptionManager.activeSubscriptionInfoList.size
                            )

                        }

                        if (simCardsLength == 0) {

                            val manager =
                                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                            simCardsLength = manager.phoneCount
                            Log.e("ATTENTION ATTENTION", "simCardsLength: $simCardsLength")

                        }
                        if (simCardsLength > 1) {

                            val localList = localSubscriptionManager.activeSubscriptionInfoList
                            val simInfo1 = localList[0] as SubscriptionInfo
                            val simInfo2 = localList[1] as SubscriptionInfo
                            val sim1 = simInfo1.subscriptionId
                            val sim2 = simInfo2.subscriptionId

                            smsManager = if (pAccount == "1") {
                                SmsManager.getSmsManagerForSubscriptionId(sim1)
                            } else {
                                SmsManager.getSmsManagerForSubscriptionId(sim2)
                            }

                            smsManager.sendTextMessage(code, null, message, sentPI, deliveredPI)

                            val msg = "$amount sent to $phone. Completed! Press send to complete."

                            util.onShowMessageSuccess(msg, context)

                        } else if (simCardsLength == 1) {

                            val localList = localSubscriptionManager.activeSubscriptionInfoList
                            val simInfo1 = localList[0] as SubscriptionInfo
                            val sim1 = simInfo1.subscriptionId

                            if (pAccount == "1") {

                                smsManager = SmsManager.getSmsManagerForSubscriptionId(sim1)

                                smsManager.sendTextMessage(
                                    code,
                                    null,
                                    message,
                                    sentPI,
                                    deliveredPI
                                ) // TODO Use with future update

                                val msg =
                                    "$amount sent to $phone. Completed! Press send to complete."
                                util.onShowMessageSuccess(msg, context)

                            } else {

                                val msg = "You only have one sim card. Please update " +
                                        "your settings with appropriate info and " +
                                        "set \"phone 1\" to the correct number."

                                util.onShowErrorMessage(msg, context)

                            }

                        }

                    }

                }
                else {

                    withContext(Dispatchers.Main) {

                        val msg = "You have denied some " +
                                "permissions permanently. Functions will not work without these permission. " +
                                "Please grant the permissions for this app in your phone's settings."
                        util.onShowMessage(msg, context)
                        Log.e(TAG, msg)

                    }

                }

            }

        }
        else {

            if (pAccount == "1") {
                SimUtil.sendSMS(context, 0, code, null, message, sentPI, deliveredPI)
            }
            else {
                SimUtil.sendSMS(context, 1, code, null, message, sentPI, deliveredPI)
            }

            val msg = "$amount for Airtime sent to $phone. Completed! Press send to complete."

            util.onShowMessageSuccess(msg, context)

        }

    }

    // TODO Updated with SMS Intent: Fixed temporally for SMS Policy
    //2u 08012345678 500 2314 to 432
    fun smsAirtimeTransfer(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        code: String,
        phone: String,
        amount: String,
        pin: String
    ) {

        val util: Util = Util()

        val message = "2U $phone $amount $pin"

        val sendSmsIntent = Intent(Intent.ACTION_VIEW)

        sendSmsIntent.data = Uri.parse("sms:$code")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            //SubscriptionManager localSubscriptionManager = SubscriptionManager.from(context);

            val localSubscriptionManager =
                context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

            CoroutineScope(Dispatchers.IO).launch {

                val result = fragment.permissionsBuilder(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CALL_PHONE,
                ).build().sendSuspend()

                if (result.allGranted()) { // All the permissions are granted.

                    withContext(Dispatchers.Main) {

                        var simCardsLength = localSubscriptionManager.activeSubscriptionInfoCount

                        if (simCardsLength == 0) {

                            simCardsLength = localSubscriptionManager.activeSubscriptionInfoList.size

                            Log.e(
                                "ATTENTION ATTENTION", "getActiveSubscriptionInfoList().size(): " +
                                        localSubscriptionManager.activeSubscriptionInfoList.size
                            )

                        }
                        if (simCardsLength == 0) {

                            val manager =
                                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

                            simCardsLength = manager.phoneCount // manager.activeModemCount

                            Log.e("ATTENTION ATTENTION", "simCardsLength: $simCardsLength")

                        }
                        if (simCardsLength > 1) {

                            if (pAccount == "1")
                                sendToSim(context, sendSmsIntent, 0)
                            else
                                sendToSim(context, sendSmsIntent, 1)

                            sendSmsIntent.putExtra("sms_body", message)

                            val msg = "$amount sent to $phone. Completed!"
                            util.onShowMessageSuccess(msg, context)
                            Log.e(TAG, msg)

                            //startActivity(sendSmsIntent);
                            activity.startActivityForResult(
                                Intent.createChooser(
                                    sendSmsIntent,
                                    context.getString(R.string.choose_messenger_instructions)
                                ),
                                5
                            )

                        } else if (simCardsLength == 1) {

                            if (pAccount == "1") {

                                sendToSim(context, sendSmsIntent, 0)

                                sendSmsIntent.putExtra("sms_body", message)

                                val msg = "$amount sent to $phone. Completed!"
                                util.onShowMessageSuccess(msg, context)
                                Log.e(TAG, msg)

                                //startActivity(sendSmsIntent);
                                activity.startActivityForResult(
                                    Intent.createChooser(
                                        sendSmsIntent,
                                        context.getString(R.string.choose_messenger_instructions)
                                    ),
                                    5
                                )

                            } else {

                                val msg = "You only have one sim card. Please update " +
                                        "your settings with appropriate info and " +
                                        "set \"phone 1\" to the correct number."

                                util.onShowErrorMessage(msg, context)
                                Log.e(TAG, msg)

                            }

                        }

                    }

                }
                else {

                    withContext(Dispatchers.Main) {

                        val msg = "You have denied some " +
                                "permissions permanently. Functions will not work without these permission. " +
                                "Please grant the permissions for this app in your phone's settings."
                        util.onShowMessage(msg, context)
                        Log.e(TAG, msg)

                    }

                }

            }

        }
        else {

            if (pAccount == "1")
                sendToSim(context, sendSmsIntent, 0)
            else
                sendToSim(context, sendSmsIntent, 1)

            sendSmsIntent.putExtra("sms_body", message)
            val msg = "$amount sent to $phone. Completed!"
            util.onShowMessageSuccess(msg, context)

            //startActivity(sendSmsIntent);
            activity.startActivityForResult(
                Intent.createChooser(
                    sendSmsIntent,
                    context.getString(R.string.choose_messenger_instructions)
                ),
                5
            )

        }

    }

    private fun commonAirtimeTransfer(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        code: String,
        contactNumber: String,
        amount: String,
        pin: String
    ) {

        val ussdCode = "*" + code + "*" + pin + "*" + amount + "*" + contactNumber + Uri.encode("#")

        val intent: Intent = Intent("android.intent.action.CALL", Uri.parse("tel:$ussdCode"))

        if (pAccount == "1") {
            sendToSim(context, intent, 0)
        }
        else {
            sendToSim(context, intent, 1)
        }

        /*if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {

            TelecomManager telecomManager = (TelecomManager) context.getSystemService(TelecomManager.class);

            //intent.setPackage(telecomManager.getDefaultDialerPackage());
            intent.setPackage("com.android.server.telecom");
        }
        else {

            PackageManager packageManager = context.getPackageManager();
            List activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

            for(int j = 0 ; j < activities.size() ; j++)
            {
                if(activities.get(j).toString().toLowerCase().contains("com.android.phone"))
                {
                    intent.setPackage("com.android.phone");
                }
                else if(activities.get(j).toString().toLowerCase().contains("call"))
                {
                    String pack = (activities.get(j).toString().split("[ ]")[1].split("[/]")[0]);

                    intent.setPackage(pack);
                }
            }
        }*/

        intent.setPackage("com.android.server.telecom")

        val util: Util = Util()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            CoroutineScope(Dispatchers.IO).launch {

                val result = fragment.permissionsBuilder(
                    Manifest.permission.CALL_PHONE,
                ).build().sendSuspend()

                if (result.allGranted()) { // All the permissions are granted.

                    withContext(Dispatchers.Main) {

                        val msg: String = "Airtime transfer requested."
                        util.onShowMessageSuccess(msg, context)
                        Log.e(TAG, msg)

                        context.startActivity(intent)

                    }

                }
                else {

                    withContext(Dispatchers.Main) {

                        val msg = "You have denied some " +
                                "permissions permanently. Functions will not work without these permission. " +
                                "Please grant the permissions for this app in your phone's settings."
                        util.onShowMessage(msg, context)
                        Log.e(TAG, msg)

                    }

                }

            }

        }
        else {

            val msg: String = "Airtime transfer requested."
            util.onShowMessageSuccess(msg, context)
            Log.e(TAG, msg)

            context.startActivity(intent)

        }



    }

    private fun etisalatAirtimeTransfer(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        code: String,
        contactNumber: String,
        amount: String,
        pin: String
    ) {

        val ussdCode = "*" + code + "*" + contactNumber + "*" + amount + "*" + pin + Uri.encode("#")

        val intent: Intent = Intent("android.intent.action.CALL", Uri.parse("tel:$ussdCode"))

        if (pAccount == "1") {
            sendToSim(context, intent, 0)
        }
        else {
            sendToSim(context, intent, 1)
        }

        /*if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {

            TelecomManager telecomManager = (TelecomManager) context.getSystemService(TelecomManager.class);

            //intent.setPackage(telecomManager.getDefaultDialerPackage());
            intent.setPackage("com.android.server.telecom");
        }
        else {

            PackageManager packageManager = context.getPackageManager();
            List activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

            for(int j = 0 ; j < activities.size() ; j++)
            {
                if(activities.get(j).toString().toLowerCase().contains("com.android.phone"))
                {
                    intent.setPackage("com.android.phone");
                }
                else if(activities.get(j).toString().toLowerCase().contains("call"))
                {
                    String pack = (activities.get(j).toString().split("[ ]")[1].split("[/]")[0]);

                    intent.setPackage(pack);
                }
            }
        }*/

        intent.setPackage("com.android.server.telecom")

        val util: Util = Util()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            CoroutineScope(Dispatchers.IO).launch {

                val result = fragment.permissionsBuilder(
                    Manifest.permission.CALL_PHONE,
                ).build().sendSuspend()

                if (result.allGranted()) { // All the permissions are granted.

                    withContext(Dispatchers.Main) {

                        val msg: String = "Airtime transfer requested."
                        util.onShowMessageSuccess(msg, context)
                        Log.e(TAG, msg)

                        context.startActivity(intent)

                    }

                }
                else {

                    withContext(Dispatchers.Main) {

                        val msg = "You have denied some " +
                                "permissions permanently. Functions will not work without these permission. " +
                                "Please grant the permissions for this app in your phone's settings."
                        util.onShowMessage(msg, context)
                        Log.e(TAG, msg)

                    }

                }

            }

        }
        else {

            val msg: String = "Airtime transfer requested."
            util.onShowMessageSuccess(msg, context)
            Log.e(TAG, msg)

            context.startActivity(intent)

        }

    }

    /// TODO Might Cause Problems. Need To Test
    private fun airtimeTransfer(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        modelList: MutableList<User>,
        pAccount: String,
        name: String,
        network: String,
        phoneNumber: String,
        amount: String,
    ) {

        var pin: String = ""

        if (modelList[0].network == network && modelList.size == 1) {

            if (modelList[0].pin!= null) pin = modelList[0].pin!!

        }
        else if (modelList.size > 1 && modelList[0].network == network ) {

            if (modelList[1].pin!= null) pin = modelList[1].pin!!

        }

        if (pAccount == "1") {

            if (modelList[0].pin == null || modelList[0].pin!!.trim() == "") {

                val dialogBuilder = AlertDialog.Builder(context)

                dialogBuilder.setMessage("You need to set pin in Edit Tab in order to transfer airtime.")
                    .setCancelable(true)
                    .setPositiveButton("Ok", DialogInterface.OnClickListener {

                            dialog, id -> dialog.cancel()

                    })

                val alert = dialogBuilder.create()
                alert.setTitle("Pin required.")
                alert.show()

                return

            }
            else {

                pin = modelList[0].pin!!

            }

        }
        else if (pAccount == "2") {

            if (modelList[1].pin == null || modelList[1].pin!!.trim() == "") {

                val dialogBuilder = AlertDialog.Builder(context)

                dialogBuilder.setMessage("You need to set pin in Edit Tab in order to transfer airtime.")
                    .setCancelable(true)
                    .setPositiveButton("Ok", DialogInterface.OnClickListener {

                            dialog, id -> dialog.cancel()

                    })

                val alert = dialogBuilder.create()
                alert.setTitle("Pin required.")
                alert.show()

                return

            }
            else {

                pin = modelList[1].pin!!

            }

        }

        val pNetworks = mutableListOf<String>()

        if (modelList[0].network != null && modelList[0].network != "Choose Network") {

            pNetworks.add(modelList[0].network!!)

        }
        if (modelList.size > 1 &&modelList[1].network != null && modelList[1].network != "Choose Network") {

            pNetworks.add(modelList[0].network!!)

        }

        if (pNetworks.contains(network)) {

            when (network) {

                "Airtel" -> {

                    val code = "432"
                    smsAirtimeTransfer(
                        context,
                        fragment,
                        activity,
                        pAccount,
                        code,
                        phoneNumber,
                        amount,
                        pin
                    )

                    Toasty.success(
                        context, "Transferring airtime to $name with $network complete.",
                        Toasty.LENGTH_LONG, true
                    ).show()

                    Log.e(TAG, "Transferring airtime to $name with $network complete.")

                }
                "Etisalat(9Mobile)" -> {

                    val code = "223"
                    etisalatAirtimeTransfer(
                        context,
                        fragment,
                        activity,
                        pAccount,
                        code,
                        phoneNumber,
                        amount,
                        pin
                    )

                    Toasty.success(
                        context, "Transferring airtime to $name with $network complete.",
                        Toasty.LENGTH_LONG, true
                    ).show()

                    Log.e(TAG, "Transferring airtime to $name with $network complete.")

                }
                "Glo Mobile" -> {

                    val code = "131"
                    commonAirtimeTransfer(
                        context,
                        fragment,
                        activity,
                        pAccount,
                        code,
                        phoneNumber,
                        amount,
                        pin
                    )

                    Toasty.success(
                        context, "Transferring airtime to $name with $network complete.",
                        Toasty.LENGTH_LONG, true
                    ).show()

                    Log.e(TAG, "Transferring airtime to $name with $network complete.")

                }
                "MTN Nigeria" -> {

                    val code = "600"
                    commonAirtimeTransfer(
                        context,
                        fragment,
                        activity,
                        pAccount,
                        code,
                        phoneNumber,
                        amount,
                        pin
                    )

                    Toasty.success(
                        context, "Transferring airtime to $name with $network complete.",
                        Toasty.LENGTH_LONG, true
                    ).show()

                    Log.e(TAG, "Transferring airtime to $name with $network complete.")

                }
                "Visafone" -> {

                    val code = "447"
                    commonAirtimeTransfer(
                        context,
                        fragment,
                        activity,
                        pAccount,
                        code,
                        phoneNumber,
                        amount,
                        pin
                    )

                    Toasty.success(
                        context, "Transferring airtime to $name with $network complete.",
                        Toasty.LENGTH_LONG, true
                    ).show()

                    Log.e(TAG, "Transferring airtime to $name with $network complete.")

                }
                else -> {

                    Toasty.error(context, "Error making airtime transfer! Please check account settings in Edit Tab.",
                        Toasty.LENGTH_LONG, true).show()

                    Log.e(TAG, "$network: Error making airtime transfer! Please check account settings in Edit Tab.")

                }
            }

        }
        else {

            KToasty.error(context, "Error making airtime transfer! Your primary account networks are not your friend's network: "
                        + network + ".", Toast.LENGTH_LONG, true).show()

            Log.e(TAG, "Error making airtime transfer!" +
                        " Your primary account networks are not your friend's network: " + network + ".")

        }

    }

    fun amountBankTopupTransfer(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        modelList: MutableList<User>,
        friend: Friend,
    ) {

        val accountChoiceBuilder: AlertDialog.Builder = AlertDialog.Builder(
            context,
            R.style.MyDialogTheme
        )

        accountChoiceBuilder.setTitle("Choose the account you want to Topup Transfer from")

        val pAccountType = mutableListOf<String>()
        val pAccountList = mutableListOf<String>()
        val phonesAndBanks: MutableList<String> = mutableListOf<String>()

        if (modelList[0].phone != null && modelList[0].phone!!.trim() != "") {

            pAccountType.add(modelList[0].phone!!)
            pAccountList.add("1")

            var bankList: String = ""

            if (modelList[0].bank1 != null) {
                bankList = modelList[0].bank1!!
            }
            if (modelList[0].bank2 != null) {

                bankList = if (bankList == "") {
                    modelList[0].bank2!!
                }
                else {
                    bankList + ", " + modelList[0].bank2!!
                }

            }
            if (modelList[0].bank3 != null) {

                bankList = if (bankList == "") {
                    modelList[0].bank3!!
                }
                else {
                    bankList + ", " + modelList[0].bank3!!
                }

            }
            if (modelList[0].bank4 != null) {

                bankList = if (bankList == "") {
                    modelList[0].bank4!!
                }
                else {
                    bankList + ", " + modelList[0].bank4!!
                }

            }

            val message: String = "${modelList[0].phone!!} associated with banks: $bankList"
            phonesAndBanks.add(message)

        }

        if (modelList.size > 1 && modelList[1].phone != null && modelList[1].phone != "") {

            pAccountType.add(modelList[1].phone!!)
            pAccountList.add("2")

            var bankList: String = ""

            if (modelList[1].bank1 != null) {
                bankList = modelList[1].bank1!!
            }
            if (modelList[1].bank2 != null) {

                bankList = if (bankList == "") {
                    modelList[1].bank2!!
                }
                else {
                    bankList + ", " + modelList[1].bank2!!
                }

            }
            if (modelList[1].bank3 != null) {

                bankList = if (bankList == "") {
                    modelList[1].bank3!!
                }
                else {
                    bankList + ", " + modelList[1].bank3!!
                }

            }
            if (modelList[1].bank4 != null) {

                bankList = if (bankList == "") {
                    modelList[1].bank4!!
                }
                else {
                    bankList + ", " + modelList[1].bank4!!
                }

            }

            if (bankList != "") {

                val message: String = "${modelList[1].phone!!} associated with banks: $bankList"
                phonesAndBanks.add(message)

            }


        }

        val bankChoiceBuilder: AlertDialog.Builder = AlertDialog.Builder(
            context,
            R.style.MyDialogTheme)

        bankChoiceBuilder.setTitle("Choose your Bank")

        val pBanks = mutableListOf<String>()

        val amountBuilder: AlertDialog.Builder = AlertDialog.Builder(
            context,
            R.style.MyDialogTheme
        )

        amountBuilder.setTitle("Amount")

        val amountViewInflated: View = LayoutInflater.from(context).inflate(
            R.layout.input_amount,
            null
        )

        val amountInput = amountViewInflated.findViewById<View>(R.id.input_amount) as TextInputEditText

        amountBuilder.setView(amountViewInflated)

        val friendPhoneChoiceBuilder: AlertDialog.Builder = AlertDialog.Builder(
            context,
            R.style.MyDialogTheme)

        friendPhoneChoiceBuilder.setTitle("Choose your friend's phone number")

        if (phonesAndBanks.size == 1) {

            val user: User = modelList[0]
            val pAccount: String = pAccountList[0]

            if (user.bank1 != null && user.bank1 != "Choose Bank") {

                pBanks.add(user.bank1!!)

            }
            if (user.bank2 != null && user.bank2 != "Choose Bank") {

                pBanks.add(user.bank2!!)

            }
            if (user.bank3 != null && user.bank3 != "Choose Bank") {

                pBanks.add(user.bank3!!)

            }
            if (user.bank4 != null && user.bank4 != "Choose Bank") {

                pBanks.add(user.bank4!!)

            }

            if (pBanks.size == 1) {

                amountBuilder.setPositiveButton(android.R.string.ok) {

                        dialog, which ->

                    dialog.dismiss()

                    val amountInputStr = amountInput.text.toString()

                    val name: String = if (friend.name == null || friend.name!!.trim() == "") {
                        "Unknown Name"
                    }
                    else {
                        friend.name!!
                    }

                    val networks: MutableList<String> = mutableListOf<String>()
                    val phones: MutableList<String> = mutableListOf<String>()
                    val phonesAndNetworks: MutableList<String> = mutableListOf<String>()

                    if ((friend.network1 != null && friend.network1 != "Choose Network") &&
                        (friend.phone1 != null && friend.phone1!!.trim() != "")) {
                        networks.add(friend.network1!!)
                        phones.add(friend.phone1!!)
                        phonesAndNetworks.add("${friend.phone1}: ${friend.network1}")
                    }

                    if ((friend.network2 != null && friend.network2 != "Choose Network") &&
                        (friend.phone2 != null && friend.phone2!!.trim() != "")) {
                        networks.add(friend.network2!!)
                        phones.add(friend.phone2!!)
                        phonesAndNetworks.add("${friend.phone2}: ${friend.network2}")
                    }

                    if ((friend.network3 != null && friend.network3 != "Choose Network") &&
                        (friend.phone3 != null && friend.phone3!!.trim() != "")) {
                        networks.add(friend.network3!!)
                        phones.add(friend.phone3!!)
                        phonesAndNetworks.add("${friend.phone3}: ${friend.network3}")
                    }

                    Log.e("ATTENTION ATTENTION", "Choose your friend's phone number")
                    Log.e("ATTENTION ATTENTION", "\n\n\nFriend's Phone Numbers: ${phonesAndNetworks.toString()}")

                    friendPhoneChoiceBuilder.setSingleChoiceItems(
                        phonesAndNetworks.toTypedArray(),
                        -1)
                    {

                            dialogInterface, i ->

                        dialogInterface.dismiss()

                        val network: String = networks[i]
                        val phone: String = phones[i]

                        bankTopupTransfer(
                            context, fragment, activity, pAccount, name, amountInputStr,
                            phone, pBanks[0], network
                        )

                    }

                    friendPhoneChoiceBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                    friendPhoneChoiceBuilder.show()

                }

                amountBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                amountBuilder.show()

            }
            else {

                bankChoiceBuilder.setSingleChoiceItems(pBanks.toTypedArray(), -1) {

                        dialogInterface, i ->

                    dialogInterface.dismiss()

                    amountBuilder.setPositiveButton(android.R.string.ok) {

                            dialog, which ->

                        dialog.dismiss()

                        val amountInputStr = amountInput.text.toString()

                        val name: String = if (friend.name == null || friend.name!!.trim() == "") {
                            "Unknown Name"
                        }
                        else {
                            friend.name!!
                        }

                        val networks: MutableList<String> = mutableListOf<String>()
                        val phones: MutableList<String> = mutableListOf<String>()
                        val phonesAndNetworks: MutableList<String> = mutableListOf<String>()

                        if ((friend.network1 != null && friend.network1 != "Choose Network") &&
                            (friend.phone1 != null && friend.phone1!!.trim() != "")) {
                            networks.add(friend.network1!!)
                            phones.add(friend.phone1!!)
                            phonesAndNetworks.add("${friend.phone1}: ${friend.network1}")
                        }

                        if ((friend.network2 != null && friend.network2 != "Choose Network") &&
                            (friend.phone2 != null && friend.phone2!!.trim() != "")) {
                            networks.add(friend.network2!!)
                            phones.add(friend.phone2!!)
                            phonesAndNetworks.add("${friend.phone2}: ${friend.network2}")
                        }

                        if ((friend.network3 != null && friend.network3 != "Choose Network") &&
                            (friend.phone3 != null && friend.phone3!!.trim() != "")) {
                            networks.add(friend.network3!!)
                            phones.add(friend.phone3!!)
                            phonesAndNetworks.add("${friend.phone3}: ${friend.network3}")
                        }

                        friendPhoneChoiceBuilder.setTitle("Choose your friend's phone number")

                        Log.e("ATTENTION ATTENTION", "Choose your friend's phone number")
                        Log.e("ATTENTION ATTENTION", "\n\n\nFriend's Phone Numbers: ${phonesAndNetworks.toString()}")

                        friendPhoneChoiceBuilder.setSingleChoiceItems(
                            phonesAndNetworks.toTypedArray(),
                            -1)
                        {

                                dialogInterface, i ->

                            dialogInterface.dismiss()

                            val network: String = networks[i]
                            val phone: String = phones[i]

                            bankTopupTransfer(
                                context, fragment, activity, pAccount, name, amountInputStr,
                                phone, pBanks[i], network
                            )

                        }

                        friendPhoneChoiceBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                        friendPhoneChoiceBuilder.show()

                    }

                    amountBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                    amountBuilder.show()

                }

                bankChoiceBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                bankChoiceBuilder.show()

            }

        }
        else {

            accountChoiceBuilder.setSingleChoiceItems(phonesAndBanks.toTypedArray(), -1) {

                    dialogInterface, i ->

                dialogInterface.dismiss()

                val user: User = modelList[i]
                val pAccount: String = pAccountList[i]

                if (user.bank1 != null && user.bank1 != "Choose Bank") {

                    pBanks.add(user.bank1!!)

                }
                if (user.bank2 != null && user.bank2 != "Choose Bank") {

                    pBanks.add(user.bank2!!)

                }
                if (user.bank3 != null && user.bank3 != "Choose Bank") {

                    pBanks.add(user.bank3!!)

                }
                if (user.bank4 != null && user.bank4 != "Choose Bank") {

                    pBanks.add(user.bank4!!)

                }

                if (pBanks.size == 1) {

                    amountBuilder.setPositiveButton(android.R.string.ok) {

                            dialog, which ->

                        dialog.dismiss()

                        val amountInputStr = amountInput.text.toString()

                        val name: String = if (friend.name == null || friend.name!!.trim() == "") {
                            "Unknown Name"
                        }
                        else {
                            friend.name!!
                        }

                        val networks: MutableList<String> = mutableListOf<String>()
                        val phones: MutableList<String> = mutableListOf<String>()
                        val phonesAndNetworks: MutableList<String> = mutableListOf<String>()

                        if ((friend.network1 != null && friend.network1 != "Choose Network") &&
                            (friend.phone1 != null && friend.phone1!!.trim() != "")) {
                            networks.add(friend.network1!!)
                            phones.add(friend.phone1!!)
                            phonesAndNetworks.add("${friend.phone1}: ${friend.network1}")
                        }

                        if ((friend.network2 != null && friend.network2 != "Choose Network") &&
                            (friend.phone2 != null && friend.phone2!!.trim() != "")) {
                            networks.add(friend.network2!!)
                            phones.add(friend.phone2!!)
                            phonesAndNetworks.add("${friend.phone2}: ${friend.network2}")
                        }

                        if ((friend.network3 != null && friend.network3 != "Choose Network") &&
                            (friend.phone3 != null && friend.phone3!!.trim() != "")) {
                            networks.add(friend.network3!!)
                            phones.add(friend.phone3!!)
                            phonesAndNetworks.add("${friend.phone3}: ${friend.network3}")
                        }

                        Log.e("ATTENTION ATTENTION", "Choose your friend's phone number")
                        Log.e("ATTENTION ATTENTION", "\n\n\nFriend's Phone Numbers: ${phonesAndNetworks.toString()}")

                        friendPhoneChoiceBuilder.setSingleChoiceItems(
                            phonesAndNetworks.toTypedArray(),
                            -1)
                        {

                                dialogInterface, i ->

                            dialogInterface.dismiss()

                            val network: String = networks[i]
                            val phone: String = phones[i]

                            bankTopupTransfer(context, fragment,
                                activity, pAccount, name, amountInputStr,
                                phone, pBanks[0], network)

                        }

                        friendPhoneChoiceBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                        friendPhoneChoiceBuilder.show()

                    }

                    amountBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                    amountBuilder.show()

                }
                else {

                    bankChoiceBuilder.setSingleChoiceItems(pBanks.toTypedArray(), -1) {

                            dialogInterface, i ->

                        dialogInterface.dismiss()

                        amountBuilder.setPositiveButton(android.R.string.ok) {

                                dialog, which ->

                            dialog.dismiss()

                            val amountInputStr = amountInput.text.toString()

                            val name: String = if (friend.name == null || friend.name!!.trim() == "") {
                                "Unknown Name"
                            }
                            else {
                                friend.name!!
                            }

                            val networks: MutableList<String> = mutableListOf<String>()
                            val phones: MutableList<String> = mutableListOf<String>()
                            val phonesAndNetworks: MutableList<String> = mutableListOf<String>()

                            if ((friend.network1 != null && friend.network1 != "Choose Network") &&
                                (friend.phone1 != null && friend.phone1!!.trim() != "")) {
                                networks.add(friend.network1!!)
                                phones.add(friend.phone1!!)
                                phonesAndNetworks.add("${friend.phone1}: ${friend.network1}")
                            }

                            if ((friend.network2 != null && friend.network2 != "Choose Network") &&
                                (friend.phone2 != null && friend.phone2!!.trim() != "")) {
                                networks.add(friend.network2!!)
                                phones.add(friend.phone2!!)
                                phonesAndNetworks.add("${friend.phone2}: ${friend.network2}")
                            }

                            if ((friend.network3 != null && friend.network3 != "Choose Network") &&
                                (friend.phone3 != null && friend.phone3!!.trim() != "")) {
                                networks.add(friend.network3!!)
                                phones.add(friend.phone3!!)
                                phonesAndNetworks.add("${friend.phone3}: ${friend.network3}")
                            }

                            friendPhoneChoiceBuilder.setTitle("Choose your friend's phone number")

                            Log.e("ATTENTION ATTENTION", "Choose your friend's phone number")
                            Log.e("ATTENTION ATTENTION", "\n\n\nFriend's Phone Numbers: ${phonesAndNetworks.toString()}")

                            friendPhoneChoiceBuilder.setSingleChoiceItems(
                                phonesAndNetworks.toTypedArray(),
                                -1)
                            {

                                    dialogInterface, i ->

                                dialogInterface.dismiss()

                                val network: String = networks[i]
                                val phone: String = phones[i]

                                bankTopupTransfer(context, fragment,
                                    activity, pAccount, name, amountInputStr,
                                    phone, pBanks[i], network)

                            }

                            friendPhoneChoiceBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                            friendPhoneChoiceBuilder.show()

                        }

                        amountBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                        amountBuilder.show()

                    }

                    bankChoiceBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                    bankChoiceBuilder.show()

                }

            }

            accountChoiceBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
            accountChoiceBuilder.show()

        }

    }

    private fun commonBankTopupTransfer(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        code: String,
        name: String,
        amount: String,
        phoneNumber: String,
        bank: String
    ) {

        val ussdCode = "*" + code + "*" + amount + "*" + phoneNumber + Uri.encode("#")

        val intent: Intent = Intent("android.intent.action.CALL", Uri.parse("tel:$ussdCode"))

        if (pAccount == "1") {
            sendToSim(context, intent, 0)
        }
        else {
            sendToSim(context, intent, 1)
        }

        /*if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {

            TelecomManager telecomManager = (TelecomManager) context.getSystemService(TelecomManager.class);

            //intent.setPackage(telecomManager.getDefaultDialerPackage());
            intent.setPackage("com.android.server.telecom");
        }
        else {

            PackageManager packageManager = context.getPackageManager();
            List activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

            for(int j = 0 ; j < activities.size() ; j++)
            {
                if(activities.get(j).toString().toLowerCase().contains("com.android.phone"))
                {
                    intent.setPackage("com.android.phone");
                }
                else if(activities.get(j).toString().toLowerCase().contains("call"))
                {
                    String pack = (activities.get(j).toString().split("[ ]")[1].split("[/]")[0]);

                    intent.setPackage(pack);
                }
            }
        }*/

        intent.setPackage("com.android.server.telecom")


        val util: Util = Util()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            CoroutineScope(Dispatchers.IO).launch {

                val result = fragment.permissionsBuilder(
                    Manifest.permission.CALL_PHONE,
                ).build().sendSuspend()

                if (result.allGranted()) { // All the permissions are granted.

                    withContext(Dispatchers.Main) {

                        val msg: String = "Bank Topup of $amount to $name with $bank complete."
                        util.onShowMessageSuccess(msg, context)
                        Log.e(TAG, msg)

                        context.startActivity(intent)

                    }

                }
                else {

                    withContext(Dispatchers.Main) {

                        val msg = "You have denied some " +
                                "permissions permanently. Functions will not work without these permission. " +
                                "Please grant the permissions for this app in your phone's settings."
                        util.onShowMessage(msg, context)
                        Log.e(TAG, msg)

                    }

                }

            }
        }
        else {

            val msg: String = "Bank Topup of $amount to $name with $bank complete."
            util.onShowMessageSuccess(msg, context)
            Log.e(TAG, msg)

            context.startActivity(intent)

        }

    }

    // https://www.wazobiawap.com/2017/11/how-to-buy-airtime-from-jaiz-bank-to.html#comment-form
    // Updated January 05, 2019
    private fun jaizBankTopUpTransfer(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        code: String,
        name: String,
        amount: String,
        phoneNumber: String,
        bank: String,
        network: String
    ) {

        val util: Util = Util()

        if (network == "Glo Mobile") {

            val ussdCode = "*" + code + "*" + phoneNumber + "*" + amount + Uri.encode("#")

            val intent: Intent = Intent("android.intent.action.CALL", Uri.parse("tel:$ussdCode"))

            if (pAccount == "1") {
                sendToSim(context, intent, 0)
            } else {
                sendToSim(context, intent, 1)
            }

            /*if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {

                TelecomManager telecomManager = (TelecomManager) context.getSystemService(TelecomManager.class);

                //intent.setPackage(telecomManager.getDefaultDialerPackage());
                intent.setPackage("com.android.server.telecom");
            }
            else {

                PackageManager packageManager = context.getPackageManager();
                List activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

                for(int j = 0 ; j < activities.size() ; j++)
                {
                    if(activities.get(j).toString().toLowerCase().contains("com.android.phone"))
                    {
                        intent.setPackage("com.android.phone");
                    }
                    else if(activities.get(j).toString().toLowerCase().contains("call"))
                    {
                        String pack = (activities.get(j).toString().split("[ ]")[1].split("[/]")[0]);

                        intent.setPackage(pack);
                    }
                }
            }*/

            intent.setPackage("com.android.server.telecom")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                CoroutineScope(Dispatchers.IO).launch {

                    val result = fragment.permissionsBuilder(
                        Manifest.permission.CALL_PHONE,
                    ).build().sendSuspend()

                    if (result.allGranted()) { // All the permissions are granted.

                        withContext(Dispatchers.Main) {

                            val msg: String = "Bank TopUp of $amount to $name with $bank complete."
                            util.onShowMessageSuccess(msg, context)
                            Log.e(TAG, msg)

                            context.startActivity(intent)

                        }

                    }
                    else {

                        withContext(Dispatchers.Main) {

                            val msg = "You have denied some " +
                                    "permissions permanently. Functions will not work without these permission. " +
                                    "Please grant the permissions for this app in your phone's settings."
                            util.onShowMessageSuccess(msg, context)
                            Log.e(TAG, msg)

                        }

                    }

                }

            }
            else {

                val msg: String = "Bank TopUp of $amount to $name with $bank complete."
                util.onShowMessageSuccess(msg, context)
                Log.e(TAG, msg)

                context.startActivity(intent)

            }



        }
        else {

            val msg: String = "Jaiz Bank only allows Airtime TopUp of friends who are GLO subscribers."
            util.onShowMessage(msg, context)
            Log.e(TAG, msg)

        }

    }

    // https://www.codingdemos.com/android-alertdialog-single-choice/
    // https://android--code.blogspot.com/2015/08/android-alertdialog-multichoice.html
    private fun bankTopupTransfer(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        name: String,
        amount: String,
        phoneNumber: String,
        bank: String,
        network: String
    ) {

        when (bank) {

            "Access Bank" -> {

                val code = "901"
                commonBankTopupTransfer(
                    context,
                    fragment,
                    activity,
                    pAccount,
                    code,
                    name,
                    amount,
                    phoneNumber,
                    bank
                )

            }
            "Diamond Bank" -> {

                val code = "426"
                commonBankTopupTransfer(
                    context,
                    fragment,
                    activity,
                    pAccount,
                    code,
                    name,
                    amount,
                    phoneNumber,
                    bank
                )

            }
            "EcoBank" -> {

                val code = "326"
                commonBankTopupTransfer(
                    context,
                    fragment,
                    activity,
                    pAccount,
                    code,
                    name,
                    phoneNumber,
                    amount,
                    bank
                ) // reverse

            }
            "FCMB Bank" -> {

                val code = "329"
                commonBankTopupTransfer(
                    context,
                    fragment,
                    activity,
                    pAccount,
                    code,
                    name,
                    amount,
                    phoneNumber,
                    bank
                )

            }
            "Fidelity Bank" -> {

                val code = "770"
                commonBankTopupTransfer(
                    context,
                    fragment,
                    activity,
                    pAccount,
                    code,
                    name,
                    phoneNumber,
                    amount,
                    bank
                ) // reverse

            }
            "First Bank" -> {

                val code = "894"
                commonBankTopupTransfer(
                    context,
                    fragment,
                    activity,
                    pAccount,
                    code,
                    name,
                    amount,
                    phoneNumber,
                    bank
                )

            }
            "GTBank" -> {

                val code = "737"
                commonBankTopupTransfer(
                    context,
                    fragment,
                    activity,
                    pAccount,
                    code,
                    name,
                    amount,
                    phoneNumber,
                    bank
                )

            }
            "Heritage Bank" -> {

                val code = "322*030"
                commonBankTopupTransfer(
                    context,
                    fragment,
                    activity,
                    pAccount,
                    code,
                    name,
                    phoneNumber,
                    amount,
                    bank
                ) // reverse

            }
            "Jaiz Bank" -> {

                val code = "389*301"
                jaizBankTopUpTransfer(
                    context,
                    fragment,
                    activity,
                    pAccount,
                    code,
                    name,
                    amount,
                    phoneNumber,
                    bank,
                    network
                )

            }
            "Keystone Bank" -> {

                val code = "7111"
                commonBankTopupTransfer(
                    context,
                    fragment,
                    activity,
                    pAccount,
                    code,
                    name,
                    amount,
                    phoneNumber,
                    bank
                )

            }
            "Polaris Bank" -> {

                val code = "833"
                commonBankTopupTransfer(
                    context,
                    fragment,
                    activity,
                    pAccount,
                    code,
                    name,
                    amount,
                    phoneNumber,
                    bank
                )

            }
            "Stanbic IBTC Bank" -> {

                val code = "909"
                commonBankTopupTransfer(
                    context,
                    fragment,
                    activity,
                    pAccount,
                    code,
                    name,
                    amount,
                    phoneNumber,
                    bank
                )

            }
            "Sterling Bank" -> {

                val code = "822"
                commonBankTopupTransfer(
                    context,
                    fragment,
                    activity,
                    pAccount,
                    code,
                    name,
                    amount,
                    phoneNumber,
                    bank
                )

            }
            "UBA Bank" -> {

                val code = "919"
                commonBankTopupTransfer(
                    context,
                    fragment,
                    activity,
                    pAccount,
                    code,
                    name,
                    phoneNumber,
                    amount,
                    bank
                ) // reverse

            }
            "Union Bank" -> {

                val code = "826"
                commonBankTopupTransfer(
                    context,
                    fragment,
                    activity,
                    pAccount,
                    code,
                    name,
                    amount,
                    phoneNumber,
                    bank
                )

            }
            "Unity Bank" -> {

                val code = "7799"
                commonBankTopupTransfer(
                    context,
                    fragment,
                    activity,
                    pAccount,
                    code,
                    name,
                    phoneNumber,
                    amount,
                    bank
                ) // reverse

            }
            "Wema Bank" -> {

                val code = "945"
                commonBankTopupTransfer(
                    context,
                    fragment,
                    activity,
                    pAccount,
                    code,
                    name,
                    phoneNumber,
                    amount,
                    bank
                ) // reverse

            }
            "Zenith Bank" -> {

                val code = "966"
                commonBankTopupTransfer(
                    context,
                    fragment,
                    activity,
                    pAccount,
                    code,
                    name,
                    amount,
                    phoneNumber,
                    bank
                )

            }
            else -> {

                Toasty.error(
                    context,
                    "Error making bank transfer! Please check account settings in Edit Tab.",
                    Toasty.LENGTH_LONG,
                    true
                ).show()

                Log.e(
                    TAG,
                    "$bank: Error making bank transfer! Please check account settings in Edit Tab."
                )

            }

        }

    }

    private fun checkBalance(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        checkBalanceCode: String,
    ) {

        val ussdCode = "*" + checkBalanceCode + Uri.encode("#")

        val intent: Intent = Intent("android.intent.action.CALL", Uri.parse("tel:$ussdCode"))

        if (pAccount == "1") {
            sendToSim(context, intent, 0)
        }
        else {

            val simNumber: Int = if (pAccount == "1") {
                0
            } else {
                1
            }

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("com.android.phone.force.slot", true)
            intent.putExtra("Cdma_Supp", true)
            //Add all slots here, according to device.. (different device require different key so put all together)

            for (s in simSlotName) intent.putExtra(s, simNumber) //0 or 1 according to sim.......

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

                @SuppressLint("MissingPermission")
                val list = telecomManager.callCapablePhoneAccounts

                val localSubscriptionManager =
                    context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

                @SuppressLint("MissingPermission")
                val activeSubscriptionInfoList = localSubscriptionManager.activeSubscriptionInfoList

                for (phoneAccountHandle in list) {

                    if (phoneAccountHandle.id.contains(activeSubscriptionInfoList[simNumber].iccId)) {

                        Log.e("ATTENTION ATTENTION", "Does This Ever Run?")

                        intent.putExtra(
                            "android.telecom.extra.PHONE_ACCOUNT_HANDLE",
                            phoneAccountHandle as Parcelable
                        )

//                    return

                    }

                }

            }

        }


        /*if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {

            TelecomManager telecomManager = (TelecomManager) context.getSystemService(TelecomManager.class);

            //intent.setPackage(telecomManager.getDefaultDialerPackage());
            intent.setPackage("com.android.server.tel+ecom");
        }
        else {

            PackageManager packageManager = context.getPackageManager();
            List activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

            for(int j = 0 ; j < activities.size() ; j++)
            {
                if(activities.get(j).toString().toLowerCase().contains("com.android.phone"))
                {
                    intent.setPackage("com.android.phone");
                }
                else if(activities.get(j).toString().toLowerCase().contains("call"))
                {
                    String pack = (activities.get(j).toString().split("[ ]")[1].split("[/]")[0]);

                    intent.setPackage(pack);
                }
            }
        }*/

        intent.setPackage("com.android.server.telecom")

        val util: Util = Util()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            CoroutineScope(Dispatchers.IO).launch {

                val result = fragment.permissionsBuilder(
                    Manifest.permission.CALL_PHONE,
                ).build().sendSuspend()

                if (result.allGranted()) { // All the permissions are granted.

                    withContext(Dispatchers.Main) {

                        val msg: String = "Balance requested."
                        util.onShowMessageSuccess(msg, context)
                        Log.e(TAG, msg)

                        context.startActivity(intent)

                    }

                }
                else {

                    withContext(Dispatchers.Main) {

                        val msg = "You have denied some " +
                                "permissions permanently. Functions will not work without these permission. " +
                                "Please grant the permissions for this app in your phone's settings."
                        util.onShowMessage(msg, context)
                        Log.e(TAG, msg)

                    }

                }

            }

        }
        else {

            val msg: String = "Balance requested."
            util.onShowMessageSuccess(msg, context)
            Log.e(TAG, msg)

            context.startActivity(intent)

        }

    }

    private fun checkBalanceGloAirtime(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        checkBalanceCode: String,
    ) {

        val ussdCode = Uri.encode("#") + checkBalanceCode + Uri.encode("#")

        val intent: Intent = Intent("android.intent.action.CALL", Uri.parse("tel:$ussdCode"))

        if (pAccount == "1") {
            sendToSim(context, intent, 0)
        }
        else {

            val simNumber: Int = if (pAccount == "1") {
                0
            } else {
                1
            }

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("com.android.phone.force.slot", true)
            intent.putExtra("Cdma_Supp", true)
            //Add all slots here, according to device.. (different device require different key so put all together)

            for (s in simSlotName) intent.putExtra(s, simNumber) //0 or 1 according to sim.......

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

                @SuppressLint("MissingPermission")
                val list = telecomManager.callCapablePhoneAccounts

                val localSubscriptionManager =
                    context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

                @SuppressLint("MissingPermission")
                val activeSubscriptionInfoList = localSubscriptionManager.activeSubscriptionInfoList

                for (phoneAccountHandle in list) {

                    if (phoneAccountHandle.id.contains(activeSubscriptionInfoList[simNumber].iccId)) {

                        Log.e("ATTENTION ATTENTION", "Does This Ever Run?")

                        intent.putExtra(
                            "android.telecom.extra.PHONE_ACCOUNT_HANDLE",
                            phoneAccountHandle as Parcelable
                        )

//                    return

                    }

                }

            }

        }


        /*if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {

            TelecomManager telecomManager = (TelecomManager) context.getSystemService(TelecomManager.class);

            //intent.setPackage(telecomManager.getDefaultDialerPackage());
            intent.setPackage("com.android.server.tel+ecom");
        }
        else {

            PackageManager packageManager = context.getPackageManager();
            List activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

            for(int j = 0 ; j < activities.size() ; j++)
            {
                if(activities.get(j).toString().toLowerCase().contains("com.android.phone"))
                {
                    intent.setPackage("com.android.phone");
                }
                else if(activities.get(j).toString().toLowerCase().contains("call"))
                {
                    String pack = (activities.get(j).toString().split("[ ]")[1].split("[/]")[0]);

                    intent.setPackage(pack);
                }
            }
        }*/

        intent.setPackage("com.android.server.telecom")

        val util: Util = Util()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            CoroutineScope(Dispatchers.IO).launch {

                val result = fragment.permissionsBuilder(
                    Manifest.permission.CALL_PHONE,
                ).build().sendSuspend()

                if (result.allGranted()) { // All the permissions are granted.

                    withContext(Dispatchers.Main) {

                        val msg: String = "Balance requested."
                        util.onShowMessageSuccess(msg, context)
                        Log.e(TAG, msg)

                        context.startActivity(intent)

                    }

                }
                else {

                    withContext(Dispatchers.Main) {

                        val msg = "You have denied some " +
                                "permissions permanently. Functions will not work without these permission. " +
                                "Please grant the permissions for this app in your phone's settings."
                        util.onShowMessage(msg, context)
                        Log.e(TAG, msg)

                    }

                }

            }

        }
        else {

            val msg: String = "Balance requested."
            util.onShowMessageSuccess(msg, context)
            Log.e(TAG, msg)

            context.startActivity(intent)

        }

    }

    // https://www.ngbuzz.com/index.php?topic=1542.0
    // https://www.intogeek.net/how-to-load-recharge-cards/
    fun checkAirtimeBalance(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        user: User,
        pAccount: String,
    ) {

        when (val network: String? = user.network) {

            "Airtel" -> {

                val checkBalanceCode = "123"
                checkBalance(context, fragment, activity, pAccount, checkBalanceCode)
                Log.e(TAG, "$network: from key to rechargeBool")

            }
            "Etisalat(9Mobile)" -> {

                val checkBalanceCode = "223"
                checkBalance(context, fragment, activity, pAccount, checkBalanceCode)
                Log.e(TAG, "$network: from key to rechargeBool")

            }
            "Glo Mobile" -> {

                val checkBalanceCode = "124"
                checkBalanceGloAirtime(context, fragment, activity, pAccount, checkBalanceCode)
                Log.e(TAG, "$network: from key to rechargeBool")

            }
            "MTN Nigeria" -> {

                val checkBalanceCode = "556"
                checkBalance(context, fragment, activity, pAccount, checkBalanceCode)
                Log.e(TAG, "$network: from key to rechargeBool")

            }
            "Visafone" -> {

                val checkBalanceCode = "775"
                checkBalance(context, fragment, activity, pAccount, checkBalanceCode)
                Log.e(TAG, "$network: from key to rechargeBool")

            }
            else -> {

                KToasty.error(context, "Error checking balance!", Toast.LENGTH_LONG, true).show()
                Log.w(TAG, "$network: Error checking balance!")

            }

        }

    }

    // https://www.ngbuzz.com/index.php?topic=1542.0
    // https://www.intogeek.net/how-to-load-recharge-cards/
    fun checkDataBalance(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        user: User,
        pAccount: String,
    ) {

        val network: String? = user.network
        val phone: String? = user.phone

        when (network) {

            "Airtel" -> {

                val checkBalanceCode = "140"
                checkBalance(context, fragment, activity, pAccount, checkBalanceCode)
                Log.e(TAG, "$network: from key to rechargeBool")

            }
            "Etisalat(9Mobile)" -> {

                val checkBalanceCode = "228"
                checkBalance(context, fragment, activity, pAccount, checkBalanceCode)
                Log.e(TAG, "$network: from key to rechargeBool")

            }
            "Glo Mobile" -> {

                val checkBalanceCode = "127*0"
                checkBalance(context, fragment, activity, pAccount, checkBalanceCode)
                Log.e(TAG, "$network: from key to rechargeBool")

            }
            "MTN Nigeria" -> {

                val checkBalanceCode = "131*4"
                checkBalance(context, fragment, activity, pAccount, checkBalanceCode)
                Log.e(TAG, "$network: from key to rechargeBool")

            }
            "Visafone" -> {

                val checkBalanceCode = "444*603*$phone"
                checkBalance(context, fragment, activity, pAccount, checkBalanceCode)
                Log.e(TAG, "$network: from key to rechargeBool")

            }
            else -> {

                KToasty.error(context, "Error checking balance!", Toast.LENGTH_LONG, true).show()
                Log.w(TAG, "$network: Error checking balance!")

            }

        }

    }

    // https://tech.mntrends.com/2019/02/10/all-bank-airtime-recharge-codes-in-nigeria-how-to-load-airtime-via-ussd-code/
    private fun requestMoney(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        requestMoneyCode: String,
        phoneNumber: String?,
        amount: String,
        password: String?,
        network: String?,
    ) {

        var ussdCode = ""

        if (network != null) {

            when (network) {

                "Airtel" -> {
                    ussdCode = "*" + "322*082" + "*" + amount + Uri.encode("#")
                }
                "Etisalat(9Mobile)" -> {
                    ussdCode = "*" + "322*082" + "*" + amount + Uri.encode("#")
                }
                "Glo Mobile" -> {
                    ussdCode = "*" + "805*082" + "*" + amount + Uri.encode("#")
                }
                "MTN Nigeria" -> {
                    ussdCode = "*" + "322*082" + "*" + amount + Uri.encode("#")
                }
                else -> {
                    ussdCode = "*" + requestMoneyCode + "*" + amount + Uri.encode("#")
                    Toasty.error(context, "Error choosing network!", Toasty.LENGTH_LONG, true)
                        .show()
                    Log.w(TAG, "$network: Error choosing network!")
                }

            }

        }
        else {

            if (phoneNumber == null && password == null) {
                ussdCode = "*" + requestMoneyCode + "*" + amount + Uri.encode("#")
            }
            if (phoneNumber != null && password != null) {
                ussdCode = "*" + requestMoneyCode + "*" + phoneNumber +
                        "*" + amount + "*" + password + Uri.encode("#")
            }

        }

        val intent: Intent = Intent("android.intent.action.CALL", Uri.parse("tel:$ussdCode"))

        if (pAccount == "1") {
            sendToSim(context, intent, 0)
        }
        else {
            sendToSim(context, intent, 1)
        }

        intent.setPackage("com.android.server.telecom")

        val util: Util = Util()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            CoroutineScope(Dispatchers.IO).launch {

                val result = fragment.permissionsBuilder(
                    Manifest.permission.CALL_PHONE,
                ).build().sendSuspend()

                if (result.allGranted()) { // All the permissions are granted.

                    withContext(Dispatchers.Main) {

                        val msg: String = "$amount requested."
                        util.onShowMessageSuccess(msg, context)
                        Log.e(TAG, msg)

                        context.startActivity(intent)

                    }

                }
                else {

                    withContext(Dispatchers.Main) {

                        val msg: String = "You have denied some " +
                                "permissions permanently. Functions will not work without these permission. " +
                                "Please grant the permissions for this app in your phone's settings."
                        util.onShowMessage(msg, context)
                        Log.e(TAG, msg)

                    }

                }

            }

        }
        else {

            val msg: String = "$amount requested."
            util.onShowMessageSuccess(msg, context)
            Log.e(TAG, msg)

            context.startActivity(intent)

        }

    }

    // https://tech.mntrends.com/2019/02/10/all-bank-airtime-recharge-codes-in-nigeria-how-to-load-airtime-via-ussd-code/
    private fun requestMoneyZenith(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        pAccount: String,
        amount: String,
        network: String?,
    ) {

        var ussdCode = ""

        if (network != null) {

            when (network) {

                "Airtel" -> {
                    ussdCode = "*" + "966" + "*" + amount + Uri.encode("#")
                }
                "Etisalat(9Mobile)" -> {
                    ussdCode = "*" + "966" + "*" + amount + Uri.encode("#")
                }
                "Glo Mobile" -> {
                    ussdCode = "*" + "966" + "*" + amount + Uri.encode("#")
                }
                "MTN Nigeria" -> {
                    ussdCode = "*" + "302" + "*" + amount + Uri.encode("#")
                }
                else -> {
                    KToasty.error(context, "Error choosing network!", Toasty.LENGTH_LONG, true)
                        .show()
                    println("$network: Error choosing network!")
                }

            }

        }

        val intent: Intent = Intent("android.intent.action.CALL", Uri.parse("tel:$ussdCode"))

        if (pAccount == "1") {
            sendToSim(context, intent, 0)
        }
        else {
            sendToSim(context, intent, 1)
        }

        /*if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {

            TelecomManager telecomManager = (TelecomManager) context.getSystemService(TelecomManager.class);

            //intent.setPackage(telecomManager.getDefaultDialerPackage());
            intent.setPackage("com.android.server.telecom");
        }
        else {

            PackageManager packageManager = context.getPackageManager();
            List activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

            for(int j = 0 ; j < activities.size() ; j++)
            {
                if(activities.get(j).toString().toLowerCase().contains("com.android.phone"))
                {
                    intent.setPackage("com.android.phone");
                }
                else if(activities.get(j).toString().toLowerCase().contains("call"))
                {
                    String pack = (activities.get(j).toString().split("[ ]")[1].split("[/]")[0]);

                    intent.setPackage(pack);
                }
            }
        }*/

        intent.setPackage("com.android.server.telecom")

        val util: Util = Util()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            CoroutineScope(Dispatchers.IO).launch {

                val result = fragment.permissionsBuilder(
                    Manifest.permission.CALL_PHONE,
                ).build().sendSuspend()

                if (result.allGranted()) { // All the permissions are granted.

                    withContext(Dispatchers.Main) {

                        val msg: String = "$amount requested."
                        util.onShowMessageSuccess(msg, context)
                        Log.e(TAG, msg)

                        context.startActivity(intent)

                    }

                }
                else {

                    withContext(Dispatchers.Main) {

                        val msg = "You have denied some " +
                                "permissions permanently. Functions will not work without these permission. " +
                                "Please grant the permissions for this app in your phone's settings."
                        util.onShowMessage(msg, context)
                        Log.e(TAG, msg)

                    }

                }

            }

        }
        else {

            val msg: String = "$amount requested."
            util.onShowMessageSuccess(msg, context)
            Log.e(TAG, msg)

            context.startActivity(intent)

        }


    }

    // https://tech.mntrends.com/2019/02/10/all-bank-airtime-recharge-codes-in-nigeria-how-to-load-airtime-via-ussd-code/
    fun bankChoiceRecharge(
        context: Context,
        fragment: Fragment,
        activity: Activity,
        user: User,
        pAccount: String,
    ) {

        val phone: String? = user.phone
        val password: String? = user.pin
        val network: String? = user.network

        val bankChoiceBuilder: AlertDialog.Builder = AlertDialog.Builder(
            context,
            R.style.MyDialogTheme
        )
        bankChoiceBuilder.setTitle("Choose your Bank")

        val pBanks = mutableListOf<String>()

        if (user.bank1 != null && user.bank1 != "Choose Bank") {

            pBanks.add(user.bank1!!)

        }
        if (user.bank2 != null && user.bank2 != "Choose Bank") {

            pBanks.add(user.bank2!!)

        }
        if (user.bank3 != null && user.bank3 != "Choose Bank") {

            pBanks.add(user.bank3!!)

        }
        if (user.bank4 != null && user.bank4 != "Choose Bank") {

            pBanks.add(user.bank4!!)

        }


        bankChoiceBuilder.setSingleChoiceItems(pBanks.toTypedArray(), -1) {

                dialogInterface, i ->

            dialogInterface.dismiss()

            val amountBuilder: AlertDialog.Builder = AlertDialog.Builder(
                context,
                R.style.MyDialogTheme
            )
            amountBuilder.setTitle("Amount")

            val amountViewInflated: View = LayoutInflater
                .from(context)
                .inflate(R.layout.input_amount, null)

            val amountInput = amountViewInflated.findViewById<View>(R.id.input_amount) as TextInputEditText
            amountBuilder.setView(amountViewInflated)
            amountBuilder.setPositiveButton(android.R.string.ok) {

                    dialog, which ->

                dialog.dismiss()

                val amount: String = amountInput.text.toString()

                when (pBanks[i]) {

                    "Access Bank" -> {

                        val requestMoneyCode = "901"
                        requestMoney(
                            context, fragment, activity, pAccount, requestMoneyCode,
                            null, amount, null, null
                        )
                        Log.e(TAG, "Recharge from " + pBanks[i] + " was successful")

                    }
                    "Diamond Bank" -> {

                        val requestMoneyCode = "710*555"
                        requestMoney(
                            context, fragment, activity, pAccount, requestMoneyCode,
                            phone, amount, password, null
                        )
                        Log.e(TAG, "Recharge from " + pBanks[i] + " was successful")

                    }
                    "EcoBank" -> {

                        val requestMoneyCode = "326"
                        requestMoney(
                            context, fragment, activity, pAccount, requestMoneyCode,
                            null, amount, null, null
                        )
                        Log.e(TAG, "Recharge from " + pBanks[i] + " was successful")

                    }
                    "FCMB Bank" -> {

                        val requestMoneyCode = "389*214"
                        requestMoney(
                            context, fragment, activity, pAccount, requestMoneyCode,
                            null, amount, null, null
                        )
                        Log.e(TAG, "Recharge from " + pBanks[i] + " was successful")

                    }
                    "Fidelity Bank" -> {

                        val requestMoneyCode = "770"
                        requestMoney(
                            context, fragment, activity, pAccount, requestMoneyCode,
                            null, amount, null, null
                        )
                        Log.e(TAG, "Recharge from " + pBanks[i] + " was successful")

                    }
                    "First Bank" -> {

                        val requestMoneyCode = "894"
                        requestMoney(
                            context, fragment, activity, pAccount, requestMoneyCode,
                            null, amount, null, null
                        )
                        Log.e(TAG, "Recharge from " + pBanks[i] + " was successful")

                    }
                    "GTBank" -> {

                        val requestMoneyCode = "737"
                        requestMoney(
                            context, fragment, activity, pAccount, requestMoneyCode,
                            null, amount, null, null
                        )
                        Log.e(TAG, "Recharge from " + pBanks[i] + " was successful")

                    }
                    "Heritage Bank" -> {

                        val requestMoneyCode = "322*030"
                        requestMoney(
                            context, fragment, activity, pAccount, requestMoneyCode,
                            null, amount, null, null
                        )
                        Log.e(TAG, "Recharge from " + pBanks[i] + " was successful")

                    }
                    "Jaiz Bank" -> {

                        val requestMoneyCode = "389*301"
                        requestMoney(
                            context, fragment, activity, pAccount, requestMoneyCode,
                            null, amount, null, null
                        )
                        Log.e(TAG, "Recharge from " + pBanks[i] + " was successful")

                    }
                    "Keystone Bank" -> {

                        val requestMoneyCode = "124"
                        requestMoney(
                            context, fragment, activity, pAccount, requestMoneyCode,
                            null, amount, null, network
                        )
                        Log.e(TAG, "Recharge from " + pBanks[i] + " was successful")

                    }
                    "Polaris Bank" -> {

                        val requestMoneyCode = "389*076*1"
                        requestMoney(
                            context, fragment, activity, pAccount, requestMoneyCode,
                            null, amount, null, null
                        )
                        Log.e(TAG, "Recharge from " + pBanks[i] + " was successful")

                    }
                    "Stanbic IBTC Bank" -> {

                        val requestMoneyCode = "909"
                        requestMoney(
                            context, fragment, activity, pAccount, requestMoneyCode,
                            null, amount, null, null
                        )
                        Log.e(TAG, "Recharge from " + pBanks[i] + " was successful")

                    }
                    "Sterling Bank" -> {

                        val requestMoneyCode = "822"
                        requestMoney(
                            context, fragment, activity, pAccount, requestMoneyCode,
                            null, amount, null, null
                        )
                        Log.e(TAG, "Recharge from " + pBanks[i] + " was successful")

                    }
                    "UBA Bank" -> {

                        val requestMoneyCode = "389*033*1"
                        requestMoney(
                            context, fragment, activity, pAccount, requestMoneyCode,
                            null, amount, null, null
                        )
                        Log.e(TAG, "Recharge from " + pBanks[i] + " was successful")

                    }
                    "Union Bank" -> {

                        val requestMoneyCode = "389*032"
                        requestMoney(
                            context, fragment, activity, pAccount, requestMoneyCode,
                            null, amount, null, null
                        )
                        Log.e(TAG, "Recharge from " + pBanks[i] + " was successful")

                    }
                    "Unity Bank" -> {

                        val requestMoneyCode = "322*215"
                        requestMoney(
                            context, fragment, activity, pAccount, requestMoneyCode,
                            null, amount, null, null
                        )
                        Log.e(TAG, "Recharge from " + pBanks[i] + " was successful")

                    }
                    "Wema Bank" -> {

                        val requestMoneyCode = "322*035"
                        requestMoney(
                            context, fragment, activity, pAccount, requestMoneyCode,
                            null, amount, null, null
                        )
                        Log.e(TAG, "Recharge from " + pBanks[i] + " was successful")

                    }
                    "Zenith Bank" -> {

                        requestMoneyZenith(context, fragment, activity, pAccount, amount, network)
                        Log.e(TAG, "Recharge from " + pBanks[i] + " was successful")

                    }
                    else -> {
                        KToasty.error(
                            context, "Error recharging credit from " + pBanks[i],
                            Toast.LENGTH_LONG, true
                        ).show()
                        Log.w(TAG, "Error recharging credit from " + pBanks[i])
                    }

                }

            }

            amountBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
            amountBuilder.show()

        }

        bankChoiceBuilder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }

        bankChoiceBuilder.show()

    }

    fun codeRecharge(
        context: Context,
        activity: Activity,
        user: User,
        pAccount: String,
    ) {

        val reviewActivityIntent: Intent = Intent(context, CodeInputActivity::class.java)

        reviewActivityIntent.putExtra("pAccount", pAccount)
        reviewActivityIntent.putExtra("phone", user.phone)
        reviewActivityIntent.putExtra("network", user.network)

        activity.startActivity(reviewActivityIntent)


    }


    // https://stackoverflow.com/questions/51737667/since-the-android-getfragmentmanager-api-is-deprecated-is-there-any-alternati
    fun dataRechargeDialog(
        activity: Activity,
        user: User,
        pAccount: String,
    ) {

        val whichSimCard: Int = pAccount.toInt()
        val network: String? = user.network

        //val activity: AppCompatActivity = view.context as AppCompatActivity

        val fragment: DataRechargeDialog = DataRechargeDialog.newInstance(network, whichSimCard)

        /*getFragmentManager()
                .beginTransaction()
                .replace(R.id.root_linear_layout_V, fragment)
                .addToBackStack("DataRechargeDialog")
                .commit();*/

        try {
            fragment.show(
                (activity as AppCompatActivity).supportFragmentManager,
                "DataRechargeDialog"
            )
        }
        catch (e: NullPointerException) {
            e.printStackTrace()
        }

    }

}