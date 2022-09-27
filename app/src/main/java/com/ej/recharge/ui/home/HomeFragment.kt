package com.ej.recharge.ui.home

//import com.facebook.ads.*
import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.droidman.ktoasty.KToasty
import com.ej.recharge.R
import com.ej.recharge.init.InitApp
import com.ej.recharge.model.FriendListModel
import com.ej.recharge.model.UserListModel
import com.ej.recharge.presenter.HomeFragmentPresenter
import com.ej.recharge.repository.entity.Friend
import com.ej.recharge.repository.entity.User
import com.ej.recharge.repository.entity.UserAndFriendInfo
import com.ej.recharge.ui.DataRechargeDialog
import com.ej.recharge.ui.MainActivity
import com.ej.recharge.ui.account.LoginActivityMain
import com.ej.recharge.ui.fab.FloatingActionButton
import com.ej.recharge.ui.fab.FloatingActionMenu
import com.ej.recharge.utils.NetworkUtil
import com.ej.recharge.vision.RecognitionActivityFinal
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.coroutines.sendSuspend
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.android.gms.ads.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import com.ironsource.mediationsdk.IronSourceBannerLayout
import com.mopub.mobileads.MoPubView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class HomeFragment : Fragment(), HomeFragmentView {

  override fun displayUserMain(userList: UserListModel) {

    if (userList.userList.size > 0) {

      userMainListModel.userList = mutableListOf(userList.userList[0])
      this.userList.add(userList.userList[0])
      homeFragmentUserMainViewAdapter.notifyDataSetChanged()
      Log.e("ATTENTION ATTENTION",
        "In HomeFragment displayUserMain: ${userList.userList[0].toString()}")

      nameTv.text    = userMainListModel.userList[0].name
      emailTv.text   = userMainListModel.userList[0].email
      createdTv.text = userMainListModel.userList[0].created?.let { TimeAgo.using(it.toLong()) }

      Log.e("ATTENTION ATTENTION",
        "createdTv.text = userMainListModel.userList[0].created: ${userMainListModel.userList[0].created}")

      if (userMainListModel.userList.isNotEmpty()) {

        Log.e("ATTENTION ATTENTION", "From HomeFragment userMainListModel.userList.isNotEmpty()")
        Log.e("ATTENTION ATTENTION",
          "From HomeFragment userMainListModel.userList: ${userMainListModel.userList.toString()}")

        initFloatingActionMenu(mutableListOf(userMainListModel.userList[0]))

      }

    }

  }

  override fun displayUserSecond(userList: UserListModel) {

    if (userList.userList.size > 0  &&
            userList.userList[0].phone != null) {

      userSecondRecyclerView.visibility = View.VISIBLE
      userSecondListModel.userList = mutableListOf(userList.userList[0])
      this.userList.add(userList.userList[0])
      homeFragmentUserSecondViewAdapter.notifyDataSetChanged()

      Log.e("ATTENTION ATTENTION",
        "In HomeFragment displayUserSecond: ${userList.userList[0].toString()}")

      initFloatingActionMenu(
        mutableListOf(
          userMainListModel.userList[0],
          userSecondListModel.userList[0]))

    }
    else {

      userSecondRecyclerView.visibility = View.GONE

      Log.e("ATTENTION ATTENTION",
        "In HomeFragment displayUserSecond: userSecondRecyclerView.visibility = View.GONE")

    }

  }

  override fun displayFriends(friendList: FriendListModel) {

    loadFriends(friendList.friendList)


  }

  private fun loadFriends(friendList: MutableList<Friend>?) {

    val finalFriendList: MutableList<Friend> = mutableListOf()

    if (friendList?.size ?: 0 > 0) {

      for (friend in friendList!!) {

        if (!(friend.name == null || friend.name!!.trim() == "") && !(friend.phone1 == null || friend.phone1!!.trim() == "")) {

          finalFriendList.add(friend)

        }

      }

      if (finalFriendList.isNotEmpty()) {

        friendsListModel.friendList = finalFriendList
        homeFragmentFriendViewAdapter.notifyDataSetChanged()

        Log.e("ATTENTION ATTENTION",
          "In HomeFragment displayFriends: ${friendsListModel.friendList.toString()}")

      }
      else {

        friendRecyclerView.visibility = View.GONE

        Log.e("ATTENTION ATTENTION",
          "In HomeFragment displayUserSecond: friendRecyclerView.visibility = View.GONE")

      }

      Log.e(TAG, "\n\n\nfinalFriendList: ${finalFriendList.toString()}")

    }
    else {

      friendRecyclerView.visibility = View.GONE
      Log.e("ATTENTION ATTENTION",
        "In HomeFragment displayUserSecond: friendRecyclerView.visibility = View.GONE")

    }

  }

  @Inject
  lateinit var homeFragmentPresenter: HomeFragmentPresenter


  private lateinit var nameTv:    TextView
  private lateinit var emailTv:   TextView
  private lateinit var createdTv: TextView

  private lateinit var fam: FloatingActionMenu
  private lateinit var fab1: FloatingActionButton
  private lateinit var fab2: FloatingActionButton


  private var userList: MutableList<User> = mutableListOf<User>()

  private lateinit var userMainRecyclerView: RecyclerView
  private lateinit var homeFragmentUserMainViewAdapter: HomeUserMainViewAdapter
  private val userMainListModel = UserListModel(mutableListOf())

  private lateinit var userSecondRecyclerView: RecyclerView
  private lateinit var homeFragmentUserSecondViewAdapter: HomeUserSecondViewAdapter
  private val userSecondListModel = UserListModel(mutableListOf())

  private lateinit var friendRecyclerView: RecyclerView
  private lateinit var homeFragmentFriendViewAdapter: HomeFriendViewAdapter
  private val friendsListModel = FriendListModel(mutableListOf())

private val TAG: String = "ATTENTION ATTENTION"

  private fun askPermissions() {


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

      CoroutineScope(Dispatchers.IO).launch {

        val result = permissionsBuilder(
          Manifest.permission.CALL_PHONE,
          Manifest.permission.READ_PHONE_STATE,
          Manifest.permission.ACCESS_WIFI_STATE,
          Manifest.permission.ACCESS_NETWORK_STATE,
          Manifest.permission.CHANGE_WIFI_STATE,
          Manifest.permission.INTERNET,
          Manifest.permission.WRITE_EXTERNAL_STORAGE,
          Manifest.permission.READ_EXTERNAL_STORAGE,
        ).build().sendSuspend()

        if (result.allGranted()) {

        }
        else {

          withContext(Dispatchers.Main) {

            val message: String = "You have denied some permissions permanently, " +
                    "if the app force close try granting permission from settings."
            KToasty.info(requireContext(), message, Toast.LENGTH_LONG, true).show()

          }

        }

      }

    }


  }

  private val PERMISSION_READ_PHONE_STATE = 18

  private fun askCameraAndPhonePermissionsBeforeCameraActivity(
    view: View,
    user: User,
    pAccount: String
  ) {

    Log.e("ATTENTION ATTENTION", "askCameraAndPhonePermissionsBeforeCameraActivity() ran")

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

      CoroutineScope(Dispatchers.IO).launch {

        val result = permissionsBuilder(
          Manifest.permission.READ_PHONE_STATE,
          Manifest.permission.CALL_PHONE,
          Manifest.permission.CAMERA,
        ).build().sendSuspend()

        if (result.allGranted()) { // All the permissions are granted.

          val intent: Intent = Intent(requireContext(), RecognitionActivityFinal::class.java)
          intent.putExtra("pAccount", pAccount)
          intent.putExtra("phone", user.phone)
          intent.putExtra("network", user.network)
          startActivity(intent)

        }
        else {

          withContext(Dispatchers.Main) {

            //KToasty.info(app, "Using local data", Toast.LENGTH_LONG).show()
            val message = "You have denied some " +
                    "permissions permanently. Camera recognition of recharge " +
                    "code will not work without Camera permission. Please " +
                    "grant the permissions for this app in your phone's settings."
            KToasty.info(requireContext(), message, Toast.LENGTH_LONG, true).show()

          }

        }

      }

    }
    else {

      val intent: Intent = Intent(requireContext(), RecognitionActivityFinal::class.java)
      intent.putExtra("pAccount", pAccount)
      intent.putExtra("phone", user.phone)
      intent.putExtra("network", user.network)
      startActivity(intent)

    }


  }

  lateinit var mContext: Context

  override fun onAttach(context: Context) {

    mContext = context

    (context.applicationContext as InitApp)
      .appComp().inject(this)

    super.onAttach(context)

    try {

//      initFBAds()

    }
    catch (e: Exception) {

      Log.e(TAG, "Error running initFBAds() from HomeFragment onAttach")

    }

    Log.e(TAG, "LifeCycle HomeFragment onAttach Ran")

  }

  override fun onDetach() {
    super.onDetach()

  }

  override fun onPause() {
    super.onPause()

    Log.e(TAG, "OnPause of HomeFragment")

    adView?.pause()

//    IronSource.destroyBanner(ironSourceBannerLayout)
//    IronSource.onPause(requireActivity())

  }

  var isOpen: Boolean = false
  var isFab1Active: Boolean = false
  var isFab2Active: Boolean = false

  private fun initFloatingActionMenu(userList: MutableList<User>) {


    if (userList[0].network == null) {
      fab1.visibility = View.GONE
      isFab1Active = false
    }
    else {
      fab1.visibility = View.VISIBLE
      isFab1Active = true
    }

    if (userList.size > 1) {

      if (userList[0].phone == null || userList[0].network == null) {
        fab1.visibility = View.GONE
        isFab1Active = false
      }
      else {
        fab1.labelText = userList[0].phone
        fab1.visibility = View.VISIBLE
        isFab1Active = true
      }
      if (userList[1].phone == null || userList[1].network == null) {
        fab2.visibility = View.GONE
        isFab2Active = false
      }
      else {
        fab2.labelText = userList[1].phone
        fab2.visibility = View.VISIBLE
        isFab2Active = true
      }

    }
    else if (userList.size == 1) {

      if (userList[0].phone == null || userList[0].network == null) {
        fab1.visibility = View.GONE
        isFab1Active = false
      }
      else {
        fab1.labelText = userList[0].phone
        fab1.visibility = View.VISIBLE
        isFab1Active = true
      }

    }
    else {
      fam.visibility = View.GONE
      fab1.visibility = View.GONE
      fab2.visibility = View.GONE
      isFab1Active = false
      isFab2Active = false
    }

    fam.setOnMenuButtonClickListener {

      fam.toggle(true)

    }

    if (userList[0].network == null) {
      fab1.visibility = View.GONE
    }
    else {
      fab1.visibility = View.VISIBLE
    }

    if (userList.size > 1) {

      if (userList[1].network == null) {
        fab2.visibility = View.GONE
      }
      else {
        fab2.visibility = View.VISIBLE
      }

    }
    else {
      fab2.visibility = View.GONE
    }

    Log.e("ATTENTION ATTENTION", "fam.setOnClickListener ran")
    Log.e("ATTENTION ATTENTION", "fam isOpen = ${isOpen.toString()}")
    Log.e("ATTENTION ATTENTION", "fab1 Active: ${isFab1Active.toString()}")
    Log.e("ATTENTION ATTENTION", "fab1 Active: ${isFab2Active.toString()}")

    fab1.setOnClickListener {

      fam.toggle(true)
      askCameraAndPhonePermissionsBeforeCameraActivity(it, userList[0], "1")

    }

    fab2.setOnClickListener {

      fam.toggle(true)
      askCameraAndPhonePermissionsBeforeCameraActivity(it, userList[1], "2")

    }


  }

  var moPubView: MoPubView? = null

  private val adUnit: String = "1e824cb53e3945b4872c4c9aceac86b2"
  private val debugAdUnit: String = "b195f8dd8ded45fe847ad89ed1d016da"

