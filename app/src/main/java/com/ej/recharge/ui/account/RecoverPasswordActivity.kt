package com.ej.recharge.ui.account

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ej.recharge.R
import com.ej.recharge.utils.ImeHelper
import com.ej.recharge.utils.NetworkUtil
import com.ej.recharge.utils.Util
//import com.facebook.ads.Ad
//import com.facebook.ads.AdError
//import com.facebook.ads.AdListener
//import com.facebook.ads.AdSize
//import com.facebook.ads.AdView
import com.google.android.gms.ads.*
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ironsource.mediationsdk.IronSourceBannerLayout
//import com.mopub.mobileads.MoPubView


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

//        initFBAds()

        initAdmob()

    }

    private fun initAdmob() {

        // https://developers.google.com/admob/android/quick-start?hl=en-US#kotlin
        // https://developers.google.com/admob/android/banner
        // https://developers.google.com/ad-manager/mobile-ads-sdk/android/banner
        MobileAds.initialize(this) {

        }

//        // TODO Remove For Release vvv
//        val testDeviceIds: List<String> = listOf("6638E8A228E3CC5D4711029B8808E246") // listOf("78D47CB8E8C50C8391083ABA46D59A17")
//        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
//        MobileAds.setRequestConfiguration(configuration)
//        // TODO Remove For Release ^^^

        val mAdView: AdView = findViewById(R.id.pract_adView)

        mAdView.adListener = object : AdListener() {

            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.e(TAG, "RecoverPasswordActivity onAdLoaded")
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                // Code to be executed when an ad request fails.
                Log.e(TAG, "RecoverPasswordActivity onAdFailedToLoad: ${adError.toString()}")
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                Log.e(TAG, "RecoverPasswordActivity onAdOpened")
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                Log.e(TAG, "RecoverPasswordActivity onAdClicked")
            }

            override fun onAdImpression() {
                super.onAdImpression()
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
                Log.e(TAG, "RecoverPasswordActivity onAdClosed")
            }

        }

        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

    }

//    var moPubView: MoPubView? = null

    private var adUnit: String = "ca8da1a9954d436f9b81f39bf980712a"
    private val debugAdUnit: String = "b195f8dd8ded45fe847ad89ed1d016da"

//    private fun initAds() {
//
//        moPubView = findViewById(R.id.pract_moPubView)
//
//        moPubView!!.setAdUnitId(adUnit); // Enter your Ad Unit ID from www.mopub.com
////        moPubView.adSize = MoPubAdSize // Call this if you are not setting the ad size in XML or wish to use an ad size other than what has been set in the XML. Note that multiple calls to `setAdSize()` will override one another, and the MoPub SDK only considers the most recent one.
////        moPubView.loadAd(MoPubAdSize) // Call this if you are not calling setAdSize() or setting the size in XML, or if you are using the ad size that has not already been set through either setAdSize() or in the XML
//
//        moPubView!!.bannerAdListener = object : MoPubView.BannerAdListener {
//
//            override fun onBannerLoaded(banner: MoPubView) {
//                Log.e(TAG, "RecoverPasswordActivity onBannerLoaded")
//            }
//
//            override fun onBannerFailed(banner: MoPubView?, error: MoPubErrorCode?) {
//                Log.e(TAG, "RecoverPasswordActivity onBannerFailed: ${error.toString()}")
//            }
//
//            override fun onBannerClicked(banner: MoPubView?) {
//                Log.e(TAG, "RecoverPasswordActivity onBannerClicked")
//            }
//
//            override fun onBannerExpanded(banner: MoPubView?) {
//                Log.e(TAG, "RecoverPasswordActivity onBannerExpanded")
//            }
//
//            override fun onBannerCollapsed(banner: MoPubView?) {
//                Log.e(TAG, "RecoverPasswordActivity onBannerCollapsed")
//            }
//
//        }
//
//        moPubView!!.loadAd()
//
//    }

    private var adView: AdView? = null

    private lateinit var ironSourceBannerLayout: IronSourceBannerLayout

