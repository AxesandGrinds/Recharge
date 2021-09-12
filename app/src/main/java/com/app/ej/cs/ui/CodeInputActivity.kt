package com.app.ej.cs.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.app.ej.cs.R
import com.app.ej.cs.utils.PhoneUtil
import com.app.ej.cs.utils.Util
import com.app.ej.cs.utils.ironSourceAppKey
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdListener
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.google.android.gms.ads.*
import com.ironsource.mediationsdk.ISBannerSize
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.IronSourceBannerLayout
import com.ironsource.mediationsdk.logger.IronSourceError
import com.ironsource.mediationsdk.sdk.BannerListener
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubView

class CodeInputActivity : AppCompatActivity(),
  ActivityCompat.OnRequestPermissionsResultCallback {


  private lateinit var pAccount: String
  private lateinit var phone: String
  private lateinit var network: String
  private lateinit var code: String


  companion object {

    private const val TAG = "CodeInputActivity"
    private const val PERMISSION_REQUESTS = 1

  }


  private fun runRecharge() {

    var successful: Boolean = true
    val ussdStarter: String =

    when (network) {

      "Airtel" -> {
        
        "126"

      }
      "Etisalat(9Mobile)" -> {

        "222"

      }
      "Glo Mobile" -> {

        "123"

      }
      "MTN Nigeria" -> {

        "555"

      }
      "Visafone" -> {

        "889"
        
      }
      else -> {

        successful = false
        ""

      }
      
    }

    var message: String = "Recharge Started"
    
    if (successful) {
      phoneUtil.simpleRecharge(this,  this, pAccount, code, ussdStarter)
    }
    else {
      message = "Error Recharging Phone!"
      util.onShowErrorMessage(message, this)
    }

    Log.e(TAG, "${network}: $message")
    finish()
    
  }

  private val util: Util = Util()
  private val phoneUtil: PhoneUtil = PhoneUtil()


  private fun recharge2() {

    val lastDigit: Char = code[code.length - 1]
    val isDigit: Boolean = Character.isDigit(lastDigit)

    Log.e(TAG, "$network: from vision to key")

    when (network) {

      "Airtel" -> {

        if ((code.length == 19 || code.length == 14) && isDigit) { }
        else if (isDigit) { }

      }
      "Etisalat(9Mobile)" -> {

        if ((code.length == 19 || code.length == 15) && isDigit) { }
        else if (isDigit) { }

      }
      "Glo Mobile" -> {

        if (code.length == 17 && isDigit) { }
        else if (isDigit) { }

      }
      "MTN Nigeria" -> {

        if ((code.length == 19 ||
                  code.length == 14 ||
                  code.length == 12 ||
                  code.length == 11) && isDigit
        ) { }
        else if (isDigit) { }

      }
      "Visafone" -> {

        if (code.length == 19 && isDigit) { }
        else if (isDigit) { }

      }

      else -> println("no match")

    }

  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_code_input)

    util.addFontToAppBarTitle(supportActionBar!!, applicationContext)

    val intent = intent
    pAccount = intent.getStringExtra("pAccount")!!
    phone    = intent.getStringExtra("phone")!!
    network  = intent.getStringExtra("network")!!

    val enterCodeEt = findViewById<EditText>(R.id.enter_recharge_code_et)
    val doneButton = findViewById<Button>(R.id.done_btn)

    doneButton.setOnClickListener {

      code = enterCodeEt.text.toString()
      code = code.replace("-", "")
      code = code.replace(" ", "")

      runRecharge()

    }

//    MyMoPub().init(this, adUnit)

//    Handler(Looper.getMainLooper()).postDelayed({
//    }, 200)


    bannerContainer = findViewById<FrameLayout>(R.id.ironsSource_ciact_banner_container)

    initFBAds()

//    MobileAds.initialize(this)
//
//    val mAdView: AdView = findViewById(R.id.ciact_adView)
//
//    val adRequest = AdRequest.Builder().build()
//    mAdView.loadAd(adRequest)
//
//    mAdView.adListener = object : AdListener() {
//
//      override fun onAdLoaded() {}
//
//      override fun onAdFailedToLoad(adError: LoadAdError) {}
//
//      override fun onAdOpened() {}
//
//      override fun onAdClicked() {}
//
//      override fun onAdImpression() {
//        super.onAdImpression()
//      }
//
//      override fun onAdClosed() {}
//
//    }

  }

  var moPubView: MoPubView? = null

  private val adUnit: String = "bfcaf3b320c7442fb7dd51b3dde50f68"
  private val debugAdUnit: String = "b195f8dd8ded45fe847ad89ed1d016da"

  private fun initAds() {

    moPubView = findViewById(R.id.ciact_moPubView)

    moPubView!!.setAdUnitId(adUnit); // Enter your Ad Unit ID from www.mopub.com
//        moPubView.adSize = MoPubAdSize // Call this if you are not setting the ad size in XML or wish to use an ad size other than what has been set in the XML. Note that multiple calls to `setAdSize()` will override one another, and the MoPub SDK only considers the most recent one.
//        moPubView.loadAd(MoPubAdSize) // Call this if you are not calling setAdSize() or setting the size in XML, or if you are using the ad size that has not already been set through either setAdSize() or in the XML

    moPubView!!.bannerAdListener = object : MoPubView.BannerAdListener {

      override fun onBannerLoaded(banner: MoPubView) {
        Log.e(TAG, "CodeInputActivity onBannerLoaded")
      }

      override fun onBannerFailed(banner: MoPubView?, error: MoPubErrorCode?) {
        Log.e(TAG, "CodeInputActivity onBannerFailed: ${error.toString()}")
      }

      override fun onBannerClicked(banner: MoPubView?) {
        Log.e(TAG, "CodeInputActivity onBannerClicked")
      }

      override fun onBannerExpanded(banner: MoPubView?) {
        Log.e(TAG, "CodeInputActivity onBannerExpanded")
      }

      override fun onBannerCollapsed(banner: MoPubView?) {
        Log.e(TAG, "CodeInputActivity onBannerCollapsed")
      }

    }

    moPubView!!.loadAd()

  }

  private var adView: AdView? = null

  private lateinit var ironSourceBannerLayout: IronSourceBannerLayout

  private lateinit var bannerContainer: FrameLayout

  private fun initFBAds() {

    IronSource.setMetaData("Facebook_IS_CacheFlag","ALL")

    var facebookAdsRefreshRate: Int = 0
    var facebookAdsRemoved: Boolean = false

    val adListener: AdListener = object : AdListener {

      override fun onError(ad: Ad?, adError: AdError) {

        Log.e(TAG, "CodeInputActivity FB Banner onError: ${adError.errorMessage}")

//        Toast.makeText(
//          this@CodeInputActivity,
//          "Ad Error: " + adError.errorMessage,
//          Toast.LENGTH_LONG
//        ).show()

        this@CodeInputActivity.runOnUiThread {

          Runnable {
            bannerContainer.removeView(adView)
            facebookAdsRemoved = true
          }

        }

      }

      override fun onAdLoaded(ad: Ad?) {
        Log.e(TAG, "CodeInputActivity onBannerLoaded")

        this@CodeInputActivity.runOnUiThread {

          Runnable {

            if (facebookAdsRefreshRate > 0 && facebookAdsRemoved) {
              bannerContainer.addView(adView)
              facebookAdsRemoved = false
            }

            bannerContainer.removeView(ironSourceBannerLayout)

            facebookAdsRefreshRate++

          }

        }

      }

      override fun onAdClicked(ad: Ad?) {
        Log.e(TAG, "CodeInputActivity onBannerClicked")
      }

      override fun onLoggingImpression(ad: Ad?) {
        // Ad impression logged callback
      }

    }

//    adView = AdView(requireContext(), "IMG_16_9_APP_INSTALL#411762013708850_411801847038200", AdSize.BANNER_HEIGHT_50)
//    val adContainer: LinearLayout = findViewById<LinearLayout>(R.id.ciact_banner)
//    adContainer.addView(adView)

    adView = AdView(this@CodeInputActivity, "411762013708850_411801847038200", AdSize.BANNER_HEIGHT_50)

    IronSource.init(this, ironSourceAppKey, IronSource.AD_UNIT.BANNER)

    ironSourceBannerLayout = IronSource.createBanner(this, ISBannerSize.BANNER)

    val layoutParams: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
      FrameLayout.LayoutParams.MATCH_PARENT,
      FrameLayout.LayoutParams.WRAP_CONTENT
    )

    bannerContainer.addView(adView)

    bannerContainer.addView(ironSourceBannerLayout, 0, layoutParams)

    var ironSourceRefreshRate: Int = 0
    var ironSourceRemoved: Boolean = false

    ironSourceBannerLayout.bannerListener = object : BannerListener {

      override fun onBannerAdLoaded() {
        // Called after a banner ad has been successfully loaded

        Log.e(TAG, "CodeInputActivity onBannerAdLoaded")

        this@CodeInputActivity.runOnUiThread {

          Runnable {

            if (ironSourceRefreshRate > 0 && ironSourceRemoved) {
              bannerContainer.addView(ironSourceBannerLayout, 0, layoutParams)
              ironSourceRemoved = false
            }

            bannerContainer.removeView(adView)

            ironSourceRefreshRate++

          }

        }

      }

      override fun onBannerAdLoadFailed(error: IronSourceError) {
        // Called after a banner has attempted to load an ad but failed.

        Log.e(TAG, "CodeInputActivity onBannerFailed: ${error.errorMessage}")

        this@CodeInputActivity.runOnUiThread {

          Runnable {
            bannerContainer.removeView(ironSourceBannerLayout)
            ironSourceRemoved = true
//            bannerContainer.removeAllViews()
          }

        }

      }

      override fun onBannerAdClicked() {
        // Called after a banner has been clicked.
      }

      override fun onBannerAdScreenPresented() {
        // Called when a banner is about to present a full screen content.
      }

      override fun onBannerAdScreenDismissed() {
        // Called after a full screen content has been dismissed
      }

      override fun onBannerAdLeftApplication() {
        // Called when a user would be taken out of the application context.
      }

    }

    IronSource.loadBanner(ironSourceBannerLayout)

    adView!!.loadAd()

    adView?.loadAd(adView?.buildLoadAdConfig()?.withAdListener(adListener)?.build())

  }

  override fun onResume() {
    super.onResume()

    Log.e(TAG, "CodeInputActivity onResume Called")

    IronSource.onResume(this)

    initFBAds()

  }

  override fun onPause() {
    super.onPause()

    Log.e(TAG, "CodeInputActivity onPause Called")

    IronSource.onPause(this)

    IronSource.destroyBanner(ironSourceBannerLayout)

  }

  override fun onDestroy() {

    IronSource.destroyBanner(ironSourceBannerLayout)

    super.onDestroy()

    moPubView?.destroy()

    adView?.destroy()

    Log.e(TAG, "CodeInputActivity onDestroy Called")

  }

}
