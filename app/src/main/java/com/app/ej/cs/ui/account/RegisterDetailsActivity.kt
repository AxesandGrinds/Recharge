package com.app.ej.cs.ui.account


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.database.Cursor
import android.location.*
import android.location.LocationListener
import android.net.Uri
import android.os.*
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.transition.Fade
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.ej.cs.R
import com.app.ej.cs.model.FriendListModel
import com.app.ej.cs.model.UserListModel
import com.app.ej.cs.presenter.AddFriendDoneListener
import com.app.ej.cs.presenter.PickContactListener
import com.app.ej.cs.repository.entity.Friend
import com.app.ej.cs.repository.entity.LocationUser
import com.app.ej.cs.repository.entity.User
import com.app.ej.cs.repository.entity.UserAndFriendInfo
import com.app.ej.cs.service.AppLocationService
import com.app.ej.cs.ui.MainActivity
import com.app.ej.cs.ui.edit.EditFriendViewAdapter
import com.app.ej.cs.ui.edit.EditUserMainViewAdapter
import com.app.ej.cs.ui.edit.EditUserSecondViewAdapter
import com.app.ej.cs.utils.*
import com.droidman.ktoasty.KToasty
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.coroutines.sendSuspend
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import es.dmoral.toasty.Toasty
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.michaelrocks.libphonenumber.android.Phonenumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.fixedRateTimer


