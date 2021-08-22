package com.app.ej.cs.ui.account

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.app.ej.cs.R
import com.app.ej.cs.ui.MyMoPub
import com.app.ej.cs.utils.ImeHelper
import com.app.ej.cs.utils.NetworkUtil
import com.app.ej.cs.utils.Util
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdListener
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.google.android.gms.ads.*
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ironsource.mediationsdk.IronSource
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubView


class RecoverPasswordActivity:  AppCompatActivity(), View.OnClickListener,
    ImeHelper.DonePressedListener {

    override fun onClick(view: View?) {

        if (view?.id == R.id.button_done) {

            onDonePressed()

        }

    }

    private lateinit var auth: FirebaseAuth

    private var firebaseUser: FirebaseUser? = null
    private var userId:       String? = null

    private val util: Util = Util()

    var mProgressBar: ProgressBar? = null
    var mSubmitButton: Button? = null
    var mEmailInputLayout: TextInputLayout? = null
    var mEmailEditText: EditText? = null
    var mEmailFieldValidator: EmailFieldValidator? = null

    private val EMAIL = "extra_email"

    private val networkUtil: NetworkUtil = NetworkUtil()

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_forgot_password)

        util.addFontToAppBarTitle(supportActionBar!!, applicationContext)

        mProgressBar = findViewById<ProgressBar>(R.id.top_progress_bar)
        mSubmitButton = findViewById<Button>(R.id.button_done)
        mEmailInputLayout = findViewById<TextInputLayout>(R.id.email_layout)
        mEmailEditText = findViewById<EditText>(R.id.email)
        mEmailFieldValidator = EmailFieldValidator(mEmailInputLayout)

        val email: String? = intent.getStringExtra(EMAIL)

        if (email != null) {

            mEmailEditText!!.setText(email)

        }

        ImeHelper.setImeOnDoneListener(mEmailEditText!!, this)

        mSubmitButton!!.setOnClickListener(this)

//    adUni//t = debugAdUni//t

//        MyMoPub().init(this, adUnit)

//        Handler(Looper.getMainLooper()).postDelayed({
//        }, 200)

        initFBAds()

