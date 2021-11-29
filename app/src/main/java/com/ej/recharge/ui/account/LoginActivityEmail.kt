package com.ej.recharge.ui.account

import android.annotation.SuppressLint
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ej.recharge.R
import com.ej.recharge.repository.entity.Friend
import com.ej.recharge.repository.entity.User
import com.ej.recharge.repository.entity.UserAndFriendInfo
import com.ej.recharge.ui.MainActivity
import com.ej.recharge.utils.AnimationUtil
import com.ej.recharge.utils.Util
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.*
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
import kotlinx.android.synthetic.main.activity_login_email.*
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class LoginActivityEmail : AppCompatActivity() {

  var activity: Activity? = null
  private lateinit var context: Context

  private val TAG = LoginActivityEmail::class.java.simpleName

  private var emailHolder: TextInputLayout? = null
  private  var passwordHolder:TextInputLayout? = null

  private lateinit var emailEt: TextInputEditText
  private lateinit  var passwordEt:TextInputEditText

  private var mDetailText: TextView? = null

  private lateinit var mProgressBar: ProgressBar

  private lateinit var mFirestore: FirebaseFirestore
  private lateinit var auth: FirebaseAuth
  private lateinit var firebaseUser: FirebaseUser
  private lateinit var myUserId: String

  private lateinit var signInButton: MaterialButton
  private lateinit var registerButton: MaterialButton
  private lateinit var forgotPasswordButton: MaterialButton

  private val STATE_INITIALIZED = 1
  private val STATE_VERIFY_FAILED = 2
  private val STATE_VERIFY_SUCCESS = 3
  private val STATE_SIGNIN_FAILED = 4
  private val STATE_SIGNIN_SUCCESS = 5


  private var util: Util = Util()

  private val PREFNAME: String = "local_user"

  private lateinit var e164Number: String

  override fun onBackPressed() {

//    super.onBackPressed()

    Log.e("ATTENTION ATTENTION", "LoginActivityEmail onBackPressed()")

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
    val intent = Intent(context, LoginActivityEmail::class.java)
    context.startActivity(intent)
  }

  override fun attachBaseContext(newBase: Context?) {
    super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!))
  }

  override fun onStart() {
    super.onStart()

    val currentUser = auth.currentUser
    updateUI(currentUser)

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

    Log.e("ATTENTION ATTENTION", "LoginActivityEmail requestRegistration()")

    RegisterActivity.startActivity(this@LoginActivityEmail)

    finish()

  }

  private fun updateUI(uiState: Int) {
    auth!!.currentUser?.let { updateUI(uiState, it, null) }
  }

  private fun updateUI(user: FirebaseUser?) {

    if (user != null) {
      updateUI(STATE_SIGNIN_SUCCESS, user)
    }
    else {
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

      STATE_VERIFY_FAILED -> {

        mDetailText!!.setText(R.string.status_sign_in_password_incorrect)
        mDetailText?.setTextColor(ContextCompat.getColor(this,R.color.red))

      }
      STATE_VERIFY_SUCCESS ->
      {

        mDetailText!!.setText(R.string.status_sign_in_success)
        mDetailText?.setTextColor(ContextCompat.getColor(this,R.color.green))

      }
      STATE_SIGNIN_FAILED -> {

        mDetailText!!.setText(R.string.status_sign_in_failed)
        mDetailText?.setTextColor(ContextCompat.getColor(this,R.color.red))

      }
      STATE_SIGNIN_SUCCESS -> {

        mDetailText!!.setText(R.string.status_sign_in_success)
        mDetailText?.setTextColor(ContextCompat.getColor(this,R.color.green))

      }

    }

  }

  private fun validateEmail(): Boolean {

    val email: String = emailEt.text!!.trim().toString()

    if (TextUtils.isEmpty(email)) {

      AnimationUtil.shakeView(emailEt, this)

      emailEt.error = "Invalid email."

      return false

    }

    return true

  }

  private fun validatePassword(): Boolean {

    val password: String = passwordEt.text!!.toString()

    if (TextUtils.isEmpty(password) || password.length < 6) {

      AnimationUtil.shakeView(passwordEt, this)

      passwordEt.error = "Invalid password."

      return false

    }

    return true

  }


  private fun goToMain() {

    Log.e("ATTENTION ATTENTION", "LoginActivityEmail goToMain()")

    val i = Intent(this@LoginActivityEmail, MainActivity::class.java)

    startActivity(i)

    finish()

  }

  @SuppressLint("MissingPermission")
  private fun isOnline(context: Context) : Boolean {

    var result: Boolean = false

    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

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
  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle) {
    super.onRestoreInstanceState(savedInstanceState)
  }


  override fun onCreate(savedInstanceState: Bundle?) {
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

    requestWindowFeature(Window.FEATURE_NO_TITLE)


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

      window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

      window.statusBarColor = ContextCompat.getColor(this, R.color.white)

    }

    setContentView(R.layout.activity_login_email)

    MobileAds.initialize(this)

    /// TODO Remove For Release vvv
