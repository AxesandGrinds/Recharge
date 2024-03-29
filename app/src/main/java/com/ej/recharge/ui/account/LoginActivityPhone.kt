package com.ej.recharge.ui.account

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.transition.Fade
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ej.recharge.R
import com.ej.recharge.repository.entity.Friend
import com.ej.recharge.repository.entity.User
import com.ej.recharge.repository.entity.UserAndFriendInfo
import com.ej.recharge.ui.MainActivity
import com.ej.recharge.utils.AnimationUtil
import com.ej.recharge.utils.PinEntryEditText
import com.ej.recharge.utils.TimerFlow
import com.ej.recharge.utils.Util
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper
//import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask


class LoginActivityPhone : AppCompatActivity() {

  var activity: Activity? = null
  private lateinit var context: Context

  private val TAG = LoginActivityPhone::class.java.simpleName

  private var phone_number_holder: TextInputLayout? = null
//  private  var verification_code_holder:TextInputLayout? = null

  private lateinit var phone_numberEt: TextInputEditText
//  private lateinit  var verification_codeEt:TextInputEditText
  private lateinit var countdown_tv: TextView
  private lateinit var passcode_explanation_tv: TextView

  private var mDetailText: TextView? = null

  private lateinit var mProgressBar: ProgressBar

  private lateinit var mFirestore: FirebaseFirestore
  private lateinit var mAuth: FirebaseAuth
  private lateinit var mFirebaseUser: FirebaseUser
  private lateinit var myUserId: String

  private lateinit var sendVerificationButton: MaterialButton
  private lateinit var resendVerificationButton:MaterialButton
  private lateinit var loginButton:MaterialButton
  private lateinit var registerButton: MaterialButton

  private val PHONE_VERIFY_REQUEST_CODE = 64206

  private val TAG_PHONE = "Phone Login"

  private val viewListener: View.OnClickListener? = null

  private val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"

  private val STATE_INITIALIZED = 1
  private val STATE_CODE_SENT = 2
  private val STATE_VERIFY_FAILED = 3
  private val STATE_VERIFY_SUCCESS = 4
  private val STATE_SIGNIN_FAILED = 5
  private val STATE_SIGNIN_SUCCESS = 6

  private var mVerificationInProgress = false
  private var mVerificationId: String? = null

  private lateinit var mResendToken: ForceResendingToken
  private lateinit var mCallbacks: OnVerificationStateChangedCallbacks

  private lateinit var phoneNumberUtil: PhoneNumberUtil

  private var util: Util = Util()

  var internationalNumber: String? = null

  private val PREFNAME: String = "local_user"

  private lateinit var e164Number: String

  override fun onBackPressed() {

//    super.onBackPressed()

    Log.e("ATTENTION ATTENTION", "LoginActivityPhone onBackPressed()")

    val intent = Intent(context, LoginActivityMain::class.java)

    startActivity(intent)

    finish()

  }

  fun showLoading() {
    mProgressBar.visibility = View.VISIBLE
  }

  fun dismissLoading() {
    mProgressBar.visibility = View.GONE
  }

  fun startActivity(context: Context) {
    val intent = Intent(context, LoginActivityPhone::class.java)
    context.startActivity(intent)
  }