//        val mAdView: AdView = findViewById(R.id.pract_adView)
//
//        val adRequest = AdRequest.Builder().build()
//        mAdView.loadAd(adRequest)
//
//        mAdView.adListener = object : AdListener() {
//
//            override fun onAdLoaded() {}
//
//            override fun onAdFailedToLoad(adError: LoadAdError) {}
//
//            override fun onAdOpened() {}
//
//            override fun onAdClicked() {}
//
//            override fun onAdImpression() {
//                super.onAdImpression()
//            }
//
//            override fun onAdClosed() {}
//
//        }

    }

    var moPubView: MoPubView? = null

    private var adUnit: String = "ca8da1a9954d436f9b81f39bf980712a"
    private val debugAdUnit: String = "b195f8dd8ded45fe847ad89ed1d016da"

    private fun initAds() {

        moPubView = findViewById(R.id.pract_moPubView)

        moPubView!!.setAdUnitId(adUnit); // Enter your Ad Unit ID from www.mopub.com
//        moPubView.adSize = MoPubAdSize // Call this if you are not setting the ad size in XML or wish to use an ad size other than what has been set in the XML. Note that multiple calls to `setAdSize()` will override one another, and the MoPub SDK only considers the most recent one.
//        moPubView.loadAd(MoPubAdSize) // Call this if you are not calling setAdSize() or setting the size in XML, or if you are using the ad size that has not already been set through either setAdSize() or in the XML

        moPubView!!.bannerAdListener = object : MoPubView.BannerAdListener {

            override fun onBannerLoaded(banner: MoPubView) {
                Log.e(TAG, "RecoverPasswordActivity onBannerLoaded")
            }

            override fun onBannerFailed(banner: MoPubView?, error: MoPubErrorCode?) {
                Log.e(TAG, "RecoverPasswordActivity onBannerFailed: ${error.toString()}")
            }

            override fun onBannerClicked(banner: MoPubView?) {
                Log.e(TAG, "RecoverPasswordActivity onBannerClicked")
            }

            override fun onBannerExpanded(banner: MoPubView?) {
                Log.e(TAG, "RecoverPasswordActivity onBannerExpanded")
            }

            override fun onBannerCollapsed(banner: MoPubView?) {
                Log.e(TAG, "RecoverPasswordActivity onBannerCollapsed")
            }

        }

        moPubView!!.loadAd()

    }

    private var adView: AdView? = null

    private fun initFBAds() {

        IronSource.setMetaData("Facebook_IS_CacheFlag","ALL");

        val adListener: AdListener = object : AdListener {

            override fun onError(ad: Ad?, adError: AdError) {

                Log.e(TAG, "RecoverPasswordActivity onBannerFailed: ${adError.errorMessage}")

//                Toast.makeText(
//                    this@RecoverPasswordActivity,
//                    "Ad Error: " + adError.errorMessage,
//                    Toast.LENGTH_LONG
//                ).show()

            }

            override fun onAdLoaded(ad: Ad?) {
                Log.e(TAG, "RecoverPasswordActivity onBannerLoaded")
            }

            override fun onAdClicked(ad: Ad?) {
                Log.e(TAG, "RecoverPasswordActivity onBannerClicked")
            }

            override fun onLoggingImpression(ad: Ad?) {
                // Ad impression logged callback
            }

        }

//    adView = AdView(requireContext(), "IMG_16_9_APP_INSTALL#411762013708850_411802357038149", AdSize.BANNER_HEIGHT_50)
        adView = AdView(this@RecoverPasswordActivity, "411762013708850_411802357038149", AdSize.BANNER_HEIGHT_50)

        val adContainer: LinearLayout = findViewById<LinearLayout>(R.id.pract_banner)

        adContainer.addView(adView)

        adView!!.loadAd()

        adView?.loadAd(adView?.buildLoadAdConfig()?.withAdListener(adListener)?.build())

    }



    private val TAG: String = "ATTENTION ATTENTION"

    override fun onDonePressed() {

        if (mEmailFieldValidator!!.validate(mEmailEditText!!.text)) {

            resetPassword(mEmailEditText!!.text.toString())

        }

    }

    override fun onBackPressed() {

//    super.onBackPressed()

        Log.e("ATTENTION ATTENTION", "LoginActivityEmail onBackPressed()")

        val intent = Intent(this, LoginActivityEmail::class.java)

        startActivity(intent)

        finish()

    }

    private fun resetPassword(email: String) {

        showProgress()

        if (networkUtil.isOnline(this)) {

            auth = Firebase.auth

            auth.sendPasswordResetEmail(email).addOnCompleteListener {

                    task ->


                if (task.isSuccessful) {

                    hideProgress()
                    showEmailSentDialog(mEmailEditText!!.text.toString())

                }
                else {

                    hideProgress()

                    val emailNotSentBuilder = AlertDialog.Builder(this, R.style.MyDialogTheme)
                    emailNotSentBuilder.setTitle("Password-reset email not sent.")
                    emailNotSentBuilder.setMessage("Please try again later.")

                    emailNotSentBuilder.setPositiveButton(android.R.string.ok) {

                            dialog, which ->

                        dialog.dismiss()

                    }

                    emailNotSentBuilder.show()

                }

            }

        }
        else {

            val resetNeedsInternetBuilder = AlertDialog.Builder(this, R.style.MyDialogTheme)
            resetNeedsInternetBuilder.setTitle("Password-reset email needs internet access.")
            resetNeedsInternetBuilder.setMessage("Please try again when you have internet access.")

            resetNeedsInternetBuilder.setPositiveButton(android.R.string.ok) {

                    dialog, which ->

                dialog.dismiss()

            }

            resetNeedsInternetBuilder.show()

        }

    }

    private fun showEmailSentDialog(email: String) {

        AlertDialog.Builder(this)
            .setTitle(R.string.title_confirm_recover_password)
            .setMessage(getString(R.string.confirm_recovery_body, email))
            .setOnDismissListener(DialogInterface.OnDismissListener {

                onFinish()

            })
            .setPositiveButton(android.R.string.ok, null)
            .show()

    }

    override fun onDestroy() {

        if (moPubView != null) {
            moPubView!!.destroy()
        }

        adView?.destroy()

        super.onDestroy()

    }

    private fun onFinish() {

        val email: String? = intent.getStringExtra(EMAIL)

        val intent = Intent(this@RecoverPasswordActivity, LoginActivityEmail::class.java)

        if (email != null) {

            intent.putExtra(EMAIL, email)

        }

        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()

    }

    private fun showProgress() {
        mSubmitButton!!.isEnabled = false
        mProgressBar!!.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        mSubmitButton!!.isEnabled = true
        mProgressBar!!.visibility = View.INVISIBLE
    }

}