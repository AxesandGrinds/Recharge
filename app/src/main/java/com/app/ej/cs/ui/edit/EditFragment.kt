package com.app.ej.cs.ui.edit

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
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.ej.cs.R
import com.app.ej.cs.conf.TAG
import com.app.ej.cs.model.FriendListModel
import com.app.ej.cs.presenter.EditFragmentPresenter
import javax.inject.Inject
import com.app.ej.cs.init.InitApp
import com.app.ej.cs.model.UserListModel
import com.app.ej.cs.presenter.AddFriendDoneListener
import com.app.ej.cs.presenter.PickContactListener
import com.app.ej.cs.repository.entity.Friend
import com.app.ej.cs.repository.entity.User
import com.app.ej.cs.repository.entity.UserAndFriendInfo
import com.app.ej.cs.ui.account.RegisterDetailsActivity
import com.app.ej.cs.utils.NetworkUtil
import com.app.ej.cs.utils.PhoneUtil
import com.app.ej.cs.utils.Util
import com.droidman.ktoasty.KToasty
import com.google.android.gms.ads.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.michaelrocks.libphonenumber.android.Phonenumber

/**
 * This is the Fragment for displaying the list of Editable Material
 */
class EditFragment : Fragment(), EditFragmentView, PickContactListener {



  override fun displayUserMain(userList: UserListModel) {
    userMainListModel.userList = mutableListOf(userList.userList[0])
    editFragmentUserMainViewAdapter.notifyDataSetChanged()
  }

  override fun displayUserSecond(userList: UserListModel) {

    if (userList.userList.size > 1  &&
//            userList.userList[0].phone != userList.userList[1].phone &&
      userList.userList[1].phone != null) {

      userSecondListModel.userList = mutableListOf(userList.userList[1])
      editFragmentUserSecondViewAdapter.notifyDataSetChanged()

    }

  }

  override fun displayFriends(friendList: FriendListModel) {
    friendsListModel.friendList = friendList.friendList
    editFragmentFriendViewAdapter.notifyDataSetChanged()
  }

  private val PICK_CONTACT_REQUEST: Int = 500

/*  val userMainView = object : UserMainView {
    override fun displayUserMain(userList: UserListModel) {
      userMainListModel.userList = userList.userList
      editFragmentUserMainViewAdapter.notifyDataSetChanged()
    }
  }

  val userSecondView = object : UserSecondView {
    override fun displayUserSecond(userList: UserListModel) {
      userSecondListModel.userList = userList.userList
      editFragmentUserSecondViewAdapter.notifyDataSetChanged()
    }
  }

  val friendView = object : FriendView {
    override fun displayFriends(friendList: FriendListModel) {
      friendsListModel.friendList = friendList.friendList
      editFragmentFriendViewAdapter.notifyDataSetChanged()
    }
  }*/

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

  private var util: Util = Util()

  override fun onAttach(context: Context) {

    (context.applicationContext as InitApp) // HERE
      .appComp().inject(this)

    super.onAttach(context)

  }

  override fun onStart() {
    super.onStart()

//    addFriendDoneListener = object : AddFriendDoneListener{
//
//      override fun contactPicked(data: Intent?, index: Int) {
//
//
//      }
//
//    }

    editFragmentPresenter.displayEditDetails()

  }

  override fun onDestroy() {
    super.onDestroy()

    editFragmentPresenter.unbind()

  }