  override fun attachBaseContext(newBase: Context?) {
    super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!))
  }

  override fun onStart() {
    super.onStart()

    val currentUser = mAuth.currentUser
    updateUI(currentUser)
    if (mVerificationInProgress && validatePhoneNumber()) {
      startPhoneNumberVerification(phone_numberEt.text.toString())
    }
  }


  private fun initPhone() {

    mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

      override fun onVerificationCompleted(credential: PhoneAuthCredential) {

        Log.e(TAG, "onVerificationCompleted:$credential")
        mVerificationInProgress = false
        updateUI(STATE_VERIFY_SUCCESS, credential)
        val timer = Timer()
        timer.schedule(timerTask { signInWithPhoneAuthCredential(credential) }, 1000)

      }

      override fun onVerificationFailed(e: FirebaseException) {

        Log.e(TAG, "onVerificationFailed", e)
        mVerificationInProgress = false

        if (e is FirebaseAuthInvalidCredentialsException) {
//          phone_numberEt.error = "Invalid phone number."
          Log.e("ATTENTION ATTENTION", "FirebaseAuthInvalidCredentialsException: ${e.message}")
          Log.e("ATTENTION ATTENTION", "FirebaseAuthInvalidCredentialsException: Invalid phone number")
        }
        else if (e is FirebaseTooManyRequestsException) {
          Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
            Snackbar.LENGTH_SHORT).show()
        }

        updateUI(STATE_VERIFY_FAILED)

      }

      override fun onCodeSent(
        verificationId: String,
        token: ForceResendingToken
      ) {

        Log.d(TAG, "onCodeSent:$verificationId")
        mVerificationId = verificationId
        mResendToken = token
        updateUI(STATE_CODE_SENT)

      }

    }


  }

  private fun startPhoneNumberVerification(phoneNumber: String) {

    val options = PhoneAuthOptions.newBuilder(mAuth)
      .setPhoneNumber(phoneNumber) // Phone number to verify
      .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
      .setActivity(this) // Activity (for callback binding)
      .setCallbacks(mCallbacks) // OnVerificationStateChangedCallbacks
      .build()
    PhoneAuthProvider.verifyPhoneNumber(options)

    mVerificationInProgress = true

  }

  private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {

    mAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->

      if (task.isSuccessful) {

        Log.d(TAG, "signInWithCredential:success")
        val user = task.result.user
        myUserId = user!!.uid

        user.getIdToken(true).addOnSuccessListener { result ->

          val token_id = result.token

          Log.d(TAG, "GetTokenResult result = $token_id")

          updateUI(STATE_SIGNIN_SUCCESS, user)

          mFirestore
            .collection("locations")
            .document(myUserId)
            .get()
            .addOnSuccessListener { documentSnapshot ->

              val pref = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

              val editor = pref?.edit()
              editor?.putString("token", token_id)
              editor?.apply()


              val uid = documentSnapshot.getString("uid")

              mFirestore
                .collection("users")
                .document(myUserId)
                .get()
                .addOnSuccessListener {

                    userDocumentSnapshot ->

                  val allInfo = userDocumentSnapshot.toObject(UserAndFriendInfo::class.java) ?: UserAndFriendInfo()

                  if (allInfo.usersList.size > 0 && allInfo.usersList[0].uid == myUserId) {

                    val message: String = "Login successful."
                    util.onShowMessage(message, context)
                    readDataFromFirestoreSaveToLocal(userDocumentSnapshot)

                  }
                  else {

                    val message: String = "Requesting more registration details."
                    util.onShowMessage(message, context)
                    requestRegistration()

                  }

                }
                .addOnFailureListener { e ->

                  val message: String = "Requesting more registration details."
                  util.onShowMessage(message, context)

                  requestRegistration()

                  Log.e("Error", ".." + e.message)
                  mProgressBar.visibility = View.GONE

                }

            }
            .addOnFailureListener { e ->

              val message: String = "Requesting more registration details."
              util.onShowMessage(message, context)

              requestRegistration()

              Log.e("Error", ".." + e.message)
              mProgressBar.visibility = View.GONE

            }

        }

      }
      else {

        Log.w(TAG, "signInWithCredential:failure", task.exception)

        if (task.exception is FirebaseAuthInvalidCredentialsException) {

          passcode_explanation_tv.text = "Invalid code."
//          verification_codeEt.error = "Invalid code."

        }

        updateUI(STATE_SIGNIN_FAILED)

      }

    }

  }



  private lateinit var usersList:   MutableList<User>
  private lateinit var friendsList: MutableList<Friend>

  private lateinit var allInfoJsonSaved:   String
  private lateinit var allInfoJsonUnsaved: String

  private lateinit var userAndFriendInfo:      UserAndFriendInfo
  private lateinit var userAndFriendInfoSaved: UserAndFriendInfo

  private val gson = Gson()

  private fun readDataFromFirestoreSaveToLocal(documentSnapshot: DocumentSnapshot) {

    val sharedPref = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

    val editor = sharedPref!!.edit()

    try {

      val userHashMap = documentSnapshot.data

      Log.e("userHashMap", userHashMap.toString())

      val allInfo = documentSnapshot.toObject(UserAndFriendInfo::class.java) ?: UserAndFriendInfo()

      usersList = allInfo.usersList

      if (allInfo.friendList != null) {
        friendsList = allInfo.friendList!!
      }

      allInfoJsonSaved = Gson().toJson(allInfo)

      editor.putString("allInfoSaved", allInfoJsonSaved)
      editor.putString("allInfoUnsaved", allInfoJsonSaved)
      editor.putBoolean("email_verified", true)
      editor.putBoolean("location_received", true)

      editor.apply()

      goToMain()

    }
    catch (ex: Exception) {

      Log.e(TAG, ex.toString())

    }

  }

  private fun requestRegistration() {

    Log.e("ATTENTION ATTENTION", "LoginActivityPhone requestRegistration()")

    RegisterActivity.startActivity(this@LoginActivityPhone)

    finish()

  }

  private fun updateUI(uiState: Int) {
    mAuth!!.currentUser?.let { updateUI(uiState, it, null) }
  }

  private fun updateUI(user: FirebaseUser?) {
    if (user != null) {
      updateUI(STATE_SIGNIN_SUCCESS, user)
    } else {
      updateUI(STATE_INITIALIZED)
    }
  }

  private fun updateUI(uiState: Int, user: FirebaseUser) {
    updateUI(uiState, user, null)
  }

  private fun updateUI(uiState: Int, cred: PhoneAuthCredential) {
    updateUI(uiState, null, cred)
  }

  private fun updateUI(uiState: Int, user: FirebaseUser?, cred: PhoneAuthCredential?) {

    when (uiState) {

      STATE_INITIALIZED -> {
        mDetailText?.text = null
      }
      STATE_CODE_SENT -> {
        mDetailText!!.setText(R.string.status_code_sent)
        mDetailText?.setTextColor(ContextCompat.getColor(this,R.color.green))
      }

      STATE_VERIFY_FAILED -> {
        mDetailText!!.setText(R.string.status_verification_failed)
        mDetailText?.setTextColor(ContextCompat.getColor(this,R.color.red))
      }
      STATE_VERIFY_SUCCESS ->
      {
        if (cred != null) {

          if (cred.smsCode != null) {

            mDetailText!!.text = ""

//            verification_codeEt.setText(cred.smsCode)
            txtPinEntry.setText(cred.smsCode)

          }

        }

      }
      STATE_SIGNIN_FAILED -> {

        mDetailText!!.setText(R.string.status_sign_in_failed)
        mDetailText?.setTextColor(ContextCompat.getColor(this,R.color.red))

      }
      STATE_SIGNIN_SUCCESS -> {

        mDetailText!!.text = ""

      }

    }

  }


  private fun validatePhoneNumber(): Boolean {

    val phoneNumber: String = phone_numberEt.text!!.trim().toString()

    if (TextUtils.isEmpty(phoneNumber) || phoneNumber.length <  10) {

      AnimationUtil.shakeView(phone_numberEt, this)

      phone_numberEt.error = "Invalid phone number."

      Log.e("ATTENTION ATTENTION", "validatePhoneNumber: Invalid phone number")

      return false

    }

    return true

  }

  private fun validateCode(): Boolean {
//    val verificationCode = verification_codeEt!!.text.toString()
    val verificationCode = txtPinEntry.text.toString()
    if (TextUtils.isEmpty(verificationCode)) {
      AnimationUtil.shakeView(txtPinEntry!!, this)
//      verification_codeEt!!.error = "Code is empty."
      txtPinEntry.setText("Code is empty.")
      return false
    }
    return true
  }

  private fun goToMain() {

    Log.e("ATTENTION ATTENTION", "LoginActivityPhone goToMain()")

    val i = Intent(this@LoginActivityPhone, MainActivity::class.java)

    startActivity(i)

    finish()

  }

  @SuppressLint("MissingPermission")
  private fun isOnline(context: Context) : Boolean {

    var result: Boolean = false

    val cm =
      context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

      if (cm != null) {

        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)

        if (capabilities != null) {

          if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            Log.e("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
            return true
          }
          else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            Log.e("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
            return true
          }
          else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            Log.e("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
            return true
          }

        }

      }
      else {

        if (cm != null) {

          val activeNetwork = cm.activeNetworkInfo

          if (activeNetwork != null) {

            if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {

              result = isConnected(context)

            }
            else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {

              result = isConnected(context)

            }

          }

        }

        return result

      }


    }
    return false

  }

  @SuppressLint("MissingPermission")
  private fun isConnected(context: Context): Boolean {

    val cm = context
      .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = cm.activeNetworkInfo

    if (activeNetwork != null && activeNetwork.isConnected) {

      try {

        val url = URL("http://www.google.com/")
        val urlc = url.openConnection() as HttpURLConnection
        urlc.setRequestProperty("User-Agent", "test")
        urlc.setRequestProperty("Connection", "close")
        urlc.connectTimeout = 1000
        urlc.connect()
        return when (urlc.responseCode) {
          200 -> {
            true
          }
          else -> {
            false
          }
        }

      }
      catch (e: IOException) {
        Log.e("Error", "Error checking internet connection", e)
        return false
      }

    }
    else
      return false

  }








  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress)
  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle) {
    super.onRestoreInstanceState(savedInstanceState)
    mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS)
  }

  @ExperimentalCoroutinesApi
  private suspend fun setCountDown(millisInFuture: Long, countDownInterval: Long) {

    TimerFlow.create(millisInFuture, countDownInterval).collect {

      val originalInSeconds = it.toString().take(2)
      var inSeconds: String = Math.ceil(it.toDouble() / 1000).toInt().toString()

      var text: String = ""

      if (inSeconds == "1") {
        text = "Countdown: ${inSeconds} seconds"
      }
      else {
        text = "Countdown: ${inSeconds} second"
      }

      countdown_tv.text = text
      Log.e("ATTENTION ATTENTION", text)

      if (it == 0L) {
        countdown_tv.visibility = View.GONE
        resendVerificationButton.visibility = View.VISIBLE
        loginButton.visibility = View.GONE
        passcode_explanation_tv.visibility = View.GONE
        txtPinEntry.setText("")
        txtPinEntry.visibility = View.GONE
      }

    }
  }

  var passCode: String = ""
  private lateinit var txtPinEntry: PinEntryEditText

