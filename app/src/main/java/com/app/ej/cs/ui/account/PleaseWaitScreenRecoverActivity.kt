package com.app.ej.cs.ui.account

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.app.ej.cs.R
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule

class PleaseWaitScreenRecoverActivity : AppCompatActivity() {

  fun init() {

    hideSystemUI()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      window.attributes.layoutInDisplayCutoutMode =
        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }

  }

  private fun startWaitBeforeAd() {

    val handler = Handler(Looper.getMainLooper())

    val r = Runnable {

      showFullScreenAd()

    }

    handler.postDelayed(r, 800)

  }

  private fun hideSystemUI() {

    window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
      WindowManager.LayoutParams.FLAG_FULLSCREEN)
    supportActionBar?.hide()

    window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LOW_PROFILE
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_IMMERSIVE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

  }

  override fun onStart() {
    super.onStart()

    init()

  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_please_wait_recover)

    MobileAds.initialize(this)

    loadInterstitialAd()

    startWaitBeforeAd()

  }

  private val EMAIL = "extra_email"

  private val TAG: String = "ATTENTION ATTENTION"

  private fun goToRecoverPassword() {

    val email: String? = intent.getStringExtra(EMAIL)

    val intent = Intent(this@PleaseWaitScreenRecoverActivity, RecoverPasswordActivity::class.java)

    if (email != null) {

      intent.putExtra(EMAIL, email)

    }

    startActivity(intent)
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    finish()

  }

  private var mInterstitialAd: InterstitialAd? = null

  private fun loadInterstitialAd() {

    val adRequest = AdRequest.Builder().build()

    InterstitialAd.load(
      this,
      "ca-app-pub-5127161627511605/8927614471",
      adRequest,
      object : InterstitialAdLoadCallback() {

        override fun onAdFailedToLoad(adError: LoadAdError) {

          Log.d(TAG, adError.message)
          mInterstitialAd = null

          val handler = Handler(Looper.getMainLooper())

          val r = Runnable {

            goToRecoverPassword()

          }

          handler.postDelayed(r, 800)

        }

        override fun onAdLoaded(interstitialAd: InterstitialAd) {

          mInterstitialAd = interstitialAd

        }

      })

  }

  private fun showFullScreenAd() {

    if (mInterstitialAd != null) {

      mInterstitialAd!!.fullScreenContentCallback = object: FullScreenContentCallback() {

        override fun onAdDismissedFullScreenContent() {

          Log.d(TAG, "Ad was dismissed.")
          goToRecoverPassword()

        }

        override fun onAdFailedToShowFullScreenContent(adError: AdError?) {

          Log.d(TAG, "Ad failed to show.")
          goToRecoverPassword()

        }

        override fun onAdShowedFullScreenContent() {

          Log.d(TAG, "Ad showed fullscreen content.")
          mInterstitialAd = null
          goToRecoverPassword()

        }

      }

      mInterstitialAd!!.show(this)

    }
    else {

      goToRecoverPassword()

    }

  }

}