//  private fun initAds(view: View) {
//
//    moPubView = view.findViewById(R.id.fs_moPubView)
//
//    moPubView!!.setAdUnitId(adUnit); // Enter your Ad Unit ID from www.mopub.com
////        moPubView.adSize = MoPubAdSize // Call this if you are not setting the ad size in XML or wish to use an ad size other than what has been set in the XML. Note that multiple calls to `setAdSize()` will override one another, and the MoPub SDK only considers the most recent one.
////        moPubView.loadAd(MoPubAdSize) // Call this if you are not calling setAdSize() or setting the size in XML, or if you are using the ad size that has not already been set through either setAdSize() or in the XML
//
//    moPubView!!.bannerAdListener = object : MoPubView.BannerAdListener {
//
//      override fun onBannerLoaded(banner: MoPubView) {
//        Log.e(TAG, "HomeFragment onBannerLoaded")
//      }
//
//      override fun onBannerFailed(banner: MoPubView?, error: MoPubErrorCode?) {
//        Log.e(TAG, "HomeFragment onBannerFailed: ${error.toString()}")
//      }
//
//      override fun onBannerClicked(banner: MoPubView?) {
//        Log.e(TAG, "HomeFragment onBannerClicked")
//      }
//
//      override fun onBannerExpanded(banner: MoPubView?) {
//        Log.e(TAG, "HomeFragment onBannerExpanded")
//      }
//
//      override fun onBannerCollapsed(banner: MoPubView?) {
//        Log.e(TAG, "HomeFragment onBannerCollapsed")
//      }
//
//    }
//
//    moPubView!!.loadAd()
//
//  }


  private var adView: AdView? = null

  private lateinit var ironSourceBannerLayout: IronSourceBannerLayout

  private lateinit var bannerContainer: FrameLayout

