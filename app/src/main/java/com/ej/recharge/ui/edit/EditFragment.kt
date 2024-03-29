package com.ej.recharge.ui.edit

//import com.facebook.ads.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.droidman.ktoasty.KToasty
import com.ej.recharge.R
import com.ej.recharge.conf.TAG
import com.ej.recharge.init.InitApp
import com.ej.recharge.model.FriendListModel
import com.ej.recharge.model.UserListModel
import com.ej.recharge.presenter.EditFragmentPresenter
import com.ej.recharge.presenter.PickContactListener
import com.ej.recharge.repository.entity.Friend
import com.ej.recharge.repository.entity.User
import com.ej.recharge.repository.entity.UserAndFriendInfo
import com.ej.recharge.utils.*
import com.google.android.gms.ads.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.ironsource.mediationsdk.IronSourceBannerLayout
import com.mopub.mobileads.MoPubView
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.michaelrocks.libphonenumber.android.Phonenumber
import javax.inject.Inject


class EditFragment : Fragment(), EditFragmentView, PickContactListener {



  override fun displayUserMain(userList: UserListModel) {
    userMainListModel.userList = mutableListOf(userList.userList[0])
    editFragmentUserMainViewAdapter.notifyDataSetChanged()
  }

  override fun displayUserSecond(userList: UserListModel) {

    if (userList.userList.size > 0  &&
      userList.userList[0].phone != null) {

      userSecondListModel.userList = mutableListOf(userList.userList[0])
      editFragmentUserSecondViewAdapter.notifyDataSetChanged()

    }

  }

  override fun displayFriends(friendList: FriendListModel) {
    friendsListModel.friendList = friendList.friendList
    initEditFriendRecyclerViewAfterLoad()
    editFragmentFriendViewAdapter.notifyDataSetChanged()
  }

  private fun showDeleteOptions() {

    if (friendsListModel.friendList?.size != 0) {

//      showDeleteCheckBox = true

      friendRemoveLl.visibility = View.GONE
      friendRemoveBtn.visibility = View.GONE

      friendCompleteDeleteLl.visibility = View.VISIBLE
      friendCancelDeleteLl.visibility   = View.VISIBLE

//      friendRemoveCancelView.visibility = View.VISIBLE
//      friendRemoveView.visibility       = View.VISIBLE

      for ((i, friend) in friendsListModel.friendList!!.withIndex()) {

        friendsListModel.friendList!![i].deleteCheckBox = false
        friendsListModel.friendList!![i].showDeleteCheckBox = true

      }

      friendCompleteDeleteLl.setOnClickListener {

        if (removeIndexes.size != 0)
          deleteContacts()

      }

      friendCompleteDeleteBtn.setOnClickListener {

        if (removeIndexes.size != 0)
          deleteContacts()

      }

      friendCancelDeleteLl.setOnClickListener {

        removeDeleteOptions()

      }

      friendCancelDeleteBtn.setOnClickListener {

        removeDeleteOptions()

      }

      editFragmentFriendViewAdapter.notifyDataSetChanged()

    }

  }

  private fun removeDeleteOptions() {

//    showDeleteCheckBox = false

    friendRemoveLl.visibility = View.VISIBLE
    friendRemoveBtn.visibility = View.VISIBLE

    friendCompleteDeleteLl.visibility = View.GONE
    friendCancelDeleteLl.visibility   = View.GONE

//    friendRemoveCancelView.visibility = View.GONE
//    friendRemoveView.visibility       = View.GONE

    for ((i, friend) in friendsListModel.friendList!!.withIndex()) {

      friendsListModel.friendList!![i].deleteCheckBox = false
      friendsListModel.friendList!![i].showDeleteCheckBox = false

    }

    if (removeIndexes.size != 0)
    removeIndexes.clear()

    editFragmentFriendViewAdapter.notifyDataSetChanged()

  }

  private var removeIndexes: MutableList<Int> = mutableListOf()

  override fun addContactToIndex(index: Int) {
    removeIndexes.add(index)
  }

  override fun removeContactFromIndex(index: Int) {
    removeIndexes.remove(index)
  }

  fun saveReminder() {

//    val snackbar = Snackbar.make(requireView(), "Remember to tap the save button.", Snackbar.LENGTH_LONG)
//      snackbar.show()

    val snackbar = Snackbar.make(requireView(), "Remember to tap the save button.", Snackbar.LENGTH_INDEFINITE)

    snackbar.setAction("Dismiss") { // Call your action method here
      snackbar.dismiss()
    }

    snackbar.show()

  }

