package com.ej.recharge.ui.home

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
import com.ej.recharge.R
import com.ej.recharge.ui.MainActivity
import com.ej.recharge.utils.ironSourceAppKey
import com.facebook.ads.InterstitialAd
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.logger.IronSourceError
import com.ironsource.mediationsdk.sdk.InterstitialListener
import java.util.*


class PleaseWaitFBScreenHomeActivity : AppCompatActivity() {

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

    if (IronSource.isInterstitialReady() && !hasShown) {

      hasShown = true
      IronSource.showInterstitial("FSFullscreen")

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

  /*private fun initFBAdsOld() {

    IronSource.setMetaData("Facebook_IS_CacheFlag","ALL");

    mInterstitial = InterstitialAd(this, "411762013708850_411801403704911")

    val interstitialAdListener: InterstitialAdListener = object : InterstitialAdListener {

      override fun onInterstitialDisplayed(ad: Ad?) {
        Log.e(TAG, "PleaseWaitFBScreenHomeActivity Interstitial ad displayed.")
      }

      override fun onInterstitialDismissed(ad: Ad?) {
        Log.e(TAG, "PleaseWaitFBScreenHomeActivity Interstitial ad dismissed.")
        goToMain()
      }

      override fun onError(ad: Ad?, adError: AdError) {

        Log.e(TAG, "PleaseWaitFBScreenHomeActivity Interstitial ad failed to load: " + adError.errorMessage)

        val handler = Handler(Looper.getMainLooper())

        val r = Runnable {

          goToMain()

        }

        handler.postDelayed(r, 500)

      }

      override fun onAdLoaded(ad: Ad?) {

        Log.d(TAG, "PleaseWaitFBScreenHomeActivity Interstitial ad is loaded and ready to be displayed!")

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
        Log.d(TAG, "PleaseWaitFBScreenHomeActivity Interstitial ad clicked!")
      }

      override fun onLoggingImpression(ad: Ad?) {
        Log.d(TAG, "PleaseWaitFBScreenHomeActivity Interstitial ad impression logged!")
      }

    }

    mInterstitial?.loadAd(mInterstitial?.buildLoadAdConfig()?.withAdListener(interstitialAdListener)?.build())

    startWaitBeforeAd()

  }*/

  private fun initFBAds() {

    IronSource.setMetaData("Facebook_IS_CacheFlag","ALL")

    IronSource.init(this, ironSourceAppKey, IronSource.AD_UNIT.INTERSTITIAL)

    val ironSourceInterstitialListener: InterstitialListener = object : InterstitialListener {

      /**
       * Invoked when Interstitial Ad is ready to be shown after load function was called.
       */
      override fun onInterstitialAdReady() {
        Log.d(TAG, "PleaseWaitFBScreenHomeActivity Interstitial Ad Ready!")

        if (waitComplete) {

          hasShown = true
          IronSource.showInterstitial("FSFullscreen")

        }
        else {

          Handler(Looper.getMainLooper()).postDelayed({

            if (!hasShown) {

              hasShown = true
              waitComplete = true
              IronSource.showInterstitial("FSFullscreen")

            }

          }, 1000)

        }

      }

      /**
       * invoked when there is no Interstitial Ad available after calling load function.
       */
      override fun onInterstitialAdLoadFailed(error: IronSourceError) {
        Log.d(TAG, "PleaseWaitFBScreenHomeActivity Interstitial Ad Load Failed: " + error.errorMessage)

        val handler = Handler(Looper.getMainLooper())

        val r = Runnable {

          goToMain()

        }

        handler.postDelayed(r, 500)

      }

      /**
       * Invoked when the Interstitial Ad Unit is opened
       */
      override fun onInterstitialAdOpened() {
        Log.d(TAG, "PleaseWaitFBScreenHomeActivity Interstitial Ad Opened!")

      }

      /*
       * Invoked when the ad is closed and the user is about to return to the application.
       */
      override fun onInterstitialAdClosed() {
        Log.d(TAG, "PleaseWaitFBScreenHomeActivity Interstitial Ad Closed!")
        goToMain()
      }

      /**
       * Invoked when Interstitial ad failed to show.
       * @param error - An object which represents the reason of showInterstitial failure.
       */
      override fun onInterstitialAdShowFailed(error: IronSourceError) {
        Log.d(TAG, "PleaseWaitFBScreenHomeActivity Interstitial Ad Show Failed!: " + error.errorMessage)

        val handler = Handler(Looper.getMainLooper())

        val r = Runnable {

          goToMain()

        }

        handler.postDelayed(r, 500)

      }

      /*
       * Invoked when the end user clicked on the interstitial ad, for supported networks only.
       */
      override fun onInterstitialAdClicked() {
        Log.d(TAG, "PleaseWaitFBScreenHomeActivity Interstitial Ad Clicked!")

      }

      /** Invoked right before the Interstitial screen is about to open.
       *  NOTE - This event is available only for some of the networks.
       *  You should NOT treat this event as an interstitial impression, but rather use InterstitialAdOpenedEvent
       */
      override fun onInterstitialAdShowSucceeded() {
        Log.d(TAG, "PleaseWaitFBScreenHomeActivity Interstitial Ad Show Succeeded!")

      }

    }

    IronSource.setInterstitialListener(ironSourceInterstitialListener)

    IronSource.loadInterstitial()

    startWaitBeforeAd()

  }

  var hasShown: Boolean = false
  var waitComplete: Boolean = false

  lateinit var context: Context
  lateinit var activity: Activity

  //  @SuppressLint("MissingPermission")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_please_wait_home)

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

    val intent = Intent(this@PleaseWaitFBScreenHomeActivity, MainActivity::class.java)

    startActivity(intent)
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    finish()

  }


}