//  @ExperimentalCoroutinesApi
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    ViewPump.init(
      ViewPump.builder()
        .addInterceptor(
          CalligraphyInterceptor(
            CalligraphyConfig.Builder()
              .setDefaultFontPath("font/bold.ttf")
//              .setFontAttrId(R.attr.fontPath)
              .build()
          )
        )
        .build()
    )

    requestWindowFeature(Window.FEATURE_NO_TITLE)


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

      window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

      window.statusBarColor = ContextCompat.getColor(this, R.color.white)

    }

    setContentView(R.layout.activity_login_phone)

    mProgressBar            = findViewById<View>(R.id.progress_bar) as ProgressBar
    phone_number_holder     = findViewById<View>(R.id.h_phone) as TextInputLayout
//    verification_code_holder = findViewById<View>(R.id.h_verification_code) as TextInputLayout
    phone_numberEt          = findViewById<View>(R.id.phoneEt) as TextInputEditText
//    verification_codeEt      = findViewById<View>(R.id.verification_codeEt) as TextInputEditText
    sendVerificationButton   = findViewById<View>(R.id.send_verify_button) as MaterialButton
    resendVerificationButton = findViewById<View>(R.id.resend_verify_button) as MaterialButton
    loginButton             = findViewById<View>(R.id.verify_button) as MaterialButton
    countdown_tv            = findViewById<View>(R.id.countdown_tv) as TextView

    passcode_explanation_tv = findViewById<View>(R.id.passcode_explanation_tv) as TextView

    mDetailText = findViewById(R.id.detail)
    context = this@LoginActivityPhone

    FirebaseApp.initializeApp(this)

    mFirestore = Firebase.firestore
    mAuth = Firebase.auth

    mProgressBar.visibility = View.GONE
    mProgressBar.isIndeterminate = true

    txtPinEntry = findViewById<View>(R.id.txt_pin_entry) as PinEntryEditText
    txtPinEntry.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
      override fun afterTextChanged(s: Editable) {
        passCode = s.toString()
      }

    })

    txtPinEntry.visibility = View.GONE

    try {
      val field = TextInputLayout::class.java.getDeclaredField("defaultStrokeColor")
      field.isAccessible = true
      field[phone_number_holder] = ContextCompat.getColor(context, R.color.colorAccent)
//      field[verification_code_holder] = ContextCompat.getColor(context, R.color.colorAccent)
    }
    catch (e: NoSuchFieldException) {
      Log.w("TAG", "Failed to change box color, item might look wrong")
    }
    catch (e: IllegalAccessException) {
      Log.w("TAG", "Failed to change box color, item might look wrong")
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      val fade = Fade()
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        fade.excludeTarget(findViewById<View>(R.id.layout), true)
        fade.excludeTarget(android.R.id.statusBarBackground, true)
        fade.excludeTarget(android.R.id.navigationBarBackground, true)
        window.enterTransition = fade
        window.exitTransition = fade
      }
    }

