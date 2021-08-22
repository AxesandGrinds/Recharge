package com.app.ej.cs.ui.account

import android.annotation.SuppressLint
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
import com.app.ej.cs.R
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener
import com.ironsource.mediationsdk.IronSource
import java.util.*


class PleaseWaitFBScreenRecoverActivity : AppCompatActivity() {

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

    waitComplete = true

    if (mInterstitial!!.isAdLoaded && !hasShown) {

      hasShown = true
      mInterstitial!!.show()

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

  private var mInterstitial: InterstitialAd? = null

  private fun initFBAds() {

    IronSource.setMetaData("Facebook_IS_CacheFlag","ALL");

    mInterstitial = InterstitialAd(this, "411762013708850_411802753704776")

    val interstitialAdListener: InterstitialAdListener = object : InterstitialAdListener {

      override fun onInterstitialDisplayed(ad: Ad?) {
        Log.e(TAG, "PleaseWaitFBScreenRecoverActivity Interstitial ad displayed.")
      }

      override fun onInterstitialDismissed(ad: Ad?) {
        Log.e(TAG, "PleaseWaitFBScreenRecoverActivity Interstitial ad dismissed.")
        goToRecoverPassword()
      }

      override fun onError(ad: Ad?, adError: AdError) {

        Log.e(TAG, "PleaseWaitFBScreenRecoverActivity Interstitial ad failed to load: " + adError.errorMessage)

        val handler = Handler(Looper.getMainLooper())

        val r = Runnable {

          goToRecoverPassword()

        }

        handler.postDelayed(r, 500)

      }

      override fun onAdLoaded(ad: Ad?) {

        Log.d(TAG, "PleaseWaitFBScreenRecoverActivity Interstitial ad is loaded and ready to be displayed!")

        if (waitComplete) {

          hasShown = true
          mInterstitial?.show()

        }
        else {

          Handler(Looper.getMainLooper()).postDelayed({

            if (!hasShown) {
              hasShown = true
              waitComplete = true
              mInterstitial?.show()
            }

          }, 1000)

        }

      }

      override fun onAdClicked(ad: Ad?) {
        Log.d(TAG, "PleaseWaitFBScreenRecoverActivity Interstitial ad clicked!")
      }

      override fun onLoggingImpression(ad: Ad?) {
        Log.d(TAG, "PleaseWaitFBScreenRecoverActivity Interstitial ad impression logged!")
      }

    }

    mInterstitial?.loadAd(mInterstitial?.buildLoadAdConfig()?.withAdListener(interstitialAdListener)?.build())

    startWaitBeforeAd()

  }

  var hasShown: Boolean = false
  var waitComplete: Boolean = false

  lateinit var context: Context
  lateinit var activity: Activity

//  @SuppressLint("MissingPermission")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_please_wait_recover)

    context = this
    activity = this

//    Handler(Looper.getMainLooper()).postDelayed({
//    }, 200)

  initFBAds()

  }

  private val EMAIL = "extra_email"

  private val TAG: String = "ATTENTION ATTENTION"

  override fun onDestroy() {

    if (mInterstitial != null) {
      mInterstitial!!.destroy()
    }

    super.onDestroy()

  }

  private fun goToRecoverPassword() {

    val email: String? = intent.getStringExtra(EMAIL)

    val intent = Intent(this@PleaseWaitFBScreenRecoverActivity, RecoverPasswordActivity::class.java)

    if (email != null) {

      intent.putExtra(EMAIL, email)

    }

    startActivity(intent)
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    finish()

  }


}