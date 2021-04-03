package com.app.ej.cs.ui


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import com.app.ej.cs.ui.account.LoginActivity
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

class MainActivity : AppCompatActivity(), CheckLoggedIn {

//  override fun onCreate(savedInstanceState: Bundle?) {
//    super.onCreate(savedInstanceState)
//
////        if (savedInstanceState == null) {
////            supportFragmentManager.beginTransaction()
////                .replace(R.id.anchor, ScanFragment())
////                .commit();
////        }
//
//    setContentView(R.layout.news_activity_main)
//
//    val navView: BottomNavigationView = findViewById(R.id.nav_view)
//
//    val navController = findNavController(R.id.nav_host_fragment)
//    // Passing each menu ID as a set of Ids because each
//    // menu should be considered as top level destinations.
//    val appBarConfiguration = AppBarConfiguration(
//      setOf(
//        R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
//      )
//    )
//    setupActionBarWithNavController(navController, appBarConfiguration)
//    navView.setupWithNavController(navController)
//
//  }


  private lateinit var context: Context


  private val SHARED_PREFERENCES = "SHARED_PREFERENCES"


  private lateinit var firestore:    FirebaseFirestore
  private lateinit var auth:         FirebaseAuth

  private var firebaseUser: FirebaseUser? = null
  private var userId:       String? = null

  private val networkUtil: NetworkUtil = NetworkUtil()

  private val util: Util = Util()

  private lateinit var deleteBuilder: AlertDialog.Builder










  override fun startLogin(i: Intent?) {

    startActivity(i)
    finish()

  }

  override fun startRegister(i: Intent?) {

    startActivity(i)
    finish()

  }

  private fun loadFragment(fragment: Fragment) {

    supportFragmentManager.commit {
      replace(R.id.frame_container, fragment)
//      setReorderingAllowed(true)
//      addToBackStack("name") // name can be null
    }

//    supportFragmentManager
//      .beginTransaction()
//      .replace(R.id.frame_container, fragment)
//      .commit()

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









//
//    val tickerChannel = ticker(delayMillis = 2000, initialDelayMillis = 0)
//
//    CoroutineScope(Dispatchers.IO).launch {
//
//      for (event in tickerChannel) {
//        // the 'event' variable is of type Unit, so we don't really care about it
//        val currentTime = LocalDateTime.now()
//        println(currentTime)
//      }
//
//
//    }
//
//    delay(1000)
//
//// when you're done with the ticker and don't want more events
//    tickerChannel.cancel()

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


  private val PREFNAME: String = "local_user"

  // [START on_start_check_user]
  override fun onStart() {
    super.onStart()

    context   = this

    firestore = Firebase.firestore
    auth      = Firebase.auth

    firebaseUser = auth.currentUser
    userId       = firebaseUser?.uid

    val prefs = context.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE)
    val registeredFullyEmail    = prefs.getBoolean("email_verified", false)
    val registeredFullyLocation = prefs.getBoolean("location_received", false)

    if (firebaseUser != null) {

      if (!firebaseUser!!.isEmailVerified) {

        returnToLoginCountDown(600)

      }

    }
    else {

      val sharedPref = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

      val gson = Gson()

      var allInfoJson: String? = null

      allInfoJson = sharedPref.getString("allInfoSaved", "defaultAll")

      if (allInfoJson != "defaultAll") {

        val allInfo = gson.fromJson(allInfoJson, UserAndFriendInfo::class.java)

        Log.e("ATTENTION ATTENTION", "local allInfo.toString(): ${allInfo.toString()}")

        if (allInfo.usersList[0].created == null || allInfo.usersList[0].created == "") {
          returnToLoginCountDown(600)
        }

      }

      if (!registeredFullyLocation || !registeredFullyEmail) {
        returnToLoginCountDown(600)
      }

    }


  }









