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

package com.app.ej.cs.vision

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.app.ej.cs.R
import com.app.ej.cs.ui.CodeInputActivity
import com.app.ej.cs.utils.PhoneUtil
import com.app.ej.cs.utils.Util
import com.google.android.gms.ads.*
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubView
import java.util.*

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
    crossCheckEt.setText(code)

    val doneButton = findViewById<Button>(R.id.done_btn)

    doneButton.setOnClickListener {

      code = crossCheckEt.text.toString()
      code = code.replace("-", "")
      code = code.replace(" ", "")

      runRecharge()

    }

    initAds()

    // https://developers.google.com/ad-manager/mobile-ads-sdk/android/banner
//    MobileAds.initialize(this)

    /// TODO Remove For Release vvv
//    val testDeviceIds: List<String> = listOf("6638E8A228E3CC5D4711029B8808E246") // listOf("78D47CB8E8C50C8391083ABA46D59A17")
//    val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
//    MobileAds.setRequestConfiguration(configuration)
    /// TODO Remove For Release ^^^

//    val mAdView: AdView = findViewById(R.id.rra_adView)
//
//    val adRequest = AdRequest.Builder().build()
//    mAdView.loadAd(adRequest)
//
//    mAdView.adListener = object : AdListener() {
//
//      override fun onAdLoaded() {
//        // Code to be executed when an ad finishes loading.
//      }
//
//      override fun onAdFailedToLoad(adError: LoadAdError) {
//        // Code to be executed when an ad request fails.
//      }
//
//      override fun onAdOpened() {
//        // Code to be executed when an ad opens an overlay that
//        // covers the screen.
//      }
//
//      override fun onAdClicked() {
//        // Code to be executed when the user clicks on an ad.
//      }
//
////      override fun onAdLeftApplication() {
////        // Code to be executed when the user has left the app.
////      }
//
//      override fun onAdImpression() {
//        super.onAdImpression()
//      }
//
//      override fun onAdClosed() {
//        // Code to be executed when the user is about to return
//        // to the app after tapping on an ad.
//      }
//
//    }



  }

  lateinit var moPubView: MoPubView

  private fun initAds() {

    moPubView = findViewById(R.id.rra_moPubView)

    moPubView.setAdUnitId("b1685e583a854f458cd3cf948b67c40a"); // Enter your Ad Unit ID from www.mopub.com
//        moPubView.adSize = MoPubAdSize // Call this if you are not setting the ad size in XML or wish to use an ad size other than what has been set in the XML. Note that multiple calls to `setAdSize()` will override one another, and the MoPub SDK only considers the most recent one.
//        moPubView.loadAd(MoPubAdSize) // Call this if you are not calling setAdSize() or setting the size in XML, or if you are using the ad size that has not already been set through either setAdSize() or in the XML

    moPubView.bannerAdListener = object : MoPubView.BannerAdListener {

      override fun onBannerLoaded(banner: MoPubView) {
        Log.e(TAG, "RecognitionActivityReviewFinal onBannerLoaded")
      }

      override fun onBannerFailed(banner: MoPubView?, error: MoPubErrorCode?) {
        Log.e(TAG, "RecognitionActivityReviewFinal onBannerFailed: ${error.toString()}")
      }

      override fun onBannerClicked(banner: MoPubView?) {
        Log.e(TAG, "RecognitionActivityReviewFinal onBannerClicked")
      }

      override fun onBannerExpanded(banner: MoPubView?) {
        Log.e(TAG, "RecognitionActivityReviewFinal onBannerExpanded")
      }

      override fun onBannerCollapsed(banner: MoPubView?) {
        Log.e(TAG, "RecognitionActivityReviewFinal onBannerCollapsed")
      }

    }

    moPubView.loadAd()

  }

  override fun onDestroy() {
    super.onDestroy()

    moPubView.destroy()

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
