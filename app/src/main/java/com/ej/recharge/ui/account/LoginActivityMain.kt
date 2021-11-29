package com.ej.recharge.ui.account

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ej.recharge.R
import com.google.firebase.auth.*
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import java.util.*


class LoginActivityMain : AppCompatActivity() {

  private lateinit var activity: Activity
  private lateinit var context: Context

  private val TAG = LoginActivityMain::class.java.simpleName

  private var phoneButton: TextView? = null
  private var emailButton: TextView? = null

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

    phoneButton = findViewById<TextView>(R.id.phone_button)
    emailButton = findViewById<TextView>(R.id.email_button)

    val loginLinearLayout = findViewById<LinearLayout>(R.id.login_ll)

    val aniFade: Animation = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in)

    aniFade.setAnimationListener(object : AnimationListener {

      override fun onAnimationStart(animation: Animation) {
        phoneButton!!.isEnabled = false
        emailButton!!.isEnabled = false
      }

      override fun onAnimationRepeat(animation: Animation) {}

      override fun onAnimationEnd(animation: Animation) {
        phoneButton!!.isEnabled = true
        emailButton!!.isEnabled = true
      }

    })

    loginLinearLayout.startAnimation(aniFade)

    context = this
    activity = this

    phoneButton!!.setOnClickListener {

      val intent = Intent(this@LoginActivityMain, LoginActivityPhone::class.java)
      intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
      startActivity(intent)
      finish()

    }

    emailButton!!.setOnClickListener {

      val intent = Intent(this@LoginActivityMain, LoginActivityEmail::class.java)
      intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
      startActivity(intent)
      finish()

    }


  }



}
