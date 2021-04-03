package com.app.ej.cs.ui.edit

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.Cursor
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.app.ej.cs.R
import com.app.ej.cs.common.Binder
import com.app.ej.cs.common.events.OnViewHolderItemSelected
import com.app.ej.cs.presenter.AddFriendDoneListener
import com.app.ej.cs.presenter.PickContactListener
import com.app.ej.cs.repository.entity.Friend
import com.app.ej.cs.repository.entity.User
import com.app.ej.cs.repository.entity.UserAndFriendInfo
import com.app.ej.cs.utils.Util
import com.droidman.ktoasty.KToasty
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.michaelrocks.libphonenumber.android.Phonenumber

/**
 * The ViewHolder for the NewsList RecyclerView Adapter
 */

class EditFriendItemViewHolder(

  private val view: View,
  private val activity: Activity,
  listener: OnViewHolderItemSelected<Friend?>? = null,
  private val pickContactListener: PickContactListener,
//        private val addFriendListener: AddFriendListener,
) : RecyclerView.ViewHolder(view), Binder<Friend> { // , AddFriendDoneListener {

  private val pickFriendIB: ImageButton = view.findViewById(R.id.addFriend)

  private val showMoreDetailsIv: ImageView = view.findViewById(R.id.showMoreHandleView)

  private val friendNumberTv: TextView = view.findViewById(R.id.friendNumber)

  private val friendNameEt: TextInputEditText = view.findViewById(R.id.friendNameEt)

  private val friendPhoneEt1: TextInputEditText = view.findViewById(R.id.friendPhoneEt1)
  private val friendPhoneEt2: TextInputEditText = view.findViewById(R.id.friendPhoneEt2)
  private val friendPhoneEt3: TextInputEditText = view.findViewById(R.id.friendPhoneEt3)

  private val friendPhoneTil2: TextInputLayout = view.findViewById(R.id.h_friend_phone2)
  private val friendPhoneTil3: TextInputLayout = view.findViewById(R.id.h_friend_phone3)

  private val friendNetworkSpinner1: Spinner = view.findViewById(R.id.friendNetworkSpinner1)
  private val friendNetworkSpinner2: Spinner = view.findViewById(R.id.friendNetworkSpinner2)
  private val friendNetworkSpinner3: Spinner = view.findViewById(R.id.friendNetworkSpinner3)

  private val friendBankSpinner1: Spinner = view.findViewById(R.id.friendBankSpinner1)
  private val friendBankSpinner2: Spinner = view.findViewById(R.id.friendBankSpinner2)
  private val friendBankSpinner3: Spinner = view.findViewById(R.id.friendBankSpinner3)
  private val friendBankSpinner4: Spinner = view.findViewById(R.id.friendBankSpinner4)

  private val friendBankAccountEt1: TextInputEditText = view.findViewById(R.id.friendBankAccountEt1)
  private val friendBankAccountEt2: TextInputEditText = view.findViewById(R.id.friendBankAccountEt2)
  private val friendBankAccountEt3: TextInputEditText = view.findViewById(R.id.friendBankAccountEt3)
  private val friendBankAccountEt4: TextInputEditText = view.findViewById(R.id.friendBankAccountEt4)

  private val friendBankAccountTil2: TextInputLayout = view.findViewById(R.id.h_friend_bank_account2)
  private val friendBankAccountTil3: TextInputLayout = view.findViewById(R.id.h_friend_bank_account3)
  private val friendBankAccountTil4: TextInputLayout = view.findViewById(R.id.h_friend_bank_account4)

  private val friendDivider1: View = view.findViewById(R.id.friendDivider1)
  private val friendDivider2: View = view.findViewById(R.id.friendDivider2)
  private val friendDivider3: View = view.findViewById(R.id.friendDivider3)
  private val friendDivider4: View = view.findViewById(R.id.friendDivider4)
  private val friendDivider5: View = view.findViewById(R.id.friendDivider5)
  private val friendDivider6: View = view.findViewById(R.id.friendDivider6)

  private lateinit var currentFriend: Friend

  private val PREFNAME: String = "local_user"

  private var userAndFriendInfo: UserAndFriendInfo = UserAndFriendInfo()

  private val gson = Gson()

  private var isExpanded: Boolean? = null

  init {
    listener?.let { l ->
      view.setOnClickListener { l(currentFriend) }
    }
  }

  private fun readFromLocal() {

    val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

    val allInfoJsonSaved: String? = sharedPref.getString("allInfoSaved", "defaultAll")
    var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!

    // if (!(allInfoSavedJson == "defaultAll" && allInfoJsonUnsaved == "defaultAll"))
    if (allInfoJsonSaved != "defaultAll") {

      val allInfo = gson.fromJson(allInfoJsonSaved, UserAndFriendInfo::class.java)

      userAndFriendInfo.usersList = allInfo.usersList

      if (allInfo.friendList != null) {
        userAndFriendInfo.friendList = allInfo.friendList!!
      }

    }
    else {

      userAndFriendInfo.usersList = mutableListOf<User>(

        User(
          uid = "",
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
        ),
        User(
          uid = "",
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

      )

      userAndFriendInfo.friendList = mutableListOf<Friend>(

        Friend(

          index = 0,
          description = "Friend ${0 + 1}",

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

      )

      if (allInfoJsonUnsaved == "defaultAll") {

        val editor = sharedPref.edit()

        allInfoJsonUnsaved = Gson().toJson(userAndFriendInfo)

        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)

        editor.apply()

      }

    }

  }

  private fun setBankSpinner(mBankSpinner: Spinner) {

    val networkAdapter = ArrayAdapter.createFromResource(
      view.context,
      R.array.banks_arrays,
      android.R.layout.simple_spinner_item
    )
    networkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    mBankSpinner.adapter = networkAdapter

    mBankSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {

      override fun onNothingSelected(parent: AdapterView<*>?) {}

      override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        mBankSpinner.setSelection(position)
        //view?.context?.let { KToasty.info(it, position.toString(), Toast.LENGTH_SHORT).show() }

      }

    }

  }

  private fun setNetworkSpinner(mNetworkSpinner: Spinner) {

    val networkAdapter = ArrayAdapter.createFromResource(
      view.context,
      R.array.network_arrays,
      android.R.layout.simple_spinner_item
    )
    networkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    mNetworkSpinner.adapter = networkAdapter

    mNetworkSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {

      override fun onNothingSelected(parent: AdapterView<*>?) {
      }
      override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        mNetworkSpinner.setSelection(position)
        //view?.context?.let { KToasty.info(it, position.toString(), Toast.LENGTH_SHORT).show() }

      }

    }

  }

  // https://stackoverflow.com/questions/22066142/search-contacts-and-get-the-contact-number-from-phone-contacts-in-android
  // https://stackoverflow.com/questions/54705225/retrieveing-saved-address-from-phonebook-by-using-a-pick-intent

  private fun pickContact() {

    currentFriend.let { pickContactListener.pickContact(it.index) }

  }




  // https://webcache.googleusercontent.com/search?q=cache:Lh_O238CVYgJ:https://phpinterviewquestions.co.in/blog/ionic/googles-libphonenumber-library-supported-country-list+&cd=8&hl=en&ct=clnk&gl=ng
  // https://github.com/MichaelRocks/libphonenumber-android
  private fun setContact(data: Intent?, index: Int) {

    Log.e("ATTENTION ATTENTION", "SET CONTACT WAS ATTEMPTED")
    val phoneNumberUtil: PhoneNumberUtil =
      PhoneNumberUtil.createInstance(view.context)

    val name: String
    val number: String
    val nationalNumber: String
    val details: Array<String?>? = data?.let { getDetails(it) }
    name = details?.get(1) ?: ""
    number = details?.get(2) ?: ""

    try {

      val  phoneNumber: Phonenumber.PhoneNumber = phoneNumberUtil.parse(number, "NG")
      nationalNumber = phoneNumberUtil.format(
        phoneNumber,
        PhoneNumberUtil.PhoneNumberFormat.NATIONAL
      )


      Log.e("ATTENTION ATTENTION", "SET CONTACT INDEX: $index")
      Log.e("ATTENTION ATTENTION", "SET CONTACT CURRENT FRIEND INDEX: $index")
      Log.e("ATTENTION ATTENTION", " Do They Equal? ${index == currentFriend.index}")
      if (index == currentFriend.index) {

        friendNameEt.setText(name)
        friendPhoneEt1.setText(nationalNumber)

      }

    }
    catch (e: NumberParseException){
      e.printStackTrace()
    }

  }

  private fun getDetails(data: Intent): Array<String?> {

    val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI

    // Get the URI that points to the selected contact
    val contactUri = data.data
    // We only need the NUMBER column, because there will be only one row in the result
    val projection = arrayOf(
      ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
      ContactsContract.CommonDataKinds.Phone.NUMBER
    )

    // Perform the query on the contact to get the NUMBER column
    // We don't need a selection or sort order (there's only one result for the given URI)
    // CAUTION: The query() method should be called from a separate thread to avoid blocking
    // your app's UI thread. (For simplicity of the sample, this code doesn't do that.)
    // Consider using CursorLoader to perform the query.
    val people: Cursor? = contactUri?.let {
      view.context.contentResolver
        .query(it, projection, null, null, null)
    }
    people?.moveToFirst()

    // Retrieve the name and number from the respective columns
    val indexName = people?.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
    val indexNumber = people?.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
    val name = indexName?.let { people.getString(it) }
    val number = indexNumber?.let { people.getString(it) }
    val person = arrayOf(currentFriend.index.toString(), name, number)
    people?.close()

    return person

  }

  lateinit var mNetworkSpinner1: Spinner

  private val REQUEST_READ_CONTACTS_PERMISSION: Int = 20

  var x: Int = 0

  private fun transition(folded: Boolean) {

    val transition: Transition = Fade()
    transition.duration = 600
    transition.addTarget(R.id.h_friend_phone2)
    transition.addTarget(R.id.friendNetworkSpinner2)
    transition.addTarget(R.id.h_friend_phone3)
    transition.addTarget(R.id.friendNetworkSpinner3)
    transition.addTarget(R.id.friendBankSpinner2)
    transition.addTarget(R.id.h_friend_bank_account2)
    transition.addTarget(R.id.friendBankSpinner3)
    transition.addTarget(R.id.h_friend_bank_account3)
    transition.addTarget(R.id.friendBankSpinner4)
    transition.addTarget(R.id.h_friend_bank_account4)
    transition.addTarget(R.id.friendDivider1)
    transition.addTarget(R.id.friendDivider2)
    transition.addTarget(R.id.friendDivider3)
    transition.addTarget(R.id.friendDivider4)
    transition.addTarget(R.id.friendDivider5)
    transition.addTarget(R.id.friendDivider6)


    TransitionManager.beginDelayedTransition(view as ViewGroup, transition)

    if (folded) {

      friendPhoneTil2.visibility = View.GONE
      friendNetworkSpinner2.visibility = View.GONE

      friendPhoneTil3.visibility = View.GONE
      friendNetworkSpinner3.visibility = View.GONE

      friendBankSpinner2.visibility = View.GONE
      friendBankAccountTil2.visibility = View.GONE

      friendBankSpinner3.visibility = View.GONE
      friendBankAccountTil3.visibility = View.GONE

      friendBankSpinner4.visibility = View.GONE
      friendBankAccountTil4.visibility = View.GONE

      friendDivider1.visibility = View.GONE
      friendDivider2.visibility = View.GONE
      friendDivider3.visibility = View.GONE
      friendDivider4.visibility = View.GONE
      friendDivider5.visibility = View.GONE
      friendDivider6.visibility = View.GONE

      showMoreDetailsIv.setImageResource(R.drawable.ic_baseline_unfold_less_48)

    }
    else {

      friendPhoneTil2.visibility = View.VISIBLE
      friendNetworkSpinner2.visibility = View.VISIBLE

      friendPhoneTil3.visibility = View.VISIBLE
      friendNetworkSpinner3.visibility = View.VISIBLE

      friendBankSpinner2.visibility = View.VISIBLE
      friendBankAccountTil2.visibility = View.VISIBLE

      friendBankSpinner3.visibility = View.VISIBLE
      friendBankAccountTil3.visibility = View.VISIBLE

      friendBankSpinner4.visibility = View.VISIBLE
      friendBankAccountTil4.visibility = View.VISIBLE

      friendDivider1.visibility = View.VISIBLE
      friendDivider2.visibility = View.VISIBLE
      friendDivider3.visibility = View.VISIBLE
      friendDivider4.visibility = View.VISIBLE
      friendDivider5.visibility = View.VISIBLE
      friendDivider6.visibility = View.VISIBLE

      showMoreDetailsIv.setImageResource(R.drawable.ic_baseline_unfold_more_48)

    }

  }

  override fun bind(model: Friend) {

    currentFriend = model

    val res: Resources = view.context.resources

    val bankStringArray: Array<String>  = res.getStringArray(R.array.banks_arrays)
    val networkStringArray: Array<String>  = res.getStringArray(R.array.network_arrays)

    readFromLocal() // userAndFriendInfo initialized

    setNetworkSpinner(friendNetworkSpinner1) // TODO Might need to convert to filterTo
    setNetworkSpinner(friendNetworkSpinner2)
    setNetworkSpinner(friendNetworkSpinner3)

    setBankSpinner(friendBankSpinner1)
    setBankSpinner(friendBankSpinner2)
    setBankSpinner(friendBankSpinner3)
    setBankSpinner(friendBankSpinner4)

    // TODO Done
    pickFriendIB.setOnClickListener{


      if (ActivityCompat.checkSelfPermission(view.context, Manifest.permission.READ_CONTACTS) !=
        PackageManager.PERMISSION_GRANTED) {

        KToasty.info(
          view.context,
          "Allow Recharge App to read contacts in order to select friend.",
          Toast.LENGTH_LONG
        ).show()

        ActivityCompat.requestPermissions(
          activity, arrayOf(
            Manifest.permission.READ_CONTACTS
          ),
          REQUEST_READ_CONTACTS_PERMISSION
        )

      }
      else {

        pickContact()

      }


//      CoroutineScope(Dispatchers.Main).launch {
//
//        val result = (activity as FragmentActivity).permissionsBuilder(
//          Manifest.permission.READ_CONTACTS
//        ).build().sendSuspend()
//
//        if (result.allGranted()) { // All the permissions are granted.
//
//          pickContact()
//
//        }
//        else {
//
//          val message: String = "Allow Recharge App to read contacts in order to select friend."
//          util.onShowMessage(message, view.context, view)
//
//        }
//
//      }




    }

    // TODO Done
    showMoreDetailsIv.setOnClickListener {

      val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
      var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
      val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

      val folded: Boolean
      if (isExpanded == null) {
        folded = !model.folded
        isExpanded = folded
      }
      else {
        folded = !isExpanded!!
        isExpanded = !isExpanded!!
      }

      if (allInfoUnsaved.friendList?.size ?: 0 > model.index) {

        userAndFriendInfo.friendList?.get(model.index)?.folded = folded
        allInfoUnsaved.friendList?.get(model.index)?.folded = folded

      }
      else {

        val newFriend: Friend =

          Friend(
            index = model.index,
            description = "Friend ${model.index + 1}",

            folded = folded,

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

        userAndFriendInfo.friendList?.add(newFriend)
        allInfoUnsaved.friendList?.add(newFriend)

      }

      allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)

      val editor = sharedPref!!.edit()
      editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
      editor.apply()

      transition(folded)

    }

    // TODO Done
    friendNameEt.addTextChangedListener(object : TextWatcher {

      override fun afterTextChanged(s: Editable) {}
      override fun beforeTextChanged(
        s: CharSequence, start: Int,
        count: Int, after: Int
      ) {
      }

      override fun onTextChanged(
        s: CharSequence, start: Int,
        before: Int, count: Int
      ) {

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        if (allInfoUnsaved.friendList?.size ?: 0 > model.index) {

          val name: String = friendNameEt.text.toString()
          userAndFriendInfo.friendList?.get(model.index)?.name = name
          allInfoUnsaved.friendList?.get(model.index)?.name = name

        }
        else {

          val newFriend: Friend =

            Friend(
              index = model.index,
              description = "Friend ${model.index + 1}",

              folded = false,

              name = s.toString(),
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

          userAndFriendInfo.friendList?.add(newFriend)
          allInfoUnsaved.friendList?.add(newFriend)

        }

        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string

        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

//        allInfoJson = sharedPref.getString("allInfoSaved", "defaultAll").toString()
//        val allInfoSaved = gson.fromJson(allInfoJson, UserAndFriendInfo::class.java)

      }

    })

    friendPhoneEt1.addTextChangedListener(object : TextWatcher {

      override fun afterTextChanged(s: Editable) {}
      override fun beforeTextChanged(
        s: CharSequence, start: Int,
        count: Int, after: Int
      ) {
      }

      override fun onTextChanged(
        s: CharSequence, start: Int,
        before: Int, count: Int
      ) {

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        val phone1: String = friendPhoneEt1.text.toString()
        userAndFriendInfo.friendList?.get(model.index)?.phone1 = phone1
        allInfoUnsaved.friendList?.get(model.index)?.phone1 = phone1
        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string

        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    })

    friendPhoneEt2.addTextChangedListener(object : TextWatcher {

      override fun afterTextChanged(s: Editable) {}
      override fun beforeTextChanged(
        s: CharSequence, start: Int,
        count: Int, after: Int
      ) {
      }

      override fun onTextChanged(
        s: CharSequence, start: Int,
        before: Int, count: Int
      ) {

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        val phone2: String = friendPhoneEt2.text.toString()
        userAndFriendInfo.friendList?.get(model.index)?.phone2 = phone2
        allInfoUnsaved.friendList?.get(model.index)?.phone2 = phone2
        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string

        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    })

    friendPhoneEt3.addTextChangedListener(object : TextWatcher {

      override fun afterTextChanged(s: Editable) {}
      override fun beforeTextChanged(
        s: CharSequence, start: Int,
        count: Int, after: Int
      ) {
      }

      override fun onTextChanged(
        s: CharSequence, start: Int,
        before: Int, count: Int
      ) {

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        val phone3: String = friendPhoneEt3.text.toString()
        userAndFriendInfo.friendList?.get(model.index)?.phone3 = phone3
        allInfoUnsaved.friendList?.get(model.index)?.phone3 = phone3
        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string

        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    })

    friendNetworkSpinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

      override fun onNothingSelected(parent: AdapterView<*>?) {}

      override fun onItemSelected(parent: AdapterView<*>?, view2: View?, position: Int, id: Long) {

        val selectedItem = parent?.getItemAtPosition(position).toString()

        val network1: String = selectedItem

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

//        if (!(allInfoUnsaved.friendList?.size ?: 0 >= model.index ||
//          userAndFriendInfo.friendList?.size ?: 0 >= model.index)) {
//
//          if (selectedItem != "Choose Network") {
//            userAndFriendInfo.friendList?.get(model.index)?.network1 = network1
//            allInfoUnsaved.friendList?.get(model.index)?.network1 = network1
//          }
//          else {
//            userAndFriendInfo.friendList?.get(model.index)?.network1 = null
//            allInfoUnsaved.friendList?.get(model.index)?.network1 = null
//          }
//
//        }

        if (selectedItem != "Choose Network") {
          userAndFriendInfo.friendList?.get(model.index)?.network1 = network1
          allInfoUnsaved.friendList?.get(model.index)?.network1 = network1
        }
        else {

          try {

            userAndFriendInfo.friendList?.get(model.index)?.network1 = null
            allInfoUnsaved.friendList?.get(model.index)?.network1 = null

          }
          catch (e: Exception) {

            Log.e("ATTENTION ATTENTION", "Edit Friend ${model.index} error")

          }

        }

        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string
        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    }

    friendNetworkSpinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

      override fun onNothingSelected(parent: AdapterView<*>?) {}

      override fun onItemSelected(parent: AdapterView<*>?, view2: View?, position: Int, id: Long) {

        val selectedItem = parent?.getItemAtPosition(position).toString()

        val network2: String = selectedItem

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

//        if (!(allInfoUnsaved.friendList?.size ?: 0 >= model.index ||
//                  userAndFriendInfo.friendList?.size ?: 0 >= model.index)) {
//
//          if (selectedItem != "Choose Network") {
//            userAndFriendInfo.friendList?.get(model.index)?.network2 = network2
//            allInfoUnsaved.friendList?.get(model.index)?.network2 = network2
//          }
//          else {
//            userAndFriendInfo.friendList?.get(model.index)?.network2 = null
//            allInfoUnsaved.friendList?.get(model.index)?.network2 = null
//          }
//
//        }

        if (selectedItem != "Choose Network") {
          userAndFriendInfo.friendList?.get(model.index)?.network2 = network2
          allInfoUnsaved.friendList?.get(model.index)?.network2 = network2
        }
        else {

          try {

            userAndFriendInfo.friendList?.get(model.index)?.network2 = null
            allInfoUnsaved.friendList?.get(model.index)?.network2 = null

          }
          catch (e: Exception) {

            Log.e("ATTENTION ATTENTION", "Edit Friend ${model.index} error")

          }

        }

        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string
        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    }

    friendNetworkSpinner3.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

      override fun onNothingSelected(parent: AdapterView<*>?) {}

      override fun onItemSelected(parent: AdapterView<*>?, view2: View?, position: Int, id: Long) {

        val selectedItem = parent?.getItemAtPosition(position).toString()

        val network3: String = selectedItem

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

//        if (!(allInfoUnsaved.friendList?.size ?: 0 >= model.index ||
//                  userAndFriendInfo.friendList?.size ?: 0 >= model.index)) {
//
//          if (selectedItem != "Choose Network") {
//            userAndFriendInfo.friendList?.get(model.index)?.network3 = network3
//            allInfoUnsaved.friendList?.get(model.index)?.network3 = network3
//          }
//          else {
//            userAndFriendInfo.friendList?.get(model.index)?.network3 = null
//            allInfoUnsaved.friendList?.get(model.index)?.network3 = null
//          }
//
//        }

        if (selectedItem != "Choose Network") {
          userAndFriendInfo.friendList?.get(model.index)?.network3 = network3
          allInfoUnsaved.friendList?.get(model.index)?.network3 = network3
        }
        else {

          try {

            userAndFriendInfo.friendList?.get(model.index)?.network3 = null
            allInfoUnsaved.friendList?.get(model.index)?.network3 = null

          }
          catch (e: Exception) {

            Log.e("ATTENTION ATTENTION", "Edit Friend ${model.index} error")

          }

        }

        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string
        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    }

    friendBankSpinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

      override fun onNothingSelected(parent: AdapterView<*>?) {}

      override fun onItemSelected(parent: AdapterView<*>?, view2: View?, position: Int, id: Long) {

        val selectedItem = parent?.getItemAtPosition(position).toString()

        val bank1: String = selectedItem

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

//        if (!(allInfoUnsaved.friendList?.size ?: 0 >= model.index ||
//                  userAndFriendInfo.friendList?.size ?: 0 >= model.index)) {
//
//          if (selectedItem != "Choose Network") {
//            userAndFriendInfo.friendList?.get(model.index)?.bank1 = bank1
//            allInfoUnsaved.friendList?.get(model.index)?.bank1 = bank1
//          }
//          else {
//            userAndFriendInfo.friendList?.get(model.index)?.bank1 = null
//            allInfoUnsaved.friendList?.get(model.index)?.bank1 = null
//          }
//
//
//        }

        if (selectedItem != "Choose Bank") {
          userAndFriendInfo.friendList?.get(model.index)?.bank1 = bank1
          allInfoUnsaved.friendList?.get(model.index)?.bank1 = bank1
        }
        else {

          try {

            userAndFriendInfo.friendList?.get(model.index)?.bank1 = null
            allInfoUnsaved.friendList?.get(model.index)?.bank1 = null

          }
          catch (e: Exception) {

            Log.e("ATTENTION ATTENTION", "Edit Friend ${model.index} error")

          }

        }

        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string
        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    }

    friendBankSpinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

      override fun onNothingSelected(parent: AdapterView<*>?) {}

      override fun onItemSelected(parent: AdapterView<*>?, view2: View?, position: Int, id: Long) {

        val selectedItem = parent?.getItemAtPosition(position).toString()

        val bank2: String = selectedItem

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

//        if (!(allInfoUnsaved.friendList?.size ?: 0 >= model.index ||
//                  userAndFriendInfo.friendList?.size ?: 0 >= model.index)) {
//
//          if (selectedItem != "Choose Network") {
//            userAndFriendInfo.friendList?.get(model.index)?.bank2 = bank2
//            allInfoUnsaved.friendList?.get(model.index)?.bank2 = bank2
//          }
//          else {
//            userAndFriendInfo.friendList?.get(model.index)?.bank2 = null
//            allInfoUnsaved.friendList?.get(model.index)?.bank2 = null
//          }
//
//        }

        if (selectedItem != "Choose Bank") {
          userAndFriendInfo.friendList?.get(model.index)?.bank2 = bank2
          allInfoUnsaved.friendList?.get(model.index)?.bank2 = bank2
        }
        else {

          try {

            userAndFriendInfo.friendList?.get(model.index)?.bank2 = null
            allInfoUnsaved.friendList?.get(model.index)?.bank2 = null

          }
          catch (e: Exception) {

            Log.e("ATTENTION ATTENTION", "Edit Friend ${model.index} error")

          }

        }

        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string
        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    }

    friendBankSpinner3.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

      override fun onNothingSelected(parent: AdapterView<*>?) {}

      override fun onItemSelected(parent: AdapterView<*>?, view2: View?, position: Int, id: Long) {

        val selectedItem = parent?.getItemAtPosition(position).toString()

        val bank3: String = selectedItem

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

//        if (!(allInfoUnsaved.friendList?.size ?: 0 >= model.index ||
//                  userAndFriendInfo.friendList?.size ?: 0 >= model.index)) {
//
//          if (selectedItem != "Choose Network") {
//            userAndFriendInfo.friendList?.get(model.index)?.bank3 = bank3
//            allInfoUnsaved.friendList?.get(model.index)?.bank3 = bank3
//          }
//          else {
//            userAndFriendInfo.friendList?.get(model.index)?.bank3 = null
//            allInfoUnsaved.friendList?.get(model.index)?.bank3 = null
//          }
//
//        }

        if (selectedItem != "Choose Bank") {
          userAndFriendInfo.friendList?.get(model.index)?.bank3 = bank3
          allInfoUnsaved.friendList?.get(model.index)?.bank3 = bank3
        }
        else {

          try {

            userAndFriendInfo.friendList?.get(model.index)?.bank3 = null
            allInfoUnsaved.friendList?.get(model.index)?.bank3 = null

          }
          catch (e: Exception) {

            Log.e("ATTENTION ATTENTION", "Edit Friend ${model.index} error")

          }

        }

        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string
        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    }

    friendBankSpinner4.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

      override fun onNothingSelected(parent: AdapterView<*>?) {}

      override fun onItemSelected(parent: AdapterView<*>?, view2: View?, position: Int, id: Long) {

        val selectedItem = parent?.getItemAtPosition(position).toString()

        val bank4: String = selectedItem

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

//        if (!(allInfoUnsaved.friendList?.size ?: 0 >= model.index ||
//                  userAndFriendInfo.friendList?.size ?: 0 >= model.index)) {
//
//          if (selectedItem != "Choose Network") {
//            userAndFriendInfo.friendList?.get(model.index)?.bank4 = bank4
//            allInfoUnsaved.friendList?.get(model.index)?.bank4 = bank4
//          }
//          else {
//            userAndFriendInfo.friendList?.get(model.index)?.bank4 = null
//            allInfoUnsaved.friendList?.get(model.index)?.bank4 = null
//          }
//
//        }

        if (selectedItem != "Choose Bank") {
          userAndFriendInfo.friendList?.get(model.index)?.bank4 = bank4
          allInfoUnsaved.friendList?.get(model.index)?.bank4 = bank4
        }
        else {

          try {

            userAndFriendInfo.friendList?.get(model.index)?.bank4 = null
            allInfoUnsaved.friendList?.get(model.index)?.bank4 = null

          }
          catch (e: Exception) {

            Log.e("ATTENTION ATTENTION", "Edit Friend ${model.index} error")

          }

        }

        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string
        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    }

    friendBankAccountEt1.addTextChangedListener(object : TextWatcher {

      override fun afterTextChanged(s: Editable) {}
      override fun beforeTextChanged(
        s: CharSequence, start: Int,
        count: Int, after: Int
      ) {
      }

      override fun onTextChanged(
        s: CharSequence, start: Int,
        before: Int, count: Int
      ) {

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        val accountNumber1: String = friendBankAccountEt1.text.toString()

        if (allInfoUnsaved.friendList?.size ?: 0 > model.index) {

          userAndFriendInfo.friendList?.get(model.index)?.accountNumber1 = accountNumber1
          allInfoUnsaved.friendList?.get(model.index)?.accountNumber1 = accountNumber1
          allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string

          val editor = sharedPref!!.edit()
          editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
          editor.apply()

        }

      }

    })

    friendBankAccountEt2.addTextChangedListener(object : TextWatcher {

      override fun afterTextChanged(s: Editable) {}
      override fun beforeTextChanged(
        s: CharSequence, start: Int,
        count: Int, after: Int
      ) {
      }

      override fun onTextChanged(
        s: CharSequence, start: Int,
        before: Int, count: Int
      ) {

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        val accountNumber2: String = friendBankAccountEt2.text.toString()

        if (allInfoUnsaved.friendList?.size ?: 0 > model.index) {

          userAndFriendInfo.friendList?.get(model.index)?.accountNumber2 = accountNumber2
          allInfoUnsaved.friendList?.get(model.index)?.accountNumber2 = accountNumber2
          allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string

          val editor = sharedPref!!.edit()
          editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
          editor.apply()

        }

      }

    })

    friendBankAccountEt3.addTextChangedListener(object : TextWatcher {

      override fun afterTextChanged(s: Editable) {}
      override fun beforeTextChanged(
        s: CharSequence, start: Int,
        count: Int, after: Int
      ) {
      }

      override fun onTextChanged(
        s: CharSequence, start: Int,
        before: Int, count: Int
      ) {

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        val accountNumber3: String = friendBankAccountEt3.text.toString()

        if (allInfoUnsaved.friendList?.size ?: 0 > model.index) {

          userAndFriendInfo.friendList?.get(model.index)?.accountNumber3 = accountNumber3
          allInfoUnsaved.friendList?.get(model.index)?.accountNumber3 = accountNumber3
          allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string

          val editor = sharedPref!!.edit()
          editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
          editor.apply()

        }

      }

    })

    friendBankAccountEt4.addTextChangedListener(object : TextWatcher {

      override fun afterTextChanged(s: Editable) {}
      override fun beforeTextChanged(
        s: CharSequence, start: Int,
        count: Int, after: Int
      ) {
      }

      override fun onTextChanged(
        s: CharSequence, start: Int,
        before: Int, count: Int
      ) {

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        val accountNumber4: String = friendBankAccountEt4.text.toString()

        if (allInfoUnsaved.friendList?.size ?: 0 > model.index) {

          userAndFriendInfo.friendList?.get(model.index)?.accountNumber4 = accountNumber4
          allInfoUnsaved.friendList?.get(model.index)?.accountNumber4 = accountNumber4
          allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string

          val editor = sharedPref!!.edit()
          editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
          editor.apply()

        }

      }

    })


    model.run {

      val friendNumberString: String = "Friend ${(index+1).toString()}"

      friendNumberTv.text = friendNumberString

      if (name != null) {
        friendNameEt.setText(name)
      }

      if (phone1 != null) {
        friendPhoneEt1.setText(phone1)
      }

      if (phone2 != null) {
        friendPhoneEt2.setText(phone2)
      }

      if (phone3 != null) {
        friendPhoneEt3.setText(phone3)
      }

      if (network1 != null) {
        val networkItemPosition: Int = networkStringArray.indexOf(network1)
        friendNetworkSpinner1.setSelection(networkItemPosition)
      }

      if (network2 != null) {
        val networkItemPosition: Int = networkStringArray.indexOf(network2)
        friendNetworkSpinner2.setSelection(networkItemPosition)
      }

      if (network3 != null) {
        val networkItemPosition: Int = networkStringArray.indexOf(network3)
        friendNetworkSpinner3.setSelection(networkItemPosition)
      }

      if (bank1 != null) {
        val bankItemPosition: Int = bankStringArray.indexOf(bank1)
        friendBankSpinner1.setSelection(bankItemPosition)
      }

      if (bank2 != null) {
        val bankItemPosition: Int = bankStringArray.indexOf(bank2)
        friendBankSpinner2.setSelection(bankItemPosition)
      }

      if (bank3 != null) {
        val bankItemPosition: Int = bankStringArray.indexOf(bank3)
        friendBankSpinner3.setSelection(bankItemPosition)
      }

      if (bank4 != null) {
        val bankItemPosition: Int = bankStringArray.indexOf(bank4)
        friendBankSpinner4.setSelection(bankItemPosition)
      }

      if (accountNumber1 != null) {
        friendBankAccountEt1.setText(accountNumber1)
      }

      if (accountNumber2 != null) {
        friendBankAccountEt2.setText(accountNumber2)
      }

      if (accountNumber3 != null) {
        friendBankAccountEt3.setText(accountNumber3)
      }

      if (accountNumber4 != null) {
        friendBankAccountEt4.setText(accountNumber4)
      }

      transition(folded)


//      friendNameEt.setText(name)
//
//      friendPhoneEt1.setText(phone1)
//      friendPhoneEt2.setText(phone2)
//      friendPhoneEt3.setText(phone3)
//
//      setNetworkSpinner(friendNetworkSpinner1)
//      setNetworkSpinner(friendNetworkSpinner2)
//      setNetworkSpinner(friendNetworkSpinner3)
//
//      friendBankAccountEt1.setText(accountNumber1)
//      friendBankAccountEt2.setText(accountNumber2)
//      friendBankAccountEt3.setText(accountNumber3)
//      friendBankAccountEt4.setText(accountNumber4)
//
//      setBankSpinner(friendBankSpinner1)
//      setBankSpinner(friendBankSpinner2)
//      setBankSpinner(friendBankSpinner3)
//      setBankSpinner(friendBankSpinner4)

    }

  }

//  override fun contactPicked(data: Intent?, index: Int) {
//
////    val name: String
////    val number: String
////    var nationalNumber: String
////    val details = getDetails(data!!)
////    index = details[0]?.toInt()!!
////    name = details[1]!!
////    number = details[2]!!
//
//    Log.e("ATTENTION ATTENTION", "override fun contactPicked(...) RAN")
//
//    setContact(data, index)
//
//  }


}