  override fun deleteContacts() {

    val dialogBuilder = AlertDialog.Builder(requireContext())

    dialogBuilder.setMessage("Do you want to delete selected friends?")
      .setCancelable(true)
      .setPositiveButton("Proceed", DialogInterface.OnClickListener {

          dialog, id -> dialog.cancel()


        if (networkUtil.isOnline(requireContext())) {


          for (index in removeIndexes) {

            runDeleteContacts(index)

          }

          runReOrderContacts()
          removeIndexes.clear()

          updateData(true)

        }
        else {

//      val snackbar = Snackbar.make(viewSave, "", Snackbar.LENGTH_LONG)
          val toast = KToasty.info(requireContext(), "You need internet access to remove friends.", Toast.LENGTH_LONG)

//      snackbar.show()
          toast.show()

        }

//        saveReminder()

      })
      .setNegativeButton("Cancel", DialogInterface.OnClickListener {
          dialog, id -> dialog.cancel()
      })

    val alert = dialogBuilder.create()
    alert.setTitle("Are You Sure?")
    alert.show()

  }

  private fun runDeleteContacts(index: Int) {

    for ((i, friend) in friendsListModel.friendList!!.withIndex()) {

      if (friendsListModel.friendList?.get(i)?.index == index) {

        friendsListModel.friendList?.remove(friend)

      }

    }

  }

  private fun runReOrderContacts() {

    var size = friendsListModel.friendList?.size!! - 1

    for (i in 0..size) {

      friendsListModel.friendList!![i].index = i
      friendsListModel.friendList!![i].description = "Friend ${i+1}"

    }

    runDeleteContactsLocal()

  }




  private fun runDeleteContactsLocal() {

    val sharedPref = activity?.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

    allInfoJsonUnsaved = sharedPref!!.getString("allInfoUnsaved", allInfoJsonSaved)!!

    Log.e("ATTENTION ATTENTION", "allInfoJsonSaved: $allInfoJsonSaved")
    Log.e("ATTENTION ATTENTION", "allInfoJsonUnsaved: $allInfoJsonUnsaved")

    try {

      if (allInfoJsonUnsaved != "defaultAll") {

        userAndFriendInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        for (index in removeIndexes) {

          for ((i, friend) in userAndFriendInfoUnsaved.friendList!!.withIndex()) {

            if (userAndFriendInfoUnsaved.friendList?.get(i)?.index == index) {

              userAndFriendInfoUnsaved.friendList?.remove(friend)

            }

          }

        }

        val size = userAndFriendInfoUnsaved.friendList?.size!! - 1

        for (i in 0..size) {

          userAndFriendInfoUnsaved.friendList!![i].index = i
          userAndFriendInfoUnsaved.friendList!![i].description = "Friend ${i+1}"

        }

        allInfoJsonUnsaved = Gson().toJson(userAndFriendInfoUnsaved)

        val editor = sharedPref.edit()

        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)

        editor.apply()

        val message: String = "You have successfully deleted an entry for a friend."

        util.onShowMessageSuccess(message, requireContext())

      }

      editFragmentFriendViewAdapter.notifyDataSetChanged()

    }
    catch (e: Exception) {

      Log.e("ATTENTION ATTENTION", "Error deleting friend entry: ${e.toString()}");

    }

  }

//  private var showDeleteCheckBox: Boolean = false

  private val PICK_CONTACT_REQUEST: Int = 500

  @Inject
  lateinit var editFragmentPresenter: EditFragmentPresenter

  private lateinit var userMainRecyclerView: RecyclerView
  private lateinit var editFragmentUserMainViewAdapter: EditUserMainViewAdapter
  private val userMainListModel = UserListModel(mutableListOf())

  private lateinit var userSecondRecyclerView: RecyclerView
  private lateinit var editFragmentUserSecondViewAdapter: EditUserSecondViewAdapter
  private val userSecondListModel = UserListModel(mutableListOf())

  private lateinit var friendRecyclerView: RecyclerView
  private lateinit var editFragmentFriendViewAdapter: EditFriendViewAdapter
  private val friendsListModel = FriendListModel(mutableListOf())


  private lateinit var friendRemoveBtn: ImageButton
  private lateinit var friendCompleteDeleteBtn: ImageButton
  private lateinit var friendCancelDeleteBtn: ImageButton

  private lateinit var friendRemoveLl: LinearLayout
  private lateinit var friendCompleteDeleteLl: LinearLayout
  private lateinit var friendCancelDeleteLl: LinearLayout

  private lateinit var friendRemoveCancelView: View
  private lateinit var friendRemoveView: View

  private var util: Util = Util()

  override fun onAttach(context: Context) {

    (context.applicationContext as InitApp)
      .appComp().inject(this)

    super.onAttach(context)

    try {

//      initFBAds()

    }
    catch (e: Exception) {

      Log.e(TAG, "Error running initFBAds() from EditFragment onAttach")

    }

    Log.e(TAG, "LifeCycle EditFragment onAttach Ran")

  }

  override fun onStart() {
    super.onStart()

    editFragmentPresenter.displayEditDetails()

  }

  override fun onDestroy() {

    adView?.destroy()

//    moPubView?.destroy()

//    IronSource.destroyBanner(ironSourceBannerLayout)

    editFragmentPresenter.unbind()

    super.onDestroy()

  }

  var moPubView: MoPubView? = null

  private val adUnit: String = "b12a73e64c8a4167a69e47961866bda4"
  private val debugAdUnit: String = "b195f8dd8ded45fe847ad89ed1d016da"

