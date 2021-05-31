package com.app.ej.cs.ui.account

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.app.ej.cs.R
import com.app.ej.cs.utils.ImeHelper
import com.app.ej.cs.utils.NetworkUtil
import com.app.ej.cs.utils.Util
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


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

        val mAdView: AdView = findViewById(R.id.pract_adView)

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

    private val TAG: String = "ATTENTION ATTENTION"

    override fun onDonePressed() {

        if (mEmailFieldValidator!!.validate(mEmailEditText!!.text)) {

            resetPassword(mEmailEditText!!.text.toString())

        }

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