//  private fun initFBAds() {
//
//    IronSource.setMetaData("Facebook_IS_CacheFlag","ALL")
//
//    var facebookAdsRefreshRate: Int = 0
//    var facebookAdsRemoved: Boolean = false
//
//    val adListener: AdListener = object : AdListener {
//
//      override fun onError(ad: Ad?, adError: AdError) {
//
//        Log.e(TAG, "HomeFragment onBannerFailed: ${adError.errorMessage}")
//
////        Toast.makeText(
////          requireContext(),
////          "Ad Error: " + adError.errorMessage,
////          Toast.LENGTH_LONG
////        ).show()
//
//        activity?.runOnUiThread {
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
//        Log.e(TAG, "HomeFragment onBannerLoaded")
//
//        activity?.runOnUiThread {
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
//        Log.e(TAG, "HomeFragment onBannerClicked")
//      }
//
//      override fun onLoggingImpression(ad: Ad?) {
//      // Ad impression logged callback
//      }
//
//    }
//
//    if (context == null) {
//
//      return
//
//    }
//
////    adView = AdView(requireContext(), "IMG_16_9_APP_INSTALL#411762013708850_411799720371746", AdSize.BANNER_HEIGHT_50) // TEST
////    val adContainer = view.findViewById(R.id.fs_banner) as LinearLayout
////    adContainer.addView(adView)
//
//    adView = AdView(requireContext(), "411762013708850_411799720371746", AdSize.BANNER_HEIGHT_50)
//
//    IronSource.init(requireActivity(), ironSourceAppKey, IronSource.AD_UNIT.BANNER)
//
//    ironSourceBannerLayout = IronSource.createBanner(requireActivity(), ISBannerSize.BANNER)
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
//        activity?.runOnUiThread {
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
//        activity?.runOnUiThread {
//
//          Runnable {
//            bannerContainer.removeView(ironSourceBannerLayout)
//            ironSourceRemoved = true
////            bannerContainer.removeAllViews()
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
//    adView?.loadAd()
//
//    adView?.loadAd(adView?.buildLoadAdConfig()?.withAdListener(adListener)?.build())
//
////    IntegrationHelper.validateIntegration(requireActivity())
//
//  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {

    val view = inflater.inflate(R.layout.home_fragment_lists_layout, container, false)

