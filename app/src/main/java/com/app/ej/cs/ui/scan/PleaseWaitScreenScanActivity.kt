package com.app.ej.cs.ui.scan

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.app.ej.cs.BuildConfig
import com.app.ej.cs.R
import com.app.ej.cs.ui.MainActivity
import com.app.ej.cs.ui.MyMoPub
import com.app.ej.cs.ui.account.RecoverPasswordActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubInterstitial
import java.util.*

class PleaseWaitScreenScanActivity : AppCompatActivity(),
  MoPubInterstitial.InterstitialAdListener {

  fun init() {

    hideSystemUI()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      window.attributes.layoutInDisplayCutoutMode =
        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }

  }

  override fun onInterstitialLoaded(interstitial: MoPubInterstitial?) {
    Log.e(TAG, "PleaseWaitScreenScanActivity onInterstitialLoaded")
  }

  override fun onInterstitialFailed(interstitial: MoPubInterstitial?, error: MoPubErrorCode?) {
    Log.e(TAG, "PleaseWaitScreenScanActivity onInterstitialFailed: ${error.toString()}")

    val handler = Handler(Looper.getMainLooper())

    val r = Runnable {

      goToMain()

    }

    handler.postDelayed(r, 1000)

  }

  override fun onInterstitialShown(interstitial: MoPubInterstitial?) {
    Log.e(TAG, "PleaseWaitScreenScanActivity onInterstitialShown")
  }

  override fun onInterstitialClicked(interstitial: MoPubInterstitial?) {
    Log.e(TAG, "PleaseWaitScreenScanActivity onInterstitialClicked")
  }

  override fun onInterstitialDismissed(interstitial: MoPubInterstitial?) {

    Log.e(TAG, "PleaseWaitScreenScanActivity onInterstitialDismissed")
    goToMain()

  }

  private fun startWaitBeforeAd() {

    val handler = Handler(Looper.getMainLooper())

    val r = Runnable {

      showFullScreenAd()

    }

    handler.postDelayed(r, 1000)

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

  private val adUnit: String = "5ee244d94ff9448c8be9096bca4e0be4"
  private val debugAdUnit: String = "24534e1901884e398f1253216226017e"

  private fun initAds() {

    mInterstitial = MoPubInterstitial(activity, adUnit)

    mInterstitial!!.interstitialAdListener = this

    mInterstitial!!.load()

    startWaitBeforeAd()

  }

  override fun onDestroy() {

    if (mInterstitial != null) {
      mInterstitial!!.destroy()
    }

    super.onDestroy()

  }

  lateinit var context: Context
  lateinit var activity: Activity

  private var mInterstitial: MoPubInterstitial? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_please_wait_scan)

    context = this
    activity = this

    MyMoPub().init(this, adUnit)

    Handler(Looper.getMainLooper()).postDelayed({
      initAds()
    }, 200)



//    MobileAds.initialize(this)
//
//    loadInterstitialAd()

  }

  private val TAG: String = "ATTENTION ATTENTION"

  val PREFNAME: String = "local_user"

  private fun runAdTimeUpdate() {

    val sharedPref = getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
    val weeklyInterstitialAd = sharedPref.getInt("weeklyInterstitialAd", 0)

    val thisDate: Date = Calendar.getInstance().time

    val editor = sharedPref!!.edit()
    editor.putInt("weeklyInterstitialAd", weeklyInterstitialAd + 1)
    editor.putString("lastWeekDateTime", thisDate.toString())
    editor.apply()

  }

  private fun goToMain() {

    runAdTimeUpdate()

    val intent = Intent(this@PleaseWaitScreenScanActivity, MainActivity::class.java)

    startActivity(intent)
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    finish()

  }

  private var mInterstitialAd: InterstitialAd? = null

  private fun loadInterstitialAd() {

    val adRequest = AdRequest.Builder().build()

    InterstitialAd.load(
      this,
      "ca-app-pub-5127161627511605/6554467118",
      adRequest,
      object : InterstitialAdLoadCallback() {

        override fun onAdFailedToLoad(adError: LoadAdError) {

          Log.d(TAG, adError.message)
          mInterstitialAd = null

          val handler = Handler(Looper.getMainLooper())

          val r = Runnable {

            goToMain()

          }

          handler.postDelayed(r, 800)

        }

        override fun onAdLoaded(interstitialAd: InterstitialAd) {

          mInterstitialAd = interstitialAd

        }

      })

  }

  private fun showFullScreenAd() {

    if (mInterstitial!!.isReady) {

      mInterstitial!!.show()

    }
    else {

      // Caching is likely already in progress if `isReady()` is false.
      // Avoid calling `load()` here and instead rely on the callbacks as suggested below.

      goToMain()

    }

  }

//  private fun showFullScreenAd2() {
//
//    if (mInterstitialAd != null) {
//
//      mInterstitialAd!!.fullScreenContentCallback = object: FullScreenContentCallback() {
//
//        override fun onAdDismissedFullScreenContent() {
//
//          Log.d(TAG, "Ad was dismissed.")
//          goToMain()
//
//        }
//
//        override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
//
//          Log.d(TAG, "Ad failed to show.")
//          goToMain()
//
//        }
//
//        override fun onAdShowedFullScreenContent() {
//
//          Log.d(TAG, "Ad showed fullscreen content.")
//          mInterstitialAd = null
//          goToMain()
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
//      goToMain()
//
//    }
//
//  }
  
}