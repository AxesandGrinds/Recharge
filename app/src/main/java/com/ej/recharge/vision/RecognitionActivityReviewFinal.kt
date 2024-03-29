/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ej.recharge.vision

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.ej.recharge.R
import com.ej.recharge.utils.PhoneUtil
import com.ej.recharge.utils.Util
//import com.facebook.ads.Ad
//import com.facebook.ads.AdError
//import com.facebook.ads.AdListener
//import com.facebook.ads.AdSize
//import com.facebook.ads.AdView
import com.google.android.gms.ads.*
import com.ironsource.mediationsdk.IronSourceBannerLayout
import com.mopub.mobileads.MoPubView

/**
 * Demo app chooser which takes care of runtime permission requesting and allow you pick from all
 * available testing Activities.
 */
class RecognitionActivityReviewFinal :
  AppCompatActivity(),
  ActivityCompat.OnRequestPermissionsResultCallback,
  OnItemClickListener {


  private lateinit var pAccount: String
  private lateinit var phone: String
  private lateinit var network: String
  private lateinit var code: String


  companion object {

    private const val TAG = "RecActivityReviewFinal"
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

    if (successful) {
      phoneUtil.simpleRecharge(this,  this, pAccount, code, ussdStarter)
      Log.e(TAG, "${network}: Recharge Started")
      finish()
    }
    else {
      val message: String = "Error Recharging Phone!"
      Log.e(TAG, "${network}: Error Recharging Phone!")
      util.onShowErrorMessage(message, this)
      finish()
    }
    
  }

  private val util: Util = Util()
  private val phoneUtil: PhoneUtil = PhoneUtil()


  private fun recharge2() {

    val lastDigit: Char = code[code.length - 1]
    val isDigit: Boolean = Character.isDigit(lastDigit)

    Log.e(TAG, "$network: from vision to key")

    when (network) {

      "Airtel" -> {

        if ((code.length == 19 || code.length == 14) && isDigit) {
        } else if (isDigit) {
        }

      }
      "Etisalat(9Mobile)" -> {

        if ((code.length == 19 || code.length == 15) && isDigit) {
        } else if (isDigit) {
        }

      }
      "Glo Mobile" -> {

        if (code.length == 17 && isDigit) {
        } else if (isDigit) {
        }

      }
      "MTN Nigeria" -> {

        if ((code.length == 19 ||
                  code.length == 14 ||
                  code.length == 12 ||
                  code.length == 11) && isDigit
        ) {
        } else if (isDigit) {
        }

      }
      "Visafone" -> {

        if (code.length == 19 && isDigit) {
        } else if (isDigit) {
        }

      }

      else -> println("no match")

    }




  }





  @SuppressLint("MissingPermission")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.vision_activity_review)

    util.addFontToAppBarTitle(supportActionBar!!, applicationContext)

    val intent = intent
    pAccount = intent.getStringExtra("pAccount")!!
    phone    = intent.getStringExtra("phone")!!
    network  = intent.getStringExtra("network")!!
    code     = intent.getStringExtra("code")!!

    val crossCheckEt = findViewById<EditText>(R.id.cross_check_et)

    crossCheckEt.setOnClickListener {}

    code = code.replace("-", "")
    code = code.replace(" ", "")
    code = code.replace("[^0-9]".toRegex(), "")
    crossCheckEt.setText(code)

    val doneButton = findViewById<Button>(R.id.done_btn)

    doneButton.setOnClickListener {

      code = crossCheckEt.text.toString()
      code = code.replace("-", "")
      code = code.replace(" ", "")
      code = code.replace("[^0-9]".toRegex(), "")
      runRecharge()

    }

//    MyMoPub().init(this, adUnit)

//    Handler(Looper.getMainLooper()).postDelayed({
//    }, 200)

