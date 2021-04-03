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

package com.app.ej.cs.ui

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.app.ej.cs.R
import com.app.ej.cs.utils.PhoneUtil
import com.app.ej.cs.utils.Util
import com.google.android.gms.ads.*

/**
 * Demo app chooser which takes care of runtime permission requesting and allow you pick from all
 * available testing Activities.
 */
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
      utils.onShowErrorMessage(message, this)
    }

    Log.e(TAG, "${network}: $message")
    finish()
    
  }

  private val utils: Util = Util()
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






  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_code_input)

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

    // https://developers.google.com/ad-manager/mobile-ads-sdk/android/banner
    MobileAds.initialize(this) { }


    /// TODO Remove For Release vvv
//    val testDeviceIds: List<String> = listOf("6638E8A228E3CC5D4711029B8808E246") // listOf("78D47CB8E8C50C8391083ABA46D59A17")
//    val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
//    MobileAds.setRequestConfiguration(configuration)
    /// TODO Remove For Release ^^^


    val mAdView: AdView = findViewById(R.id.ciact_adView)

    val adRequest = AdRequest.Builder().build()
    mAdView.loadAd(adRequest)

    mAdView.adListener = object : AdListener() {

      override fun onAdLoaded() {
        // Code to be executed when an ad finishes loading.
      }

      override fun onAdFailedToLoad(adError: LoadAdError) {
        // Code to be executed when an ad request fails.
      }

      override fun onAdOpened() {
        // Code to be executed when an ad opens an overlay that
        // covers the screen.
      }

      override fun onAdClicked() {
        // Code to be executed when the user clicks on an ad.
      }

      override fun onAdLeftApplication() {
        // Code to be executed when the user has left the app.
      }

      override fun onAdClosed() {
        // Code to be executed when the user is about to return
        // to the app after tapping on an ad.
      }

    }



  }










}
