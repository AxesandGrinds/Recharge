package com.app.ej.cs.ui


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.app.ej.cs.R
import com.app.ej.cs.presenter.CheckLoggedIn
import com.app.ej.cs.repository.entity.UserAndFriendInfo
import com.app.ej.cs.ui.account.LoginActivityMain
import com.app.ej.cs.ui.account.RegisterActivity
import com.app.ej.cs.ui.edit.EditFragment
import com.app.ej.cs.ui.scan.ScanFragment
import com.app.ej.cs.utils.NetworkUtil
import com.app.ej.cs.utils.Util
import com.droidman.ktoasty.KToasty
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

  private lateinit var context: Context

  private lateinit var firestore:    FirebaseFirestore
  private lateinit var auth:         FirebaseAuth

  private var firebaseUser: FirebaseUser? = null
  private var userId:       String? = null

  private val networkUtil: NetworkUtil = NetworkUtil()

  private val util: Util = Util()

  private lateinit var deleteBuilder: AlertDialog.Builder




  private fun loadFragment(fragment: Fragment) {

    supportFragmentManager.commit {
      replace(R.id.frame_container, fragment)

    }

  }

  private fun returnToLoginCountDown(duration: Long) {

    GlobalScope.launch(Dispatchers.Main) {
      val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(duration)
      val tickSeconds = 1
      for (second in totalSeconds downTo tickSeconds) {

        delay(1000)

      }

      CoroutineScope(Dispatchers.Main).launch {

        returnToLogin(context)

      }


    }

  }

  private fun returnToRegistrationCountDown(duration: Long) {

    GlobalScope.launch(Dispatchers.Main) {
      val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(duration)
      val tickSeconds = 1
      for (second in totalSeconds downTo tickSeconds) {

        delay(1000)

      }

      CoroutineScope(Dispatchers.Main).launch {

        returnToRegister(context)

      }


    }

  }

  private fun returnToLoginCountDownOrig(duration: Long) {

    CoroutineScope(Dispatchers.IO).launch  {

      object : CountDownTimer(duration, 1000) {
        override fun onTick(millisUntilFinished: Long) {}
        override fun onFinish() {

          CoroutineScope(Dispatchers.Main).launch {

            returnToLogin(context)

          }

        }
      }.start()

    }

  }

  fun startActivity(context: Context) {

    val intent = Intent(context, MainActivity::class.java)

    context.startActivity(intent)

  }

  fun startActivity(context: Context, validate: Boolean) {

    val intent = Intent(context, MainActivity::class.java).putExtra(
      "validate",
      validate
    )

    context.startActivity(intent)

  }



  private fun goToRegistration() {

    val i = Intent(this@MainActivity, RegisterActivity::class.java)

    startActivity(i)

    finish()

  }

  private val PREFNAME: String = "local_user"

  override fun onStart() {
    super.onStart()

    context   = this

    firestore = Firebase.firestore
    auth      = Firebase.auth

    firebaseUser = auth.currentUser
    userId       = firebaseUser?.uid

    val prefs = context.getSharedPreferences(PREFNAME, MODE_PRIVATE)
    val registeredFullyEmail    = prefs.getBoolean("email_verified", false)
    val registeredFullyLocation = prefs.getBoolean("location_received", false)

    if (!registeredFullyEmail && !registeredFullyLocation) {

      returnToLoginCountDown(0)

    }

  }

  override fun onDestroy() {
    super.onDestroy()

  }


  @SuppressLint("SetTextI18n")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_main_final)

    context = this

    util.addFontToAppBarTitle(supportActionBar!!, applicationContext)

    val navView = findViewById<BottomNavigationView>(R.id.nav_view)
    deleteBuilder = AlertDialog.Builder(context, R.style.MyDialogTheme)
    deleteBuilder.setTitle("Are you sure you want to delete this account?")

    deleteBuilder.setPositiveButton(android.R.string.ok) {

        dialog, which ->

      dialog.dismiss()

      if (networkUtil.isOnline(context)) {
        deleteAccount()
      }
      else {
        val message: String = "You need internet access in order to delete account. Make sure internet is working."
        util.onShowErrorMessage(message, context)
      }

    }

    deleteBuilder.setNegativeButton(android.R.string.cancel) { dialog, which ->
      dialog.cancel()
    }

    navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

    if (savedInstanceState == null) {
      navView.menu.performIdentifierAction(R.id.navigation_scan, 0)
    }

  }

  private fun deleteAccount() {

    firebaseUser!!.delete().addOnCompleteListener { task ->

      if (task.isSuccessful) {

        val message: String = "You have successfully deleted your account."
        util.onShowMessage(message, context)

        val sharedPref = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

        val editor = sharedPref!!.edit()

        editor.clear()

        editor.apply()

        returnToLoginCountDown(3000)

      }
      else {

        KToasty.error(context, "Error: " + task.exception, Toast.LENGTH_LONG).show()

      }

    }

  }

  private val mOnNavigationItemSelectedListener =
    BottomNavigationView.OnNavigationItemSelectedListener { item ->
      when (item.itemId) {
        R.id.navigation_scan -> {
          loadFragment(ScanFragment())
          return@OnNavigationItemSelectedListener true
        }
        R.id.navigation_edit -> {
          loadFragment(EditFragment())
          return@OnNavigationItemSelectedListener true
        }
      }
      false
    }

  private fun menuIconWithText(r: Drawable, title: String): CharSequence {

    r.setBounds(0, 0, r.intrinsicWidth, r.intrinsicHeight)
    val sb = SpannableString("    $title")
    val imageSpan = ImageSpan(r, ImageSpan.ALIGN_BOTTOM)
    sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

    return sb

  }

  private lateinit var sharedPref: SharedPreferences

  override fun onCreateOptionsMenu(menu: Menu): Boolean {

    menu.add(
      R.id.main_menu,
      R.id.delete_account,
      1,
      menuIconWithText(
        resources.getDrawable(R.drawable.ic_delete_forever_black_24dp),
        resources.getString(R.string.delete_account)))

    sharedPref = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
    val isPasswordAccount = sharedPref.getBoolean("isPasswordAccount", false)

    var signOutOrder: Int = 2

    if (isPasswordAccount) {

      menu.add(
        R.id.main_menu,
        R.id.change_password,
        2,
        menuIconWithText(
          resources.getDrawable(R.drawable.ic_baseline_password_24),
          resources.getString(R.string.change_password)))

      signOutOrder = 3

    }

    menu.add(
      R.id.main_menu,
      R.id.sign_out,
      signOutOrder,
      menuIconWithText(
        resources.getDrawable(R.drawable.ic_sign_out_24dp),
        resources.getString(R.string.sign_out)))

    return true

  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {

    when (item.itemId) {

      R.id.sign_out -> {
        Toasty.info(this, "Signing out.", Toasty.LENGTH_SHORT).show()
        signOut()
      }

      R.id.change_password -> {

        changePassword()

      }

      R.id.delete_account -> {
        runDelete()
      }
      else -> { }

    }
    return super.onOptionsItemSelected(item)
  }

  private fun changePassword() {

    if (networkUtil.isOnline(context)) {

      if (firebaseUser != null) {

        val changePasswordBuilder = AlertDialog.Builder(context, R.style.MyDialogTheme)
        changePasswordBuilder.setTitle("Are you sure?")
        changePasswordBuilder.setMessage("Are you sure you want to change your password?")

        changePasswordBuilder.setPositiveButton("Yes") {

            dialog, which ->

          dialog.dismiss()

          if (networkUtil.isOnline(context)) {

            auth.sendPasswordResetEmail(firebaseUser!!.email!!)
              .addOnCompleteListener {

                  task ->

                if (task.isSuccessful) {

                  val emailSentBuilder = AlertDialog.Builder(context, R.style.MyDialogTheme)
                  emailSentBuilder.setTitle("Check your email for instructions")
                  emailSentBuilder.setMessage("Instructions have been emailed to you. Check your email for instructions to change your password.")

                  emailSentBuilder.setPositiveButton(android.R.string.ok) {

                      dialog, which ->

                    dialog.dismiss()

                  }

                  emailSentBuilder.show()

                }
                else {

                  val emailNotSentBuilder = AlertDialog.Builder(context, R.style.MyDialogTheme)
                  emailNotSentBuilder.setTitle("Email with instructions not sent")
                  emailNotSentBuilder.setMessage("For some reason, instructions to update your password was not sent. Please try again later.")

                  emailNotSentBuilder.setPositiveButton(android.R.string.ok) {

                      dialog, which ->

                    dialog.dismiss()

                  }

                  emailNotSentBuilder.show()

                }

              }

          }
          else {

            val message: String = "You need internet access in order to delete account. Make sure internet is working."
            util.onShowErrorMessageLong(message, context)

          }

        }

        changePasswordBuilder.setNegativeButton(android.R.string.cancel) { dialog, which ->
          dialog.cancel()
        }

        changePasswordBuilder.show()

      }
      else {

        val message: String = "You haven't authenticated in some time. Sign out and sign back in to continue."
        util.onShowErrorMessageLong(message, context)

      }

    }
    else {

      val message: String = "You need internet access in order to update password. Make sure internet is working."
      util.onShowErrorMessageLong(message, context)

    }

  }

  private fun runDelete() {

    Toasty.info(this, "Deleting Account.", Toasty.LENGTH_LONG).show()
    deleteBuilder.show()

  }

  fun returnToLogin(context: Context) {
    val intent = Intent(context, LoginActivityMain::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
    finish()
  }

  fun returnToRegister(context: Context) {

    val intent = Intent(context, RegisterActivity::class.java)
    context.startActivity(intent)
    finish()
  }

  private fun signOut() {

    FirebaseAuth.getInstance().signOut()

    val sharedPref = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

    val editor = sharedPref!!.edit()

    editor.clear()

    editor.apply()

    returnToLoginCountDown(600)

  }


}