//    initFBAds()

    initAdmob()



  }

  private fun initAdmob() {

    // https://developers.google.com/admob/android/quick-start?hl=en-US#kotlin
    // https://developers.google.com/admob/android/banner
    // https://developers.google.com/ad-manager/mobile-ads-sdk/android/banner
    MobileAds.initialize(this) {

    }

//    // TODO Remove For Release vvv
//    val testDeviceIds: List<String> = listOf("6638E8A228E3CC5D4711029B8808E246") // listOf("78D47CB8E8C50C8391083ABA46D59A17")
//    val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
//    MobileAds.setRequestConfiguration(configuration)
//    // TODO Remove For Release ^^^

    val mAdView: AdView = findViewById(R.id.rra_adView)

    mAdView.adListener = object : AdListener() {

      override fun onAdLoaded() {
        // Code to be executed when an ad finishes loading.
        Log.e(TAG, "RecognitionActivityReviewFinal onAdLoaded")
      }

      override fun onAdFailedToLoad(adError: LoadAdError) {
        // Code to be executed when an ad request fails.
        Log.e(TAG, "RecognitionActivityReviewFinal onAdFailedToLoad: ${adError.toString()}")
      }

      override fun onAdOpened() {
        // Code to be executed when an ad opens an overlay that
        // covers the screen.
        Log.e(TAG, "RecognitionActivityReviewFinal onAdOpened")
      }

      override fun onAdClicked() {
        // Code to be executed when the user clicks on an ad.
        Log.e(TAG, "RecognitionActivityReviewFinal onAdClicked")
      }

      override fun onAdImpression() {
        super.onAdImpression()
      }

      override fun onAdClosed() {
        // Code to be executed when the user is about to return
        // to the app after tapping on an ad.
        Log.e(TAG, "RecognitionActivityReviewFinal onAdClosed")
      }

    }

    val adRequest = AdRequest.Builder().build()
    mAdView.loadAd(adRequest)

  }

  var moPubView: MoPubView? = null

  private val adUnit: String = "b1685e583a854f458cd3cf948b67c40a"
  private val debugAdUnit: String = "b195f8dd8ded45fe847ad89ed1d016da"

//  private fun initAds() {
//
//    moPubView = findViewById(R.id.rra_moPubView)
//
//    moPubView!!.setAdUnitId(adUnit); // Enter your Ad Unit ID from www.mopub.com
////        moPubView.adSize = MoPubAdSize // Call this if you are not setting the ad size in XML or wish to use an ad size other than what has been set in the XML. Note that multiple calls to `setAdSize()` will override one another, and the MoPub SDK only considers the most recent one.
////        moPubView.loadAd(MoPubAdSize) // Call this if you are not calling setAdSize() or setting the size in XML, or if you are using the ad size that has not already been set through either setAdSize() or in the XML
//
//    moPubView!!.bannerAdListener = object : MoPubView.BannerAdListener {
//
//      override fun onBannerLoaded(banner: MoPubView) {
//        Log.e(TAG, "RecognitionActivityReviewFinal onBannerLoaded")
//      }
//
//      override fun onBannerFailed(banner: MoPubView?, error: MoPubErrorCode?) {
//        Log.e(TAG, "RecognitionActivityReviewFinal onBannerFailed: ${error.toString()}")
//      }
//
//      override fun onBannerClicked(banner: MoPubView?) {
//        Log.e(TAG, "RecognitionActivityReviewFinal onBannerClicked")
//      }
//
//      override fun onBannerExpanded(banner: MoPubView?) {
//        Log.e(TAG, "RecognitionActivityReviewFinal onBannerExpanded")
//      }
//
//      override fun onBannerCollapsed(banner: MoPubView?) {
//        Log.e(TAG, "RecognitionActivityReviewFinal onBannerCollapsed")
//      }
//
//    }
//
//    moPubView!!.loadAd()
//
//  }

  private var adView: AdView? = null

  private lateinit var ironSourceBannerLayout: IronSourceBannerLayout