//  private fun initAds(view: View) {
//
//    moPubView = view.findViewById(R.id.fe_moPubView)
//
//    moPubView!!.setAdUnitId(adUnit); // Enter your Ad Unit ID from www.mopub.com
////        moPubView.adSize = MoPubAdSize // Call this if you are not setting the ad size in XML or wish to use an ad size other than what has been set in the XML. Note that multiple calls to `setAdSize()` will override one another, and the MoPub SDK only considers the most recent one.
////        moPubView.loadAd(MoPubAdSize) // Call this if you are not calling setAdSize() or setting the size in XML, or if you are using the ad size that has not already been set through either setAdSize() or in the XML
//
//    moPubView!!.bannerAdListener = object : MoPubView.BannerAdListener {
//
//      override fun onBannerLoaded(banner: MoPubView) {
//        Log.e(TAG, "EditFragment onBannerLoaded")
//      }
//
//      override fun onBannerFailed(banner: MoPubView?, error: MoPubErrorCode?) {
//        Log.e(TAG, "EditFragment onBannerFailed: ${error.toString()}")
//      }
//
//      override fun onBannerClicked(banner: MoPubView?) {
//        Log.e(TAG, "EditFragment onBannerClicked")
//      }
//
//      override fun onBannerExpanded(banner: MoPubView?) {
//        Log.e(TAG, "EditFragment onBannerExpanded")
//      }
//
//      override fun onBannerCollapsed(banner: MoPubView?) {
//        Log.e(TAG, "EditFragment onBannerCollapsed")
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
//        Log.e(TAG, "EditFragment onBannerFailed: ${adError.errorMessage}")
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
//
//        Log.e(TAG, "EditFragment onBannerLoaded")
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
//        Log.e(TAG, "EditFragment onBannerClicked")
//      }
//
//      override fun onLoggingImpression(ad: Ad?) {
//        // Ad impression logged callback
//      }
//
//    }
//
////    adView = AdView(requireContext(), "IMG_16_9_APP_INSTALL#411762013708850_411800960371622", AdSize.BANNER_HEIGHT_50)
////    val adContainer = view.findViewById(R.id.fe_banner) as LinearLayout
////    adContainer.addView(adView)
//
//    adView = AdView(requireContext(), "411762013708850_411800960371622", AdSize.BANNER_HEIGHT_50)
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
//    }
//
//    IronSource.loadBanner(ironSourceBannerLayout)
//
//    adView!!.loadAd()
//
//    adView?.loadAd(adView?.buildLoadAdConfig()?.withAdListener(adListener)?.build())
//
//  }

