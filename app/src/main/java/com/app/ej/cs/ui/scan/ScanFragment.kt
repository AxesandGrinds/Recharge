package com.app.ej.cs.ui.scan


//import com.github.clans.fab.FloatingActionButton
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.ej.cs.R
import com.app.ej.cs.init.InitApp
import com.app.ej.cs.model.FriendListModel
import com.app.ej.cs.model.UserListModel
import com.app.ej.cs.presenter.ScanFragmentPresenter
import com.app.ej.cs.repository.entity.Friend
import com.app.ej.cs.repository.entity.User
import com.app.ej.cs.repository.entity.UserAndFriendInfo
import com.app.ej.cs.ui.DataRechargeDialog
import com.app.ej.cs.ui.MainActivity
import com.app.ej.cs.ui.account.LoginActivity
import com.app.ej.cs.vision.RecognitionActivityFinal
import com.droidman.ktoasty.KToasty
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.coroutines.sendSuspend
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.gson.Gson
import kotlinx.android.synthetic.main.scan_fragment_lists_layout.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


/**
 * This is the Fragment for displaying the list of Info Material
 */
class ScanFragment : Fragment(), ScanFragmentView {

  override fun displayUserMain(userList: UserListModel) {

    if (userList.userList.size > 0) {

      userMainListModel.userList = mutableListOf(userList.userList[0])
      this.userList.add(userList.userList[0])
      scanFragmentUserMainViewAdapter.notifyDataSetChanged()
      Log.e(
        "ATTENTION ATTENTION",
        "In ScanFragment displayUserMain: ${userList.userList[0].toString()}"
      )

      initScanMainUserDetails(requireView())

      if (userMainListModel.userList.isNotEmpty()) {

        Log.e("ATTENTION ATTENTION", "From ScanFragment userMainListModel.userList.isNotEmpty()")
        Log.e(
          "ATTENTION ATTENTION",
          "From ScanFragment userMainListModel.userList: ${userMainListModel.userList.toString()}"
        )

        initFloatingActionMenu(mutableListOf(userMainListModel.userList[0]))

      }

    }



  }

  override fun displayUserSecond(userList: UserListModel) {

    if (userList.userList.size > 1  &&
//            userList.userList[0].phone != userList.userList[1].phone &&
            userList.userList[1].phone != null) {

      userSecondRecyclerView.visibility = View.VISIBLE
      userSecondListModel.userList = mutableListOf(userList.userList[1])
      this.userList.add(userList.userList[1])
      scanFragmentUserSecondViewAdapter.notifyDataSetChanged()

      Log.e(
        "ATTENTION ATTENTION",
        "In ScanFragment displayUserSecond: ${userList.userList[1].toString()}"
      )

      initFloatingActionMenu(
        mutableListOf(
          userMainListModel.userList[0],
          userSecondListModel.userList[0]
        )
      )

    }
    else {

      userSecondRecyclerView.visibility = View.GONE

      Log.e(
        "ATTENTION ATTENTION",
        "In ScanFragment displayUserSecond: userSecondRecyclerView.visibility = View.GONE"
      )

    }


  }

  override fun displayFriends(friendList: FriendListModel) {

    loadFriends(friendList.friendList)


  }

  private fun loadFriends(friendList: MutableList<Friend>?) {

    val finalFriendList: MutableList<Friend> = mutableListOf()

    if (friendList?.size ?: 0 > 0) {

      for (friend in friendList!!) {

        if (!(friend.name == null || friend.name == "") && !(friend.phone1 == null || friend.phone1 == "")) {

          finalFriendList.add(friend)

        }

      }

      if (finalFriendList.isNotEmpty()) {

        friendsListModel.friendList = finalFriendList
        scanFragmentFriendViewAdapter.notifyDataSetChanged()

        Log.e(
          "ATTENTION ATTENTION",
          "In ScanFragment displayFriends: ${friendsListModel.friendList.toString()}"
        )

      }
      else {

        friendRecyclerView.visibility = View.GONE

        Log.e(
          "ATTENTION ATTENTION",
          "In ScanFragment displayUserSecond: friendRecyclerView.visibility = View.GONE"
        )

      }

      Log.e(TAG, "\n\n\nfinalFriendList: ${finalFriendList.toString()}")

    }
    else {

      friendRecyclerView.visibility = View.GONE
      Log.e(
        "ATTENTION ATTENTION",
        "In ScanFragment displayUserSecond: friendRecyclerView.visibility = View.GONE"
      )


    }

  }