class RegisterDetailsActivity() : AppCompatActivity(),
    LocationListener, PickContactListener {

    private lateinit var addFriendDoneListener: AddFriendDoneListener

    private val TAG: String = RegisterDetailsActivity::class.java.simpleName
    private lateinit var  context: Context

    var registrationComplete: Boolean = false
    var email: String? = null
    var network1: String? = null
    var address: String? = null
    var city: String? = null
    var statee: String? = null
    var country: String? = null
    var postalCode: String? = null
    var knownName: String? = null
    var finalAddress: String? = null
    private var longitude: Double = 0.0
    private var latitude: Double = 0.0
    private var network1Spinner: Spinner? = null
    private var phone1Et: TextInputEditText? = null
    private var phone1Layout: TextInputLayout? = null
    private var pin1Et: TextInputEditText? = null
    private var pin2Et: TextInputEditText? = null

    override fun pickContact(index: Int) {

        val pickContactIntent: Intent = Intent(
                Intent.ACTION_PICK,
                Uri.parse("content://contacts")
        )

        // Show user only contacts w/ phone numbers
        pickContactIntent.setDataAndType(
                ContactsContract.Contacts.CONTENT_URI,
                ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        )

        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST + index)

    }

    override fun deleteContact(index: Int) {

        val dialogBuilder = AlertDialog.Builder(this)

        // set message of alert dialog
        dialogBuilder.setMessage("Do you want to delete friend ${index + 1}?")
                // if the dialog is cancelable
                .setCancelable(true)
                // positive button text and action
                .setPositiveButton("Proceed", DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()
                    runDeleteContact(index)
                })
                // negative button text and action
                .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()
                })

        // create dialog box
        val alert = dialogBuilder.create()
        // set title for alert dialog box
        alert.setTitle("Are You Sure?")
        // show alert dialog
        alert.show()

    }

    private fun runDeleteContact(index: Int) {

        friendsListModel.friendList?.removeAt(index)

        val size = friendsListModel.friendList?.size!! - 1

        for (i in 0..size) {

            friendsListModel.friendList!![i].index = i
            friendsListModel.friendList!![i].description = "Friend ${i+1}"

        }

        runDeleteContactLocal(index)
//        editFragmentFriendViewAdapter.notifyDataSetChanged()

    }

    private fun runDeleteContactLocal(index: Int) {

        val sharedPref = getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

        allInfoJsonSaved   = sharedPref.getString("allInfoSaved", "defaultAll")!!
        allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!

        Log.e("ATTENTION ATTENTION", "Before Deletion Current Json")
        Log.e("ATTENTION ATTENTION", "allInfoJsonSaved: $allInfoJsonSaved")
        Log.e("ATTENTION ATTENTION", "allInfoJsonUnsaved: $allInfoJsonUnsaved")

//        Log.e("ATTENTION ATTENTION", "sharedPreferences: ${sharedPref.all}")
//        userAndFriendInfoSaved = gson.fromJson(allInfoJsonSaved, UserAndFriendInfo::class.java)

        if (allInfoJsonUnsaved != "defaultAll") {

            userAndFriendInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

            userAndFriendInfoUnsaved.friendList?.removeAt(index)

            val size = userAndFriendInfoUnsaved.friendList?.size!! - 1

            for (i in 0..size) {

                userAndFriendInfoUnsaved.friendList!![i].index = i
                userAndFriendInfoUnsaved.friendList!![i].description = "Friend ${i+1}"

            }

            allInfoJsonUnsaved = Gson().toJson(userAndFriendInfoUnsaved)

            val editor = sharedPref!!.edit()

            editor.putString("allInfoUnsaved", allInfoJsonUnsaved)

            editor.apply()

            val message: String = "You have successfully deleted an entry for a friend."

            util.onShowMessageSuccess(message, context)


            Log.e("ATTENTION ATTENTION", "After Deletion Current Json")
            Log.e("ATTENTION ATTENTION", "allInfoJsonSaved: $allInfoJsonSaved")
            Log.e("ATTENTION ATTENTION", "allInfoJsonUnsaved: $allInfoJsonUnsaved")

        }

        editFragmentFriendViewAdapter.notifyDataSetChanged()

    }

    private lateinit var pinBuilder: AlertDialog.Builder
    private lateinit var newPinYesBuilder: AlertDialog.Builder
    private lateinit var rememberBuilder: AlertDialog.Builder

    private lateinit var pinViewInflated: View
    private lateinit var newPinInput: TextInputEditText
    private lateinit var newPinStr: String

    private var db    = Firebase.firestore
    private var auth = Firebase.auth

    private var firebaseUser: FirebaseUser? = null
    private var userId: String? = null

    private lateinit var mProgressBar: ProgressBar
    private lateinit var mProgressBarLocation: ProgressBar

    init {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            CoroutineScope(Dispatchers.IO).launch {

                val result = permissionsBuilder(
//                    Manifest.permission.CAMERA,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ).build().sendSuspend()

                context = this@RegisterDetailsActivity

                if (!result.allGranted()) { // All the permissions are not granted.

                    withContext(Dispatchers.Main) {
                        //KToasty.info(app, "Using local data", Toast.LENGTH_LONG).show()
                        val message: String = "You have denied some permissions permanently, " +
                                "if the app force close try granting permission from settings."
                        KToasty.info(context, message, Toast.LENGTH_LONG, true).show()

                    }

                }

            }

        }

    }

    override  fun attachBaseContext(newBase: Context){
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }



    private fun generalUpdatePinInDatabase(pAccount: String){


        val  userDocumentReference = //: DocumentReference =

            db
                .collection("users")
                .document(auth.currentUser!!.uid)

        if (pAccount == "1") {

            val map: MutableMap<String, Any> = HashMap<String, Any>()
            map["pin1"] = pin1Et!!.text.toString()

            userDocumentReference
                .update(map)
                .addOnSuccessListener(OnSuccessListener<Void?> {

                    val pref: SharedPreferences = context
                            .getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
                    val editor: SharedPreferences.Editor = pref.edit()
                    editor.putString("pin1", pin1Et!!.text.toString())
                    editor.apply()
                    Log.i("Update", "success")

                })
                .addOnFailureListener(OnFailureListener { e ->
                    Log.w("Update", "failed: " + e.message)
                    KToasty.info(
                            context,
                            "Update failed: " + e.message, Toast.LENGTH_LONG, true
                    ).show()
                })


        }
        else {

            val map: MutableMap<String, Any> = HashMap<String, Any>()
            map["pin2"] = pin2Et!!.text.toString()

            userDocumentReference.update(map)
                .addOnSuccessListener(OnSuccessListener<Void?> {

                    val pref: SharedPreferences = context
                            .getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
                    val editor: SharedPreferences.Editor = pref.edit()
                    editor.putString("pin2", pin2Et!!.text.toString())
                    editor.apply()
                    Log.i("Update", "success")

                })
                .addOnFailureListener(OnFailureListener { e ->
                    KToasty.info(context, "Update failed: " + e.message, Toasty.LENGTH_LONG, true)
                            .show()
                    Log.w("Update", "failed: " + e.message)
                })
        }
    }

    private   fun showPinUpdateMessage(newPin: String) {
        val msg: String = "Your pin $newPin has been set. Press send to complete."
        val pinUpdateMessageToast: Toast = KToasty.success(context, msg, Toast.LENGTH_LONG, true)
        val pinUpdateMessageSnackbar: Snackbar = Snackbar.make(
                (rda_coordinatorLayout),
                "",
                Snackbar.LENGTH_LONG
        )
        pinUpdateMessageToast.show()
        pinUpdateMessageSnackbar.show()
    }

    private   fun showErrorMessage(message: String) {
        val errorMessageToast: Toast = KToasty.error(context, message, Toast.LENGTH_LONG, true)
        val errorMessageSnackbar: Snackbar = Snackbar.make(
                (rda_coordinatorLayout),
                "",
                Snackbar.LENGTH_LONG
        )
        errorMessageToast.show()
        errorMessageSnackbar.show()
    }

    // TODO Use with future update
    @SuppressLint("MissingPermission")
    private fun smsNewPinAuto(pAccount: String, network: String, newPin: String) {

        val code: String
        val message: String

        when(network) {

            "Airtel" -> {
                code = "432"
                message = "PIN 1234 $newPin"
            }
            "MTN Nigeria" -> {
                code = "777"
                message = "0000 $newPin $newPin"
            }
            else -> {
                code = ""
                message = ""
                KToasty.error(context, "Error setting your new pin!", Toast.LENGTH_LONG, true).show()
                println("$network: Error setting your new pin!")
            }
        }

        val SENT: String = "SMS_SENT"
        val DELIVERED: String = "SMS_DELIVERED"
        var smsManager: SmsManager
        val sentPI: PendingIntent = PendingIntent
            .getBroadcast(context, 0, Intent(SENT), 0)
        val deliveredPI: PendingIntent = PendingIntent
            .getBroadcast(context, 0, Intent(DELIVERED), 0)

        if (newPin.length == 4) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {

                val localSubscriptionManager = getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    CoroutineScope(Dispatchers.IO).launch {

                    val result = permissionsBuilder(
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.SEND_SMS,
                    ).build().sendSuspend()

                    if (result.allGranted()) { // All the permissions are granted.

                        if (localSubscriptionManager.activeSubscriptionInfoCount > 1) {
                            val localList: List<SubscriptionInfo> = localSubscriptionManager.activeSubscriptionInfoList
                            val simInfo1: SubscriptionInfo = localList[0] as SubscriptionInfo
                            val simInfo2: SubscriptionInfo = localList[1] as SubscriptionInfo
                            val sim1: Int = simInfo1.subscriptionId
                            val sim2: Int = simInfo2.subscriptionId

                            if (pAccount == "1") {
                                smsManager = SmsManager.getSmsManagerForSubscriptionId(sim1) as SmsManager
                                smsManager.sendTextMessage(code, null, message, sentPI, deliveredPI)
                                generalUpdatePinInDatabase("1")
                                pin1Et!!.setText(newPin)
                            }
                            else {
                                smsManager = SmsManager.getSmsManagerForSubscriptionId(sim2) as SmsManager
                                smsManager.sendTextMessage(code, null, message, sentPI, deliveredPI)
                                generalUpdatePinInDatabase("2")
                                pin2Et!!.setText(newPin)
                            }
                            showPinUpdateMessage(newPin)

                        }
                        else if (localSubscriptionManager.activeSubscriptionInfoCount == 1) {

                            val localList: List<SubscriptionInfo> = localSubscriptionManager.activeSubscriptionInfoList
                            val simInfo1: SubscriptionInfo = localList[0] as SubscriptionInfo
                            val sim1: Int = simInfo1.subscriptionId

                            if (pAccount == "1"){
//                                smsManager = SmsManager.getSmsManagerForSubscriptionId(sim1)
                                smsManager = SmsManager.getSmsManagerForSubscriptionId(sim1)
                                smsManager.sendTextMessage(code, null, message, sentPI, deliveredPI)
                                generalUpdatePinInDatabase("1")
                                pin1Et!!.setText(newPin)
                                showPinUpdateMessage(newPin)
                            } else {
                                val msg: String = "You only have one sim card. Please update your settings with " +
                                        "appropriate info and set \"phone 1\" to the correct number."
                                showErrorMessage(msg)
                            }
                        }

                    }
                    else {

                        withContext(Dispatchers.Main) {
                            //KToasty.info(app, "Using local data", Toast.LENGTH_LONG).show()
                            val message2: String = "You have denied some permissions permanently, " +
                                    "if the app force close try granting permission from settings."
                            KToasty.info(context, message2, Toast.LENGTH_LONG, true).show()

                        }

                    }

                }

                }
                else {

                    if (localSubscriptionManager.activeSubscriptionInfoCount > 1) {
                        val localList: List<SubscriptionInfo> = localSubscriptionManager.activeSubscriptionInfoList
                        val simInfo1: SubscriptionInfo = localList[0] as SubscriptionInfo
                        val simInfo2: SubscriptionInfo = localList[1] as SubscriptionInfo
                        val sim1: Int = simInfo1.subscriptionId
                        val sim2: Int = simInfo2.subscriptionId

                        if (pAccount == "1") {
                            smsManager = SmsManager.getSmsManagerForSubscriptionId(sim1) as SmsManager
                            smsManager.sendTextMessage(code, null, message, sentPI, deliveredPI)
                            generalUpdatePinInDatabase("1")
                            pin1Et!!.setText(newPin)
                        }
                        else {
                            smsManager = SmsManager.getSmsManagerForSubscriptionId(sim2) as SmsManager
                            smsManager.sendTextMessage(code, null, message, sentPI, deliveredPI)
                            generalUpdatePinInDatabase("2")
                            pin2Et!!.setText(newPin)
                        }
                        showPinUpdateMessage(newPin)

                    }
                    else if (localSubscriptionManager.activeSubscriptionInfoCount == 1) {

                        val localList: List<SubscriptionInfo> = localSubscriptionManager.activeSubscriptionInfoList
                        val simInfo1: SubscriptionInfo = localList[0] as SubscriptionInfo
                        val sim1: Int = simInfo1.subscriptionId

                        if (pAccount == "1"){
//                                smsManager = SmsManager.getSmsManagerForSubscriptionId(sim1)
                            smsManager = SmsManager.getSmsManagerForSubscriptionId(sim1)
                            smsManager.sendTextMessage(code, null, message, sentPI, deliveredPI)
                            generalUpdatePinInDatabase("1")
                            pin1Et!!.setText(newPin)
                            showPinUpdateMessage(newPin)
                        } else {
                            val msg: String = "You only have one sim card. Please update your settings with " +
                                    "appropriate info and set \"phone 1\" to the correct number."
                            showErrorMessage(msg)
                        }
                    }

                }

            }
            else {

                if ((pAccount == "1")){
                    SimUtil.sendSMS(context, 0, code, null, message, sentPI, deliveredPI)
                    generalUpdatePinInDatabase("1")
                    pin1Et!!.setText(newPin)
                }
                else {
                    SimUtil.sendSMS(context, 1, code, null, message, sentPI, deliveredPI)
                    generalUpdatePinInDatabase("2")
                    pin2Et!!.setText(newPin)
                }

                showPinUpdateMessage(newPin)

            }

        }
        else {
            val msg: String = "Your pin must be a 4 digit number. Please fix pin."
            showErrorMessage(msg)
        }


    }

    // TODO Updated with SMS Intent: Fixed temporally for SMS Policy
    @SuppressLint("MissingPermission")
   /* private   fun smsNewPin(pAccount: String, network: String, newPin: String) {

        val code: String
        val message: String

        when (network) {

            "Airtel" -> {
                code = "432"
                message = "PIN 1234 $newPin"
            }
            "MTN Nigeria" -> {
                code = "777"
                message = "0000 $newPin $newPin"
            }
            else -> {
                code = ""
                message = ""
                KToasty.error(context, "Error setting your new pin!", Toast.LENGTH_LONG, true).show()
                println("$network: Error setting your new pin!")
            }
        }

        val sendSmsIntent: Intent = Intent(Intent.ACTION_VIEW)
        sendSmsIntent.data = Uri.parse("sms:$code")

        if (newPin.length == 4){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1){

                val localSubscriptionManager = getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    CoroutineScope(Dispatchers.IO).launch {

                        val result = permissionsBuilder(
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.CALL_PHONE,
                            Manifest.permission.SEND_SMS,
                        ).build().sendSuspend()

                        if (result.allGranted()) { // All the permissions are granted.

                            if (localSubscriptionManager.activeSubscriptionInfoCount > 1){
                                if ((pAccount == "1")){
                                    sendSmsIntent.putExtra("simSlot", 0)
                                    generalUpdatePinInDatabase("1")
                                    pin1Et!!.setText(newPin)
                                } else {
                                    sendSmsIntent.putExtra("simSlot", 1)
                                    generalUpdatePinInDatabase("2")
                                    pin2Et!!.setText(newPin)
                                }
                                showPinUpdateMessage(newPin)
                                sendSmsIntent.putExtra("sms_body", message)
                                //startActivity(sendSmsIntent);
                                startActivityForResult(
                                    Intent.createChooser(
                                        sendSmsIntent,
                                        getString(R.string.choose_messenger_instructions)
                                    ), 5
                                )
                            }
                            else if (localSubscriptionManager.activeSubscriptionInfoCount == 1) {

                                sendSmsIntent.putExtra("sms_body", message)

                                if ((pAccount == "1")){
                                    sendSmsIntent.putExtra("simSlot", 0)
                                    generalUpdatePinInDatabase("1")
                                    pin1Et!!.setText(newPin)
                                    showPinUpdateMessage(newPin)

                                    //startActivity(sendSmsIntent);
                                    startActivityForResult(
                                        Intent.createChooser(
                                            sendSmsIntent, getString(
                                                R.string.choose_messenger_instructions
                                            )
                                        ), 5
                                    )
                                } else {
                                    val msg: String = ("You only have one sim card. Please update " +
                                            "your settings with appropriate info and " +
                                            "set \"phone 1\" to the correct number.")
                                    showErrorMessage(msg)
                                }
                            }

                        }
                        else {

                            withContext(Dispatchers.Main) {
                                //KToasty.info(app, "Using local data", Toast.LENGTH_LONG).show()
                                val message2: String = "You have denied some permissions permanently, " +
                                        "if the app force close try granting permission from settings."
                                KToasty.info(context, message2, Toast.LENGTH_LONG, true).show()

                            }

                        }

                    }

                }
                else {

                    if (localSubscriptionManager.activeSubscriptionInfoCount > 1){
                        if ((pAccount == "1")){
                            sendSmsIntent.putExtra("simSlot", 0)
                            generalUpdatePinInDatabase("1")
                            pin1Et!!.setText(newPin)
                        } else {
                            sendSmsIntent.putExtra("simSlot", 1)
                            generalUpdatePinInDatabase("2")
                            pin2Et!!.setText(newPin)
                        }
                        showPinUpdateMessage(newPin)
                        sendSmsIntent.putExtra("sms_body", message)
                        //startActivity(sendSmsIntent);
                        startActivityForResult(
                            Intent.createChooser(
                                sendSmsIntent,
                                getString(R.string.choose_messenger_instructions)
                            ), 5
                        )
                    }
                    else if (localSubscriptionManager.activeSubscriptionInfoCount == 1) {

                        sendSmsIntent.putExtra("sms_body", message)

                        if ((pAccount == "1")){
                            sendSmsIntent.putExtra("simSlot", 0)
                            generalUpdatePinInDatabase("1")
                            pin1Et!!.setText(newPin)
                            showPinUpdateMessage(newPin)

                            //startActivity(sendSmsIntent);
                            startActivityForResult(
                                Intent.createChooser(
                                    sendSmsIntent, getString(
                                        R.string.choose_messenger_instructions
                                    )
                                ), 5
                            )
                        } else {
                            val msg: String = ("You only have one sim card. Please update " +
                                    "your settings with appropriate info and " +
                                    "set \"phone 1\" to the correct number.")
                            showErrorMessage(msg)
                        }
                    }

                }



            }
            else {
                if ((pAccount == "1")){
                    sendSmsIntent.putExtra("simSlot", 0)
                    generalUpdatePinInDatabase("1")
                    pin1Et!!.setText(newPin)
                } else {
                    sendSmsIntent.putExtra("simSlot", 1)
                    generalUpdatePinInDatabase("2")
                    pin2Et!!.setText(newPin)
                }
                showPinUpdateMessage(newPin)
                sendSmsIntent.putExtra("sms_body", message)
                //startActivity(sendSmsIntent);
                startActivityForResult(
                        Intent.createChooser(
                                sendSmsIntent,
                                getString(R.string.choose_messenger_instructions)
                        ), 5
                )
            }
        }
        else {
            val msg: String = "Your pin must be a 4 digit number. Please fix pin."
            showErrorMessage(msg)
        }

    }*/
























    private fun callNewPin(pAccount: String, network: String, newPin: String) {

        val ussdCode: String

        when (network) {
            "Etisalat(9Mobile)" -> {
                ussdCode = "*247" + "*0000*" + newPin + android.net.Uri.encode("#")
            }
            "Glo Mobile" -> {
                ussdCode = "*132" + "*00000*" + newPin + "*" + newPin + android.net.Uri.encode("#")
            }
            else -> {
                ussdCode = ""
                KToasty.error(context, "Error setting your new pin!", Toast.LENGTH_LONG, true).show()
                println("$network: Error setting your new pin!")
            }
        }

        val intent: Intent = Intent(
                "android.intent.action.CALL",
                android.net.Uri.parse("tel:$ussdCode")
        )

        if ((pAccount == "1")){
            intent.putExtra("simSlot", 0)
            pin1Et!!.setText(newPin)
        }
        else {
            intent.putExtra("simSlot", 1)
            pin2Et!!.setText(newPin)
        }

        intent.setPackage("com.android.server.telecom")

        if (newPin.length == 4 && network == "Glo Mobile") {


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                CoroutineScope(Dispatchers.IO).launch {

                    val result = permissionsBuilder(
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.CALL_PHONE,
                    ).build().sendSuspend()

                    if (result.allGranted()) { // All the permissions are granted.

                        startActivity(intent)
                        KToasty.success(context,
                            "Your pin $newPin has been set.", Toast.LENGTH_LONG, true
                        ).show()

                    }
                    else {

                        withContext(Dispatchers.Main) {
                            //KToasty.info(app, "Using local data", Toast.LENGTH_LONG).show()
                            val message: String = "You have denied some permissions permanently, " +
                                    "if the app force close try granting permission from settings."
                            KToasty.info(context, message, Toast.LENGTH_LONG, true).show()

                        }

                    }

                }

            }
            else {

                startActivity(intent)
                KToasty.success(
                    context,
                    "Your pin $newPin has been set.",
                    Toast.LENGTH_LONG,
                    true
                ).show()

            }

        }
        else if (newPin.length != 4 && network == "Glo Mobile") {
            KToasty.success(
                    context,
                    "Your pin must be a 5 digit number for Glo Mobile. Please fix pin.",
                    Toast.LENGTH_LONG,
                    true
            ).show()
        }

        if (newPin.length == 3 && (network == "Etisalat(9Mobile)")){


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                CoroutineScope(Dispatchers.IO).launch {

                    val result = permissionsBuilder(
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.CALL_PHONE,
                    ).build().sendSuspend()

                    if (result.allGranted()) { // All the permissions are granted.

                        startActivity(intent)
                        KToasty.success(
                            context,
                            "Your pin $newPin has been set.",
                            Toast.LENGTH_LONG,
                            true
                        ).show()

                    }
                    else {

                        withContext(Dispatchers.Main) {
                            //KToasty.info(app, "Using local data", Toast.LENGTH_LONG).show()
                            val message: String = "You have denied some permissions permanently, " +
                                    "if the app force close try granting permission from settings."
                            KToasty.info(context, message, Toast.LENGTH_LONG, true).show()

                        }

                    }

                }

            }
            else {

                startActivity(intent)
                KToasty.success(context, "Your pin $newPin has been set.",
                    Toast.LENGTH_LONG, true).show()

            }

        }
        else if (newPin.length != 3 && (network == "Etisalat(9Mobile)")) {

            KToasty.success(context, "Your pin must be a 5 digit number for Etisalat(9Mobile). Please fix pin.",
                    Toast.LENGTH_LONG, true).show()

        }

    }

    /*private fun createNewPin(pAccount: String, newPin: String, network: String){

        when (network) {

            "Airtel" -> {
                smsNewPin(pAccount, network, newPin)
            }
            "Etisalat(9Mobile)" -> {
                callNewPin(pAccount, network, newPin)
            }
            "Glo Mobile" -> {
                callNewPin(pAccount, network, newPin)
            }
            "MTN Nigeria" -> {
                smsNewPin(pAccount, network, newPin)
            }
            else -> {
                KToasty.error(
                        context,
                        "Error making airtime transfer pin!",
                        Toast.LENGTH_LONG,
                        true
                ).show()
                println("$network: Error making airtime transfer pin!")
            }

        }

    }*/

    /*private fun setupNewPin(pAccount: String) {

        pinBuilder = AlertDialog.Builder(context, R.style.MyDialogTheme)
        pinBuilder.setTitle("Confirm")
        pinBuilder.setMessage("Have you created a pin with this number before?")

        pinBuilder.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
            val message =
                    "Please enter current pin. You can use this app to change pin after sign up."
            KToasty.info(context, message, Toast.LENGTH_LONG, true).show()
        })

        pinBuilder.setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->

            dialog.dismiss()
            rememberBuilder = androidx.appcompat.app.AlertDialog.Builder(context, R.style.MyDialogTheme)
            rememberBuilder.setTitle("Remember!")
            rememberBuilder.setMessage("Airtel, MTN and 9Mobile have 4 digit transfer pin. Glo has 5-digit transfer pin.")
            rememberBuilder.setPositiveButton(
                    "YES",
                    DialogInterface.OnClickListener { rememberDialog, _ ->

                        rememberDialog.dismiss()

                        try {
                            if (
                                    phone1Et!!.text.toString() != "" &&
                                    network1 != null &&
                                    network1 != "Choose Network"
                            ) {

                                Log.i("ATTENTION ATTENTION", "not empty network1:  $network1")

                                newPinYesBuilder = AlertDialog.Builder(context, R.style.MyDialogTheme)
                                newPinYesBuilder.setTitle("Put in your desired pin")
                                pinViewInflated = LayoutInflater.from(context).inflate(
                                        R.layout.scan_dialog_input_newly_registered_pin,
                                        null
                                )
                                newPinInput =
                                        pinViewInflated.findViewById<View>(R.id.input_new_pin) as TextInputEditText
                                newPinYesBuilder.setView(pinViewInflated)
                                newPinYesBuilder.setPositiveButton(
                                        android.R.string.ok,
                                        DialogInterface.OnClickListener { dialog, _ ->

                                            dialog.dismiss()
                                            newPinStr = newPinInput.text.toString()
                                            if ((pAccount == "1")) {
                                                createNewPin("1", newPinStr, network1!!)
                                            } else {
                                                createNewPin("2", newPinStr, network1!!)
                                            }

                                        })
                                newPinYesBuilder.setNegativeButton(
                                        android.R.string.cancel,
                                        DialogInterface.OnClickListener { newPinDialog, _ ->
                                            newPinDialog.cancel()

                                        })
                                newPinYesBuilder.create().show()
                            } else {

                                Log.w("ATTENTION ATTENTION", "empty network1:  $network1")

                                if ((phone1Et!!.text.toString() == "")) {
                                    AnimationUtil.shakeView(
                                            phone1Et!!,
                                            this@RegisterDetailsActivity
                                    )
                                    phone1Layout!!.error = getString(R.string.error_enter_number)
                                    KToasty.error(
                                            context,
                                            "Please enter number first.",
                                            Toast.LENGTH_LONG,
                                            true
                                    ).show()
                                }
                                if (network1 == null) {
                                    network1Spinner?.let {
                                        AnimationUtil.shakeView(
                                                it,
                                                this@RegisterDetailsActivity
                                        )
                                    }
                                    KToasty.info(
                                            context,
                                            "Please choose network first.",
                                            Toast.LENGTH_LONG,
                                            true
                                    ).show()
                                }
                                if ((network1 == "Choose Network")) {
                                    network1Spinner?.let {
                                        AnimationUtil.shakeView(
                                                it,
                                                this@RegisterDetailsActivity
                                        )
                                    }
                                    KToasty.info(
                                            context,
                                            "Please choose network first.",
                                            Toast.LENGTH_LONG,
                                            true
                                    ).show()
                                }
                            }
                        } catch (e: NullPointerException) {

                        }

                    })

            val alert: AlertDialog = rememberBuilder.create()
            alert.show()

        })

        pinBuilder.create().show()

    }*/

    private fun returnToLogin(context: Context){
        val intent: Intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
//        context.startActivity(intent);
//        ((CheckLoggedIn) getApplicationContext()).startLogin(intent);
        finish();
    }

    private lateinit var rda_coordinatorLayout: CoordinatorLayout







    private lateinit var userMainRecyclerView: RecyclerView
    private lateinit var editFragmentUserMainViewAdapter: EditUserMainViewAdapter
    private var userMainListModel: UserListModel = UserListModel(mutableListOf())

    private lateinit var userSecondRecyclerView: RecyclerView
    private lateinit var editFragmentUserSecondViewAdapter: EditUserSecondViewAdapter
    private var userSecondListModel: UserListModel = UserListModel(mutableListOf())

    private lateinit var friendRecyclerView: RecyclerView
    private lateinit var editFragmentFriendViewAdapter: EditFriendViewAdapter
    private var friendsListModel: FriendListModel= FriendListModel(mutableListOf())





    private lateinit var registerBtn: MaterialButton

    private lateinit var oneMoreFriendBtn: ImageButton

    private val networkUtil: NetworkUtil = NetworkUtil()

    private lateinit var  phoneNumberUtil: PhoneNumberUtil

    private var util: Util = Util()

    private val PREFNAME: String = "local_user"

    private val gson = Gson()
    private var allInfoJsonSaved:   String = ""
    private var allInfoJsonUnsaved: String = ""

    private var userAndFriendInfoUnsaved : UserAndFriendInfo = UserAndFriendInfo()
    private var userAndFriendInfoSaved : UserAndFriendInfo = UserAndFriendInfo()

    private fun initRegisterButton() {

        registerBtn.setOnClickListener { view: View? ->

            mProgressBar.visibility = View.VISIBLE

//            registerUser()
            getLocation()

        }

    }

    private fun initAddOneMoreFriendButton() {

        oneMoreFriendBtn = findViewById<ImageButton>(R.id.add_one_more_friend_btn)

        oneMoreFriendBtn.setOnClickListener() { _ ->

            val message: String = "You can add more friends after registration in the Edit Tab"

            util.onShowMessage(message, context)

            Log.e("ATTENTION ATTENTION", "One More Friend Button Clicked." +
                    " New Friend Not Added In Registration Details")

            /*Log.e("ATTENTION ATTENTION", "One More Friend Button Clicked. New Friend Added")

            val index: Int = friendsListModel.friendList?.size ?: 0

            val newFriend: Friend =

                Friend(
                    index = index,
                    description = "Friend ${index + 1}",

                    folded = false,

                    name = null,
                    phone1 = null,
                    phone2 = null,
                    phone3 = null,

                    network1 = null,
                    network2 = null,
                    network3 = null,

                    bank1 = null,
                    bank2 = null,
                    bank3 = null,
                    bank4 = null,

                    accountNumber1 = null,
                    accountNumber2 = null,
                    accountNumber3 = null,
                    accountNumber4 = null,
                )

            friendsListModel.friendList?.add(newFriend)

            val sharedPref = getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

            allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
            allInfoJsonSaved = sharedPref.getString("allInfoSaved", "defaultAll")!!

            if (allInfoJsonUnsaved != "defaultAll") {

                val allInfo = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

                allInfo.friendList?.add(newFriend)

                val editor = sharedPref.edit()

                allInfoJsonUnsaved = Gson().toJson(allInfo)

//                editor.putString("allInfoSaved", allInfoJsonSaved)
                editor.putString("allInfoUnsaved", allInfoJsonUnsaved)

                editor.apply()

                val message: String = "You have successfully added an entry for one more friend."

                util.onShowMessage(message, context)

                Log.e("ATTENTION ATTENTION", "One More Friend Button Clicked. New Friend Added")

                Log.e("ATTENTION ATTENTION", "sharedPreferences: ${sharedPref.all}")

                Log.e("ATTENTION ATTENTION", "allInfoJsonSaved: $allInfoJsonSaved")

                Log.e("ATTENTION ATTENTION", "allInfoJsonUnsaved: $allInfoJsonUnsaved")

            }

            editFragmentFriendViewAdapter.notifyDataSetChanged()*/


        }

    }

    private fun initEditMainRecyclerView() {

        userMainRecyclerView = findViewById(R.id.edit_user_main_recycler_view)
        userMainRecyclerView.layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
        )

        val dividerItemDecoration = DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
        )

        userMainRecyclerView.addItemDecoration(dividerItemDecoration)

        userMainRecyclerView.isNestedScrollingEnabled = false

        editFragmentUserMainViewAdapter = EditUserMainViewAdapter(userMainListModel, firebaseUser!!) {
            val message: String = "${it?.name}@${it?.phone} Clicked"
//            KToasty.info(context, "${it?.name}@${it?.phone} Clicked", Toast.LENGTH_LONG).show()
        }

        userMainRecyclerView.adapter = editFragmentUserMainViewAdapter

    }

    private fun initEditSecondRecyclerView() {


        userSecondRecyclerView = findViewById(R.id.edit_user_second_recycler_view)
        userSecondRecyclerView.layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
        )

        val dividerItemDecoration = DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
        )

        userSecondRecyclerView.addItemDecoration(dividerItemDecoration)

        userSecondRecyclerView.isNestedScrollingEnabled = false

        editFragmentUserSecondViewAdapter = EditUserSecondViewAdapter(userSecondListModel) {
//            KToasty.info(context, "${it?.name}@${it?.phone} Clicked", Toast.LENGTH_LONG).show()
        }

        userSecondRecyclerView.adapter = editFragmentUserSecondViewAdapter

    }

    private fun initEditFriendRecyclerView() {

        friendRecyclerView = findViewById(R.id.edit_user_friends_recycler_view)
        friendRecyclerView.layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
        )

        val dividerItemDecoration = DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
        )

        friendRecyclerView.addItemDecoration(dividerItemDecoration)

        friendRecyclerView.isNestedScrollingEnabled = false

        editFragmentFriendViewAdapter = EditFriendViewAdapter(friendsListModel, this, this)
        {
            val message: String = "${it?.name} Clicked"
//            util.onShowMessage(message, context)
        }

        friendRecyclerView.adapter = editFragmentFriendViewAdapter

    }

    /*private fun registerData() {

        val sharedPref = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

        val editor = sharedPref.edit()

        allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "allInfoJsonUnsaved").toString()
        allInfoJsonSaved   = sharedPref.getString("allInfoSaved", allInfoJsonUnsaved).toString()

        userAndFriendInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)
        userAndFriendInfoSaved   = gson.fromJson(allInfoJsonSaved,   UserAndFriendInfo::class.java)

        val user = auth.currentUser

        if (user != null) userId = user.uid;

        if (util.checkIfAllMandatoryExist(userAndFriendInfoUnsaved)) {

            db
                    .collection("users")
                    .document(userId!!)
                    .set(userAndFriendInfoUnsaved, SetOptions.merge())
                    .addOnSuccessListener {

                        editor.putString("allInfoSaved", allInfoJsonUnsaved)
                        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
                        editor.apply()

                        Log.d(TAG, "Save complete!")

                        mProgressBar.visibility = View.GONE
                        mProgressBarLocation.visibility = View.GONE

                        util.onShowMessage("Email verified. Account created.", context)

                        goToMain()

                    }
                    .addOnFailureListener { e -> Log.e(TAG, "Error saving", e)
                    }

        }
        else {

            if (userAndFriendInfoUnsaved.usersList[0].name == null  || userAndFriendInfoUnsaved.usersList[0].name  == "") {

                util.onShowErrorMessage("you must set name", context)

            }
            else if (userAndFriendInfoUnsaved.usersList[0].phone == null || userAndFriendInfoUnsaved.usersList[0].phone == "") {

                util.onShowErrorMessage("you must set phone1", context)

            }
            else if (userAndFriendInfoUnsaved.usersList[0].network != null && userAndFriendInfoUnsaved.usersList[0].network != "") {

                util.onShowErrorMessage("you must set network1", context)

            }
            else if (userAndFriendInfoUnsaved.usersList[0].email == null || userAndFriendInfoUnsaved.usersList[0].email == "") {

                util.onShowErrorMessage("you must set email", context)

            }


        }


/// Practise from and to Json
//    allInfoJsonSaved = gson.toJson(userAndFriendInfo)  // json string
//    allInfoJsonSaved = sharedPref.getString("allInfoSaved", "defaultAll").toString()
//    val allInfoSaved = gson.fromJson(allInfoJsonSaved, UserAndFriendInfo::class.java)
//
//
//    allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll").toString()
//    val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)
//    allInfoJsonSaved = gson.toJson(userAndFriendInfo)  // json string
//    editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
//    editor.apply()

    }*/


    private fun initModels() {

        created = System.currentTimeMillis().toString()

        val newUser1: User =

            User(
                uid = userId!!,
                index = 0,
                description = "User 1",
                folded = true,
                created = created,
                name = null,
                email = null,
                phone = null,
                network = null,
                pin = null,
                bank1 = null,
                bank2 = null,
                bank3 = null,
                bank4 = null,
                smartCardNumber1 = null,
                smartCardNumber2 = null,
                smartCardNumber3 = null,
                smartCardNumber4 = null,
                meterNumber1 = null,
                meterNumber2 = null,
                meterNumber3 = null
            )

        val newUser2: User =

            User(
                uid = userId!!,
                index = 1,
                description = "User 2",
                folded = true,
                created = created,
                name = null,
                email = null,
                phone = null,
                network = null,
                pin = null,
                bank1 = null,
                bank2 = null,
                bank3 = null,
                bank4 = null,
                smartCardNumber1 = null,
                smartCardNumber2 = null,
                smartCardNumber3 = null,
                smartCardNumber4 = null,
                meterNumber1 = null,
                meterNumber2 = null,
                meterNumber3 = null
            )

        val newFriend: Friend =

            Friend(
                index = 0,
                description = "Friend ${0 + 1}",
                folded = true,
                name = null,
                phone1 = null,
                phone2 = null,
                phone3 = null,

                network1 = null,
                network2 = null,
                network3 = null,

                bank1 = null,
                bank2 = null,
                bank3 = null,
                bank4 = null,

                accountNumber1 = null,
                accountNumber2 = null,
                accountNumber3 = null,
                accountNumber4 = null,
            )

        userMainListModel.userList   = mutableListOf(newUser1)
        userSecondListModel.userList = mutableListOf(newUser2)
        friendsListModel.friendList  = mutableListOf(newFriend)

        userAndFriendInfoUnsaved.usersList = mutableListOf(newUser1, newUser2)
        userAndFriendInfoSaved.usersList   = mutableListOf(newUser1, newUser2)

        userAndFriendInfoUnsaved.friendList = friendsListModel.friendList
        userAndFriendInfoSaved.friendList   = friendsListModel.friendList

        val sharedPref = getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        allInfoJsonUnsaved = Gson().toJson(userAndFriendInfoUnsaved)

        editor.putString("allInfoSaved", allInfoJsonUnsaved)
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)

        editor.apply()

        Log.e("ATTENTION ATTENTION", "sharedPreferences in init: ${sharedPref.all}")

    }

    var token: String = ""

    override  fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ViewPump.init(
                ViewPump.builder()
                        .addInterceptor(
                                CalligraphyInterceptor(
                                        CalligraphyConfig.Builder()
                                                .setDefaultFontPath("font/bold.ttf")
                                                .setFontAttrId(R.attr.fontPath)
                                                .build()
                                )
                        )
                        .build()
        )

        setContentView(R.layout.activity_register_details)

        context = this

        val pref = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

        token = pref.getString("token", "defaultAll")!!

        if (auth.currentUser == null) {

            goToLogin()

        }
        else {

            firebaseUser = auth.currentUser as FirebaseUser

        }

        if (firebaseUser == null) {

            goToLogin()

        }
        else {

            userId = firebaseUser!!.uid

        }

