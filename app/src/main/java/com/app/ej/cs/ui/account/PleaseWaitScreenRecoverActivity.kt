package com.app.ej.cs.ui.account

import android.annotation.SuppressLint
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
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubInterstitial
import java.util.*


class PleaseWaitScreenRecoverActivity : AppCompatActivity(),
  MoPubInterstitial.InterstitialAdListener {

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

    handler.postDelayed(r, 1000)

  }

  private fun showFullScreenAd() {

    if (mInterstitial.isReady) {

      mInterstitial.show()

    }
    else {

      // Caching is likely already in progress if `isReady()` is false.
      // Avoid calling `load()` here and instead rely on the callbacks as suggested below.

      goToRecoverPassword()

    }

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

  private lateinit var mInterstitial: MoPubInterstitial

  @SuppressLint("MissingPermission")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_please_wait_recover)

    mInterstitial = MoPubInterstitial(this, "255d2dc0c68345fa87b1b50c07e059eb")

    mInterstitial.interstitialAdListener = this

    mInterstitial.load()

    startWaitBeforeAd()

//    MobileAds.initialize(this)
//
//    loadInterstitialAd()

  }

  override fun onInterstitialLoaded(interstitial: MoPubInterstitial?) {
    Log.e(TAG, "onInterstitialLoaded")
  }

  override fun onInterstitialFailed(interstitial: MoPubInterstitial?, error: MoPubErrorCode?) {
    Log.e(TAG, "onInterstitialFailed: ${error.toString()}")

    val handler = Handler(Looper.getMainLooper())

    val r = Runnable {

      goToRecoverPassword()

    }

    handler.postDelayed(r, 1000)
  }

  override fun onInterstitialShown(interstitial: MoPubInterstitial?) {
    Log.e(TAG, "onInterstitialShown")
  }

  override fun onInterstitialClicked(interstitial: MoPubInterstitial?) {
    Log.e(TAG, "onInterstitialClicked")
  }

  override fun onInterstitialDismissed(interstitial: MoPubInterstitial?) {

    Log.e(TAG, "onInterstitialDismissed")
    goToRecoverPassword()

  }

  private val EMAIL = "extra_email"

  private val TAG: String = "ATTENTION ATTENTION"

  override fun onDestroy() {
    mInterstitial.destroy()
    super.onDestroy()
  }

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

//  private var mInterstitialAd: InterstitialAd? = null
//
//  private fun loadInterstitialAd() {
//
//    val adRequest = AdRequest.Builder().build()
//
//    InterstitialAd.load(
//      this,
//      "ca-app-pub-5127161627511605/8927614471",
//      adRequest,
//      object : InterstitialAdLoadCallback() {
//
//        override fun onAdFailedToLoad(adError: LoadAdError) {
//
//          Log.d(TAG, adError.message)
//          mInterstitialAd = null
//
//          val handler = Handler(Looper.getMainLooper())
//
//          val r = Runnable {
//
//            goToRecoverPassword()
//
//          }
//
//          handler.postDelayed(r, 800)
//
//        }
//
//        override fun onAdLoaded(interstitialAd: InterstitialAd) {
//
//          mInterstitialAd = interstitialAd
//
//        }
//
//      })
//
//  }
//
//  private fun showFullScreenAd2() {
//
//    if (mInterstitialAd != null) {
//
//      mInterstitialAd!!.fullScreenContentCallback = object: FullScreenContentCallback() {
//
//        override fun onAdDismissedFullScreenContent() {
//
//          Log.d(TAG, "Ad was dismissed.")
//          goToRecoverPassword()
//
//        }
//
//        override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
//
//          Log.d(TAG, "Ad failed to show.")
//          goToRecoverPassword()
//
//        }
//
//        override fun onAdShowedFullScreenContent() {
//
//          Log.d(TAG, "Ad showed fullscreen content.")
//          mInterstitialAd = null
//          goToRecoverPassword()
//
//        }
//
//      }
//
//      mInterstitialAd!!.show(this)
//
//    }
//    else {
//
//      goToRecoverPassword()
//
//    }
//
//  }

}