//    MyMoPub().init(requireContext(), adUnit)

//    Handler(Looper.getMainLooper()).postDelayed({
//    }, 200)

//    bannerContainer = view.findViewById<FrameLayout>(R.id.ironsSource_fs_banner_container)

//    initFBAds()

    homeFragmentPresenter.bind(this)

    initAdmob(view)

    initHomeMainUserDetails(view)

    initHomeMainRecyclerView(view)
    initHomeSecondRecyclerView(view)
    initHomeFriendRecyclerView(view)

    checkSignedUp()

    askPermissions()

    return view

  }

  private fun initAdmob(view: View) {

    // https://developers.google.com/admob/android/quick-start?hl=en-US#kotlin
    // https://developers.google.com/admob/android/banner
    // https://developers.google.com/ad-manager/mobile-ads-sdk/android/banner
    MobileAds.initialize(requireContext()) {

    }

//        // TODO Remove For Release vvv
//        val testDeviceIds: List<String> = listOf("5F6F853F02514ED424E56C85A36C7F0E") // listOf("78D47CB8E8C50C8391083ABA46D59A17")
//        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
//        MobileAds.setRequestConfiguration(configuration)
//        // TODO Remove For Release ^^^

    val mAdView: AdView = view.findViewById(R.id.fs_adView)

    mAdView.adListener = object : AdListener() {

      override fun onAdLoaded() {
        // Code to be executed when an ad finishes loading.
        Log.e(TAG, "HomeFragment onAdLoaded")
      }

      override fun onAdFailedToLoad(adError: LoadAdError) {
        // Code to be executed when an ad request fails.
        Log.e(TAG, "HomeFragment onAdFailedToLoad: ${adError.toString()}")
      }

      override fun onAdOpened() {
        // Code to be executed when an ad opens an overlay that
        // covers the screen.
        Log.e(TAG, "HomeFragment onAdOpened")
      }

      override fun onAdClicked() {
        // Code to be executed when the user clicks on an ad.
        Log.e(TAG, "HomeFragment onAdClicked")
      }

      override fun onAdImpression() {
        super.onAdImpression()
      }

      override fun onAdClosed() {
        // Code to be executed when the user is about to return
        // to the app after tapping on an ad.
        Log.e(TAG, "HomeFragment onAdClosed")
      }

    }

    val adRequest = AdRequest.Builder().build()
    mAdView.loadAd(adRequest)

  }

  fun toCalendar(date: Date): Calendar {

    val cal = Calendar.getInstance()
    cal.time = date

    return cal

  }

  val PREFNAME: String = "local_user"

  private fun checkWeeklyInterstitialAd() {

    val currentTimeMillis = System.currentTimeMillis()

    val sharedPref = requireContext().getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
    val weeklyInterstitialAd = sharedPref.getInt("weeklyInterstitialAd", 0)
    val lastWeekDateTime = sharedPref.getString("lastWeekDateTime", "empty")!!

    val thisDate: Date = Calendar.getInstance().time

    if (weeklyInterstitialAd > 0) {

      val dateFormat: SimpleDateFormat = SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)

      val date1: Date = dateFormat.parse(lastWeekDateTime)!!

      val second3 = 3L * 1000

      val day2 = 2L * 24 * 60 * 60 * 1000

      val day3 = 3L * 24 * 60 * 60 * 1000

      val day7 = 7L * 24 * 60 * 60 * 1000

      val olderThan3Seconds: Boolean = thisDate.after(Date(date1.time + second3))

      val olderThan2Days: Boolean = thisDate.after(Date(date1.time + day2))

      val olderThan3Days: Boolean = thisDate.after(Date(date1.time + day3))

      val olderThan7Days: Boolean = thisDate.after(Date(date1.time + day7))

      if (olderThan7Days) {

        val weeklySentBuilder = AlertDialog.Builder(requireContext(), R.style.MyDialogTheme)
        weeklySentBuilder.setTitle("Hello")
        weeklySentBuilder.setMessage("Please view ad. This ad shows up once every 7 days. This will help to keep Recharge App free.")

        weeklySentBuilder.setPositiveButton(android.R.string.ok) {

            dialog, which ->

          dialog.dismiss()

          goToPleaseWaitBeforeWeeklyInterstitialAd()

        }

        weeklySentBuilder.show()

      }
      else {

        firebaseConfigCheckForUpdate()

      }

    }
    else {

      firebaseConfigCheckForUpdate()

      val editor = sharedPref!!.edit()
      editor.putInt("weeklyInterstitialAd", 1)
      editor.putString("lastWeekDateTime", thisDate.toString())
      editor.apply()

    }

  }

  private fun goToPleaseWaitBeforeWeeklyInterstitialAd() {

    val intent = Intent(context, PleaseWaitFBScreenHomeActivity::class.java)

    startActivity(intent)
    requireActivity().finish()

  }

  private fun checkSignedUp() {

    Log.e("ATTENTION ATTENTION", "checkSignedUp in HomeFragment")
    val gson = Gson()

    val sharedPref = requireContext().getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
    val allInfoJsonSaved = sharedPref.getString("allInfoSaved", "defaultAll")!!

    if (allInfoJsonSaved != "defaultAll") {

      val allInfoSaved = gson.fromJson(allInfoJsonSaved, UserAndFriendInfo::class.java)

      Log.e("ATTENTION ATTENTION", "allInfoSaved: ${allInfoSaved.toString()}")

      if (allInfoSaved.usersList.size > 0) {

        userMainListModel.userList = mutableListOf(allInfoSaved.usersList[0])
        homeFragmentUserMainViewAdapter.notifyDataSetChanged()

      }

      if (allInfoSaved.usersList.size > 1) {

        userSecondListModel.userList = mutableListOf(allInfoSaved.usersList[1])
        homeFragmentUserSecondViewAdapter.notifyDataSetChanged()

      }

      loadFriends(allInfoSaved.friendList)

      val emailVerified   = sharedPref.getBoolean("email_verified", false)
      val locationReceived = sharedPref.getBoolean("location_received", false)

      if (userMainListModel.userList.isEmpty() && (!emailVerified || !locationReceived)) {

        (activity as MainActivity).let{

          val intent = Intent(it, LoginActivityMain::class.java)
          it.startActivity(intent)
          it.finish()
          Log.e("ATTENTION ATTENTION", "should end activity with it.finish()")
          Log.e("ATTENTION ATTENTION", "'allInfoJsonSaved' != defaultAll")
          Log.e("ATTENTION ATTENTION", "Makes no sense to finish.")

        }

      }
      else {

        homeFragmentPresenter.displayHomeDetails()

//        CoroutineScope(Dispatchers.IO).launch {
//
//          val result = permissionsBuilder(
//            Manifest.permission.READ_PHONE_STATE,
//          ).build().sendSuspend()
//
//          if (result.allGranted()) {
//            homeFragmentPresenter.displayHomeDetails()
//          }
//          else {
//
//            withContext(Dispatchers.Main) {
//
//              val message: String = "You have denied some permissions permanently, " +
//                      "if the app force close try granting permission from settings."
//              KToasty.info(requireContext(), message, Toast.LENGTH_LONG, true).show()
//
//              val snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
//              snackbar.setAction("Ok", View.OnClickListener {
//                snackbar.dismiss()
//            }).show()
//
//
//            }
//
//          }
//
//        }

        Handler(Looper.getMainLooper()).postDelayed({
          if (isAttachedToActivity()) {
            checkWeeklyInterstitialAd()
          }
        }, 200)

      }

    }
    else {

      (activity as MainActivity).let{
        val intent = Intent(it, LoginActivityMain::class.java)
        it.startActivity(intent)
        it.finish()
        Log.e("ATTENTION ATTENTION", "should end activity with it.finish()")
        Log.e("ATTENTION ATTENTION", "'allInfoJsonSaved' == defaultAll")

      }

    }


  }

  private fun isAttachedToActivity(): Boolean {
    return isVisible && activity != null
  }

  private val requestPermissionLauncher =
    registerForActivityResult(
      ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
      if (isGranted) {
        Log.i("Permission: ", "Granted")
      } else {
        Log.i("Permission: ", "Denied")
      }
    }

  private fun View.showSnackbar(
    view: View,
    msg: String,
    length: Int,
    actionMessage: CharSequence?,
    action: (View) -> Unit
  ) {
    val snackbar = Snackbar.make(view, msg, length)
    if (actionMessage != null) {
      snackbar.setAction(actionMessage) {
        action(this)
      }.show()
    } else {
      snackbar.show()
    }
  }

  fun onClickRequestPermission(view: View) {
    when {
      ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.READ_PHONE_STATE
      ) == PackageManager.PERMISSION_GRANTED -> {
        requireView().showSnackbar(
          view,
          getString(R.string.permission_granted),
          Snackbar.LENGTH_INDEFINITE,
          null
        ) {}
      }

      ActivityCompat.shouldShowRequestPermissionRationale(
        requireActivity(),
        Manifest.permission.READ_PHONE_STATE
      ) -> {
        requireView().showSnackbar(
          view,
          getString(R.string.permission_required),
          Snackbar.LENGTH_INDEFINITE,
          getString(R.string.ok)
        ) {
          requestPermissionLauncher.launch(
            Manifest.permission.READ_PHONE_STATE
          )
        }
      }

      else -> {
        requestPermissionLauncher.launch(
          Manifest.permission.READ_PHONE_STATE
        )
      }
    }
  }


  private val UPDATE_REQUEST_CODE: Int = 3030

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

    if (requestCode == UPDATE_REQUEST_CODE) {

      if (resultCode != RESULT_OK) {

        Log.e("MY_APP", "Update flow failed! Result code: $resultCode")

        checkIfUpdateNecessary()

      }

    }

  }

  private var mFirebaseRemoteConfig: FirebaseRemoteConfig? = null

  private fun firebaseConfigCheckForUpdate() {

    val networkUtil: NetworkUtil = NetworkUtil()

    if (networkUtil.isOnline(requireContext())) {

      val defaultsRate: HashMap<String, Any> = HashMap()
      defaultsRate["new_version_code"] = getVersionCode().toString()

      mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
      val configSettings: FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings.Builder()
        .setMinimumFetchIntervalInSeconds(10) // change to 3600 on published app
        .build()

      mFirebaseRemoteConfig!!.setConfigSettingsAsync(configSettings)
      mFirebaseRemoteConfig!!.setDefaultsAsync(defaultsRate)

      mFirebaseRemoteConfig!!.fetchAndActivate()
        .addOnCompleteListener(requireActivity()
        ) {

            task ->

          if (task.isSuccessful) {

            val newVersionCode: String = mFirebaseRemoteConfig!!.getString("new_version_code")

            Log.e(LOG, "HomeFragment firebaseConfigCheckForUpdate newVersionCode: $newVersionCode")

            if (getVersionCode() != 0L) {

              if (newVersionCode.toLong() > getVersionCode()) {

                showUpdateDialog()

              }

            }

          }
          else Log.e(LOG, "mFirebaseRemoteConfig.fetchAndActivate() not Successful")

        }

    }

  }

  private fun showUpdateDialog() {

    val appPackageName: String = "com.ej.recharge"

    val dialogBuilder = AlertDialog.Builder(requireContext())

    dialogBuilder.setMessage("This version of Recharge App is obsolete. Please update app from play store.")
      .setCancelable(false)
      .setPositiveButton("Proceed", DialogInterface.OnClickListener {

          dialog, which ->

        dialog.cancel()

        try {

          startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))

        }
        catch (e: ActivityNotFoundException) {

          startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))

        }

      })

    val alert = dialogBuilder.create()
    alert.setTitle("Update")
    alert.show()

  }

  lateinit var pInfo: PackageInfo

  private fun getVersionCode() : Long {

    var versionCode: Long = 0L

    if (!isAdded) {
      return versionCode
    }

    try {

      pInfo = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0);

      versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

        Log.e(LOG, "HomeFragment longVersionCode: ${pInfo.longVersionCode.toString()}")

        pInfo.longVersionCode

      }
      else {

        pInfo.versionCode.toLong()

      }

    }
    catch (e: PackageManager.NameNotFoundException) {

      Log.e(LOG, "HomeFragment getVersionCode: NameNotFoundException: "+ e.message)

    }
    catch (e: IllegalStateException) {

      Log.e(LOG, "HomeFragment getVersionCode: IllegalStateException: " + e.message)

    }
    catch (e: Exception) {

      Log.e(LOG, "HomeFragment getVersionCode: Exception Occurred: " + e.message)

    }

    return versionCode

}

  private val LOG: String = "ATTENTION ATTENTION"

  private var appUpdateManager: AppUpdateManager? = null

  private lateinit var installStateUpdatedListener: InstallStateUpdatedListener

  private var updateType: Int = AppUpdateType.FLEXIBLE

  private fun checkIfUpdateNecessary() {

    appUpdateManager = AppUpdateManagerFactory.create(requireContext())

    val appUpdateInfoTask = appUpdateManager!!.appUpdateInfo

    appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->

      if (

        appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
        appUpdateInfo.updatePriority() > 1 &&
        (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) ||
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) )

      ) {

        if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {

          updateType = AppUpdateType.FLEXIBLE

          installStateUpdatedListener = InstallStateUpdatedListener {

              state ->

            if (state.installStatus() == InstallStatus.DOWNLOADING) {

              val bytesDownloaded = state.bytesDownloaded()
              val totalBytesToDownload = state.totalBytesToDownload()
              // Show update progress bar.

            }
            else if (state.installStatus() == InstallStatus.DOWNLOADED) {

              popupSnackbarForCompleteUpdate()

            }

          }

          appUpdateManager!!.registerListener(installStateUpdatedListener)

          appUpdateManager!!
            .startUpdateFlowForResult(
              appUpdateInfo,
              AppUpdateType.FLEXIBLE,
              requireActivity(),
              UPDATE_REQUEST_CODE)

        }
        else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {

          updateType = AppUpdateType.IMMEDIATE

          appUpdateManager!!
            .startUpdateFlowForResult(
              appUpdateInfo,
              AppUpdateType.IMMEDIATE,
              requireActivity(),
              UPDATE_REQUEST_CODE)

        }

      }

    }


  }

  private fun popupSnackbarForCompleteUpdate() {

    Snackbar.make(
      requireView(),
      "An update has just been downloaded.",
      Snackbar.LENGTH_INDEFINITE
    ).apply {

      setAction("RESTART") {
        appUpdateManager!!.unregisterListener(installStateUpdatedListener)
        appUpdateManager!!.completeUpdate()
      }

      setActionTextColor(resources.getColor(R.color.light_grey))
      show()

    }

  }

  override fun onResume() {

    Log.e(TAG, "onResume of HomeFragment")

    if (appUpdateManager != null) {

      if (updateType == AppUpdateType.FLEXIBLE) {

        appUpdateManager!!
          .appUpdateInfo
          .addOnSuccessListener {

              appUpdateInfo ->

            // If the update is downloaded but not installed,
            // notify the user to complete the update.
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
              popupSnackbarForCompleteUpdate()
            }

          }

      }
      else if (updateType == AppUpdateType.IMMEDIATE) {

        appUpdateManager!!
          .appUpdateInfo
          .addOnSuccessListener {

              appUpdateInfo ->

            if (
              appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            ) {

              appUpdateManager!!
                .startUpdateFlowForResult(
                  appUpdateInfo,
                  AppUpdateType.IMMEDIATE,
                  requireActivity(),
                  UPDATE_REQUEST_CODE)

            }

          }

      }

    }

