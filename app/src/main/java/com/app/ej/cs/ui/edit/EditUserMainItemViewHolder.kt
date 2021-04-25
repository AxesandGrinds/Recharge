package com.app.ej.cs.ui.edit

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.app.ej.cs.R
import com.app.ej.cs.common.Binder
import com.app.ej.cs.common.events.OnViewHolderItemSelected
import com.app.ej.cs.repository.entity.Friend
import com.app.ej.cs.repository.entity.User
import com.app.ej.cs.repository.entity.UserAndFriendInfo
import com.app.ej.cs.ui.account.RegisterDetailsActivity
import com.app.ej.cs.utils.AnimationUtil
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import java.lang.Exception

class EditUserMainItemViewHolder(

  private val view: View,
  private val activity: Activity,
  private val firebaseUser: FirebaseUser?,
  listener: OnViewHolderItemSelected<User?>? = null

) : RecyclerView.ViewHolder(view), Binder<User> {

  private val showMoreDetailsIv: ImageView = view.findViewById(R.id.showMoreHandleView)

  private val expandTv: TextView = view.findViewById(R.id.expand)

  private val optionalExplanationTv: TextView = view.findViewById(R.id.optional_explain_tv)

  private val nameEt: TextInputEditText = view.findViewById(R.id.nameEt)

  private val emailEt: TextInputEditText = view.findViewById(R.id.emailEt)

  private val passwordEt: TextInputEditText = view.findViewById(R.id.password_et)

  private val reenterPasswordEt: TextInputEditText = view.findViewById(R.id.reenter_password_et)

  private val passwordTil: TextInputLayout = view.findViewById(R.id.h_password)

  private val reenterPasswordTil: TextInputLayout = view.findViewById(R.id.h_reenter_password)

  private val phone1Et: TextInputEditText = view.findViewById(R.id.phone1Et)

  private val network1Spinner: Spinner = view.findViewById(R.id.network1Spinner)

  private val pin1Et: TextInputEditText = view.findViewById(R.id.pin1Et)

  private val p1Bank1Spinner: Spinner = view.findViewById(R.id.p1Bank1Spinner)
  private val p1Bank2Spinner: Spinner = view.findViewById(R.id.p1Bank2Spinner)
  private val p1Bank3Spinner: Spinner = view.findViewById(R.id.p1Bank3Spinner)
  private val p1Bank4Spinner: Spinner = view.findViewById(R.id.p1Bank4Spinner)

  private val smartCardNumber1Et: TextInputEditText = view.findViewById(R.id.smartCardNumber11Et)
  private val smartCardNumber2Et: TextInputEditText = view.findViewById(R.id.smartCardNumber12Et)
  private val smartCardNumber3Et: TextInputEditText = view.findViewById(R.id.smartCardNumber13Et)
  private val smartCardNumber4Et: TextInputEditText = view.findViewById(R.id.smartCardNumber14Et)

  private val smartCardNumberTil2: TextInputLayout = view.findViewById(R.id.h_smartCardNumber12)
  private val smartCardNumberTil3: TextInputLayout = view.findViewById(R.id.h_smartCardNumber13)
  private val smartCardNumberTil4: TextInputLayout = view.findViewById(R.id.h_smartCardNumber14)

  private val meterNumber1Et: TextInputEditText = view.findViewById(R.id.meterNumber11Et)
  private val meterNumber2Et: TextInputEditText = view.findViewById(R.id.meterNumber12Et)
  private val meterNumber3Et: TextInputEditText = view.findViewById(R.id.meterNumber13Et)

  private val meterNumberTil2: TextInputLayout = view.findViewById(R.id.h_meterNumber12)
  private val meterNumberTil3: TextInputLayout = view.findViewById(R.id.h_meterNumber13)

  private val PREFNAME: String = "local_user"

  private var userAndFriendInfo: UserAndFriendInfo = UserAndFriendInfo()

  private val gson = Gson()

  private var currentUser: User? = null

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
        ),

        User(
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

      )

      userAndFriendInfo.friendList = mutableListOf<Friend>(

              Friend(

                index = 0,
                description = "Friend ${0 + 1}",

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

      }

    }

  }

  private fun runOnce(index: Int) {

    try {

      var tempPhone1String: String? = firebaseUser?.phoneNumber

      if (tempPhone1String!!.contains("+234")) {
        tempPhone1String = tempPhone1String.replace("+234", "0")
      }

      phone1Et.setText(tempPhone1String)

      runOnceTwo(index = index, phone = tempPhone1String)

    }
    catch (e: Exception) {

      e.printStackTrace()

    }

  }

  private fun runOnceTwo(index: Int, phone: String) {

    val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
    var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
    val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

    userAndFriendInfo.usersList[index].phone = phone
    allInfoUnsaved.usersList[index].phone = phone
    allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)

    val editor = sharedPref!!.edit()
    editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
    editor.apply()

    Log.e("ATTENTION ATTENTION", "runOnce(index: Int) ran, phone1: $phone")

  }

  private fun transition(folded: Boolean) {

    val transition: Transition = Fade()
    transition.duration = 600
    transition.addTarget(R.id.h_smartCardNumber12)
    transition.addTarget(R.id.h_smartCardNumber13)
    transition.addTarget(R.id.h_smartCardNumber14)
    transition.addTarget(R.id.h_meterNumber12)
    transition.addTarget(R.id.h_meterNumber13)

    TransitionManager.beginDelayedTransition(view as ViewGroup, transition)

    if (folded) {

      smartCardNumberTil2.visibility = View.GONE
      smartCardNumberTil3.visibility = View.GONE
      smartCardNumberTil4.visibility = View.GONE

      meterNumberTil2.visibility = View.GONE
      meterNumberTil3.visibility = View.GONE

      showMoreDetailsIv.setImageResource(R.drawable.ic_baseline_unfold_less_48)
      expandTv.text = "Expand"
      expandTv.setTextColor(ContextCompat.getColor(view.context, R.color.colorPrimary3))

    }
    else {

      smartCardNumberTil2.visibility = View.VISIBLE
      smartCardNumberTil3.visibility = View.VISIBLE
      smartCardNumberTil4.visibility = View.VISIBLE

      meterNumberTil2.visibility = View.VISIBLE
      meterNumberTil3.visibility = View.VISIBLE

      showMoreDetailsIv.setImageResource(R.drawable.ic_baseline_unfold_more_48)
      expandTv.text = "Collapse"
      expandTv.setTextColor(ContextCompat.getColor(view.context, R.color.black))

    }

  }

  override fun bind(model: User) {

    currentUser = model

    val res: Resources = view.context.resources

    val bankStringArray:    Array<String> = res.getStringArray(R.array.banks_arrays)
    val networkStringArray: Array<String> = res.getStringArray(R.array.network_arrays)

    readFromLocal() // userAndFriendInfo initialized

    runOnce(index = model.index)

    Log.e("ATTENTION ATTENTION", "WORKING SO FAR: after runOnce")

    setNetworkSpinner(network1Spinner)
    Log.e("ATTENTION ATTENTION", "WORKING SO FAR: setNetworkSpinner(network1Spinner)")
    setBankSpinner(p1Bank1Spinner)
    Log.e("ATTENTION ATTENTION", "WORKING SO FAR: setBankSpinner(p1Bank1Spinner)")
    setBankSpinner(p1Bank2Spinner)
    Log.e("ATTENTION ATTENTION", "WORKING SO FAR: setBankSpinner(p1Bank2Spinner)")
    setBankSpinner(p1Bank3Spinner)
    Log.e("ATTENTION ATTENTION", "WORKING SO FAR: setBankSpinner(p1Bank3Spinner)")
    setBankSpinner(p1Bank4Spinner)
    Log.e("ATTENTION ATTENTION", "WORKING SO FAR: setBankSpinner(p1Bank4Spinner)")

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

    nameEt.addTextChangedListener(object : TextWatcher {

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

        val name: String = nameEt.text.toString()
        userAndFriendInfo.usersList[model.index].name = name
        allInfoUnsaved.usersList[model.index].name = name
        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)

        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    })

    emailEt.addTextChangedListener(object : TextWatcher {

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

        val email: String = emailEt.text.toString()
        userAndFriendInfo.usersList[model.index].email = email
        allInfoUnsaved.usersList[model.index].email = email
        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)

        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    })

    passwordEt.addTextChangedListener(object : TextWatcher {

      override fun afterTextChanged(s: Editable) {}
      override fun beforeTextChanged(
        s: CharSequence, start: Int,
        count: Int, after: Int
      ) {}

      override fun onTextChanged(
        s: CharSequence, start: Int,
        before: Int, count: Int
      ) {

        val password: String = passwordEt.text.toString()
        val reenterPassword: String = reenterPasswordEt.text.toString()

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        val editor = sharedPref!!.edit()
        editor.putString("password", password)
        editor.apply()

        if (TextUtils.isEmpty(password) || password.length < 7) {

          passwordEt.error = "Invalid password."

        }
        else {

          passwordEt.error = null

          passwordEt.setCompoundDrawables(null, null, null, null)
          passwordEt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_circle_16, 0)

          if (reenterPassword != "") {

            if (password == reenterPassword) {

              reenterPasswordEt.error = null
              reenterPasswordEt.setCompoundDrawables(null, null, null, null)
              reenterPasswordEt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_circle_16, 0)

            }

          }

        }

      }

    })

    reenterPasswordEt.addTextChangedListener(object : TextWatcher {

      override fun afterTextChanged(s: Editable) {}
      override fun beforeTextChanged(
        s: CharSequence, start: Int,
        count: Int, after: Int
      ) {}

      override fun onTextChanged(
        s: CharSequence, start: Int,
        before: Int, count: Int
      ) {

        val password: String = passwordEt.text.toString()
        val reenterPassword: String = reenterPasswordEt.text.toString()

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        val editor = sharedPref!!.edit()
        editor.putString("password2", reenterPassword)
        editor.apply()

        if (TextUtils.isEmpty(reenterPassword) || reenterPassword.length < 7) {

          reenterPasswordEt.error = "Invalid password."

        }
        else {

          if (password == reenterPassword) {

            passwordEt.error = null
            reenterPasswordEt.error = null

            passwordEt.setCompoundDrawables(null, null, null, null)
            reenterPasswordEt.setCompoundDrawables(null, null, null, null)

            passwordEt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_circle_16, 0)
            reenterPasswordEt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_circle_16, 0)

          }
          else {

            reenterPasswordEt.error = "Password doesn't match"

          }


        }

      }

    })

    if (activity is RegisterDetailsActivity) { // was

      if (firebaseUser == null) {

        passwordTil.visibility = View.VISIBLE
        reenterPasswordTil.visibility = View.VISIBLE

      }
      else {

        passwordTil.visibility = View.GONE
        reenterPasswordTil.visibility = View.GONE

      }

      optionalExplanationTv.text = view.resources.getString(R.string.optional_explain)
      optionalExplanationTv.textSize = 12F

    }
    else {

      optionalExplanationTv.text = view.resources.getString(R.string.optional_explain_edit)

      passwordTil.visibility = View.GONE
      reenterPasswordTil.visibility = View.GONE

    }


    phone1Et.addTextChangedListener(object : TextWatcher {

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

        val phone: String = phone1Et.text.toString()
        userAndFriendInfo.usersList[model.index].phone = phone
        allInfoUnsaved.usersList[model.index].phone = phone
        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)

        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    })

    pin1Et.addTextChangedListener(object : TextWatcher {

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

        val pin: String = pin1Et.text.toString()
        userAndFriendInfo.usersList[model.index].pin = pin
        allInfoUnsaved.usersList[model.index].pin = pin
        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)

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
        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)

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
        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)

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
        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)

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

        val smartCardNumber: String = smartCardNumber1Et.text.toString()
        userAndFriendInfo.usersList[model.index].smartCardNumber4 = smartCardNumber
        allInfoUnsaved.usersList[model.index].smartCardNumber4 = smartCardNumber
        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)

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
        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)

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
        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)

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
        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)

        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    })

    network1Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

      override fun onNothingSelected(parent: AdapterView<*>?) {}

      override fun onItemSelected(parent: AdapterView<*>?, view2: View?, position: Int, id: Long) {

        val selectedItem = parent?.getItemAtPosition(position).toString()

        val network: String = selectedItem

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        if (selectedItem != "Choose Network") {
          userAndFriendInfo.usersList[model.index].network = network
          allInfoUnsaved.usersList[model.index].network = network
        }
        else {
          userAndFriendInfo.usersList[model.index].network = null
          allInfoUnsaved.usersList[model.index].network = null
        }

        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)
        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    }

    p1Bank1Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

      override fun onNothingSelected(parent: AdapterView<*>?) {}

      override fun onItemSelected(parent: AdapterView<*>?, view2: View?, position: Int, id: Long) {

        val selectedItem = parent?.getItemAtPosition(position).toString()

        val bank1: String = selectedItem

        val sharedPref = view.context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        if (selectedItem != "Choose Bank") {
          userAndFriendInfo.usersList[model.index].bank1 = bank1
          allInfoUnsaved.usersList[model.index].bank1 = bank1
        }
        else {
          userAndFriendInfo.usersList[model.index].bank1 = null
          allInfoUnsaved.usersList[model.index].bank1 = null
        }

        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)
        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    }

    p1Bank2Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

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

        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)
        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    }

    p1Bank3Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

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

        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)
        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    }

    p1Bank4Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

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

        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)
        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

      }

    }

    model.run {

      if (name != null) {
        nameEt.setText(name)
      }
      if (phone != null) {
        phone1Et.setText(phone)
      }
      if (email != null) {
        emailEt.setText(email)
      }


      if (pin != null) {
        pin1Et.setText(pin)
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


      if (network != null) {
        val networkItemPosition: Int = networkStringArray.indexOf(network)
        network1Spinner.setSelection(networkItemPosition)
      }

      if (bank1 != null) {
        val bankItemPosition: Int = bankStringArray.indexOf(bank1)
        p1Bank1Spinner.setSelection(bankItemPosition)
      }

      if (bank2 != null) {
        val bankItemPosition: Int = bankStringArray.indexOf(bank2)
        p1Bank2Spinner.setSelection(bankItemPosition)
      }

      if (bank3 != null) {
        val bankItemPosition: Int = bankStringArray.indexOf(bank3)
        p1Bank3Spinner.setSelection(bankItemPosition)
      }

      if (bank4 != null) {
        val bankItemPosition: Int = bankStringArray.indexOf(bank4)
        p1Bank4Spinner.setSelection(bankItemPosition)
      }

      transition(folded)


    }

  }

}