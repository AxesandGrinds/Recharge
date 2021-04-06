package com.app.ej.cs.ui.scan

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.app.ej.cs.R
import com.app.ej.cs.common.Binder
import com.app.ej.cs.common.events.OnViewHolderItemSelected
import com.app.ej.cs.presenter.FriendMenuItemClickListener
import com.app.ej.cs.repository.entity.Friend
import com.app.ej.cs.repository.entity.User
import com.app.ej.cs.utils.Util

/**
 * The ViewHolder for the NewsList RecyclerView Adapter
 */

class ScanFriendItemViewHolder(

  private val view: View,
  private val context: Context,
  private val fragment: Fragment,
  private val activity: Activity,
  private val userList: MutableList<User>,
  listener: OnViewHolderItemSelected<Friend?>? = null

) : RecyclerView.ViewHolder(view), Binder<Friend> {

  private val friendName: TextView = view.findViewById(R.id.friendNameEt)

  private val friendPhone1: TextView = view.findViewById(R.id.friendPhone1)
  private val friendPhone2: TextView = view.findViewById(R.id.friendPhone2)
  private val friendPhone3: TextView = view.findViewById(R.id.friendPhone3)

  private val friendNetwork1: TextView = view.findViewById(R.id.friendNetwork1)
  private val friendNetwork2: TextView = view.findViewById(R.id.friendNetwork2)
  private val friendNetwork3: TextView = view.findViewById(R.id.friendNetwork3)

  private val friendBank1: TextView = view.findViewById(R.id.friendBank1)
  private val friendBank2: TextView = view.findViewById(R.id.friendBank2)
  private val friendBank3: TextView = view.findViewById(R.id.friendBank3)
  private val friendBank4: TextView = view.findViewById(R.id.friendBank4)

  private val friendDetailsLayout1: TableRow = view.findViewById(R.id.friendDetailsLayout1)
  private val friendDetailsLayout2: TableRow = view.findViewById(R.id.friendDetailsLayout2)
  private val friendDetailsLayout3: TableRow = view.findViewById(R.id.friendDetailsLayout3)

  private val friendDetailsBanksLayout1: TableRow = view.findViewById(R.id.friendDetailsBanksLayout1)
  private val friendDetailsBanksLayout2: TableRow = view.findViewById(R.id.friendDetailsBanksLayout2)

  private val menu: ImageButton = view.findViewById(R.id.fmenu)
  private lateinit var popup: PopupMenu

  private var accountNumberBool:            Boolean = false
  private var airtimePhoneTransferBool:     Boolean = false
  private var airtimeBankTopUpTransferBool: Boolean = false
  private var bankTransferBool:             Boolean = false

  private val util: Util = Util()

  private var currentFriend: Friend? = null

  init {
    listener?.let { l ->
      view.setOnClickListener { l(currentFriend) }
    }
  }

  fun checkIfNotNullSet(setterTV: TextView, text: String?) {

    if (text != null) {
      setterTV.text = text
    }
    else {
      setterTV.visibility = View.INVISIBLE
    }

  }

  private fun init(model: Friend, view: View) {

    if (model.bank1 == null || model.bank1 == "Choose Bank") {
      model.bank1 = null
    }
    if (model.bank2 == null || model.bank2 == "Choose Bank") {
      model.bank2 = null
    }
    if (model.bank3 == null || model.bank3 == "Choose Bank") {
      model.bank3 = null
    }
    if (model.bank4 == null || model.bank4 == "Choose Bank") {
      model.bank4 = null
    }

    accountNumberBool = false
    airtimePhoneTransferBool = true
    airtimeBankTopUpTransferBool = false
    bankTransferBool = false


    if (
      !(model.accountNumber1 == null || model.accountNumber1!!.trim() == "") ||
      !(model.accountNumber2 == null || model.accountNumber2!!.trim() == "") ||
      !(model.accountNumber3 == null || model.accountNumber3!!.trim() == "") ||
      !(model.accountNumber4 == null || model.accountNumber4!!.trim() == "")
    ) {
      accountNumberBool = true
    }

    if (
      (model.network1 == null || model.network1 == "Choose Network") ||
      (model.phone1 == null || model.phone1 == "")
    ) {
      airtimePhoneTransferBool = false
    }

    if (
      !(model.network2 == null || model.network2 == "Choose Network") &&
      !(model.phone2 == null || model.phone2 == "")
    ) {
      if (!airtimePhoneTransferBool)
        airtimePhoneTransferBool = true
    }

    if (
      !(model.network3 == null || model.network3 == "Choose Network") &&
      !(model.phone3 == null || model.phone3 == "")
    ) {
      if (!airtimePhoneTransferBool)
        airtimePhoneTransferBool = true
    }

    val size: Int = userList.size


    if ( !(
      (userList[0].bank1 == null || userList[0].bank1 == "Choose Bank") &&
      (userList[0].bank2 == null || userList[0].bank2 == "Choose Bank") &&
      (userList[0].bank3 == null || userList[0].bank3 == "Choose Bank") &&
      (userList[0].bank4 == null || userList[0].bank4 == "Choose Bank")

              )) {

      airtimeBankTopUpTransferBool = true



    }

    if (size > 1) {

      if ( !(
        (userList[1].bank1 == null || userList[1].bank1 == "Choose Bank") &&
        (userList[1].bank2 == null || userList[1].bank2 == "Choose Bank") &&
        (userList[1].bank3 == null || userList[1].bank3 == "Choose Bank") &&
        (userList[1].bank4 == null || userList[1].bank4 == "Choose Bank")

                )) {

        airtimeBankTopUpTransferBool = true

      }


    }

    if (
      (
              (
                      (model.phone1 == null || model.phone1!!.trim() == "") &&
                              (model.network1 == null || model.network1 == "Choose Network")
                      ) &&

                      (
                              (model.phone2 == null || model.phone2!!.trim() == "") &&
                                      (model.network2 == null || model.network2 == "Choose Network")
                              ) &&

                      (
                              (model.phone3 == null || model.phone3!!.trim() == "") &&
                                      (model.network3 == null || model.network3 == "Choose Network")
                              )
              )

    ) {

      airtimeBankTopUpTransferBool = false

    }


    if (

      (

              !(
                      (model.accountNumber1 == null || model.accountNumber1!!.trim() == "") ||
                              (model.bank1 == null || model.bank1 == "Choose Bank")
                      ) ||

                      !(
                              (model.accountNumber2 == null || model.accountNumber2!!.trim() == "") ||
                                      (model.bank2 == null || model.bank2 == "Choose Bank")
                              ) ||

                      !(
                              (model.accountNumber3 == null || model.accountNumber3!!.trim() == "") ||
                                      (model.bank3 == null || model.bank3 == "Choose Bank")
                              ) ||

                      !(
                              (model.accountNumber4 == null || model.accountNumber4!!.trim() == "") ||
                                      (model.bank4 == null || model.bank4 == "Choose Bank")
                              )
              )

    ) {
      bankTransferBool = true
    }

    initPopup(model, view)

  }

  private fun initPopup(friend: Friend, view: View) {

    popup = PopupMenu(context, view)

    popup.menu.add(
      R.id.fmenu, R.id.check_account_number, 1, util.menuIconWithText(
        ResourcesCompat.getDrawable(view.resources,
          R.drawable.ic_account_balance_black_24dp, null)!!,
        view.resources.getString(R.string.check_account_number)
      )
    ).isEnabled = accountNumberBool

    popup.menu.add(
      R.id.fmenu, R.id.airtime_phone_transfer, 2, util.menuIconWithText(
        ResourcesCompat.getDrawable(view.resources, R.drawable.ic_airplanemode_active_black_24dp, null)!!,
        view.resources.getString(R.string.airtime_phone_transfer)
      )
    ).isEnabled = airtimePhoneTransferBool

    popup.menu.add(
      R.id.fmenu, R.id.airtime_bank_topup_transfer, 3, util.menuIconWithText(
        ResourcesCompat.getDrawable(view.resources, R.drawable.ic_airplanemode_active_black_24dp, null)!!,
        view.resources.getString(R.string.airtime_bank_topup_transfer)
      )
    ).isEnabled = airtimeBankTopUpTransferBool

    popup.menu.add(
      R.id.fmenu, R.id.bank_transfer, 4, util.menuIconWithText(
        ResourcesCompat.getDrawable(view.resources, R.drawable.ic_compare_black_24dp, null)!!,
        view.resources.getString(R.string.bank_transfer)
      )
    ).isEnabled = bankTransferBool

    popup.setOnMenuItemClickListener(
      FriendMenuItemClickListener(context, fragment, activity, userList, friend)
    )

  }

  private fun showPopup() {

    popup.show()

  }

  override fun bind(model: Friend) {

    currentFriend = model

    model.run {

      if (name != null) {
        friendName.text = name
      }
      else {
        friendName.visibility = View.INVISIBLE
      }

      if (phone1 != null) {
        friendPhone1.text = phone1
      }
      else {
        friendPhone1.visibility = View.INVISIBLE
      }

      if (phone2 != null) {
        friendPhone2.text = phone2
      }
      else {
        friendPhone2.visibility = View.INVISIBLE
      }

      if (phone3 != null) {
        friendPhone3.text = phone3
      }
      else {
        friendPhone3.visibility = View.INVISIBLE
      }

      if (network1 != null) {
        friendNetwork1.text = network1
      }
      else {
        friendNetwork1.visibility = View.INVISIBLE
      }

      if (network2 != null) {
        friendNetwork2.text = network2
      }
      else {
        friendNetwork2.visibility = View.INVISIBLE
      }

      if (network3 != null) {
        friendNetwork3.text = network3
      }
      else {
        friendNetwork3.visibility = View.INVISIBLE
      }

      if (bank1 != null) {
        friendBank1.text = bank1
      }
      else {
        friendBank1.visibility = View.INVISIBLE
      }

      if (bank2 != null) {
        friendBank2.text = bank2
      }
      else {
        friendBank2.visibility = View.INVISIBLE
      }

      if (bank3 != null) {
        friendBank3.text = bank3
      }
      else {
        friendBank3.visibility = View.INVISIBLE
      }

      if (bank4 != null) {
        friendBank4.text = bank4
      }
      else {
        friendBank4.visibility = View.INVISIBLE
      }


      if (phone1 == null || phone1!!.trim() == "") {
        friendDetailsLayout1.visibility = View.GONE
      }
      else {
        friendDetailsLayout1.visibility = View.VISIBLE
      }

      if (phone2 == null || phone2!!.trim() == "") {
        friendDetailsLayout2.visibility = View.GONE
      }
      else {
        friendDetailsLayout2.visibility = View.VISIBLE
      }

      if (phone3 == null || phone3!!.trim() == "") {
        friendDetailsLayout3.visibility = View.GONE
      }
      else {
        friendDetailsLayout3.visibility = View.VISIBLE
      }

      if (bank1 == null && bank2 == null) {
        friendDetailsBanksLayout1.visibility = View.GONE
      }
      else {
        friendDetailsBanksLayout1.visibility = View.VISIBLE
      }

      if (bank3 == null && bank4 == null) {
        friendDetailsBanksLayout2.visibility = View.GONE
      }
      else {
        friendDetailsBanksLayout2.visibility = View.VISIBLE
      }



      menu.setOnClickListener {

        init(model, it)
        showPopup()

      }

//      checkIfNotNullSet(friendName, name)
//
//      checkIfNotNullSet(friendPhone1, phone1)
//      checkIfNotNullSet(friendPhone2, phone2)
//      checkIfNotNullSet(friendPhone3, phone3)
//
//      checkIfNotNullSet(friendNetwork1, network1)
//      checkIfNotNullSet(friendNetwork2, network2)
//      checkIfNotNullSet(friendNetwork3, network3)
//
//      checkIfNotNullSet(friendBank1, bank1)
//      checkIfNotNullSet(friendBank2, bank2)
//      checkIfNotNullSet(friendBank3, bank3)
//      checkIfNotNullSet(friendBank4, bank4)

//      friendName.text = name

//      friendPhone1.text = phone1
//      friendPhone2.text = phone2
//      friendPhone3.text = phone3

//      friendNetwork1.text = network1
//      friendNetwork2.text = network2
//      friendNetwork3.text = network3

//      friendBank1.text = bank1
//      friendBank2.text = bank2
//      friendBank3.text = bank3
//      friendBank4.text = bank4

    }

  }

}