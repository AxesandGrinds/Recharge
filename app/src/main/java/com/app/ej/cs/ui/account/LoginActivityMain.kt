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
import com.firebase.ui.auth.util.ui.SupportVectorDrawablesButton
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
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask

class LoginActivityMain : AppCompatActivity() {

  private lateinit var activity: Activity
  private lateinit var context: Context

  private val TAG = LoginActivityMain::class.java.simpleName




  private var phoneButton: SupportVectorDrawablesButton? = null
  private var emailButton: SupportVectorDrawablesButton? = null



  fun startActivity(context: Context) {
    val intent = Intent(context, LoginActivityMain::class.java)
    context.startActivity(intent)
  }

  override fun attachBaseContext(newBase: Context?) {
    super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!))
  }

  override fun onStart() {
    super.onStart()
  }


  private fun initPhone() {
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

    setContentView(R.layout.activity_login_main)

    phoneButton = findViewById<View>(R.id.phone_button) as SupportVectorDrawablesButton
    emailButton = findViewById<View>(R.id.email_button) as SupportVectorDrawablesButton

    context = this
    activity = this


    phoneButton!!.setOnClickListener {

      val intent = Intent(context, LoginActivityPhone::class.java)
      context.startActivity(intent)

    }

    emailButton!!.setOnClickListener {

      val intent = Intent(context, LoginActivityEmail::class.java)
      context.startActivity(intent)

    }


  }



}