//    phoneNumberUtil = PhoneNumberUtil.createInstance(context)
    phoneNumberUtil = PhoneNumberUtil.getInstance()

    resendVerificationButton.visibility = View.GONE
    sendVerificationButton.setOnClickListener(View.OnClickListener {

      if (validatePhoneNumber()) {

        try {

          val originalNumber = Objects.requireNonNull(phone_numberEt.text).toString()
          val phoneNumber = phoneNumberUtil.parse(originalNumber, "NG")
          var nationalNumber: String = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
          var internationalNumber = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
          e164Number = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
          val finalLoginNumber: String = retrieveCorrectNumber("0$nationalNumber")
          Log.e("ATTENTION ATTENTION", "originalNumber: $originalNumber")
          Log.e("ATTENTION ATTENTION", "phoneNumber: $phoneNumber")
          Log.e("ATTENTION ATTENTION", "nationalNumber: $nationalNumber")
          Log.e("ATTENTION ATTENTION", "internationalNumber: $internationalNumber")
          Log.e("ATTENTION ATTENTION", "e164Number: $e164Number")
          Log.e("ATTENTION ATTENTION", "finalLoginNumber: $finalLoginNumber")
          startPhoneNumberVerification(originalNumber)

        }
        catch (e: Exception) {

          e.printStackTrace()

        }
//        resendVerificationButton.visibility = View.VISIBLE
        sendVerificationButton.visibility = View.GONE
        countdown_tv.visibility = View.VISIBLE
        loginButton.visibility = View.VISIBLE
        passcode_explanation_tv.visibility = View.VISIBLE
        txtPinEntry.visibility = View.VISIBLE

                CoroutineScope(Dispatchers.Main).launch {
          setCountDown(60000, 1000)
        }

      }

    })

    resendVerificationButton.setOnClickListener {

      if (validatePhoneNumber()) {

        try {

          val originalNumber = Objects.requireNonNull(phone_numberEt.text).toString()
          val phoneNumber = phoneNumberUtil.parse(originalNumber, "NG")
          e164Number = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
          val finalLoginNumber: String = retrieveCorrectNumber(e164Number)

          if (mResendToken != null) {

            resendVerificationCode(originalNumber, mResendToken)

            countdown_tv.visibility = View.VISIBLE
            loginButton.visibility = View.VISIBLE
            passcode_explanation_tv.visibility = View.VISIBLE
            txtPinEntry.visibility = View.VISIBLE

            CoroutineScope(Dispatchers.Main).launch {
              setCountDown(60000, 1000)
            }

          }
          else {

            recreate()

          }

        }
        catch (e: Exception) {

          Log.e("ATTENTION ATTENTION", "Login Activity Resend Code Error: ${e.toString()}")

          e.printStackTrace()

        }

      }

    }