//    private fun initFBAds() {
//
//        IronSource.setMetaData("Facebook_IS_CacheFlag","ALL")
//
//        var facebookAdsRefreshRate: Int = 0
//        var facebookAdsRemoved: Boolean = false
//
//        val bannerContainer: FrameLayout = findViewById<FrameLayout>(R.id.ironsSource_pract_banner_container)
//
//        val adListener: AdListener = object : AdListener {
//
//            override fun onError(ad: Ad?, adError: AdError) {
//
//                Log.e(TAG, "RecoverPasswordActivity onBannerFailed: ${adError.errorMessage}")
//
////                Toast.makeText(
////                    this@RecoverPasswordActivity,
////                    "Ad Error: " + adError.errorMessage,
////                    Toast.LENGTH_LONG
////                ).show()
//
//                this@RecoverPasswordActivity.runOnUiThread {
//
//                    Runnable {
//                        bannerContainer.removeView(adView)
//                        facebookAdsRemoved = true
//                    }
//
//                }
//
//            }
//
//            override fun onAdLoaded(ad: Ad?) {
//                Log.e(TAG, "RecoverPasswordActivity onBannerLoaded")
//
//                this@RecoverPasswordActivity.runOnUiThread {
//
//                    Runnable {
//
//                        if (facebookAdsRefreshRate > 0 && facebookAdsRemoved) {
//                            bannerContainer.addView(adView)
//                            facebookAdsRemoved = false
//                        }
//
//                        bannerContainer.removeView(ironSourceBannerLayout)
//
//                        facebookAdsRefreshRate++
//
//                    }
//
//                }
//            }
//
//            override fun onAdClicked(ad: Ad?) {
//                Log.e(TAG, "RecoverPasswordActivity onBannerClicked")
//            }
//
//            override fun onLoggingImpression(ad: Ad?) {
//                // Ad impression logged callback
//            }
//
//        }
//
////    adView = AdView(requireContext(), "IMG_16_9_APP_INSTALL#411762013708850_411802357038149", AdSize.BANNER_HEIGHT_50)
////        val adContainer: LinearLayout = findViewById<LinearLayout>(R.id.pract_banner)
////        adContainer.addView(adView)
//
//        adView = AdView(this@RecoverPasswordActivity, "411762013708850_411802357038149", AdSize.BANNER_HEIGHT_50)
//
//        IronSource.init(this, ironSourceAppKey, IronSource.AD_UNIT.BANNER)
//
//        ironSourceBannerLayout = IronSource.createBanner(this, ISBannerSize.BANNER)
//
//        val layoutParams: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
//            FrameLayout.LayoutParams.MATCH_PARENT,
//            FrameLayout.LayoutParams.WRAP_CONTENT
//        )
//
//        bannerContainer.addView(adView)
//
//        bannerContainer.addView(ironSourceBannerLayout, 0, layoutParams)
//
//        var ironSourceRefreshRate: Int = 0
//        var ironSourceRemoved: Boolean = false
//
//        ironSourceBannerLayout.bannerListener = object : BannerListener {
//
//            override fun onBannerAdLoaded() {
//                // Called after a banner ad has been successfully loaded
//
//                this@RecoverPasswordActivity.runOnUiThread {
//
//                    Runnable {
//
//                        if (ironSourceRefreshRate > 0 && ironSourceRemoved) {
//                            bannerContainer.addView(ironSourceBannerLayout, 0, layoutParams)
//                            ironSourceRemoved = false
//                        }
//
//                        bannerContainer.removeView(adView)
//
//                        ironSourceRefreshRate++
//
//                    }
//
//                }
//
//            }
//
//            override fun onBannerAdLoadFailed(error: IronSourceError) {
//                // Called after a banner has attempted to load an ad but failed.
//
//                this@RecoverPasswordActivity.runOnUiThread {
//
//                    Runnable {
//                        bannerContainer.removeView(ironSourceBannerLayout)
//                        ironSourceRemoved = true
////                        bannerContainer.removeAllViews()
//                    }
//
//                }
//
//            }
//
//            override fun onBannerAdClicked() {
//                // Called after a banner has been clicked.
//            }
//
//            override fun onBannerAdScreenPresented() {
//                // Called when a banner is about to present a full screen content.
//            }
//
//            override fun onBannerAdScreenDismissed() {
//                // Called after a full screen content has been dismissed
//            }
//
//            override fun onBannerAdLeftApplication() {
//                // Called when a user would be taken out of the application context.
//            }
//
//        }
//
//        IronSource.loadBanner(ironSourceBannerLayout)
//
//        adView!!.loadAd()
//
//        adView?.loadAd(adView?.buildLoadAdConfig()?.withAdListener(adListener)?.build())
//
//    }



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

    override fun onResume() {
        super.onResume()

        Log.e(TAG, "RecoverPasswordActivity onResume Called")

        adView?.resume()

//        IronSource.onResume(this)

//        initFBAds()

    }

    override fun onPause() {
        super.onPause()

        Log.e(TAG, "RecoverPasswordActivity onResume Called")

        adView?.pause()

//        IronSource.onPause(this)

//        IronSource.destroyBanner(ironSourceBannerLayout)

    }

    override fun onDestroy() {

        Log.e(TAG, "RecoverPasswordActivity onResume Called")

        adView?.destroy()

        super.onDestroy()

//        IronSource.destroyBanner(ironSourceBannerLayout)

//        moPubView?.destroy()

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