  private fun initAds(view: View) {


    // https://developers.google.com/ad-manager/mobile-ads-sdk/android/banner
    MobileAds.initialize(
      context
    ) {

    }

    /*/// TODO Remove For Release vvv
    val testDeviceIds: List<String> = listOf("B3EEABB8EE11C2BE770B684D95219ECB")
    val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
    MobileAds.setRequestConfiguration(configuration)
    /// TODO Remove For Release ^^^*/

    val mAdView: AdView = view.findViewById(R.id.fe_adView)

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

    val view = inflater.inflate(R.layout.edit_fragment_lists_layout, container, false)
    editFragmentPresenter.bind(this)

    auth     = Firebase.auth
    firebaseUser = auth.currentUser!!

    initSaveButton(view)

    initAds(view)

    initAddOneMoreFriendButton(view)
    initEditMainRecyclerView(view)
    initEditSecondRecyclerView(view)
    initEditFriendRecyclerView(view)

    return view

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

      updateData() // snackbar and toast inside

    }
    else {

      val snackbar = Snackbar.make(viewSave, "", Snackbar.LENGTH_LONG)
      val toast = KToasty.info(requireContext(), "You need internet access to save.", Toast.LENGTH_LONG)

      snackbar.show()
      toast.show()

    }


  }

  private var userAndFriendInfoUnsaved      : UserAndFriendInfo = UserAndFriendInfo()
  private var userAndFriendInfoSaved : UserAndFriendInfo = UserAndFriendInfo()

  private val gson = Gson()
  private var allInfoJsonSaved:   String = ""
  private var allInfoJsonUnsaved: String = ""


  private fun updateEmail(message: String) {


    val snackbar = Snackbar.make(requireView(), "", Snackbar.LENGTH_LONG)
    val toast = KToasty.info(requireContext(), message, Toast.LENGTH_LONG)

    snackbar.show()
    toast.show()

  }

  private fun updateEmailWithConfirmation() {


  }

  // Access a Cloud Firestore instance from your Activity
  private val mFirestore: FirebaseFirestore = Firebase.firestore
  private lateinit var auth:         FirebaseAuth
  private lateinit var firebaseUser: FirebaseUser
  private lateinit var userId:       String

  private fun returnToLogin() {


  }

  private fun updateData() {

//    userAndFriendInfo.usersList = ArrayList(userMainListModel.userList)
//    if (friendsListModel.friendList != null)
//    userAndFriendInfo.friendsList = ArrayList(friendsListModel.friendList)
//    allInfoJsonSaved = gson.toJson(userAndFriendInfo)  // json string

    val sharedPref = activity?.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

    val editor = sharedPref!!.edit()

    allInfoJsonSaved   = sharedPref.getString("allInfoSaved", "allInfoJsonSaved").toString()
    allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", allInfoJsonSaved).toString()

    userAndFriendInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)
    userAndFriendInfoSaved = gson.fromJson(allInfoJsonSaved, UserAndFriendInfo::class.java)

    val user = firebaseUser

    if (user != null) userId = user.uid;
    else {
//      returnToLogin()
      return
    };

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
        .set(userAndFriendInfoUnsaved, SetOptions.merge())
        .addOnSuccessListener {

          editor.putString("allInfoSaved",   allInfoJsonUnsaved)
          editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
          editor.apply()

          util.onShowMessage("Save complete!", requireContext(), requireView())
          Log.d(TAG, "Save complete!")

        }
        .addOnFailureListener {
            e -> Log.e(TAG, "Error saving", e)
        }

    }
    else {

      if (userAndFriendInfoUnsaved.usersList[0].name == null  || userAndFriendInfoUnsaved.usersList[0].name  == "") {

        util.onShowErrorMessage("you must set name", requireContext(), requireView())

      }
      else if (userAndFriendInfoUnsaved.usersList[0].phone == null || userAndFriendInfoUnsaved.usersList[0].phone == "") {

        util.onShowErrorMessage("you must set phone1", requireContext(), requireView())

      }
      else if (userAndFriendInfoUnsaved.usersList[0].network != null && userAndFriendInfoUnsaved.usersList[0].network != "") {

        util.onShowErrorMessage("you must set network1", requireContext(), requireView())

      }
      else if (userAndFriendInfoUnsaved.usersList[0].email == null || userAndFriendInfoUnsaved.usersList[0].email == "") {

        util.onShowErrorMessage("you must set email", requireContext(), requireView())

      }


    }