//        FirebaseAuth.getInstance().signInWithCustomToken(token).addOnSuccessListener {
//
//            try {
//
//                if (it.user != null) {
//
//                    firebaseUser = it.user
//                    userId = it.user?.uid
//
//                }
//                else {
//
//                    goToLogin()
//
//                }
//
//            }
//            catch (e: Exception) {
//
//                KToasty.info(context, "Sorry! You can't use this app because your API level is too low.", Toast.LENGTH_LONG).show()
//
//            }
//
//        }

//        db    = Firebase.firestore
//        auth = Firebase.auth


        rda_coordinatorLayout = findViewById<View>(R.id.rda_coordinatorLayout) as CoordinatorLayout

//        mProgressBar = ProgressBar(context, null, android.R.attr.progressBarStyleLarge)
//        val params = RelativeLayout.LayoutParams(100, 100)
//        params.addRule(RelativeLayout.CENTER_IN_PARENT)
//        rda_coordinatorLayout.addView(mProgressBar, params)

        mProgressBar         = findViewById<ProgressBar>(R.id.mProgressBar) as ProgressBar
        mProgressBarLocation = findViewById<ProgressBar>(R.id.mLocationProgressBar) as ProgressBar

        mProgressBar.visibility         = View.GONE
        mProgressBarLocation.visibility = View.GONE

        mProgressBar.isIndeterminate         = true
        mProgressBarLocation.isIndeterminate = true

        registerBtn = findViewById<MaterialButton>(R.id.register_btn)

        initModels()
        initAddOneMoreFriendButton()
        initRegisterButton()
        initEditMainRecyclerView()
        initEditSecondRecyclerView()
        initEditFriendRecyclerView()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            val fade: Fade = Fade()

            fade.excludeTarget(findViewById<View>(R.id.layout), true)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fade.excludeTarget(android.R.id.statusBarBackground, true)
                fade.excludeTarget(android.R.id.navigationBarBackground, true)
                window.enterTransition = fade
                window.exitTransition = fade
            }

        }

        appLocationService = AppLocationService(this)

    }


    lateinit var appLocationService: AppLocationService



    internal inner class GeoCodeHandler : Handler() {

        override fun handleMessage(message: Message) {

            val locationAddress: String = when (message.what) {
                1 -> ({
                    val bundle = message.data
                    bundle.getString("address")
                }).toString()
                else -> null.toString()
            }
//            tvAddress.text = locationAddress


        }
    }


    override  fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {

        if (requestCode == REQUEST_LOCATION_PERMISSION) {

            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {

                KToasty.info(context, "Sorry! You can't use this app without " +
                                "granting location permission for a one time only check of location.",
                        Toast.LENGTH_LONG).show()

            }
            else {

                registerUser()

            }

        }
        else if (requestCode == REQUEST_LOCATION_PERMISSIONCHECK) {

            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {

                KToasty.info(context, "Sorry! You can't use this app without " +
                                "granting location permission for a one-time-only check of location.",
                        Toast.LENGTH_LONG).show()

            }

        }
        else if (requestCode == REQUEST_LOCATION_PERMISSION_BEGINNING) {

            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {

                KToasty.info(context, "Sorry! You can't use this app without " +
                        "granting location permission for a one-time-only check of location.",
                    Toast.LENGTH_LONG).show()

            }


        }

    }

    fun getAddressFromLocation(
            latitude: Double,
            longitude: Double,
            context: Context,
            handler: Handler
    ) {
        val thread = object : Thread() {
            override fun run() {
                val geoCoder = Geocoder(
                        context,
                        Locale.getDefault()
                )
                var result: String = null.toString()
                try {
                    val addressList = geoCoder.getFromLocation(
                            latitude, longitude, 1
                    )
                    if ((addressList != null && addressList.size > 0)) {

                        val addresss = addressList[0]
                        val sb = StringBuilder()
                        for (i in 0 until addresss.maxAddressLineIndex) {
                            sb.append(addresss.getAddressLine(i)).append("\n")
                        }

                        sb.append(addresss.locality).append("\n")
                        sb.append(addresss.postalCode).append("\n")
                        sb.append(addresss.countryName)

                        address = addresss.getAddressLine(0)
                        city = addresss.locality
                        statee = addresss.adminArea
                        country = addresss.countryName
                        postalCode = addresss.postalCode
                        knownName = addresss.featureName

                        result = sb.toString()
                    }
                }
                catch (e: IOException) {
                    Log.e("LocationAddress", "Unable connect to GeoCoder", e)
                }
                finally {
                    val message = Message.obtain()
                    message.target = handler
                    message.what = 1
                    val bundle = Bundle()
                    result = ("Latitude: " + latitude + " Longitude: " + longitude +
                            "\n\nAddress:\n" + result)
                    bundle.putString("address", result)
                    message.data = bundle
                    message.sendToTarget()
                }
            }
        }
        thread.start()
    }

    // https://stackoverflow.com/questions/32491960/android-check-permission-for-locationmanager
    private fun getAddress(lons: Double, lats: Double): String?{

        //getAddressFromLocation(lons, lats, context, GeoCodeHandler())

        val geocoder: Geocoder

        val lon: Double = lons
        val lat: Double = lats

        geocoder = Geocoder(context, Locale.getDefault())

        var  addresses: List<android.location.Address>? = null

        try {

            addresses = geocoder.getFromLocation(lat, lon, 1)

        }
        catch (e: Exception) {

            e.printStackTrace()

        }
        return if (addresses != null) {

            address = addresses[0].getAddressLine(0)
            city = addresses[0].locality
            statee = addresses[0].adminArea
            country = addresses[0].countryName
            postalCode = addresses[0].postalCode
            knownName = addresses[0].featureName
            address

        }
        else {

            "failed"

        }

    }



    /**
     * This method is call when authentication is successful.
     */
    fun updateUI(currentUser: FirebaseUser?){
        if (currentUser != null){
            val i: Intent = Intent(context, MainActivity::class.java)
            startActivity(i)
            finish();
        }
    }

    private lateinit var  mSettingsClient: SettingsClient
    private lateinit var  mLocationSettingsRequest: LocationSettingsRequest
    // https://stackoverflow.com/questions/33251373/turn-on-location-services-without-navigating-to-settings-page
    private fun displayLocationSettingsRequest(){
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = (10000 / 2).toLong()
        val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder().addLocationRequest(
                locationRequest
        )
        builder.setAlwaysShow(true)
        mLocationSettingsRequest = builder.build()
        mSettingsClient = LocationServices.getSettingsClient(context)

        //PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
            .addOnSuccessListener {
                //Success Perform Task Here
            }
            .addOnFailureListener { e ->

                when ((e as ApiException).statusCode) {

                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->

                        try {
                            val rae: ResolvableApiException = e as ResolvableApiException
                            rae.startResolutionForResult(
                                this@RegisterDetailsActivity,
                                    REQUEST_CHECK_SETTINGS)

                        } catch (sie: SendIntentException) {
                            Log.e("GPS", "Unable to execute request.")
                        }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->
                        Log.e(
                                "GPS",
                                "Location settings are inadequate, and cannot be fixed here. Fix in Settings."
                        )

                }
            }
            .addOnCanceledListener {
                Log.e("GPS", "checkLocationSettings -> onCanceled")
            }
    }

    private fun openGpsEnableSetting(){
        val intent: Intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivityForResult(intent, REQUEST_ENABLE_GPS)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CHECK_SETTINGS){
            when(resultCode){
                Activity.RESULT_OK ->                     //Success Perform Task Here
                    getLocationPoints()
                Activity.RESULT_CANCELED -> {
                    Log.e("GPS", "User denied to access location")
                    openGpsEnableSetting()
                }}

        }
        else if (requestCode == REQUEST_ENABLE_GPS) {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled: Boolean = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (!isGpsEnabled){
                openGpsEnableSetting()
            }
            else {

                //navigateToUser();
            }
        }
        else if (requestCode >= PICK_CONTACT_REQUEST) {

            if (data != null) {

                setContact(data, requestCode - PICK_CONTACT_REQUEST)

            }

//            addFriendDoneListener.contactPicked(data, requestCode - PICK_CONTACT_REQUEST)

        }
        if (resultCode == Activity.RESULT_OK) { // before after picking contact returns here

        }

    }

    private fun setContact(data: Intent?, index: Int) {

        phoneNumberUtil = PhoneNumberUtil.createInstance(context)
        val phoneUtil: PhoneUtil = PhoneUtil()

        val name: String
        val number: String
        var nationalNumber: String
        val details: Array<String>? = data?.let { phoneUtil.getDetails(it, context) }
        name = details?.get(0) ?: ""
        number = details?.get(1) ?: ""

        Log.e("ATTENTION ATTENTION", "addFriendDoneListener.contactPicked from RegisterDetailsActivity")

        Log.e("ATTENTION ATTENTION", "friendsListModel.friendList?.size: ${friendsListModel.friendList?.size ?: 0}")

        Log.e("ATTENTION ATTENTION", "index: $index")

        if (friendsListModel.friendList?.size ?: 0 > index) {

            Log.e("ATTENTION ATTENTION", "friendsListModel.friendList?.size ?: 0 > index then")

            val  phoneNumber: Phonenumber.PhoneNumber = phoneNumberUtil.parse(number, "NG")
            nationalNumber = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
            nationalNumber = nationalNumber.replace("\\s".toRegex(), "")

            friendsListModel.friendList!![index].name = name
            friendsListModel.friendList!![index].phone1 = nationalNumber

            Log.e("ATTENTION ATTENTION", "name: $name number: $nationalNumber")

        }

        editFragmentFriendViewAdapter.notifyDataSetChanged()

    }

    private fun buildAlertMessageNoLocation() {

        val  builder: AlertDialog.Builder = AlertDialog.Builder(this)

        builder.setMessage("Your Location seems to be disabled, you have to enable it (for a one time check) in order to register.")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                dialog.dismiss()
                displayLocationSettingsRequest()
                //startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
            .setNegativeButton(
                    "No"
            ) { dialog, id -> dialog.cancel() }

        val  alert: androidx.appcompat.app.AlertDialog = builder.create()

        alert.show()



    }

    private lateinit var criteria: Criteria
    private lateinit var bestProvider: String
    private lateinit var locationManager: LocationManager
    private var locationCheckComplete: Boolean = false

    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private fun getLocationPoints() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PERMISSION_GRANTED) {

                KToasty.info(
                    context,
                    "First enable LOCATION ACCESS in settings for a one time check.",
                    Toast.LENGTH_LONG
                ).show()

                mProgressBar.visibility = View.GONE

                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    REQUEST_LOCATION_PERMISSION
                )

            }
            else {

                try {

                    fusedLocationClient.lastLocation
                        .addOnSuccessListener(this) { bestLocation1 ->
                            //Location bestLocation = null; //locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            var bestLocation = bestLocation1
                            val providers = locationManager.getProviders(true)
                            for (provider in providers) {
                                locationManager.requestLocationUpdates(provider!!, 1000, 0f,
                                    object : LocationListener {
                                        override fun onLocationChanged(location: Location) {}
                                        override fun onProviderDisabled(provider: String) {}
                                        override fun onProviderEnabled(provider: String) {}
                                        override fun onStatusChanged(
                                            provider: String, status: Int,
                                            extras: Bundle) {}
                                    })
                                val l = locationManager.getLastKnownLocation(provider) ?: continue
                                if (bestLocation == null
                                    || l.accuracy < bestLocation.accuracy) {
                                    bestLocation = l
                                }
                            }

                            // Got last known location. In some rare situations this can be null.
                            if (bestLocation != null) {
                                // Logic to handle location object
                                Log.e("ATTENTION ATTENTION", "Location Received")
                                longitude = bestLocation.longitude
                                latitude = bestLocation.latitude
                                val addressRun = getAddress(longitude, latitude)

                                finalAddress = if (addressRun != "failed") {
                                    "$address, $city, $statee, $country"
                                }
                                else {
                                    "unknown"
                                }

                                Log.e(
                                    "ATTENTION ATTENTION",
                                    "Inside getLocationPoints. Got Location."
                                )

                                //KToasty.success(RegisterDetailsActivity.this, "Got Location.", Toast.LENGTH_LONG,true).show()
                                Log.e("ATTENTION ATTENTION", "longitude: $longitude")
                                Log.e("ATTENTION ATTENTION", "latitude: $latitude")
                                Log.e("ATTENTION ATTENTION", "Location: $finalAddress")
                                mProgressBar.visibility = View.GONE
                                mProgressBarLocation.visibility = View.GONE
                                registerUser()

                            }
                            else {

                                getLocationPoints()
                                Log.e("ATTENTION ATTENTION", "Location Not Received.")
                                Log.e("ATTENTION ATTENTION", "Will Try Again.")
                                val errorMessage = "Please register after granting permissions in settings."

                                //KToasty.error(RegisterDetailsActivity.this, errorMessage, Toast.LENGTH_LONG,true).show()

                            }

                        }

                }
                catch (e: SecurityException) {

                    e.printStackTrace()
                    Toasty.error(
                        this@RegisterDetailsActivity,
                        "Error: " + e.message,
                        Toasty.LENGTH_LONG
                    ).show()

                }
                catch (e: java.lang.NullPointerException) {

                    e.printStackTrace()

                    Toasty.error(
                        this@RegisterDetailsActivity,
                        "Error: " + e.message,
                        Toasty.LENGTH_LONG
                    ).show()

                }

            }

        }
        else {

            try {

                fusedLocationClient.lastLocation
                    .addOnSuccessListener(this) { bestLocation1 ->
                        //Location bestLocation = null; //locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        var bestLocation = bestLocation1
                        val providers = locationManager.getProviders(true)
                        for (provider in providers) {
                            locationManager.requestLocationUpdates(provider!!, 1000, 0f,
                                object : LocationListener {
                                    override fun onLocationChanged(location: Location) {}
                                    override fun onProviderDisabled(provider: String) {}
                                    override fun onProviderEnabled(provider: String) {}
                                    override fun onStatusChanged(
                                        provider: String, status: Int,
                                        extras: Bundle) {}
                                })
                            val l = locationManager.getLastKnownLocation(provider) ?: continue
                            if (bestLocation == null
                                || l.accuracy < bestLocation.accuracy) {
                                bestLocation = l
                            }
                        }

                        // Got last known location. In some rare situations this can be null.
                        if (bestLocation != null) {
                            // Logic to handle location object
                            Log.e("ATTENTION ATTENTION", "Location Received")
                            longitude = bestLocation.longitude
                            latitude = bestLocation.latitude
                            val addressRun = getAddress(longitude, latitude)

                            finalAddress = if (addressRun != "failed") {
                                "$address, $city, $statee, $country"
                            }
                            else {
                                "unknown"
                            }

                            Log.e(
                                "ATTENTION ATTENTION",
                                "Inside getLocationPoints. Got Location."
                            )

                            //KToasty.success(RegisterDetailsActivity.this, "Got Location.", Toast.LENGTH_LONG,true).show()
                            Log.e("ATTENTION ATTENTION", "longitude: $longitude")
                            Log.e("ATTENTION ATTENTION", "latitude: $latitude")
                            Log.e("ATTENTION ATTENTION", "Location: $finalAddress")
                            mProgressBar.visibility = View.GONE
                            mProgressBarLocation.visibility = View.GONE
                            registerUser()

                        }
                        else {
                            getLocationPoints()
                            Log.e("ATTENTION ATTENTION", "Location Not Received.")
                            Log.e("ATTENTION ATTENTION", "Will Try Again.")
                            val errorMessage = "Please register after granting permissions in settings."

                            //KToasty.error(RegisterDetailsActivity.this, errorMessage, Toast.LENGTH_LONG,true).show()
                        }
                    }

            }
            catch (e: SecurityException) {

                e.printStackTrace()

                Toasty.error(
                    this@RegisterDetailsActivity,
                    "Error: " + e.message,
                    Toasty.LENGTH_LONG
                ).show()

            }
            catch (e: java.lang.NullPointerException) {

                e.printStackTrace()

                Toasty.error(
                    this@RegisterDetailsActivity,
                    "Error: " + e.message,
                    Toasty.LENGTH_LONG
                ).show()

            }

        }

    }












    private fun getLocation() {

        locationCheckComplete = false

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

//        criteria = Criteria()
//        bestProvider = locationManager.getBestProvider(Criteria(), true).toString()

        try {

            mProgressBar.visibility = View.GONE

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                Log.e(
                        "ATTENTION ATTENTION",
                        "!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)"
                )

                buildAlertMessageNoLocation()

            }
            else {

                mProgressBarLocation.visibility = View.VISIBLE

                getLocationPoints()

            }
        }
        catch (e: NullPointerException) {
            e.printStackTrace()
        }

    }

    override  fun onLocationChanged(location: Location){
        locationManager.removeUpdates(this)
        longitude = location.longitude
        latitude = location.latitude
        val  addressRun: String? = getAddress(longitude, latitude)
        if (!(addressRun == "failed")){
            finalAddress = "$address, $city, $statee, $country"
        } else {
            finalAddress = "unknown"
        }
        mProgressBarLocation.visibility = View.GONE

        //KToasty.info(RegisterDetailsActivity.this, "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_SHORT).show();
    }
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle){}
    override fun onProviderEnabled(provider: String){}
    override fun onProviderDisabled(provider: String){}

    private lateinit var currentEmail: String

    private var registerCount = 0

    private lateinit var created: String

    private fun registerUser() {

        val sharedPref = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

        allInfoJsonSaved   = sharedPref.getString("allInfoSaved", "allInfoJsonSaved").toString()
        allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", allInfoJsonSaved).toString()

        if (allInfoJsonUnsaved == "allInfoJsonSaved") {

//            userAndFriendInfoUnsaved =  UserAndFriendInfo()
//            userAndFriendInfoUnsaved.usersList = arrayListOf()

            val message: String = "Error registering. Please try again later."
            util.onShowErrorMessage(message, context)

        }
        else {

            userAndFriendInfoSaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)
            userAndFriendInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

            val user = auth.currentUser

            if (user != null) userId = user.uid

            if (userAndFriendInfoUnsaved.usersList.size > 0) {

                userAndFriendInfoUnsaved.usersList[0].created = created
                userAndFriendInfoUnsaved.usersList[0].uid = userId!!


            }

            if (userAndFriendInfoSaved.usersList.size > 0) {

                userAndFriendInfoSaved.usersList[0].created = created
                userAndFriendInfoSaved.usersList[0].uid = userId!!

            }

            if (userAndFriendInfoUnsaved.usersList.size > 1) {

                userAndFriendInfoUnsaved.usersList[1].created = created
                userAndFriendInfoUnsaved.usersList[1].uid = userId!!

            }

            if (userAndFriendInfoSaved.usersList.size > 1) {


                userAndFriendInfoSaved.usersList[1].uid = userId!!
                userAndFriendInfoSaved.usersList[1].created = created

            }

            if (util.checkIfAllMandatoryExist(userAndFriendInfoUnsaved)) {

                Log.w("ATTENTION ATTENTION", "registerUser() ran.")

    //            val sharedPref = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

                val editor = sharedPref.edit()

                if (registerCount == 0) {

                    var phone2: String?
                    var network2: String?

                    if (userAndFriendInfoUnsaved.usersList.size > 1 &&
                        userAndFriendInfoUnsaved.usersList[1].phone != null
                        && userAndFriendInfoUnsaved.usersList[1].phone?.trim() != "") {

                        phone2   = userAndFriendInfoUnsaved.usersList[1].phone!!
                        network2 = userAndFriendInfoUnsaved.usersList[1].network!!

                    }
                    else {
                        phone2   = null
                        network2 = null
                    }

                    if (

                        longitude == null ||
                        latitude == null ||
                        address == null ||
                        city == null ||
                        statee == null ||
                        country == null ||
                        postalCode == null ||
                        knownName == null

                    ) {

                        if (longitude == null) {
                            longitude = 1.0
                        }

                        if (latitude == null) {
                            latitude = 1.0
                        }

                        if (address == null) {
                            address = "Unknown"
                        }

                        if (city == null) {
                            city = "Unknown"
                        }

                        if (statee == null) {
                            statee = "Unknown"
                        }

                        if (country == null) {
                            country = "Unknown"
                        }

                        if (postalCode == null) {
                            postalCode = "Unknown"
                        }

                        if (knownName == null) {
                            knownName = "Unknown"
                        }

                    }

                    val locationUser: LocationUser =

                        LocationUser(
                            uid = userId!!,
                            created = created,
                            name = userAndFriendInfoUnsaved.usersList[0].name!!,
                            email = userAndFriendInfoUnsaved.usersList[0].email!!,
                            phone = userAndFriendInfoUnsaved.usersList[0].phone!!,
                            network = userAndFriendInfoUnsaved.usersList[0].network!!,
                            phone2 = phone2,
                            network2 = network2,
                            longitude = longitude,
                            latitude = latitude,
                            address = address!!,
                            city = city!!,
                            state = statee!!,
                            country = country!!,
                            postalCode = postalCode!!,
                            knownName = knownName!!,
                        )

                    allInfoJsonSaved = Gson().toJson(userAndFriendInfoUnsaved)
                    allInfoJsonUnsaved = Gson().toJson(userAndFriendInfoUnsaved)

                    db
                        .collection("locations")
                        .document(userId!!)
                        .set(locationUser)
                        .addOnSuccessListener {

                            db
                                .collection("users")
                                .document(userId!!)
                                .set(userAndFriendInfoUnsaved)
                                .addOnSuccessListener {

                                    email = userAndFriendInfoUnsaved.usersList[0].email
                                    currentEmail = email!!

                                    firebaseUser!!.updateEmail(email!!)
                                        .addOnSuccessListener(OnSuccessListener<Void?> {

                                            firebaseUser!!.sendEmailVerification()
                                                .addOnSuccessListener(OnSuccessListener<Void?> {

                                                    val message: String = "Please go to email: $email and verify your email address to continue."

                                                    val verifyEmailAlertDialogBuilder = AlertDialog.Builder(context, R.style.MyDialogTheme)

                                                    verifyEmailAlertDialogBuilder.setTitle("Email Verification")
                                                    verifyEmailAlertDialogBuilder.setMessage(message)
//                                                    verifyEmailAlertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert)

                                                    verifyEmailAlertDialogBuilder.setPositiveButton("Ok") {

                                                            dialogInterface, which ->

                                                        dialogInterface.dismiss()

                                                    }

                                                    val verifyEmailAlertDialog: AlertDialog = verifyEmailAlertDialogBuilder.create()
                                                    verifyEmailAlertDialog.setCancelable(false)
                                                    verifyEmailAlertDialog.show()

//                                                    val message: String = "Please go to email: $email and verify your address to continue. Thank you."
//                                                    util.onShowMessageLong(message, context)

                                                    registerCount++

                                                    runEmailCheck(editor)

                                                })
                                                .addOnFailureListener(OnFailureListener { })
                                        })
                                        .addOnFailureListener(OnFailureListener { e ->

                                            mProgressBar.visibility = View.GONE
                                            mProgressBarLocation.visibility = View.GONE


                                            if (e.message != null) {

                                                if (e.message!!.contains("This operation is sensitive")) {

                                                    val message: String = "You took too long to register. Optional information (in gray) can be added after registration. Please log in again to register."

                                                    val tookTooLongAlertDialogBuilder = AlertDialog.Builder(context, R.style.MyDialogTheme)

                                                    tookTooLongAlertDialogBuilder.setTitle("Registration Timeout")
                                                    tookTooLongAlertDialogBuilder.setMessage(message)
//                                                    verifyEmailAlertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert)

                                                    tookTooLongAlertDialogBuilder.setPositiveButton("Ok") {

                                                            dialogInterface, which ->

                                                        goToLogin()

                                                    }

                                                    val tookTooLongAlertDialog: AlertDialog = tookTooLongAlertDialogBuilder.create()
                                                    tookTooLongAlertDialog.setCancelable(false)
                                                    tookTooLongAlertDialog.show()

                                                }

                                            }

//                                            val message: String = "Error updating email: " + e.message
//                                            util.onShowErrorMessage(message, context)

                                        })

                                }
                                .addOnFailureListener { e ->

                                    mProgressBar.visibility = View.GONE
                                    mProgressBarLocation.visibility = View.GONE

                                    val message: String = "Error saving details: " + e.message
                                    util.onShowErrorMessage(message, context)
                                    Log.e(TAG, "Error saving", e)

                                }

                        }
                        .addOnFailureListener { e ->

                            mProgressBar.visibility = View.GONE
                            mProgressBarLocation.visibility = View.GONE

                            val message: String = "Error saving details: " + e.message
                            util.onShowErrorMessage(message, context)
                            Log.e(TAG, "Error saving", e)

                        }

                }
                else {

                    email = userAndFriendInfoUnsaved.usersList[0].email
    //                currentEmail = email!!

                    if (myFixedRateTimer != null) {
                        myFixedRateTimer!!.cancel()
                    }

                    var message: String

                    if (email!! != currentEmail) {

                        registerCount = 0

                        registerUser()

                    }
                    else {

                        registerCount++

                        message = "Please go to email: $email and verify your address to continue. Thank you."

                        util.onShowMessage(message, context)

                        runEmailCheck(editor)

                    }

                }



            }
            else {

                if (userAndFriendInfoUnsaved.usersList[0].name == null  || userAndFriendInfoUnsaved.usersList[0].name  == "") {

                    util.onShowErrorMessage("You must set name", context)

                }
                else if (userAndFriendInfoUnsaved.usersList[0].phone == null || userAndFriendInfoUnsaved.usersList[0].phone == "") {

                    util.onShowErrorMessage("You must set phone 1", context)

                }
                else if (userAndFriendInfoUnsaved.usersList[0].network != null && userAndFriendInfoUnsaved.usersList[0].network != "") {

                    util.onShowErrorMessage("You must set network 1", context)

                }
                else if (userAndFriendInfoUnsaved.usersList[0].email == null || userAndFriendInfoUnsaved.usersList[0].email == "") {

                    util.onShowErrorMessage("You must set email", context)

                }

            }

        }

    }

    private var myFixedRateTimer: Timer? = null

    private fun runEmailCheck(editor: SharedPreferences.Editor) {

        mProgressBar.visibility = View.GONE
        mProgressBarLocation.visibility = View.GONE

        myFixedRateTimer = fixedRateTimer("timer",false,3000,2000) {

            this@RegisterDetailsActivity.runOnUiThread {

                db    = Firebase.firestore
                auth = Firebase.auth

                val userTask: Task<Void> = Objects.requireNonNull(auth.currentUser)!!.reload()

                userTask.addOnSuccessListener {

                    firebaseUser = auth.currentUser!!

                    Log.e("ATTENTION ATTENTION",
                        "mCurrentUser.isEmailVerified(): " + firebaseUser!!.isEmailVerified)

                    if (!firebaseUser!!.isEmailVerified) {

                        //emailVerificationCountDown(5000);
//                        val message: String = "Please go to email: $email and verify your address to continue. Thank you."
//                        util.onShowMessage(message, context)


                    }
                    else {

                        allInfoJsonSaved = Gson().toJson(userAndFriendInfoUnsaved)
                        allInfoJsonUnsaved = Gson().toJson(userAndFriendInfoUnsaved)

                        val sharedPref = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

                        val editor = sharedPref!!.edit()

                        editor.putString("allInfoSaved", allInfoJsonUnsaved)
                        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
                        editor.putBoolean("email_verified", true)
                        editor.putBoolean("location_received", true)

                        editor.apply()

                        myFixedRateTimer?.cancel()

                        val message: String = "Email verified. Account created."

                        util.onShowMessageSuccessLong(message, context)

                        registrationComplete = true

                        goToMain()

                    }

                }

            }

        }

    }

    /*private fun runEmailCheckIfEmailChanged(editor: SharedPreferences.Editor) {

        mProgressBar.visibility = View.GONE
        mProgressBarLocation.visibility = View.GONE

        myFixedRateTimer = fixedRateTimer("timer",false,3000,2000) {

            this@RegisterDetailsActivity.runOnUiThread {

                val userTask: Task<Void> = Objects.requireNonNull(auth.currentUser)!!.reload()

                userTask.addOnSuccessListener {

                    firebaseUser = auth.currentUser!!

                    Log.e(
                        "ATTENTION ATTENTION",
                        "mCurrentUser.isEmailVerified(): " + firebaseUser!!.isEmailVerified
                    )

                    if (firebaseUser!!.isEmailVerified) {

                        myFixedRateTimer?.cancel()
                        actualRegistration(editor)

                        //emailVerificationCountDown(5000);
//                        val message: String = "Please go to email: $email and verify your address to continue. Thank you."
//                        util.onShowMessage(message, context)


                    }

                }

            }

        }

    }*/

    /*private fun actualRegistration(editor: SharedPreferences.Editor) {

        var phone2: String?
        var network2: String?

        if (userAndFriendInfoUnsaved.usersList.size > 1 && userAndFriendInfoUnsaved.usersList[1].phone != null) {
            phone2   = userAndFriendInfoUnsaved.usersList[1].phone!!
            network2 = userAndFriendInfoUnsaved.usersList[1].network!!
        }
        else {
            phone2   = null
            network2 = null
        }

        val locationUser: LocationUser =

            LocationUser(
                uid = userId!!,
                created = created,
                name = userAndFriendInfoUnsaved.usersList[0].name!!,
                email = userAndFriendInfoUnsaved.usersList[0].email!!,
                phone = userAndFriendInfoUnsaved.usersList[0].phone!!,
                network = userAndFriendInfoUnsaved.usersList[0].network!!,
                phone2 = phone2,
                network2 = network2,
                longitude = longitude,
                latitude = latitude,
                address = address!!,
                city = city!!,
                state = statee!!,
                country = country!!,
                postalCode = postalCode!!,
                knownName = knownName!!,
            )

        db
            .collection("locations")
            .document(userId!!)
            .set(locationUser)
            .addOnSuccessListener {

                db
                    .collection("users")
                    .document(userId!!)
                    .set(userAndFriendInfoUnsaved)
                    .addOnSuccessListener {



                    }
                    .addOnFailureListener { e ->

                        mProgressBar.visibility = View.GONE
                        mProgressBarLocation.visibility = View.GONE

                        val message: String = "Error saving details: " + e.message
                        util.onShowErrorMessage(message, context)
                        Log.e(TAG, "Error saving", e)

                    }


            }
            .addOnFailureListener { e ->

                mProgressBar.visibility = View.GONE
                mProgressBarLocation.visibility = View.GONE

                val message: String = "Error saving details: " + e.message
                util.onShowErrorMessage(message, context)
                Log.e(TAG, "Error saving", e)

            }

    }*/

    private   fun emailVerificationCountDown(duration: Long){
        Handler().postDelayed({
            object : CountDownTimer(duration, 500) {
                override fun onTick(millisUntilFinished: kotlin.Long) {
                    KToasty.info(
                            context,
                            "Please go to email: $email and verify your address to continue. Thank you.",
                            Toast.LENGTH_LONG,
                            true
                    ).show()
                }

                override fun onFinish() {
                    //finish();
                }
            }.start()
        }, 500)
    }

    private   fun locationOnWaitCountDown(duration: kotlin.Long) {

        Handler().postDelayed({
            object : CountDownTimer(duration, 500) {
                override fun onTick(millisUntilFinished: kotlin.Long) {
                    KToasty.info(
                            context,
                            "Please enable location in settings.",
                            Toast.LENGTH_SHORT,
                            true
                    ).show()
                }

                override fun onFinish() {}

            }.start()

        }, 3000)

    }



    public  override  fun onDestroy(){
        super.onDestroy()

        if (!registrationComplete) {

            if (firebaseUser != null){

                FirebaseAuth.getInstance().signOut()

                firebaseUser!!.delete().addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        KToasty.info(context, "You have successfully deleted your account.", Toast.LENGTH_LONG).show()

                    }
                    else {

                        KToasty.error(context, "Error: " + task.exception, Toast.LENGTH_LONG)
                                .show()

                    }
                }

            }

        }

    }

    override  fun onBackPressed(){
        //super.onBackPressed();
    }

    private fun goToMain() {

        val intent: Intent = Intent(context, MainActivity::class.java)
        startActivity(intent)
        finish()

    }

    private fun goToLogin() {

        val intent: Intent = Intent(context, LoginActivity::class.java)
        startActivity(intent)
        finish()

    }

    fun onAlreadyRegistered(view: View?){
        onBackPressed()
    }

    fun openPolicy(view: View?){

        val url: String = "https://rechargeprivacypolicy.herokuapp.com"
        val i: Intent = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }

//    fun openTerms(view: View?) {
//
//        val url: String = "https://rechargeprivacypolicy.herokuapp.com"
//        val i: Intent = Intent(Intent.ACTION_VIEW)
//        i.data = android.net.Uri.parse(url)
//        startActivity(i)
//
//    }

    companion object  {

        private val PERMISSION_READ_PHONE_STATE: Int = 18
        private val PERMISSION_CALL_PHONE: Int = 19
        private val REQUEST_LOCATION_PERMISSION_BEGINNING: Int = 10
        private val REQUEST_LOCATION_PERMISSION: Int = 20
        private val REQUEST_LOCATION_PERMISSIONCHECK: Int = 21
        private val REQUEST_CHECK_SETTINGS: Int = 214
        private val REQUEST_ENABLE_GPS: Int = 416
        private val PICK_CONTACT_REQUEST: Int = 500

        fun startActivity(context: Context){
            context.startActivity(Intent(context, RegisterDetailsActivity::class.java))
        }

    }

//    override fun contactPicked(data: Intent?, index: Int) {
//
//
//    }


}
