package com.ej.recharge.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.ej.recharge.R
import com.ej.recharge.ui.account.LoginActivityMain
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.concurrent.TimeUnit

class SplashScreenActivity : AppCompatActivity() {

  fun init() {

    hideSystemUI()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      window.attributes.layoutInDisplayCutoutMode =
        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }

  }

  private lateinit var context: Context
  private val PREFNAME: String = "local_user"


  private fun goToLogin(context: Context) {
    val intent = Intent(context, LoginActivityMain::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
    finish()
  }

  private fun goToLoginCountDown(duration: Long) {

    GlobalScope.launch(Dispatchers.Main) {
      val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(duration)
      val tickSeconds = 1
      for (second in totalSeconds downTo tickSeconds) {

        delay(1000)

      }

      CoroutineScope(Dispatchers.Main).launch {

        goToLogin(context)

      }


    }

  }

  private fun runSplash() {

    val handler = Handler(Looper.getMainLooper())

    val r = Runnable {

      val prefs = context.getSharedPreferences(PREFNAME, MODE_PRIVATE)
      val registeredFullyEmail    = prefs.getBoolean("email_verified", false)
      val registeredFullyLocation = prefs.getBoolean("location_received", false)

      if (!registeredFullyEmail && !registeredFullyLocation) {

        goToLoginCountDown(0)

      }
      else {

        startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()

      }

    }

    handler.postDelayed(r, 1000)

  }

  private fun hideSystemUI() {

    requestWindowFeature(Window.FEATURE_NO_TITLE)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

      window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

      window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary3)

    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

//      window.setDecorFitsSystemWindows(false)

      WindowCompat.setDecorFitsSystemWindows(window, false)
      WindowInsetsControllerCompat(window, window.decorView).let { controller ->
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
      }

    }
    else {

      window.decorView.systemUiVisibility = (
              View.SYSTEM_UI_FLAG_FULLSCREEN
                      or View.SYSTEM_UI_FLAG_LOW_PROFILE
                      or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                      or View.SYSTEM_UI_FLAG_IMMERSIVE
                      or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                      or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

    }

  }

  override fun onStart() {
    super.onStart()
//    init()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    init()

    context   = this

    runSplash()

  }

}