/// Practise from and to Json
//    allInfoJsonSaved = gson.toJson(userAndFriendInfo)  // json string
//    allInfoJsonSaved = sharedPref.getString("allInfoSaved", "defaultAll").toString()
//    val allInfoSaved = gson.fromJson(allInfoJsonSaved, UserAndFriendInfo::class.java)
//
//
//    allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll").toString()
//    val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)
//    allInfoJsonSaved = gson.toJson(userAndFriendInfo)  // json string
//    editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
//    editor.apply()

  }

  private fun initAddOneMoreFriendButton(view: View) {

    oneMoreFriendBtn = view.findViewById<ImageButton>(R.id.add_one_more_friend_btn)

    oneMoreFriendBtn.setOnClickListener() { _ ->

      val index: Int = friendsListModel.friendList?.size ?: 0

      val newFriend: Friend =

        Friend(
          index = index,
          description = "Friend ${index + 1}",

          folded = false,

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
        )

      val sharedPref = requireContext().getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

      var allInfoJson: String? = null

      allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!

      if (allInfoJsonUnsaved != "defaultAll") {

        val allInfo = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        allInfo.friendList?.add(newFriend)

        friendsListModel.friendList?.add(newFriend)

        val editor = sharedPref.edit()

        allInfoJsonUnsaved = Gson().toJson(allInfo)

//        editor.putString("allInfoSaved", allInfoJsonSaved)
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
//        editor.putString("allInfoSaved", allInfoJsonSaved)

        editor.apply()

        val message: String = "You have successfully added an entry for one more friend."
        util.onShowMessage(message, requireContext())
        Log.e("ATTENTION ATTENTION", "One More Friend Button Clicked. New Friend Added")

        editFragmentFriendViewAdapter.notifyDataSetChanged()

      }


    }

  }

  private fun initEditMainRecyclerView(view: View) {

    if (userMainListModel.userList.size == 0) {

      val newUser: User = User(
        uid= "",
        index = 0,
        description = "User 1",
        folded = false,
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

    editFragmentUserMainViewAdapter = EditUserMainViewAdapter(userMainListModel, firebaseUser) {
//      KToasty.info(view.context, "${it?.name}@${it?.phone} Clicked", Toast.LENGTH_LONG).show()
    }

    userMainRecyclerView.adapter = editFragmentUserMainViewAdapter

  }

  private fun initEditSecondRecyclerView(view: View) {

    if (userSecondListModel.userList.size == 0) {

      val newUser2: User = User(
        uid= "",
        index = 1,
        description = "User 2",
        folded = false,
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

    userSecondRecyclerView = view.findViewById(R.id.edit_user_second_recycler_view)
    userSecondRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

    val dividerItemDecoration = DividerItemDecoration(
            context,
            LinearLayoutManager.VERTICAL
    )

    userSecondRecyclerView.addItemDecoration(dividerItemDecoration)

    userSecondRecyclerView.isNestedScrollingEnabled = false

    editFragmentUserSecondViewAdapter = EditUserSecondViewAdapter(userSecondListModel) {
//      KToasty.info(view.context, "${it?.name}@${it?.phone} Clicked", Toast.LENGTH_LONG).show()
    }

    userSecondRecyclerView.adapter = editFragmentUserSecondViewAdapter

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

  private fun initEditFriendRecyclerView(view: View) {

    if (friendsListModel.friendList?.size ?: 1 == 0) {

      val index: Int = friendsListModel.friendList?.size ?: 0

      val newFriend: Friend =

        Friend(
          index = index,
          description = "Friend ${index + 1}",

          folded = false,

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

          )

      friendsListModel.friendList?.add(newFriend)

    }

    friendRecyclerView = view.findViewById(R.id.edit_user_friends_recycler_view)
    friendRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

    /*val dividerItemDecoration = DividerItemDecoration(
            context,
            LinearLayoutManager.VERTICAL
    )

    friendRecyclerView.addItemDecoration(dividerItemDecoration)*/

    friendRecyclerView.isNestedScrollingEnabled = false

    editFragmentFriendViewAdapter = EditFriendViewAdapter(friendsListModel, requireActivity(), this, editFragment = this) {
//      KToasty.info(view.context, "${it?.name} Clicked", Toast.LENGTH_LONG).show()
    }
    itemTouchHelper.attachToRecyclerView(friendRecyclerView)
    friendRecyclerView.adapter = editFragmentFriendViewAdapter

  }

  override fun pickContact(index: Int) {

    val pickContactIntent: Intent = Intent(
      Intent.ACTION_PICK,
      Uri.parse("content://contacts")
    )

    // Show user only contacts w/ phone numbers
    pickContactIntent.setDataAndType(
      ContactsContract.Contacts.CONTENT_URI,
      ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
    )

    startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST + index)

  }

  override fun deleteContact(index: Int) {

    val dialogBuilder = AlertDialog.Builder(requireContext())

    // set message of alert dialog
    dialogBuilder.setMessage("Do you want to delete friend ${index + 1}?")
            // if the dialog is cancelable
            .setCancelable(true)
            // positive button text and action
            .setPositiveButton("Proceed", DialogInterface.OnClickListener {
              dialog, id -> dialog.cancel()
              runDeleteContact(index)
            })
            // negative button text and action
            .setNegativeButton("Cancel", DialogInterface.OnClickListener {
              dialog, id -> dialog.cancel()
            })

    // create dialog box
    val alert = dialogBuilder.create()
    // set title for alert dialog box
    alert.setTitle("Are You Sure?")
    // show alert dialog
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
//        editFragmentFriendViewAdapter.notifyDataSetChanged()

  }

  private fun runDeleteContactLocal(index: Int) {

    val sharedPref = activity?.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

//    allInfoJsonSaved   = sharedPref?.getString("allInfoSaved", "defaultAll")!!
    allInfoJsonUnsaved = sharedPref!!.getString("allInfoUnsaved", allInfoJsonSaved)!!

    Log.e("ATTENTION ATTENTION", "allInfoJsonSaved: $allInfoJsonSaved")
    Log.e("ATTENTION ATTENTION", "allInfoJsonUnsaved: $allInfoJsonUnsaved")

//    userAndFriendInfoSaved = gson.fromJson(allInfoJsonSaved, UserAndFriendInfo::class.java)

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

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode >= PICK_CONTACT_REQUEST) {

      if (data != null) {

        Log.w("ATTENTION ATTENTION", "onActivityResult: setContact(...) ran")
        setContact(data, requestCode - PICK_CONTACT_REQUEST)

      }

//      addFriendDoneListener.contactPicked(data, requestCode - PICK_CONTACT_REQUEST)

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

    Log.e(
      "ATTENTION ATTENTION",
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

      allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string

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

          folded = false,

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

          )

        friendsListModel.friendList?.add(newFriend)

      }

      allInfoUnsaved.friendList!![index].name = name
      allInfoUnsaved.friendList!![index].phone1 = nationalNumber

      allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string

      val editor = sharedPref!!.edit()
      editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
      editor.putString("allInfoSaved", allInfoJsonUnsaved)
      editor.apply()

    }

    Log.e("ATTENTION ATTENTION", "name: $name number: $nationalNumber")

    Log.e(
      "ATTENTION ATTENTION",
      "friendsListModel.friendList!![index]: index = ${index.toString()}")

    Log.e(
      "ATTENTION ATTENTION",
      "friendsListModel.friendList: ${friendsListModel.friendList.toString()}")


//    if (friendsListModel.friendList?.size ?: 0 > index) {
//
//      friendsListModel.friendList!![index].name = name
//      friendsListModel.friendList!![index].phone1 = nationalNumber
//
//      if (allInfoUnsaved.friendList?.size ?: 0 > 0) {
//
//        allInfoUnsaved.friendList!![index].name = name
//        allInfoUnsaved.friendList!![index].phone1 = nationalNumber
//
//        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string
//
//        val editor = sharedPref!!.edit()
//        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
//        editor.putString("allInfoSaved", allInfoJsonUnsaved)
//        editor.apply()
//
//      }
//      else {
//
//        val size: Int = allInfoUnsaved.friendList?.size ?: 0
//
//        for (i in index until size) {
//
//          val newFriend: Friend = Friend(
//
//            index = i,
//            description = "Friend ${i + 1}",
//
//            name = null,
//            phone1 = null,
//            phone2 = null,
//            phone3 = null,
//
//            network1 = null,
//            network2 = null,
//            network3 = null,
//
//            bank1 = null,
//            bank2 = null,
//            bank3 = null,
//            bank4 = null,
//
//            accountNumber1 = null,
//            accountNumber2 = null,
//            accountNumber3 = null,
//            accountNumber4 = null,
//
//            )
//
//          friendsListModel.friendList?.add(newFriend)
//
//        }
//
//        allInfoUnsaved.friendList!![index].name = name
//        allInfoUnsaved.friendList!![index].phone1 = nationalNumber
//
//        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string
//
//        val editor = sharedPref!!.edit()
//        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
//        editor.putString("allInfoSaved", allInfoJsonUnsaved)
//        editor.apply()
//
//      }
//
//      Log.e("ATTENTION ATTENTION", "name: $name number: $nationalNumber")
//
//      Log.e(
//        "ATTENTION ATTENTION",
//        "friendsListModel.friendList!![index]: index = ${index.toString()}")
//
//      Log.e(
//        "ATTENTION ATTENTION",
//        "friendsListModel.friendList: ${friendsListModel.friendList.toString()}")
//
//    }
//    else  {
//
//      val size: Int = allInfoUnsaved.friendList?.size ?: 0
//
//      for (i in index until size) {
//
//        val newFriend: Friend = Friend(
//
//          index = i,
//          description = "Friend ${i + 1}",
//
//          name = null,
//          phone1 = null,
//          phone2 = null,
//          phone3 = null,
//
//          network1 = null,
//          network2 = null,
//          network3 = null,
//
//          bank1 = null,
//          bank2 = null,
//          bank3 = null,
//          bank4 = null,
//
//          accountNumber1 = null,
//          accountNumber2 = null,
//          accountNumber3 = null,
//          accountNumber4 = null,
//
//          )
//
//        friendsListModel.friendList?.add(newFriend)
//
//      }
//
//      allInfoUnsaved.friendList!![index].name = name
//      allInfoUnsaved.friendList!![index].phone1 = nationalNumber
//
//      allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string
//
//      val editor = sharedPref!!.edit()
//      editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
//      editor.putString("allInfoSaved", allInfoJsonUnsaved)
//      editor.apply()
//
//    }

    editFragmentFriendViewAdapter.notifyDataSetChanged()

  }


}


