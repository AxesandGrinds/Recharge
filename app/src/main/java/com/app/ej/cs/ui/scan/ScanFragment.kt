package com.app.ej.cs.ui.scan

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
import androidx.appcompat.app.AlertDialog
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
import com.app.ej.cs.ui.account.LoginActivityMain
import com.app.ej.cs.ui.account.PleaseWaitScreenRecoverActivity
import com.app.ej.cs.ui.fab.FloatingActionButton
import com.app.ej.cs.ui.fab.FloatingActionMenu
import com.app.ej.cs.vision.RecognitionActivityFinal
import com.droidman.ktoasty.KToasty
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.coroutines.sendSuspend
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.android.gms.ads.*
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ScanFragment : Fragment(), ScanFragmentView {

  override fun displayUserMain(userList: UserListModel) {

    if (userList.userList.size > 0) {

      userMainListModel.userList = mutableListOf(userList.userList[0])
      this.userList.add(userList.userList[0])
      scanFragmentUserMainViewAdapter.notifyDataSetChanged()
      Log.e("ATTENTION ATTENTION",
        "In ScanFragment displayUserMain: ${userList.userList[0].toString()}")

      nameTv.text    = userMainListModel.userList[0].name
      emailTv.text   = userMainListModel.userList[0].email
      createdTv.text = userMainListModel.userList[0].created?.let { TimeAgo.using(it.toLong()) }

      Log.e("ATTENTION ATTENTION",
        "createdTv.text = userMainListModel.userList[0].created: ${userMainListModel.userList[0].created}")

      if (userMainListModel.userList.isNotEmpty()) {

        Log.e("ATTENTION ATTENTION", "From ScanFragment userMainListModel.userList.isNotEmpty()")
        Log.e("ATTENTION ATTENTION",
          "From ScanFragment userMainListModel.userList: ${userMainListModel.userList.toString()}")

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
      scanFragmentUserSecondViewAdapter.notifyDataSetChanged()

      Log.e("ATTENTION ATTENTION",
        "In ScanFragment displayUserSecond: ${userList.userList[0].toString()}")

      initFloatingActionMenu(
        mutableListOf(
          userMainListModel.userList[0],
          userSecondListModel.userList[0]))

    }
    else {

      userSecondRecyclerView.visibility = View.GONE

      Log.e("ATTENTION ATTENTION",
        "In ScanFragment displayUserSecond: userSecondRecyclerView.visibility = View.GONE")

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
        scanFragmentFriendViewAdapter.notifyDataSetChanged()

        Log.e("ATTENTION ATTENTION",
          "In ScanFragment displayFriends: ${friendsListModel.friendList.toString()}")

      }
      else {

        friendRecyclerView.visibility = View.GONE

        Log.e("ATTENTION ATTENTION",
          "In ScanFragment displayUserSecond: friendRecyclerView.visibility = View.GONE")

      }

      Log.e(TAG, "\n\n\nfinalFriendList: ${finalFriendList.toString()}")

    }
    else {

      friendRecyclerView.visibility = View.GONE
      Log.e("ATTENTION ATTENTION",
        "In ScanFragment displayUserSecond: friendRecyclerView.visibility = View.GONE")

    }

  }

  @Inject
  lateinit var scanFragmentPresenter: ScanFragmentPresenter


  private lateinit var nameTv:    TextView
  private lateinit var emailTv:   TextView
  private lateinit var createdTv: TextView

  private lateinit var fam: FloatingActionMenu
  private lateinit var fab1: FloatingActionButton
  private lateinit var fab2: FloatingActionButton


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

  override fun onAttach(context: Context) {

    (context.applicationContext as InitApp)
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

  private fun initAds(view: View) {

    MobileAds.initialize(requireContext())

    val mAdView: AdView = view.findViewById(R.id.fs_adView)

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

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {

    val view = inflater.inflate(R.layout.scan_fragment_lists_layout, container, false)
    scanFragmentPresenter.bind(this)

    initAds(view)

    initScanMainUserDetails(view)

    initScanMainRecyclerView(view)
    initScanSecondRecyclerView(view)
    initScanFriendRecyclerView(view)

    checkSignedUp()

    askPermissions()

    return view

  }

  fun toCalendar(date: Date): Calendar {

    val cal = Calendar.getInstance()
    cal.time = date

    return cal

  }

  val PREFNAME: String = "local_user"

  private fun checkBiDailyInterstitialAd() {

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

      if (olderThan2Days) {

        val biDailySentBuilder = AlertDialog.Builder(requireContext(), R.style.MyDialogTheme)
        biDailySentBuilder.setTitle("Hello")
        biDailySentBuilder.setMessage("Please view ad. This ad shows up once every 2 days. This will help to keep Recharge App free.")

        biDailySentBuilder.setPositiveButton(android.R.string.ok) {

            dialog, which ->

          dialog.dismiss()

          goToPleaseWaitBeforeBiDailyInterstitialAd()

        }

        biDailySentBuilder.show()

      }

    }
    else {

      val editor = sharedPref!!.edit()
      editor.putInt("weeklyInterstitialAd", 1)
      editor.putString("lastWeekDateTime", thisDate.toString())
      editor.apply()

    }

  }

  private fun goToPleaseWaitBeforeBiDailyInterstitialAd() {

    val intent = Intent(context, PleaseWaitScreenScanActivity::class.java)

    startActivity(intent)
    requireActivity().finish()

  }

  private fun checkSignedUp() {

    Log.e("ATTENTION ATTENTION", "checkSignedUp in ScanFragment")
    val gson = Gson()

    val sharedPref = requireContext().getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
    val allInfoJsonSaved = sharedPref.getString("allInfoSaved", "defaultAll")!!

    if (allInfoJsonSaved != "defaultAll") {

      val allInfoSaved = gson.fromJson(allInfoJsonSaved, UserAndFriendInfo::class.java)

      Log.e("ATTENTION ATTENTION", "allInfoSaved: ${allInfoSaved.toString()}")

      if (allInfoSaved.usersList.size > 0) {

        userMainListModel.userList = mutableListOf(allInfoSaved.usersList[0])
        scanFragmentUserMainViewAdapter.notifyDataSetChanged()

      }

      if (allInfoSaved.usersList.size > 1) {

        userSecondListModel.userList = mutableListOf(allInfoSaved.usersList[1])
        scanFragmentUserSecondViewAdapter.notifyDataSetChanged()

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

        scanFragmentPresenter.displayScanDetails()

        checkBiDailyInterstitialAd()

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

  override fun onStart() {
    super.onStart()

  }

  override fun onDestroy() {
    super.onDestroy()

    scanFragmentPresenter.unbind()

  }

  private fun initScanMainUserDetails(view: View) {

    nameTv    = view.findViewById(R.id.nameTv)
    emailTv   = view.findViewById(R.id.emailTv)
    createdTv = view.findViewById(R.id.createdTv)

    fam = view.findViewById(R.id.fam)
    fab1 = view.findViewById(R.id.fab1)
    fab2 = view.findViewById(R.id.fab2)

  }

  private fun initScanMainRecyclerView(view: View) {

    userMainRecyclerView = view.findViewById(R.id.scan_user_main_recycler_view)
    userMainRecyclerView.layoutManager = LinearLayoutManager(
      context,
      LinearLayoutManager.VERTICAL,
      false)

    val dividerItemDecoration = DividerItemDecoration(
      context,
      LinearLayoutManager.VERTICAL)

    userMainRecyclerView.addItemDecoration(dividerItemDecoration)

    userMainRecyclerView.isNestedScrollingEnabled = false

    scanFragmentUserMainViewAdapter = ScanUserMainViewAdapter(
      userMainListModel,
      requireContext(),
      this,
      requireActivity()
    ) {}

    userMainRecyclerView.adapter = scanFragmentUserMainViewAdapter

  }

  private fun initScanSecondRecyclerView(view: View) {

    userSecondRecyclerView = view.findViewById(R.id.scan_user_second_recycler_view)
    userSecondRecyclerView.layoutManager = LinearLayoutManager(
      context,
      LinearLayoutManager.VERTICAL,
      false)

    val dividerItemDecoration = DividerItemDecoration(
      context,
      LinearLayoutManager.VERTICAL)

    userSecondRecyclerView.addItemDecoration(dividerItemDecoration)

    userSecondRecyclerView.isNestedScrollingEnabled = false

    scanFragmentUserSecondViewAdapter = ScanUserSecondViewAdapter(
      userSecondListModel,
      requireContext(),
      this,
      requireActivity()
    ) {}

    userSecondRecyclerView.adapter = scanFragmentUserSecondViewAdapter

    userSecondRecyclerView.visibility = View.GONE

  }

  private fun initScanFriendRecyclerView(view: View) {

    friendRecyclerView = view.findViewById(R.id.scan_user_friends_recycler_view)
    friendRecyclerView.layoutManager = LinearLayoutManager(
      context,
      LinearLayoutManager.VERTICAL,
      false)

    val dividerItemDecoration = DividerItemDecoration(
      context,
      LinearLayoutManager.VERTICAL)

    friendRecyclerView.isNestedScrollingEnabled = false

    scanFragmentFriendViewAdapter = ScanFriendViewAdapter(
      requireContext(),
      this,
      requireActivity(),
      userList,
      friendsListModel
    ) {}

    friendRecyclerView.adapter = scanFragmentFriendViewAdapter

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