//    IronSource.onResume(requireActivity())

    try {

      adView?.resume()

    }
    catch (e: Exception) {

      Log.e(TAG, "Error running initFBAds() from HomeFragment onResume")

    }

    Log.e(TAG, "LifeCycle HomeFragment onResume Ran")

    super.onResume()

  }

  override fun onStart() {
    super.onStart()
  }

  override fun onDestroy() {

    adView?.destroy()

    homeFragmentPresenter.unbind()

//    moPubView?.destroy()

//    IronSource.destroyBanner(ironSourceBannerLayout)

    super.onDestroy()

  }

  private fun initHomeMainUserDetails(view: View) {

    nameTv    = view.findViewById(R.id.nameTv)
    emailTv   = view.findViewById(R.id.emailTv)
    createdTv = view.findViewById(R.id.createdTv)

    fam = view.findViewById(R.id.fam)
    fab1 = view.findViewById(R.id.fab1)
    fab2 = view.findViewById(R.id.fab2)

  }

  private fun initHomeMainRecyclerView(view: View) {

    userMainRecyclerView = view.findViewById(R.id.home_user_main_recycler_view)
    userMainRecyclerView.layoutManager = LinearLayoutManager(
      context,
      LinearLayoutManager.VERTICAL,
      false)

    val dividerItemDecoration = DividerItemDecoration(
      context,
      LinearLayoutManager.VERTICAL)

    userMainRecyclerView.addItemDecoration(dividerItemDecoration)

    userMainRecyclerView.isNestedScrollingEnabled = false

    homeFragmentUserMainViewAdapter = HomeUserMainViewAdapter(
      userMainListModel,
      requireContext(),
      this,
      requireActivity()
    ) {}

    userMainRecyclerView.adapter = homeFragmentUserMainViewAdapter

  }

  private fun initHomeSecondRecyclerView(view: View) {

    userSecondRecyclerView = view.findViewById(R.id.home_user_second_recycler_view)
    userSecondRecyclerView.layoutManager = LinearLayoutManager(
      context,
      LinearLayoutManager.VERTICAL,
      false)

    val dividerItemDecoration = DividerItemDecoration(
      context,
      LinearLayoutManager.VERTICAL)

    userSecondRecyclerView.addItemDecoration(dividerItemDecoration)

    userSecondRecyclerView.isNestedScrollingEnabled = false

    homeFragmentUserSecondViewAdapter = HomeUserSecondViewAdapter(
      userSecondListModel,
      requireContext(),
      this,
      requireActivity()
    ) {}

    userSecondRecyclerView.adapter = homeFragmentUserSecondViewAdapter

    userSecondRecyclerView.visibility = View.GONE

  }

  private fun initHomeFriendRecyclerView(view: View) {

    friendRecyclerView = view.findViewById(R.id.home_user_friends_recycler_view)
    friendRecyclerView.layoutManager = LinearLayoutManager(
      context,
      LinearLayoutManager.VERTICAL,
      false)

    val dividerItemDecoration = DividerItemDecoration(
      context,
      LinearLayoutManager.VERTICAL)

    friendRecyclerView.isNestedScrollingEnabled = false

    homeFragmentFriendViewAdapter = HomeFriendViewAdapter(
      requireContext(),
      this,
      requireActivity(),
      userList,
      friendsListModel
    ) {}

    friendRecyclerView.adapter = homeFragmentFriendViewAdapter

  }

  fun dataRechargeDialog(activity: Activity, network: String, whichSimCard: Int) {

    val fragment: DataRechargeDialog = DataRechargeDialog.newInstance(network, whichSimCard)

    try {
      fragment.show(childFragmentManager, "DataRechargeDialog")
    }
    catch (e: NullPointerException) {
      e.printStackTrace()
    }

  }

}


