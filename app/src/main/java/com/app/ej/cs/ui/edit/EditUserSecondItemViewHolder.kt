package com.app.ej.cs.ui.edit

import android.content.Context
import android.content.res.Resources
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.app.ej.cs.common.Binder
import com.app.ej.cs.common.events.OnViewHolderItemSelected
import com.app.ej.cs.R
import com.app.ej.cs.repository.entity.Friend
import com.app.ej.cs.repository.entity.User
import com.app.ej.cs.repository.entity.UserAndFriendInfo
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson

/**
 * The ViewHolder for the NewsList RecyclerView Adapter
 */

class EditUserSecondItemViewHolder(

  private val view: View,
  listener: OnViewHolderItemSelected<User?>? = null

) : RecyclerView.ViewHolder(view), Binder<User> {

  private val showMoreDetailsIv: ImageView = view.findViewById(R.id.showMoreHandleView)

//  private val nameEt: TextInputEditText = view.findViewById(R.id.nameEt)
//  private val emailEt: TextInputEditText = view.findViewById(R.id.emailEt)

  private val phone2Et: TextInputEditText = view.findViewById(R.id.phone2Et)
  private val network2Spinner: Spinner = view.findViewById(R.id.network2Spinner)
  private val pin2Et: TextInputEditText = view.findViewById(R.id.pin2Et)

  private val p2Bank1Spinner: Spinner = view.findViewById(R.id.p2Bank1Spinner)
  private val p2Bank2Spinner: Spinner = view.findViewById(R.id.p2Bank2Spinner)
  private val p2Bank3Spinner: Spinner = view.findViewById(R.id.p2Bank3Spinner)
  private val p2Bank4Spinner: Spinner = view.findViewById(R.id.p2Bank4Spinner)

  private val smartCardNumber1Et: TextInputEditText = view.findViewById(R.id.smartCardNumber21Et)
  private val smartCardNumber2Et: TextInputEditText = view.findViewById(R.id.smartCardNumber22Et)
  private val smartCardNumber3Et: TextInputEditText = view.findViewById(R.id.smartCardNumber23Et)
  private val smartCardNumber4Et: TextInputEditText = view.findViewById(R.id.smartCardNumber24Et)

  private val smartCardNumberTil2: TextInputLayout = view.findViewById(R.id.h_smartCardNumber22)
  private val smartCardNumberTil3: TextInputLayout = view.findViewById(R.id.h_smartCardNumber23)
  private val smartCardNumberTil4: TextInputLayout = view.findViewById(R.id.h_smartCardNumber24)

  private val meterNumber1Et: TextInputEditText = view.findViewById(R.id.meterNumber21Et)
  private val meterNumber2Et: TextInputEditText = view.findViewById(R.id.meterNumber22Et)
  private val meterNumber3Et: TextInputEditText = view.findViewById(R.id.meterNumber23Et)

  private val meterNumberTil2: TextInputLayout = view.findViewById(R.id.h_meterNumber22)
  private val meterNumberTil3: TextInputLayout = view.findViewById(R.id.h_meterNumber23)

  private var currentUser: User? = null

  private val PREFNAME: String = "local_user"

  private val PREFNAMEUNSAVED: String = "local_user_unsaved"

  private var userAndFriendInfo: UserAndFriendInfo = UserAndFriendInfo()

  private val gson = Gson()

  private var isExpanded: Boolean? = null

  init {
    listener?.let { l ->
      view.setOnClickListener { l(currentUser) }
    }
  }








  private fun readFromLocal() {

    val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)

    val allInfoJsonSaved: String? = sharedPref.getString("allInfoSaved", "defaultAll")
    var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!

    if (allInfoJsonSaved != "defaultAll") {

      val allInfo = gson.fromJson(allInfoJsonSaved, UserAndFriendInfo::class.java)

      userAndFriendInfo.usersList   = allInfo.usersList

      if (allInfo.friendList != null) {
        userAndFriendInfo.friendList = allInfo.friendList!!
      }

    }
    else {

      userAndFriendInfo.usersList = mutableListOf<User> (

        User(
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
        ),
        User(
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

    val bankAdapter = ArrayAdapter.createFromResource(
      view.context,
      R.array.banks_arrays,
      android.R.layout.simple_spinner_item
    )
    bankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    mBankSpinner.adapter = bankAdapter

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

      override fun onNothingSelected(parent: AdapterView<*>?) {}
      override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        mNetworkSpinner.setSelection(position)
        //view?.context?.let { KToasty.info(it, position.toString(), Toast.LENGTH_SHORT).show() }

      }

    }

  }

  private fun transition(folded: Boolean) {

    val transition: Transition = Fade()
    transition.duration = 600
    transition.addTarget(R.id.h_smartCardNumber22)
    transition.addTarget(R.id.h_smartCardNumber23)
    transition.addTarget(R.id.h_smartCardNumber24)
    transition.addTarget(R.id.h_meterNumber22)
    transition.addTarget(R.id.h_meterNumber23)

    TransitionManager.beginDelayedTransition(view as ViewGroup, transition)

    if (folded) {

      smartCardNumberTil2.visibility = View.GONE
      smartCardNumberTil3.visibility = View.GONE
      smartCardNumberTil4.visibility = View.GONE

      meterNumberTil2.visibility = View.GONE
      meterNumberTil3.visibility = View.GONE

      showMoreDetailsIv.setImageResource(R.drawable.ic_baseline_unfold_less_48)

    }
    else {

      smartCardNumberTil2.visibility = View.VISIBLE
      smartCardNumberTil3.visibility = View.VISIBLE
      smartCardNumberTil4.visibility = View.VISIBLE

      meterNumberTil2.visibility = View.VISIBLE
      meterNumberTil3.visibility = View.VISIBLE

      showMoreDetailsIv.setImageResource(R.drawable.ic_baseline_unfold_more_48)

    }

  }

  override fun bind(model: User) {

    currentUser = model

    val res: Resources = view.context.resources

    val bankStringArray: Array<String>     = res.getStringArray(R.array.banks_arrays)
    val networkStringArray: Array<String>  = res.getStringArray(R.array.network_arrays)

    readFromLocal() // userAndFriendInfo initialized

    setNetworkSpinner(network2Spinner)
    setBankSpinner(p2Bank1Spinner)
    setBankSpinner(p2Bank2Spinner)
    setBankSpinner(p2Bank3Spinner)
    setBankSpinner(p2Bank4Spinner)

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

      userAndFriendInfo.usersList[model.index].folded = folded
      allInfoUnsaved.usersList[model.index].folded = folded

      allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)

      val editor = sharedPref!!.edit()
      editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
      editor.apply()

      transition(folded)

    }

    phone2Et.addTextChangedListener(object : TextWatcher {

      override fun afterTextChanged(s: Editable) {}
      override fun beforeTextChanged(
        s: CharSequence, start: Int,
        count: Int, after: Int
      ) {}

      override fun onTextChanged(
        s: CharSequence, start: Int,
        before: Int, count: Int
      ) {

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        val phone: String = phone2Et.text.toString()
        userAndFriendInfo.usersList[model.index].phone = phone
        allInfoUnsaved.usersList[model.index].phone = phone
        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string

        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    })

    pin2Et.addTextChangedListener(object : TextWatcher {

      override fun afterTextChanged(s: Editable) {}
      override fun beforeTextChanged(
        s: CharSequence, start: Int,
        count: Int, after: Int
      ) {}

      override fun onTextChanged(
        s: CharSequence, start: Int,
        before: Int, count: Int
      ) {

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        val pin: String = pin2Et.text.toString()
        userAndFriendInfo.usersList[model.index].pin = pin
        allInfoUnsaved.usersList[model.index].pin = pin
        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string

        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    })

    smartCardNumber1Et.addTextChangedListener(object : TextWatcher {

      override fun afterTextChanged(s: Editable) {}
      override fun beforeTextChanged(
        s: CharSequence, start: Int,
        count: Int, after: Int
      ) {}

      override fun onTextChanged(
        s: CharSequence, start: Int,
        before: Int, count: Int
      ) {

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        val smartCardNumber: String = smartCardNumber1Et.text.toString()
        userAndFriendInfo.usersList[model.index].smartCardNumber1 = smartCardNumber
        allInfoUnsaved.usersList[model.index].smartCardNumber1 = smartCardNumber
        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string

        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    })

    smartCardNumber2Et.addTextChangedListener(object : TextWatcher {

      override fun afterTextChanged(s: Editable) {}
      override fun beforeTextChanged(
        s: CharSequence, start: Int,
        count: Int, after: Int
      ) {}

      override fun onTextChanged(
        s: CharSequence, start: Int,
        before: Int, count: Int
      ) {

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        val smartCardNumber: String = smartCardNumber2Et.text.toString()
        userAndFriendInfo.usersList[model.index].smartCardNumber2 = smartCardNumber
        allInfoUnsaved.usersList[model.index].smartCardNumber2 = smartCardNumber
        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string

        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    })

    smartCardNumber3Et.addTextChangedListener(object : TextWatcher {

      override fun afterTextChanged(s: Editable) {}
      override fun beforeTextChanged(
        s: CharSequence, start: Int,
        count: Int, after: Int
      ) {}

      override fun onTextChanged(
        s: CharSequence, start: Int,
        before: Int, count: Int
      ) {

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        val smartCardNumber: String = smartCardNumber3Et.text.toString()
        userAndFriendInfo.usersList[model.index].smartCardNumber3 = smartCardNumber
        allInfoUnsaved.usersList[model.index].smartCardNumber3 = smartCardNumber
        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string

        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    })

    smartCardNumber4Et.addTextChangedListener(object : TextWatcher {

      override fun afterTextChanged(s: Editable) {}
      override fun beforeTextChanged(
        s: CharSequence, start: Int,
        count: Int, after: Int
      ) {}

      override fun onTextChanged(
        s: CharSequence, start: Int,
        before: Int, count: Int
      ) {

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        val smartCardNumber: String = smartCardNumber4Et.text.toString()
        userAndFriendInfo.usersList[model.index].smartCardNumber4 = smartCardNumber
        allInfoUnsaved.usersList[model.index].smartCardNumber4 = smartCardNumber
        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string

        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    })

    meterNumber1Et.addTextChangedListener(object : TextWatcher {

      override fun afterTextChanged(s: Editable) {}
      override fun beforeTextChanged(
        s: CharSequence, start: Int,
        count: Int, after: Int
      ) {}

      override fun onTextChanged(
        s: CharSequence, start: Int,
        before: Int, count: Int
      ) {

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        val meterNumber: String = meterNumber1Et.text.toString()
        userAndFriendInfo.usersList[model.index].meterNumber1 = meterNumber
        allInfoUnsaved.usersList[model.index].meterNumber1 = meterNumber
        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string

        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    })

    meterNumber2Et.addTextChangedListener(object : TextWatcher {

      override fun afterTextChanged(s: Editable) {}
      override fun beforeTextChanged(
        s: CharSequence, start: Int,
        count: Int, after: Int
      ) {}

      override fun onTextChanged(
        s: CharSequence, start: Int,
        before: Int, count: Int
      ) {

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        val meterNumber: String = meterNumber2Et.text.toString()
        userAndFriendInfo.usersList[model.index].meterNumber2 = meterNumber
        allInfoUnsaved.usersList[model.index].meterNumber2 = meterNumber
        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string

        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    })

    meterNumber3Et.addTextChangedListener(object : TextWatcher {

      override fun afterTextChanged(s: Editable) {}
      override fun beforeTextChanged(
        s: CharSequence, start: Int,
        count: Int, after: Int
      ) {}

      override fun onTextChanged(
        s: CharSequence, start: Int,
        before: Int, count: Int
      ) {

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        val meterNumber: String = meterNumber3Et.text.toString()
        userAndFriendInfo.usersList[model.index].meterNumber3 = meterNumber
        allInfoUnsaved.usersList[model.index].meterNumber3 = meterNumber
        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string

        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    })

    network2Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

      override fun onNothingSelected(parent: AdapterView<*>?) {}

      override fun onItemSelected(parent: AdapterView<*>?, view2: View?, position: Int, id: Long) {

        val selectedItem = parent?.getItemAtPosition(position).toString()

        val network: String = selectedItem

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        Log.e("ATTENTION ATTENTION", "Selected network 2: $network")
        Log.e("ATTENTION ATTENTION", "before selectedItem != \"Choose Network\"")

        if (selectedItem != "Choose Network") {
          Log.e("ATTENTION ATTENTION", "after selectedItem != \"Choose Network\"")
          Log.e("ATTENTION ATTENTION", "Model Index: ${model.index} Network: ${model.network} ")
          userAndFriendInfo.usersList[model.index].network = network
          allInfoUnsaved.usersList[model.index].network = network
        }
        else {
          userAndFriendInfo.usersList[model.index].network = null
          allInfoUnsaved.usersList[model.index].network = null
        }

        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string
        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    }

    p2Bank1Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

      override fun onNothingSelected(parent: AdapterView<*>?) {}

      override fun onItemSelected(parent: AdapterView<*>?, view2: View?, position: Int, id: Long) {

        val selectedItem = parent?.getItemAtPosition(position).toString()

        val bank1: String = selectedItem

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        Log.e("ATTENTION ATTENTION", "Selected bank 1: $bank1")
        Log.e("ATTENTION ATTENTION", "before selectedItem != \"Choose Bank\"")

        if (selectedItem != "Choose Bank") {
          Log.e("ATTENTION ATTENTION", "after selectedItem != \"Choose Bank\"")
          userAndFriendInfo.usersList[model.index].bank1 = bank1
          allInfoUnsaved.usersList[model.index].bank1 = bank1
        }
        else {
          userAndFriendInfo.usersList[model.index].bank1 = null
          allInfoUnsaved.usersList[model.index].bank1 = null
        }

        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string
        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    }

    p2Bank2Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

      override fun onNothingSelected(parent: AdapterView<*>?) {}

      override fun onItemSelected(parent: AdapterView<*>?, view2: View?, position: Int, id: Long) {

        val selectedItem = parent?.getItemAtPosition(position).toString()

        val bank2: String = selectedItem

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        if (selectedItem != "Choose Bank") {
          userAndFriendInfo.usersList[model.index].bank2 = bank2
          allInfoUnsaved.usersList[model.index].bank2 = bank2
        }
        else {
          userAndFriendInfo.usersList[model.index].bank2 = null
          allInfoUnsaved.usersList[model.index].bank2 = null
        }

        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string
        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    }

    p2Bank3Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

      override fun onNothingSelected(parent: AdapterView<*>?) {}

      override fun onItemSelected(parent: AdapterView<*>?, view2: View?, position: Int, id: Long) {

        val selectedItem = parent?.getItemAtPosition(position).toString()

        val bank3: String = selectedItem

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        if (selectedItem != "Choose Bank") {
          userAndFriendInfo.usersList[model.index].bank3 = bank3
          allInfoUnsaved.usersList[model.index].bank3 = bank3
        }
        else {
          userAndFriendInfo.usersList[model.index].bank3 = null
          allInfoUnsaved.usersList[model.index].bank3 = null
        }

        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string
        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    }

    p2Bank4Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

      override fun onNothingSelected(parent: AdapterView<*>?) {}

      override fun onItemSelected(parent: AdapterView<*>?, view2: View?, position: Int, id: Long) {

        val selectedItem = parent?.getItemAtPosition(position).toString()

        val bank4: String = selectedItem

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        if (selectedItem != "Choose Bank") {
          userAndFriendInfo.usersList[model.index].bank4 = bank4
          allInfoUnsaved.usersList[model.index].bank4 = bank4
        }
        else {
          userAndFriendInfo.usersList[model.index].bank4 = null
          allInfoUnsaved.usersList[model.index].bank4 = null
        }

        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string
        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    }


    model.run {

      if (phone != null) {
        phone2Et.setText(phone)
      }

      //-----------------------------------------------------

      if (pin != null) {
        pin2Et.setText(pin)
      }

      if (smartCardNumber1 != null) {
        smartCardNumber1Et.setText(smartCardNumber1)
      }

      if (smartCardNumber2 != null) {
        smartCardNumber2Et.setText(smartCardNumber2)
      }

      if (smartCardNumber3 != null) {
        smartCardNumber3Et.setText(smartCardNumber3)
      }

      if (smartCardNumber4 != null) {
        smartCardNumber4Et.setText(smartCardNumber4)
      }

      if (meterNumber1 != null) {
        meterNumber1Et.setText(meterNumber1)
      }

      if (meterNumber2 != null) {
        meterNumber2Et.setText(meterNumber2)
      }

      if (meterNumber3 != null) {
        meterNumber3Et.setText(meterNumber3)
      }

      //-----------------------------------------------------

      if (network != null) {
        val networkItemPosition: Int = networkStringArray.indexOf(network)
        network2Spinner.setSelection(networkItemPosition)
      }

      if (bank1 != null) {
        val bankItemPosition: Int = bankStringArray.indexOf(bank1)
        p2Bank1Spinner.setSelection(bankItemPosition)
      }

      if (bank2 != null) {
        val bankItemPosition: Int = bankStringArray.indexOf(bank2)
        p2Bank2Spinner.setSelection(bankItemPosition)
      }

      if (bank3 != null) {
        val bankItemPosition: Int = bankStringArray.indexOf(bank3)
        p2Bank3Spinner.setSelection(bankItemPosition)
      }

      if (bank4 != null) {
        val bankItemPosition: Int = bankStringArray.indexOf(bank4)
        p2Bank4Spinner.setSelection(bankItemPosition)
      }

      transition(folded)

      //-----------------------------------------------------

    }

  }

}