  @SuppressLint("SetTextI18n")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main_final)

    context = this


    val actionBar: ActionBar = supportActionBar!!
    val tv: TextView = TextView(applicationContext)
    val typeface: Typeface = ResourcesCompat.getFont(this, R.font.dancingscriptvariablefontwght)!!
    val lp: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
      RelativeLayout.LayoutParams.MATCH_PARENT, // Width of TextView
      RelativeLayout.LayoutParams.WRAP_CONTENT
    ); // Height of TextView
    tv.layoutParams = lp
    tv.text = "Recharge" // ActionBar title text
    tv.textSize = 30f
    tv.setTextColor(Color.parseColor("#e64a19"))
    tv.setTypeface(typeface, Typeface.BOLD)
    actionBar.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
    actionBar.customView = tv





















    /*myViewPager = (ViewPager2) findViewById(R.id.viewpager);

        tabLayout   = (TabLayout)  findViewById(R.id.tabLayout);

        myViewPager.setOffscreenPageLimit(2);
        myViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
      //myViewPager.setPageTransformer(new DepthPageTransformer());
        myViewPager.setPageTransformer(new ZoomOutPageTransformer());

      //tabLayout.setupWithViewPager(myViewPager);

        fragmentList          = new ArrayList<>();
        fragmentTitleList     = new ArrayList<>();
        fragmentTitleIconList = new ArrayList<>();

        fragmentList.add(new ScanFragment());
        fragmentList.add(new ContactsFragment());
        fragmentList.add(new EditFragment());

        fragmentTitleList.add("Scan");
        fragmentTitleList.add("Invite Friends");
        fragmentTitleList.add("Edit");

        fragmentTitleIconList.add(R.drawable.ic_home_black_24dp);
        fragmentTitleIconList.add(R.drawable.ic_perm_contact_calendar_black_24dp);
        fragmentTitleIconList.add(R.drawable.ic_edit_black_24dp);

        fragmentStateAdapter = new MyFragmentStateAdapter(getSupportFragmentManager(), getLifecycle(), fragmentList);

        myViewPager.setAdapter(fragmentStateAdapter);

        new TabLayoutMediator(tabLayout, myViewPager, new TabLayoutMediator.OnConfigureTabCallback() {

            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {

                tab.setText(fragmentTitleList.get(position));
                tab.setIcon(fragmentTitleIconList.get(position));

            }
        }).attach();*/

    val navView = findViewById<BottomNavigationView>(R.id.nav_view)
    deleteBuilder = AlertDialog.Builder(context, R.style.MyDialogTheme)
    deleteBuilder.setTitle("Are you sure you want to delete this account?")
    deleteBuilder.setPositiveButton(android.R.string.ok) { dialog, which ->

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

  // https://stackoverflow.com/questions/38114689/how-to-delete-a-firebase-user-from-android-app
  private fun deleteAccount() {

    firebaseUser!!.delete().addOnCompleteListener { task ->

      if (task.isSuccessful) {

        val message: String = "You have successfully deleted your account."
        util.onShowMessage(message, context)

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

  override fun onCreateOptionsMenu(menu: Menu): Boolean {

    /*getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);*/

    menu.add(
      R.id.main_menu,
      R.id.delete_account,
      1,
      menuIconWithText(
        resources.getDrawable(R.drawable.ic_delete_forever_black_24dp),
        resources.getString(R.string.delete_account)
      )
    )

    menu.add(
      R.id.main_menu,
      R.id.sign_out,
      2,
      menuIconWithText(
        resources.getDrawable(R.drawable.ic_sign_out_24dp),
        resources.getString(R.string.sign_out)
      )
    )

    return true

  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.sign_out -> {
        Toasty.info(this, "Signing out.", Toasty.LENGTH_SHORT).show()
        signOut()
      }
      R.id.delete_account -> {
        Toasty.info(this, "Deleting Account.", Toasty.LENGTH_LONG).show()
        deleteBuilder.show()
      }
      else -> {
      }
    }
    return super.onOptionsItemSelected(item)
  }

  fun returnToLogin(context: Context) {
    val intent = Intent(context, LoginActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
    finish()
  }

  fun returnToRegister(context: Context) {

//        Intent intent = new Intent(context, RegisterDetailsActivity.class);
    val intent = Intent(context, RegisterActivity::class.java)
    context.startActivity(intent)
    finish()
  }

  private fun signOut() {

    FirebaseAuth.getInstance().signOut()

    returnToLoginCountDown(600)

  }












}