//  override fun onCreate(savedInstanceState: Bundle?) {
//    super.onCreate(savedInstanceState)
//
//    // Retain the instance between configuration changes
//    retainInstance = true
//  }

  private var mBundleRecyclerViewState: Bundle? = null
  private val KEY_RECYCLER_STATE = "recycler_state"

  override fun onPause() {
    super.onPause()

    adView?.pause()

//    IronSource.onPause(requireActivity())

    // save RecyclerView state
//    mBundleRecyclerViewState = Bundle()
//    val listState: Parcelable? = friendRecyclerView.layoutManager?.onSaveInstanceState()
//    mBundleRecyclerViewState?.putParcelable(KEY_RECYCLER_STATE, listState)

    if (requireActivity().isKeyboardOpen()) {

//    val imm: InputMethodManager? = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
//    imm?.hideSoftInputFromWindow(friendRecyclerView.windowToken, 0)
//      friendRecyclerView.context.inputService.hideSoftInputFromWindow(friendRecyclerView.windowToken, 0)

      Log.e("ATTENTION ATTENTION", "KEYBOARD WAS OPEN")
      val current: View? = requireActivity().currentFocus
      current?.clearFocus()

    }

  }

  override fun onResume() {
    super.onResume()

//    IronSource.onResume(requireActivity())

    try {

//      initFBAds()

      adView?.resume()

    }
    catch (e: Exception) {

      Log.e(TAG, "Error running initFBAds() from EditFragment onResume")

    }

    Log.e(TAG, "LifeCycle ScanFragment EditFragment Ran")

//    if (mBundleRecyclerViewState != null) {
//      val listState = mBundleRecyclerViewState!!.getParcelable<Parcelable>(KEY_RECYCLER_STATE)
//      friendRecyclerView.layoutManager?.onRestoreInstanceState(listState)
//    }

  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {

    val view = inflater.inflate(R.layout.edit_fragment_lists_layout, container, false)
    editFragmentPresenter.bind(this)

    auth     = Firebase.auth
    firebaseUser = auth.currentUser!!

    initSaveButton(view)

//    MyMoPub().init(requireContext(), adUnit)

//    Handler(Looper.getMainLooper()).postDelayed({
//    }, 200)

//    bannerContainer = view.findViewById<FrameLayout>(R.id.ironsSource_fe_banner_container)

//    initFBAds()

    initAdmob(view)

    initAddOneMoreFriendButton(view)
    initEditMainRecyclerView(view)
    initEditSecondRecyclerView(view)
    initEditFriendRecyclerView(view)

    return view

  }


  private fun initAdmob(view: View) {

    // https://developers.google.com/admob/android/quick-start?hl=en-US#kotlin
    // https://developers.google.com/admob/android/banner
    // https://developers.google.com/ad-manager/mobile-ads-sdk/android/banner
    MobileAds.initialize(requireContext()) {

    }

//        // TODO Remove For Release vvv
//        val testDeviceIds: List<String> = listOf("6638E8A228E3CC5D4711029B8808E246") // listOf("78D47CB8E8C50C8391083ABA46D59A17")
//        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
//        MobileAds.setRequestConfiguration(configuration)
//        // TODO Remove For Release ^^^

    val mAdView: AdView = view.findViewById(R.id.fe_adView)

    mAdView.adListener = object : AdListener() {

      override fun onAdLoaded() {
        // Code to be executed when an ad finishes loading.
        Log.e(TAG, "EditFragment onAdLoaded")
      }

      override fun onAdFailedToLoad(adError: LoadAdError) {
        // Code to be executed when an ad request fails.
        Log.e(TAG, "EditFragment onAdFailedToLoad: ${adError.toString()}")
      }

      override fun onAdOpened() {
        // Code to be executed when an ad opens an overlay that
        // covers the screen.
        Log.e(TAG, "EditFragment onAdOpened")
      }

      override fun onAdClicked() {
        // Code to be executed when the user clicks on an ad.
        Log.e(TAG, "EditFragment onAdClicked")
      }

      override fun onAdImpression() {
        super.onAdImpression()
      }

      override fun onAdClosed() {
        // Code to be executed when the user is about to return
        // to the app after tapping on an ad.
        Log.e(TAG, "EditFragment onAdClosed")
      }

    }

    val adRequest = AdRequest.Builder().build()
    mAdView.loadAd(adRequest)

  }

  private lateinit var oneMoreFriendBtn: ImageButton
  private lateinit var saveButton: MaterialButton
  private lateinit var saveButton2: MaterialButton
  private val PREFNAME: String = "local_user"

  private val networkUtil: NetworkUtil = NetworkUtil()

  private fun initSaveButton(view: View) {

    saveButton = view.findViewById(R.id.update) as MaterialButton

    saveButton2 = view.findViewById(R.id.update2) as MaterialButton

    saveButton.setOnClickListener() { viewSave -> onSavePressed(viewSave) }

    saveButton2.setOnClickListener() { viewSave -> onSavePressed(viewSave) }

  }

  private fun onSavePressed(viewSave: View) {

    if (networkUtil.isOnline(requireContext())) {

      updateData(false)

    }
    else {

//      val snackbar = Snackbar.make(viewSave, "", Snackbar.LENGTH_LONG)
      val toast = KToasty.info(requireContext(), "You need internet access to save.", Toast.LENGTH_LONG)

//      snackbar.show()
      toast.show()

    }


  }

  private var userAndFriendInfoUnsaved      : UserAndFriendInfo = UserAndFriendInfo()
  private var userAndFriendInfoSaved : UserAndFriendInfo = UserAndFriendInfo()

  private val gson = Gson()
  private var allInfoJsonSaved:   String = ""
  private var allInfoJsonUnsaved: String = ""


  private fun updateEmail(message: String) {


//    val snackbar = Snackbar.make(requireView(), "", Snackbar.LENGTH_LONG)
    val toast = KToasty.info(requireContext(), message, Toast.LENGTH_LONG)

//    snackbar.show()
    toast.show()

  }

  private fun updateEmailWithConfirmation() {


  }

  private val mFirestore: FirebaseFirestore = Firebase.firestore
  private lateinit var auth:         FirebaseAuth
  private lateinit var firebaseUser: FirebaseUser
  private lateinit var userId:       String

  private fun returnToLogin() {


  }

  fun isAttachedToActivity(): Boolean {
    return isVisible && activity != null
  }

  private fun readDataFromFirestoreSaveToLocal(documentSnapshot: DocumentSnapshot) {

    val sharedPref = requireContext().getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

    val editor = sharedPref!!.edit()

    try {

      val userHashMap = documentSnapshot.data

      Log.e("userHashMap", userHashMap.toString())

      val allInfo = documentSnapshot.toObject(UserAndFriendInfo::class.java) ?: UserAndFriendInfo()

      allInfoJsonSaved = Gson().toJson(allInfo)

      editor.putString("allInfoSaved", allInfoJsonSaved)
      editor.putString("allInfoUnsaved", allInfoJsonSaved)

      editor.apply()

      friendsListModel.friendList = allInfo.friendList
      editFragmentFriendViewAdapter.notifyDataSetChanged()

    }
    catch (ex: Exception) {

      Log.e(TAG, ex.toString())

    }

  }

  private fun updateData(isAddOrRemove: Boolean) {

    val sharedPref = activity?.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

    val editor = sharedPref!!.edit()

    allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "allInfoJsonSaved").toString()
    allInfoJsonSaved   = sharedPref.getString("allInfoSaved", "allInfoJsonSaved").toString()

    userAndFriendInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)
    userAndFriendInfoSaved   = gson.fromJson(allInfoJsonSaved,   UserAndFriendInfo::class.java)

    val user = firebaseUser

    if (user != null) userId = user.uid;
    else {
      return
    }

    val savedEmail:   String?   = userAndFriendInfoSaved.usersList[0].email
    val unSavedEmail: String? = userAndFriendInfoUnsaved.usersList[0].email

    if (savedEmail != unSavedEmail) {
      updateEmailWithConfirmation()
      return
    }


    Log.e("ATTENTION ATTENTION", "\n\n\nEdit Fragment Friends List: ")

    for ((i, friend) in (userAndFriendInfoUnsaved.friendList ?: mutableListOf()).withIndex()) {

      Log.e("ATTENTION ATTENTION", "Edit Fragment Friend ${(i + 1).toString()}: ${friend.toString()}")

    }

    if (userAndFriendInfoUnsaved.usersList.size > 0 && util.checkIfAllMandatoryExist(userAndFriendInfoUnsaved)) {

      mFirestore
        .collection("users")
        .document(userId)
        .set(userAndFriendInfoUnsaved)
//        .set(userAndFriendInfoUnsaved, SetOptions.merge())
        .addOnSuccessListener {

          if (!isAddOrRemove) {
            if (isAttachedToActivity()) {
              util.onShowMessage("Save complete!", requireContext(), requireView())
              Log.d(TAG, "Save complete!")
            }
          }

          friendsListModel.friendList = userAndFriendInfoUnsaved.friendList
//          editFragmentFriendViewAdapter.notifyDataSetChanged()

          mFirestore
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener {

                userDocumentSnapshot ->

              val allInfo = userDocumentSnapshot.toObject(UserAndFriendInfo::class.java) ?: UserAndFriendInfo()

              if (allInfo.usersList.size > 0 && allInfo.usersList[0].uid == userId) {

                if (isAttachedToActivity()) {
                  readDataFromFirestoreSaveToLocal(userDocumentSnapshot)
                }

              }

            }
            .addOnFailureListener { e ->
              Log.e(TAG, "Error saving", e)
            }

        }
        .addOnFailureListener {
            e -> Log.e(TAG, "Error saving", e)
        }

    }
    else {

      if (userAndFriendInfoUnsaved.usersList[0].name == null  || userAndFriendInfoUnsaved.usersList[0].name  == "") {

        util.onShowErrorMessage("Enter your name", requireContext(), requireView())

      }
      else if (userAndFriendInfoUnsaved.usersList[0].phone == null || userAndFriendInfoUnsaved.usersList[0].phone == "") {

        util.onShowErrorMessage("Enter your phone number", requireContext(), requireView())

      }
      else if (userAndFriendInfoUnsaved.usersList[0].network != null && userAndFriendInfoUnsaved.usersList[0].network != "") {

        util.onShowErrorMessage("Enter your network", requireContext(), requireView())

      }
      else if (userAndFriendInfoUnsaved.usersList[0].email == null || userAndFriendInfoUnsaved.usersList[0].email == "") {

        util.onShowErrorMessage("Enter your email address", requireContext(), requireView())

      }


    }

  }

  private fun initAddOneMoreFriendButton(view: View) {

    oneMoreFriendBtn = view.findViewById<ImageButton>(R.id.add_one_more_friend_btn)

    oneMoreFriendBtn.setOnClickListener() { _ ->

      val sharedPref = requireContext().getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

      var allInfoJson: String? = null

      allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!

      if (networkUtil.isOnline(requireContext())) {

        if (allInfoJsonUnsaved != "defaultAll") {

          val allInfo = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

          val index: Int = allInfo.friendList?.size ?: 0

          val newFriend: Friend =

            Friend(
              index = index,
              description = "Friend ${index + 1}",

              folded = true,

              name = null,
              phone1 = null,
              phone2 = null,
              phone3 = null,

              network1 = null,
              network2 = null,
              network3 = null,

              bank1 = null,
              bank2 = null,
              bank3 = null,
              bank4 = null,

              accountNumber1 = null,
              accountNumber2 = null,
              accountNumber3 = null,
              accountNumber4 = null,
              showDeleteCheckBox = false,
              deleteCheckBox = false,
            )

          allInfo.friendList?.add(newFriend)

          friendsListModel.friendList?.add(newFriend)

          val editor = sharedPref.edit()

          allInfoJsonUnsaved = Gson().toJson(allInfo)

          editor.putString("allInfoUnsaved", allInfoJsonUnsaved)

          editor.apply()

          val message: String = "You have successfully added an entry for one more friend."
          util.onShowMessage(message, requireContext())
          Log.e("ATTENTION ATTENTION", "One More Friend Button Clicked. New Friend Added")

          editFragmentFriendViewAdapter.notifyDataSetChanged()

        }

        updateData(true)

      }
      else {

//      val snackbar = Snackbar.make(viewSave, "", Snackbar.LENGTH_LONG)
        val toast = KToasty.info(requireContext(), "You need internet access to add friend.", Toast.LENGTH_LONG)

//      snackbar.show()
        toast.show()

      }

    }

  }

  private fun initEditMainRecyclerView(view: View) {

    if (userMainListModel.userList.size == 0) {

      val newUser: User = User(
        uid= "",
        index = 0,
        description = "User 1",
        folded = true,
        created = null,
        name = null,
        email = null,
        phone = null,
        network = null,
        pin = null,
        bank1 = null,
        bank2 = null,
        bank3 = null,
        bank4 = null,
        smartCardNumber1 = null,
        smartCardNumber2 = null,
        smartCardNumber3 = null,
        smartCardNumber4 = null,
        meterNumber1 = null,
        meterNumber2 = null,
        meterNumber3 = null
      )

      userMainListModel.userList.add(newUser)

    }

    userMainRecyclerView = view.findViewById(R.id.edit_user_main_recycler_view)
    userMainRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

    val dividerItemDecoration = DividerItemDecoration(
            context,
            LinearLayoutManager.VERTICAL
    )

    userMainRecyclerView.addItemDecoration(dividerItemDecoration)

    userMainRecyclerView.isNestedScrollingEnabled = false

    editFragmentUserMainViewAdapter = EditUserMainViewAdapter(userMainListModel, requireActivity(), firebaseUser) {}

    userMainRecyclerView.adapter = editFragmentUserMainViewAdapter

  }

  private fun initEditSecondRecyclerView(view: View) {

    userSecondRecyclerView = view.findViewById(R.id.edit_user_second_recycler_view)

    val phoneUtil: PhoneUtil = PhoneUtil()

    val isDualSimPhone: Boolean = phoneUtil.isDualSim(requireContext())

    if (isDualSimPhone) {

      if (userSecondListModel.userList.size == 0) {

        val newUser2: User = User(
          uid= "",
          index = 1,
          description = "User 2",
          folded = true,
          created = null,
          name = null,
          email = null,
          phone = null,
          network = null,
          pin = null,
          bank1 = null,
          bank2 = null,
          bank3 = null,
          bank4 = null,
          smartCardNumber1 = null,
          smartCardNumber2 = null,
          smartCardNumber3 = null,
          smartCardNumber4 = null,
          meterNumber1 = null,
          meterNumber2 = null,
          meterNumber3 = null
        )

        userSecondListModel.userList.add(newUser2)

      }

      userSecondRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

      val dividerItemDecoration = DividerItemDecoration(
        context,
        LinearLayoutManager.VERTICAL
      )

      userSecondRecyclerView.addItemDecoration(dividerItemDecoration)

      userSecondRecyclerView.isNestedScrollingEnabled = false

      editFragmentUserSecondViewAdapter = EditUserSecondViewAdapter(userSecondListModel) {}

      userSecondRecyclerView.adapter = editFragmentUserSecondViewAdapter

    }
    else {

      userSecondRecyclerView.visibility = View.GONE

    }


  }

  private val itemTouchHelper by lazy {

    val simpleItemTouchCallback = object :

      ItemTouchHelper.SimpleCallback(
      ItemTouchHelper.UP or
              ItemTouchHelper.DOWN or
              ItemTouchHelper.START or
              ItemTouchHelper.END,
      0) {

      override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
      ): Boolean {

        val adapter = recyclerView.adapter as EditFriendViewAdapter
        val from = viewHolder.bindingAdapterPosition
        val to = target.bindingAdapterPosition
        adapter.moveItem(from, to)
        adapter.notifyItemMoved(from, to)

        return true

      }

      override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

      override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
          viewHolder?.itemView?.alpha = 0.4f
        }
      }

      override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        viewHolder.itemView.alpha = 1.0f
      }

    }

    ItemTouchHelper(simpleItemTouchCallback)
  }

  fun startDragging(viewHolder: RecyclerView.ViewHolder) {
    itemTouchHelper.startDrag(viewHolder)
  }

  private fun initEditFriendRecyclerViewAfterLoad() {

    if (friendsListModel.friendList?.size ?: 1 == 0) {

      val index: Int = friendsListModel.friendList?.size ?: 0

      val newFriend: Friend =

        Friend(
          index = index,
          description = "Friend ${index + 1}",

          folded = true,

          name = null,
          phone1 = null,
          phone2 = null,
          phone3 = null,

          network1 = null,
          network2 = null,
          network3 = null,

          bank1 = null,
          bank2 = null,
          bank3 = null,
          bank4 = null,

          accountNumber1 = null,
          accountNumber2 = null,
          accountNumber3 = null,
          accountNumber4 = null,
          showDeleteCheckBox = false,
          deleteCheckBox = false,

          )

      friendsListModel.friendList?.add(newFriend)

      Log.e("ATTENTION ATTENTION IMP", "ADDED NEW FRIEND ON initEditFriendRecyclerView: MODEL INDEX $index")

    }

  }

  private fun initEditFriendRecyclerView(view: View) {

    friendRemoveLl = view.findViewById(R.id.remove_friends_layout)
    friendRemoveBtn = view.findViewById(R.id.remove_friends_btn)

    friendCompleteDeleteLl = view.findViewById(R.id.remove_layout)
    friendCancelDeleteLl   = view.findViewById(R.id.remove_cancel_layout)

    friendCompleteDeleteBtn = view.findViewById(R.id.remove_btn)
    friendCancelDeleteBtn   = view.findViewById(R.id.remove_cancel_btn)

//    friendRemoveCancelView = view.findViewById(R.id.remove_cancel_view)
//    friendRemoveView = view.findViewById(R.id.remove_view)

    friendRemoveLl.setOnClickListener {
      showDeleteOptions()
    }

    friendRemoveBtn.setOnClickListener {
      showDeleteOptions()
    }


    friendRecyclerView = view.findViewById(R.id.edit_user_friends_recycler_view)
    friendRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

    friendRecyclerView.isNestedScrollingEnabled = false

    editFragmentFriendViewAdapter = EditFriendViewAdapter(
      friendsListModel, requireActivity(),
      this, editFragment = this) {}

    itemTouchHelper.attachToRecyclerView(friendRecyclerView)
    friendRecyclerView.adapter = editFragmentFriendViewAdapter

  }

  override fun pickContact(index: Int) {

    val pickContactIntent: Intent = Intent(
      Intent.ACTION_PICK,
      Uri.parse("content://contacts"))

    pickContactIntent.setDataAndType(
      ContactsContract.Contacts.CONTENT_URI,
      ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE)

    startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST + index)

  }

  override fun deleteContact(index: Int) {

    val dialogBuilder = AlertDialog.Builder(requireContext())

    dialogBuilder.setMessage("Do you want to delete friend ${index + 1}?")
            .setCancelable(true)
            .setPositiveButton("Proceed", DialogInterface.OnClickListener {
              dialog, id -> dialog.cancel()

              if (networkUtil.isOnline(requireContext())) {

                runDeleteContact(index)
                updateData(true)

              }
              else {

        //      val snackbar = Snackbar.make(viewSave, "", Snackbar.LENGTH_LONG)
                val toast = KToasty.info(requireContext(), "You need internet access to add friend.", Toast.LENGTH_LONG)

        //      snackbar.show()
                toast.show()

              }
//              saveReminder()

            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener {
              dialog, id -> dialog.cancel()
            })

    val alert = dialogBuilder.create()
    alert.setTitle("Are You Sure?")
    alert.show()

  }

  private fun runDeleteContact(index: Int) {

    friendsListModel.friendList?.get(index)

    friendsListModel.friendList?.removeAt(index)

    val size = friendsListModel.friendList?.size!! - 1

    for (i in 0..size) {

      friendsListModel.friendList!![i].index = i
      friendsListModel.friendList!![i].description = "Friend ${i+1}"

    }

    runDeleteContactLocal(index)

  }

  private fun runDeleteContactLocal(index: Int) {

    val sharedPref = activity?.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

    allInfoJsonUnsaved = sharedPref!!.getString("allInfoUnsaved", allInfoJsonSaved)!!

    Log.e("ATTENTION ATTENTION", "allInfoJsonSaved: $allInfoJsonSaved")
    Log.e("ATTENTION ATTENTION", "allInfoJsonUnsaved: $allInfoJsonUnsaved")

    try {

      if (allInfoJsonUnsaved != "defaultAll") {

        userAndFriendInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        userAndFriendInfoUnsaved.friendList?.removeAt(index)

        val size = userAndFriendInfoUnsaved.friendList?.size!! - 1

        for (i in 0..size) {

          userAndFriendInfoUnsaved.friendList!![i].index = i
          userAndFriendInfoUnsaved.friendList!![i].description = "Friend ${i+1}"

        }

        allInfoJsonUnsaved = Gson().toJson(userAndFriendInfoUnsaved)

        val editor = sharedPref.edit()

        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)

        editor.apply()

        val message: String = "You have successfully deleted an entry for a friend."

        util.onShowMessageSuccess(message, requireContext())

      }

      editFragmentFriendViewAdapter.notifyDataSetChanged()

    }
    catch (e: Exception) {

      Log.e("ATTENTION ATTENTION", "Error deleting friend entry: ${e.toString()}");

    }

  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode >= PICK_CONTACT_REQUEST) {

      if (data != null) {

        Log.w("ATTENTION ATTENTION", "onActivityResult: setContact(...) ran")
        setContact(data, requestCode - PICK_CONTACT_REQUEST)

      }

    }

  }

  private fun setContact(data: Intent?, index: Int) {

    val phoneNumberUtil = PhoneNumberUtil.createInstance(context)
    val phoneUtil: PhoneUtil = PhoneUtil()

    val name: String
    val number: String
    var nationalNumber: String
    val details: Array<String>? = data?.let { phoneUtil.getDetails(it, requireContext()) }
    name = details?.get(0) ?: ""
    number = details?.get(1) ?: ""

    Log.e("ATTENTION ATTENTION", "addFriendDoneListener.contactPicked from RegisterDetailsActivity")

    Log.e("ATTENTION ATTENTION",
      "friendsListModel.friendList?.size: ${friendsListModel.friendList?.size ?: 0}")

    Log.e("ATTENTION ATTENTION", "index: $index")

    Log.e("ATTENTION ATTENTION", "friendsListModel.friendList?.size ?: 0 > index then")

    val phoneNumber: Phonenumber.PhoneNumber = phoneNumberUtil.parse(number, "NG")
    nationalNumber =
      phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
    nationalNumber = nationalNumber.replace("\\s".toRegex(), "")

    val sharedPref = requireContext().getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
    var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
    val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

    if (allInfoUnsaved.friendList?.size ?: 0 > 0) {

      allInfoUnsaved.friendList!![index].name = name
      allInfoUnsaved.friendList!![index].phone1 = nationalNumber

      allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)

      val editor = sharedPref!!.edit()
      editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
      editor.putString("allInfoSaved", allInfoJsonUnsaved)
      editor.apply()

    }
    else {

      val size: Int = allInfoUnsaved.friendList?.size ?: 0

      for (i in index until size) {

        val newFriend: Friend = Friend(

          index = i,
          description = "Friend ${i + 1}",

          folded = true,

          name = null,
          phone1 = null,
          phone2 = null,
          phone3 = null,

          network1 = null,
          network2 = null,
          network3 = null,

          bank1 = null,
          bank2 = null,
          bank3 = null,
          bank4 = null,

          accountNumber1 = null,
          accountNumber2 = null,
          accountNumber3 = null,
          accountNumber4 = null,
          showDeleteCheckBox = false,
          deleteCheckBox = false,

          )

        friendsListModel.friendList?.add(newFriend)

      }

      allInfoUnsaved.friendList!![index].name = name
      allInfoUnsaved.friendList!![index].phone1 = nationalNumber

      allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)

      val editor = sharedPref!!.edit()
      editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
      editor.putString("allInfoSaved", allInfoJsonUnsaved)
      editor.apply()

    }

    Log.e("ATTENTION ATTENTION", "name: $name number: $nationalNumber")

    Log.e("ATTENTION ATTENTION",
      "friendsListModel.friendList!![index]: index = ${index.toString()}")

    Log.e("ATTENTION ATTENTION",
      "friendsListModel.friendList: ${friendsListModel.friendList.toString()}")

    editFragmentFriendViewAdapter.notifyDataSetChanged()

  }


}