//    val testDeviceIds: List<String> = listOf("E9DEDC61204CFB33008E54C7F35245C8")
//    val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
//    MobileAds.setRequestConfiguration(configuration)
    /// TODO Remove For Release ^^^

    mProgressBar   = findViewById<View>(R.id.progress_bar) as ProgressBar
    emailHolder    = findViewById<View>(R.id.h_email) as TextInputLayout
    passwordHolder = findViewById<View>(R.id.h_password) as TextInputLayout
    emailEt        = findViewById<View>(R.id.emailEt) as TextInputEditText
    passwordEt     = findViewById<View>(R.id.passwordEt) as TextInputEditText
    mDetailText    = findViewById(R.id.detail)

    signInButton         = findViewById<View>(R.id.sign_in_email_btn) as MaterialButton
    registerButton       = findViewById<View>(R.id.register_btn) as MaterialButton
    forgotPasswordButton = findViewById<View>(R.id.forgot_password_btn) as MaterialButton

    context = this@LoginActivityEmail
    activity = this
    mFirestore = Firebase.firestore
    auth = Firebase.auth

    mProgressBar.isIndeterminate = true
    mProgressBar.visibility = View.GONE

    try {

      val field = TextInputLayout::class.java.getDeclaredField("defaultStrokeColor")
      field.isAccessible = true
      field[emailHolder] = ContextCompat.getColor(context, R.color.colorAccent)
      field[passwordHolder] = ContextCompat.getColor(context, R.color.colorAccent)

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

    signInButton.setOnClickListener {

      if (validateEmail() && validatePassword()) {

        try {

          attemptSignIn()

        }
        catch (e: Exception) {

          e.printStackTrace()

        }

      }

    }

    registerButton.setOnClickListener{

      val message: String = "Requesting more registration details."
      util.onShowMessage(message, context)
      requestRegistration()

    }

    forgotPasswordButton.setOnClickListener{

      goToPleaseWaitBeforeRecoverPassword()

    }

  }

  private fun goToPleaseWaitBeforeRecoverPassword() {

    val intent = Intent(context, PleaseWaitFBScreenRecoverActivity::class.java)

    if (emailEt.text?.trim().toString() != "") {

      intent.putExtra(EMAIL, emailEt.text?.trim().toString())

    }

    context.startActivity(intent)

  }

  private fun goToRecoverPassword() {

    val intent = Intent(this@LoginActivityEmail, RecoverPasswordActivity::class.java)

    if (emailEt.text?.trim().toString() != "") {

      intent.putExtra(EMAIL, emailEt.text?.trim().toString())

    }

    startActivity(intent)
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    finish()

  }

  val EMAIL = "extra_email"

  private fun attemptSignIn() {

    signIn(emailEt.text?.trim().toString(), passwordEt.text.toString())

  }

  private fun signIn(email: String, password: String) {

    Log.d(TAG, "signIn:$email")


    mProgressBar.visibility = View.VISIBLE

    auth.signInWithEmailAndPassword(email, password)

      .addOnCompleteListener(activity!!) {

          task ->

        if (task.isSuccessful) {

          Log.d(TAG, "signInWithEmail:success")

          val user = auth.currentUser

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

                mFirestore
                  .collection("users")
                  .document(myUserId)
                  .get()
                  .addOnSuccessListener {

                      userDocumentSnapshot ->

                    val allInfo = userDocumentSnapshot.toObject(UserAndFriendInfo::class.java) ?: UserAndFriendInfo()

                    if (allInfo.usersList.size > 0 && allInfo.usersList[0].uid == myUserId) {

                      editor?.putBoolean("isPasswordAccount", true)
                      editor?.apply()

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

          Log.e(TAG, "signInWithEmail:failure", task.exception)

          if (task.exception is FirebaseAuthInvalidCredentialsException) {

            Toast.makeText(context, "Authentication failed.",
              Toast.LENGTH_SHORT).show()

          }

          updateUI(STATE_SIGNIN_FAILED)

        }

        if (!task.isSuccessful) {

          mDetailText?.text = context.getString(R.string.auth_failed)

        }

        mProgressBar.visibility = View.GONE

      }

  }

  private fun signOut() {
    auth.signOut()
    updateUI(null)
  }


}