//    loginButton.setOnClickListener {
//      val code = verification_codeEt.text.toString()
//      if (validateCode()) {
//        mVerificationId?.let { it1 -> verifyPhoneNumberWithCode(it1, code) }
//      }
//    }

    loginButton.setOnClickListener {
//      val code = verification_codeEt.text.toString()
      if (validateCode()) {
        mVerificationId?.let { it1 -> verifyPhoneNumberWithCode(it1, passCode) }
      }
    }

    initPhone()

  }

  private fun verifyPhoneNumberWithCode(verificationId: String, code: String) {

    try {
      val credential = PhoneAuthProvider.getCredential(verificationId, code)
      signInWithPhoneAuthCredential(credential)
    }
    catch (e: IllegalArgumentException) {
      e.printStackTrace()
    }

  }

  private fun resendVerificationCode(phoneNumber: String, token: ForceResendingToken) {

    val options = PhoneAuthOptions.newBuilder(mAuth)
      .setPhoneNumber(phoneNumber)
      .setTimeout(60L, TimeUnit.SECONDS)
      .setActivity(this)
      .setCallbacks(mCallbacks)
      .setForceResendingToken(token)
      .build()
    PhoneAuthProvider.verifyPhoneNumber(options)

    mVerificationInProgress = true

  }

  private fun retrieveCorrectNumber(internationalNumber: String?): String {

    var finalNumber: String = ""

    if (internationalNumber!![0] == '0') {

      finalNumber = internationalNumber.replaceFirst("(?:0)+".toRegex(), "+234")

    }
    else if (internationalNumber.contains("+234")) {

      finalNumber = internationalNumber

    }

//    var finalFinalNumber: String = ""
//    finalFinalNumber = "${finalNumber.substring(0,4)} ${finalNumber.substring(4,7)}-${finalNumber.substring(7,10)}-${finalNumber.substring(10,14)}"

    return finalNumber

  }



}