  @Inject
  lateinit var scanFragmentPresenter: ScanFragmentPresenter


  private lateinit var nameTv:    TextView
  private lateinit var emailTv:   TextView
  private lateinit var createdTv: TextView

  private var userList: MutableList<User> = mutableListOf<User>()

  private lateinit var userMainRecyclerView: RecyclerView
  private lateinit var scanFragmentUserMainViewAdapter: ScanUserMainViewAdapter
  private val userMainListModel = UserListModel(mutableListOf())

  private lateinit var userSecondRecyclerView: RecyclerView
  private lateinit var scanFragmentUserSecondViewAdapter: ScanUserSecondViewAdapter
  private val userSecondListModel = UserListModel(mutableListOf())

  private lateinit var friendRecyclerView: RecyclerView
  private lateinit var scanFragmentFriendViewAdapter: ScanFriendViewAdapter
  private val friendsListModel = FriendListModel(mutableListOf())

private val TAG: String = "ATTENTION ATTENTION"



  private fun askPermissions() {


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

      CoroutineScope(Dispatchers.IO).launch {

        val result = permissionsBuilder(
          Manifest.permission.CALL_PHONE,
          Manifest.permission.ACCESS_WIFI_STATE,
          Manifest.permission.ACCESS_NETWORK_STATE,
          Manifest.permission.CHANGE_WIFI_STATE,
          Manifest.permission.INTERNET,
          Manifest.permission.WRITE_EXTERNAL_STORAGE,
          Manifest.permission.READ_EXTERNAL_STORAGE,
        ).build().sendSuspend()

        if (result.allGranted()) { // All the permissions are granted.

        }
        else {

          withContext(Dispatchers.Main) {
            //KToasty.info(app, "Using local data", Toast.LENGTH_LONG).show()
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

//    if (
//
//      ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_PHONE_STATE)
//      == PackageManager.PERMISSION_GRANTED &&
//      ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
//      == PackageManager.PERMISSION_GRANTED &&
//      ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
//      == PackageManager.PERMISSION_GRANTED
//
//    ) {
//
//
//      val intent: Intent = Intent(requireContext(), RecognitionActivityFinal::class.java)
//      intent.putExtra("pAccount", pAccount)
//      intent.putExtra("phone", user.phone)
//      intent.putExtra("network", user.network)
//      startActivity(intent)
//
//    }
//    else {
//
//      ActivityCompat.requestPermissions(
//        requireActivity(),
//        arrayOf(Manifest.permission.READ_PHONE_STATE,
//          Manifest.permission.CALL_PHONE,
//          Manifest.permission.CAMERA),
//        PERMISSION_READ_PHONE_STATE
//      )
//
//    }


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

  override fun onAttach(context: Context) {

    (context.applicationContext as InitApp) // HERE
      .appComp().inject(this)

    super.onAttach(context)

  }

  override fun onDetach() {
    super.onDetach()
  }

  override fun onResume() {
    Log.e(TAG, "onResume of ScanFragment")
    super.onResume()
  }

  override fun onPause() {
    Log.e(TAG, "OnPause of ScanFragment")
    super.onPause()
  }

  /*private var fam: FloatingActionMenu ? = null
  private var fab1: FloatingActionButton ? = null
  private var fab2: FloatingActionButton ? = null*/

  /*var fab1_text: TextView? = null
  var fab2_text: TextView? = null

  private var fab_open: Animation? = null
  private var fab_close: Animation? = null
  private var fab_clock: Animation? = null
  private var fab_anticlock: Animation? = null*/

  var isOpen: Boolean = false
  var isFab1Active: Boolean = false
  var isFab2Active: Boolean = false

  private fun initFloatingActionMenu(userList: MutableList<User>) {


    if (userList[0].network == null) {
      fab1!!.visibility = View.GONE
      isFab1Active = false
    }
    else {
      fab1!!.visibility = View.VISIBLE
      isFab1Active = true
    }

    if (userList.size > 1) {

      if (userList[0].phone == null || userList[0].network == null) {
        fab1!!.visibility = View.GONE
        isFab1Active = false
      }
      else {
        fab1!!.labelText = userList[0].phone
        fab1!!.visibility = View.VISIBLE
        isFab1Active = true
      }
      if (userList[1].phone == null || userList[1].network == null) {
        fab2!!.visibility = View.GONE
        isFab2Active = false
      }
      else {
        fab2!!.labelText = userList[1].phone
        fab2!!.visibility = View.VISIBLE
        isFab2Active = true
      }

    }
    else if (userList.size == 1) {

      if (userList[0].phone == null || userList[0].network == null) {
        fab1!!.visibility = View.GONE
        isFab1Active = false
      }
      else {
        fab1!!.labelText = userList[0].phone
        fab1!!.visibility = View.VISIBLE
        isFab1Active = true
      }

    }
    else {
      fam!!.visibility = View.GONE
      fab1!!.visibility = View.GONE
      fab2!!.visibility = View.GONE
      isFab1Active = false
      isFab2Active = false
    }

    fam!!.setOnMenuButtonClickListener {

      /*if (fabMenu!!.isOpened) {
        //Toasty.info(context, fabMenuYellow.getMenuButtonLabelText(), Toasty.LENGTH_LONG).show();
      }*/

      fam!!.toggle(true)

    }

    if (userList[0].network == null) {
      fab1?.visibility = View.GONE
    }
    else {
      fab1?.visibility = View.VISIBLE
    }

    if (userList.size > 1) {

      if (userList[1].network == null) {
        fab2?.visibility = View.GONE
      }
      else {
        fab2?.visibility = View.VISIBLE
      }

    }
    else {
      fab2?.visibility = View.GONE
    }

    Log.e("ATTENTION ATTENTION", "fam!!.setOnClickListener ran")
    Log.e("ATTENTION ATTENTION", "fam isOpen = ${isOpen.toString()}")
    Log.e("ATTENTION ATTENTION", "fab1 Active: ${isFab1Active.toString()}")
    Log.e("ATTENTION ATTENTION", "fab1 Active: ${isFab2Active.toString()}")

    /*fam!!.setOnClickListener {

        view ->


      Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
        .setAction("Action", null)
        .show()

      Log.e("ATTENTION ATTENTION", "fam!!.setOnClickListener ran")
      Log.e("ATTENTION ATTENTION", "fam isOpen = ${isOpen.toString()}")
      Log.e("ATTENTION ATTENTION", "fab1 Active: ${isFab1Active.toString()}")
      Log.e("ATTENTION ATTENTION", "fab1 Active: ${isFab2Active.toString()}")


    }*/

    /*fab1?.showAnimation = AnimationUtils.loadAnimation(context, R.anim.scale_up)
    fab1?.showAnimation = AnimationUtils.loadAnimation(context, R.anim.scale_down)
    fab2?.showAnimation = AnimationUtils.loadAnimation(context, R.anim.scale_up)
    fab2?.showAnimation = AnimationUtils.loadAnimation(context, R.anim.scale_down)*/

    fab1!!.setOnClickListener {

      fam!!.toggle(true)

//      showInterstitialAd()
      askCameraAndPhonePermissionsBeforeCameraActivity(it, userList[0], "1")

    }

    fab2!!.setOnClickListener {

      fam!!.toggle(true)
      askCameraAndPhonePermissionsBeforeCameraActivity(it, userList[1], "2")

    }


  }

  private fun initAds(view: View) {


    // https://developers.google.com/ad-manager/mobile-ads-sdk/android/banner
    MobileAds.initialize(
      context
    ) {

    }

    /// TODO Remove For Release vvv
//    val testDeviceIds: List<String> = listOf("E9DEDC61204CFB33008E54C7F35245C8") // listOf("78D47CB8E8C50C8391083ABA46D59A17")
//    val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
//    MobileAds.setRequestConfiguration(configuration)
    /// TODO Remove For Release ^^^

    val mAdView: AdView = view.findViewById(R.id.fs_adView)

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

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {

    val view = inflater.inflate(R.layout.scan_fragment_lists_layout, container, false)
    scanFragmentPresenter.bind(this)

    /*fam = view.findViewById(R.id.fam)
    fab1   = view.findViewById(R.id.fab1)
    fab2   = view.findViewById(R.id.fab2)

    fab1_text = view.findViewById(R.id.fab1_text)
    fab2_text = view.findViewById(R.id.fab2_text)

    fab_close = AnimationUtils.loadAnimation(context, R.anim.fab_close);
    fab_open = AnimationUtils.loadAnimation(context, R.anim.fab_open);
    fab_clock = AnimationUtils.loadAnimation(context, R.anim.fab_rotate_clock);
    fab_anticlock = AnimationUtils.loadAnimation(context, R.anim.fab_rotate_anticlock);*/

    initAds(view)

    initScanMainRecyclerView(view)
    initScanSecondRecyclerView(view)
    initScanFriendRecyclerView(view)

    checkSignedUp()



    askPermissions()

    return view

  }

  private var mInterstitialAd: InterstitialAd? = null

  private fun showFullScreen() {

    mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {

      override fun onAdDismissedFullScreenContent() {

        Log.d(TAG, "Ad was dismissed.")

      }

      override fun onAdFailedToShowFullScreenContent(adError: AdError?) {

        Log.d(TAG, "Ad failed to show.")

      }

      override fun onAdShowedFullScreenContent() {

        Log.d(TAG, "Ad showed fullscreen content.")
        mInterstitialAd = null

      }

    }

    mInterstitialAd!!.show(requireActivity())

  }

  private fun showInterstitialAd() {

    val adRequest = AdRequest.Builder().build()

    InterstitialAd.load(
      requireContext(),
      "ca-app-pub-5127161627511605/6554467118",
      adRequest,
      object : InterstitialAdLoadCallback() {

      override fun onAdFailedToLoad(adError: LoadAdError) {

        Log.d(TAG, adError.message)
        mInterstitialAd = null

      }

      override fun onAdLoaded(interstitialAd: InterstitialAd) {

        mInterstitialAd = interstitialAd
        showFullScreen()

      }

    })


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

//      val comparison: Int = date1.compareTo(thisDate)

      /*val c = Calendar.getInstance()
      c.time = date1
      c.add(Calendar.DATE, 7)

      if (c.time >= thisDate){

        val editor = sharedPref!!.edit()
        editor.putInt("weeklyInterstitialAd", weeklyInterstitialAd + 1)
        editor.putString("lastWeekDateTime", thisDate.toString())
        editor.apply()

        showInterstitialAd()

      }*/

      val second3 = 3L * 1000

      val day3 = 3L * 24 * 60 * 60 * 1000

      val day7 = 7L * 24 * 60 * 60 * 1000

      val olderThan3Seconds: Boolean = thisDate.after(Date(date1.time + second3))

      val olderThan3Days: Boolean = thisDate.after(Date(date1.time + day3))

      val olderThan7Days: Boolean = thisDate.after(Date(date1.time + day7))

      if (olderThan3Days) {

        val editor = sharedPref!!.edit()
        editor.putInt("weeklyInterstitialAd", weeklyInterstitialAd + 1)
        editor.putString("lastWeekDateTime", thisDate.toString())
        editor.apply()

        showInterstitialAd()

      }

//        if (comparison == 0 || comparison > 0) { }

    }
    else {

      val editor = sharedPref!!.edit()
      editor.putInt("weeklyInterstitialAd", 1)
      editor.putString("lastWeekDateTime", thisDate.toString())
      editor.apply()

//      showInterstitialAd()

    }

  }


  private fun checkSignedUp() {

    Log.e("ATTENTION ATTENTION", "checkSignedUp in ScanFragment")
    val gson = Gson()

    val sharedPref = requireContext().getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
    val allInfoJsonSaved = sharedPref.getString("allInfoSaved", "defaultAll")!!

    if (allInfoJsonSaved != "defaultAll") {

      val allInfoSaved = gson.fromJson(allInfoJsonSaved, UserAndFriendInfo::class.java)

      Log.e("ATTENTION ATTENTION", "allInfoSaved: ${allInfoSaved.toString()}")

      userMainListModel.userList = mutableListOf(allInfoSaved.usersList[0])
      scanFragmentUserMainViewAdapter.notifyDataSetChanged()

      if (allInfoSaved.usersList.size > 1) {

        userSecondListModel.userList = mutableListOf(allInfoSaved.usersList[1])
        scanFragmentUserSecondViewAdapter.notifyDataSetChanged()

      }

      loadFriends(allInfoSaved.friendList)

      if (userMainListModel.userList.isEmpty()) {

        (activity as MainActivity).let{

          val intent = Intent(it, LoginActivity::class.java)
          it.startActivity(intent)
          it.finish()
          Log.e("ATTENTION ATTENTION", "should end activity with it.finish()")
          Log.e("ATTENTION ATTENTION", "'allInfoJsonSaved' != defaultAll")
          Log.e("ATTENTION ATTENTION", "Makes no sense to finish.")

        }

      }
      else {

        checkWeeklyInterstitialAd ()

      }

    }
    else {

      (activity as MainActivity).let{
        val intent = Intent(it, LoginActivity::class.java)
        it.startActivity(intent)
        it.finish()
        Log.e("ATTENTION ATTENTION", "should end activity with it.finish()")
        Log.e("ATTENTION ATTENTION", "'allInfoJsonSaved' == defaultAll")

      }

    }


  }

  override fun onStart() {
    super.onStart()

    scanFragmentPresenter.displayScanDetails()

  }

  override fun onDestroy() {
    super.onDestroy()

    scanFragmentPresenter.unbind()

  }

  private fun initScanMainUserDetails(view: View) {

    nameTv    = view.findViewById(R.id.nameTv)
    emailTv   = view.findViewById(R.id.emailTv)
    createdTv = view.findViewById(R.id.createdTv)

    nameTv.text    = userMainListModel.userList[0].name
    emailTv.text   = userMainListModel.userList[0].email
    createdTv.text = userMainListModel.userList[0].created?.let { TimeAgo.using(it.toLong()) }

    Log.e(
      "ATTENTION ATTENTION",
      "createdTv.text = userMainListModel.userList[0].created: ${userMainListModel.userList[0].created}"
    )

  }

  private fun initScanMainRecyclerView(view: View) {

    userMainRecyclerView = view.findViewById(R.id.scan_user_main_recycler_view)
    userMainRecyclerView.layoutManager = LinearLayoutManager(
      context,
      LinearLayoutManager.VERTICAL,
      false
    )

    val dividerItemDecoration = DividerItemDecoration(
      context,
      LinearLayoutManager.VERTICAL
    )

    userMainRecyclerView.addItemDecoration(dividerItemDecoration)

    userMainRecyclerView.isNestedScrollingEnabled = false

    scanFragmentUserMainViewAdapter = ScanUserMainViewAdapter(
      userMainListModel,
      requireContext(),
      this,
      requireActivity()
    ) {
//      KToasty.info(view.context, "${it?.name}@${it?.phone} Clicked", Toast.LENGTH_LONG).show()
    }

    userMainRecyclerView.adapter = scanFragmentUserMainViewAdapter

  }

  private fun initScanSecondRecyclerView(view: View) {

    userSecondRecyclerView = view.findViewById(R.id.scan_user_second_recycler_view)
    userSecondRecyclerView.layoutManager = LinearLayoutManager(
      context,
      LinearLayoutManager.VERTICAL,
      false
    )

    val dividerItemDecoration = DividerItemDecoration(
      context,
      LinearLayoutManager.VERTICAL
    )

    userSecondRecyclerView.addItemDecoration(dividerItemDecoration)

    userSecondRecyclerView.isNestedScrollingEnabled = false

    scanFragmentUserSecondViewAdapter = ScanUserSecondViewAdapter(
      userSecondListModel,
      requireContext(),
      this,
      requireActivity()
    ) {
//      KToasty.info(view.context, "${it?.name}@${it?.phone} Clicked", Toast.LENGTH_LONG).show()
    }

    userSecondRecyclerView.adapter = scanFragmentUserSecondViewAdapter

  }

  private fun initScanFriendRecyclerView(view: View) {

    friendRecyclerView = view.findViewById(R.id.scan_user_friends_recycler_view)
    friendRecyclerView.layoutManager = LinearLayoutManager(
      context,
      LinearLayoutManager.VERTICAL,
      false
    )

    val dividerItemDecoration = DividerItemDecoration(
      context,
      LinearLayoutManager.VERTICAL
    )

//    friendRecyclerView.addItemDecoration(dividerItemDecoration)

    friendRecyclerView.isNestedScrollingEnabled = false

    scanFragmentFriendViewAdapter = ScanFriendViewAdapter(
      requireContext(),
      this,
      requireActivity(),
      userList,
      friendsListModel
    ) {
//      KToasty.info(view.context, "${it?.name} Clicked", Toast.LENGTH_LONG).show()
    }

    friendRecyclerView.adapter = scanFragmentFriendViewAdapter

  }

  // https://stackoverflow.com/questions/51737667/since-the-android-getfragmentmanager-api-is-deprecated-is-there-any-alternati
  fun dataRechargeDialog(activity: Activity, network: String, whichSimCard: Int) {

    val fragment: DataRechargeDialog = DataRechargeDialog.newInstance(network, whichSimCard)

    /*getFragmentManager()
            .beginTransaction()
            .replace(R.id.root_linear_layout_V, fragment)
            .addToBackStack("DataRechargeDialog")
            .commit();*/

    try {
      fragment.show(childFragmentManager, "DataRechargeDialog")
    }
    catch (e: NullPointerException) {
      e.printStackTrace()
    }

  }

}