//  private fun initFBAds() {
//
//    IronSource.setMetaData("Facebook_IS_CacheFlag","ALL")
//
//    var facebookAdsRefreshRate: Int = 0
//    var facebookAdsRemoved: Boolean = false
//
//    val bannerContainer: FrameLayout = findViewById<FrameLayout>(R.id.ironsSource_rra_banner_container)
//
//    val adListener: AdListener = object : AdListener {
//
//      override fun onError(ad: Ad?, adError: AdError) {
//
//        Log.e(TAG, "RecognitionActivityReviewFinal onBannerFailed: ${adError.errorMessage}")
//
////        Toast.makeText(
////          this@RecognitionActivityReviewFinal,
////          "Ad Error: " + adError.errorMessage,
////          Toast.LENGTH_LONG
////        ).show()
//
//        this@RecognitionActivityReviewFinal.runOnUiThread {
//
//          Runnable {
//            bannerContainer.removeView(adView)
//            facebookAdsRemoved = true
//          }
//
//        }
//
//      }
//
//      override fun onAdLoaded(ad: Ad?) {
//        Log.e(TAG, "RecognitionActivityReviewFinal onBannerLoaded")
//
//        this@RecognitionActivityReviewFinal.runOnUiThread {
//
//          Runnable {
//
//            if (facebookAdsRefreshRate > 0 && facebookAdsRemoved) {
//              bannerContainer.addView(adView)
//              facebookAdsRemoved = false
//            }
//
//            bannerContainer.removeView(ironSourceBannerLayout)
//
//            facebookAdsRefreshRate++
//
//          }
//
//        }
//
//      }
//
//      override fun onAdClicked(ad: Ad?) {
//        Log.e(TAG, "RecognitionActivityReviewFinal onBannerClicked")
//      }
//
//      override fun onLoggingImpression(ad: Ad?) {
//        // Ad impression logged callback
//      }
//
//    }
//
////    adView = AdView(requireContext(), "IMG_16_9_APP_INSTALL#411762013708850_411801847038200", AdSize.BANNER_HEIGHT_50)
////    val adContainer: LinearLayout = findViewById<LinearLayout>(R.id.rra_banner)
////    adContainer.addView(adView)
//
//    adView = AdView(this@RecognitionActivityReviewFinal, "411762013708850_411801847038200", AdSize.BANNER_HEIGHT_50)
//
//    IronSource.init(this, ironSourceAppKey, IronSource.AD_UNIT.BANNER)
//
//    ironSourceBannerLayout = IronSource.createBanner(this, ISBannerSize.BANNER)
//
//    val layoutParams: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
//      FrameLayout.LayoutParams.MATCH_PARENT,
//      FrameLayout.LayoutParams.WRAP_CONTENT
//    )
//
//    bannerContainer.addView(adView)
//
//    bannerContainer.addView(ironSourceBannerLayout, 0, layoutParams)
//
//    var ironSourceRefreshRate: Int = 0
//    var ironSourceRemoved: Boolean = false
//
//    ironSourceBannerLayout.bannerListener = object : BannerListener {
//
//      override fun onBannerAdLoaded() {
//        // Called after a banner ad has been successfully loaded
//
//        this@RecognitionActivityReviewFinal.runOnUiThread {
//
//          Runnable {
//
//            if (ironSourceRefreshRate > 0 && ironSourceRemoved) {
//              bannerContainer.addView(ironSourceBannerLayout, 0, layoutParams)
//              ironSourceRemoved = false
//            }
//
//            bannerContainer.removeView(adView)
//
//            ironSourceRefreshRate++
//
//          }
//
//        }
//
//      }
//
//      override fun onBannerAdLoadFailed(error: IronSourceError) {
//        // Called after a banner has attempted to load an ad but failed.
//
//        this@RecognitionActivityReviewFinal.runOnUiThread {
//
//          Runnable {
//            bannerContainer.removeView(ironSourceBannerLayout)
//            ironSourceRemoved = true
////                        bannerContainer.removeAllViews()
//          }
//
//        }
//
//      }
//
//      override fun onBannerAdClicked() {
//        // Called after a banner has been clicked.
//      }
//
//      override fun onBannerAdScreenPresented() {
//        // Called when a banner is about to present a full screen content.
//      }
//
//      override fun onBannerAdScreenDismissed() {
//        // Called after a full screen content has been dismissed
//      }
//
//      override fun onBannerAdLeftApplication() {
//        // Called when a user would be taken out of the application context.
//      }
//
//    }
//
//    IronSource.loadBanner(ironSourceBannerLayout)
//
//    adView!!.loadAd()
//
//    adView?.loadAd(adView?.buildLoadAdConfig()?.withAdListener(adListener)?.build())
//
//  }

  override fun onResume() {
    super.onResume()

    Log.e(TAG, "RecognitionActivityReviewFinal onResume Called")

    adView?.resume()

//    IronSource.onResume(this)

//    initFBAds()

  }

  override fun onPause() {
    super.onPause()

    Log.e(TAG, "RecognitionActivityReviewFinal onPause Called")

    adView?.pause()

//    IronSource.onPause(this)

//    IronSource.destroyBanner(ironSourceBannerLayout)

  }

  override fun onDestroy() {

    Log.e(TAG, "RecognitionActivityReviewFinal onDestroy Called")

    adView?.destroy()

    super.onDestroy()

//    IronSource.destroyBanner(ironSourceBannerLayout)

//    moPubView?.destroy()

  }

  private class MyArrayAdapter(
    private val ctx: Context,
    resource: Int,
    private val selectedCodes: Array<String>
  ) : ArrayAdapter<String>(ctx, resource, selectedCodes) {

    private var descriptionIds: IntArray? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

      var view = convertView

      if (convertView == null) {
        val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(android.R.layout.simple_list_item_2, null)
      }

      (view!!.findViewById<View>(android.R.id.text1) as TextView).text = selectedCodes[position]

      descriptionIds?.let {
        (view.findViewById<View>(android.R.id.text2) as TextView).setText(it[position])
      }

      return view

    }

    fun setDescriptionIds(descriptionIds: IntArray) {
      this.descriptionIds = descriptionIds
    }

  }

  override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {

  }










}
