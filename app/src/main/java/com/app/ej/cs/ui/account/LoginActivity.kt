package com.app.ej.cs.ui.account

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.transition.Fade
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.app.ej.cs.R
import com.app.ej.cs.repository.entity.Friend
import com.app.ej.cs.repository.entity.User
import com.app.ej.cs.repository.entity.UserAndFriendInfo
import com.app.ej.cs.ui.MainActivity
import com.app.ej.cs.utils.AnimationUtil
import com.app.ej.cs.utils.Util
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {


  var activity: Activity? = null
  private lateinit var context: Context

  private val TAG = LoginActivity::class.java.simpleName

  private var phone_number_holder: TextInputLayout? = null
  private  var verification_code_holder:TextInputLayout? = null

  private lateinit var phone_numberEt: TextInputEditText
  private lateinit  var verification_codeEt:TextInputEditText

  private var mDetailText: TextView? = null

  private lateinit var mProgressBar: ProgressBar

  private lateinit var mFirestore: FirebaseFirestore
  private lateinit var auth: FirebaseAuth
  private lateinit var firebaseUser: FirebaseUser
  private lateinit var myUserId: String

  private lateinit var sendVerificationButton: MaterialButton
  private lateinit var resendVerificationButton:MaterialButton
  private lateinit var verifyLoginButton:MaterialButton
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

  fun showLoading() {
    mProgressBar.visibility = View.VISIBLE
  }

  fun dismissLoading() {
    mProgressBar.visibility = View.GONE
  }

  fun startActivity(context: Context) {
    val intent = Intent(context, LoginActivity::class.java)
    context.startActivity(intent)
  }

  override fun attachBaseContext(newBase: Context?) {
    super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!))
  }

  override fun onStart() {
    super.onStart()

    //init();
    val currentUser = auth.currentUser
    updateUI(currentUser)
    if (mVerificationInProgress && validatePhoneNumber()) {
      startPhoneNumberVerification(phone_numberEt.text.toString())
    }
  }

  /*
     * Phone authentication starts here
     * */
  private fun initPhone() {

    mCallbacks = object : OnVerificationStateChangedCallbacks() {

      override fun onVerificationCompleted(credential: PhoneAuthCredential) {

        // This callback will be invoked in two situations:
        // 1 - Instant verification. In some cases the phone number can be instantly
        //     verified without needing to send or enter a verification code.
        // 2 - Auto-retrieval. On some devices Google Play services can automatically
        //     detect the incoming verification SMS and perform verification without
        //     user action.
        Log.d(TAG, "onVerificationCompleted:$credential")
        mVerificationInProgress = false
        signInWithPhoneAuthCredential(credential)
        updateUI(STATE_VERIFY_SUCCESS, credential)

      }

      override fun onVerificationFailed(e: FirebaseException) {

        Log.w(TAG, "onVerificationFailed", e)
        mVerificationInProgress = false

        if (e is FirebaseAuthInvalidCredentialsException) {
          phone_numberEt.error = "Invalid phone number."
        }
        else if (e is FirebaseTooManyRequestsException) {
          // The SMS quota for the project has been exceeded
          Snackbar.make(
            findViewById(android.R.id.content), "Quota exceeded.",
            Snackbar.LENGTH_SHORT
          ).show()
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
    PhoneAuthProvider.getInstance().verifyPhoneNumber(
      phoneNumber,
      60,  // Timeout duration
      TimeUnit.SECONDS,  // Unit of timeout
      this,  // Activity (for callback binding)
      mCallbacks
    ) // OnVerificationStateChangedCallbacks
    mVerificationInProgress = true
  }

  private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {

    auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->

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

              if (uid != null) {

                mFirestore
                        .collection("users")
                        .document(myUserId)
                        .get()
                        .addOnSuccessListener { userDocumentSnapshot ->

                          val message: String = "Login successful."
                          util.onShowMessage(message, context)

                          readDataFromFirestoreSaveToLocal(userDocumentSnapshot)

                          goToMain()

                        }
                        .addOnFailureListener { e ->

                          val message: String = "Requesting more registration details."
                          util.onShowMessage(message, context)

                          requestRegistration()

                          Log.e("Error", ".." + e.message)
                          mProgressBar.visibility = View.GONE

                        }

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

      }
      else {
        Log.w(TAG, "signInWithCredential:failure", task.exception)
        if (task.exception is FirebaseAuthInvalidCredentialsException) {
          verification_codeEt.error = "Invalid code."
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

      editor.apply()

    }
    catch (ex: Exception){
      Log.e(TAG, ex.toString())
    }

  }

  private fun requestRegistration() {

    RegisterActivity.startActivity(this@LoginActivity)
    finish()
  }

  private fun updateUI(uiState: Int) {
    auth!!.currentUser?.let { updateUI(uiState, it, null) }
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

      STATE_INITIALIZED -> mDetailText?.text = null
      STATE_CODE_SENT -> mDetailText!!.setText(R.string.status_code_sent)
      STATE_VERIFY_FAILED -> mDetailText!!.setText(R.string.status_verification_failed)

      STATE_VERIFY_SUCCESS ->

        if (cred != null) {
          if (cred.smsCode != null) {
            verification_codeEt.setText(cred.smsCode)
          }
        }

      STATE_SIGNIN_FAILED -> mDetailText!!.setText(R.string.status_sign_in_failed)
      STATE_SIGNIN_SUCCESS -> {
      }

    }

  }


  private fun validatePhoneNumber(): Boolean {
    val phoneNumber = phone_numberEt!!.text.toString()
    if (TextUtils.isEmpty(phoneNumber)) {
      AnimationUtil.shakeView(phone_numberEt, this)
      phone_numberEt.error = "Invalid phone number."
      return false
    }
    return true
  }

  private fun validateCode(): Boolean {
    val verificationCode = verification_codeEt!!.text.toString()
    if (TextUtils.isEmpty(verificationCode)) {
      AnimationUtil.shakeView(verification_codeEt!!, this)
      verification_codeEt!!.error = "Code is empty."
      return false
    }
    return true
  }

  private fun goToMain() {
    val i = Intent(this@LoginActivity, MainActivity::class.java)
    startActivity(i)
    finish()
  }

  private fun isOnline(context: Context) : Boolean {

    var result: Boolean = false

    val cm =
      context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

      if (cm != null) {

        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)

        if (capabilities != null) {

          if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
            return true
          }
          else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
            return true
          }
          else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
            return true
          }

        }

      }
      else {

        if (cm != null) {

          val activeNetwork = cm.activeNetworkInfo

          if (activeNetwork != null) {

            // connected to the internet
            if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {

              //result = true;
              //result = canAccessInternet();
              result = isConnected(context)

            }
            else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {

              //result = true;
              //result = canAccessInternet();
              result = isConnected(context)

            }

          }

        }

        return result

      }


    }
    return false

  }

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
        urlc.connectTimeout = 1000 // mTimeout is in seconds
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
        Log.i("Error", "Error checking internet connection", e)
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


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    ViewPump.init(
      ViewPump.builder()
        .addInterceptor(
          CalligraphyInterceptor(
            CalligraphyConfig.Builder()
              .setDefaultFontPath("fonts/bold.ttf")
              .setFontAttrId(R.attr.fontPath)
              .build()
          )
        )
        .build()
    )

    requestWindowFeature(Window.FEATURE_NO_TITLE)
    window.setFlags(
      WindowManager.LayoutParams.FLAG_FULLSCREEN,
      WindowManager.LayoutParams.FLAG_FULLSCREEN
    )

    setContentView(R.layout.activity_login_final)


    mProgressBar             = findViewById<View>(R.id.mProgressBar) as ProgressBar
    phone_number_holder      = findViewById<View>(R.id.h_phone) as TextInputLayout
    verification_code_holder = findViewById<View>(R.id.h_verification_code) as TextInputLayout
    phone_numberEt           = findViewById<View>(R.id.phoneEt) as TextInputEditText
    verification_codeEt      = findViewById<View>(R.id.verification_codeEt) as TextInputEditText
    sendVerificationButton   = findViewById<View>(R.id.send_verify_button) as MaterialButton
    resendVerificationButton = findViewById<View>(R.id.resend_verify_button) as MaterialButton
    verifyLoginButton        = findViewById<View>(R.id.verify_button) as MaterialButton
    //registerButton           = (MaterialButton) findViewById(R.id.register_button);

    //registerButton.setVisibility(View.GONE);
    mDetailText = findViewById(R.id.detail)
    context = this@LoginActivity
    mFirestore = Firebase.firestore
    auth = Firebase.auth

    //askPermission();

    mProgressBar.visibility = View.GONE
    mProgressBar.isIndeterminate = true

    try {
      val field = TextInputLayout::class.java.getDeclaredField("defaultStrokeColor")
      field.isAccessible = true
      field[phone_number_holder] = ContextCompat.getColor(context, R.color.colorAccent)
      field[verification_code_holder] = ContextCompat.getColor(context, R.color.colorAccent)
    } catch (e: NoSuchFieldException) {
      Log.w("TAG", "Failed to change box color, item might look wrong")
    } catch (e: IllegalAccessException) {
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

    phoneNumberUtil = PhoneNumberUtil.createInstance(context)

    resendVerificationButton.visibility = View.GONE
    sendVerificationButton.setOnClickListener(View.OnClickListener {

      if (validatePhoneNumber()) {

        try {

          val originalNumber = Objects.requireNonNull(phone_numberEt.text).toString()
          val phoneNumber = phoneNumberUtil.parse(originalNumber, "NG")
          e164Number = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
          val finalLoginNumber: String = retrieveCorrectNumber(e164Number)
          Log.i("ATTENTION ATTENTION", "e164Number: $e164Number")
          Log.i("ATTENTION ATTENTION", "finalLoginNumber: $finalLoginNumber")
          startPhoneNumberVerification(finalLoginNumber)

//          Log.i("ATTENTION ATTENTION", "phone_numberEt.getText().toString(): " + originalNumber);
//          String previousInternationalNumberString = util.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
//          Log.i("ATTENTION ATTENTION", "e164Number = util.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);");
//          Log.i("ATTENTION ATTENTION", "e164Number: " + e164Number);
//          Log.i("ATTENTION ATTENTION", "internationalNumber = util.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);");
//          Log.i("ATTENTION ATTENTION", "previousInternationalNumberString: " + previousInternationalNumberString);

        } catch (e: NumberParseException) {
          e.printStackTrace()
        }
        resendVerificationButton.visibility = View.VISIBLE
        sendVerificationButton.visibility = View.GONE
      }
    })
    resendVerificationButton.setOnClickListener {
      if (validatePhoneNumber()) {
        try {
          val originalNumber = Objects.requireNonNull(phone_numberEt.text).toString()
          val phoneNumber = phoneNumberUtil.parse(originalNumber, "NG")
          e164Number = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
          val finalLoginNumber: String = retrieveCorrectNumber(e164Number)
          resendVerificationCode(finalLoginNumber, mResendToken)
        } catch (e: NumberParseException) {
          e.printStackTrace()
        }
      }
    }
    verifyLoginButton.setOnClickListener {
      val code = verification_codeEt.text.toString()
      if (validateCode()) {
        mVerificationId?.let { it1 -> verifyPhoneNumberWithCode(it1, code) }
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

    val options = PhoneAuthOptions.newBuilder(auth)
      .setPhoneNumber(phoneNumber)       // Phone number to verify
      .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
      .setActivity(this)                 // Activity (for callback binding)
      .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
      .setForceResendingToken(token)     // ForceResendingToken from callbacks
      .build()
    PhoneAuthProvider.verifyPhoneNumber(options)

  }

  private fun retrieveCorrectNumber(internationalNumber: String?): String {

    var finalNumber: String = ""

    if (internationalNumber!![0] == '0') {

      //finalNumber = "+234" + internationalNumber.substring(1);
      finalNumber = internationalNumber.replaceFirst("(?:0)+".toRegex(), "+234")

    }
    else if (internationalNumber.contains("+234")) {

      finalNumber = internationalNumber

    }

    return finalNumber

  }



}
