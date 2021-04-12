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

    MobileAds.initialize(this)

    val mAdView: AdView = findViewById(R.id.ciact_adView)

    val adRequest = AdRequest.Builder().build()
    mAdView.loadAd(adRequest)

    mAdView.adListener = object : AdListener() {

      override fun onAdLoaded() {}

      override fun onAdFailedToLoad(adError: LoadAdError) {}

      override fun onAdOpened() {}

      override fun onAdClicked() {}

      override fun onAdImpression() {
        super.onAdImpression()
      }

      override fun onAdClosed() {}

    }

  }

}
