package com.app.ej.cs.ui.scan

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.app.ej.cs.common.Binder
import com.app.ej.cs.common.events.OnViewHolderItemSelected
import com.app.ej.cs.R
import com.app.ej.cs.presenter.UserMenuItemClickListener
import com.app.ej.cs.repository.entity.User
import com.app.ej.cs.utils.Util

class ScanUserSecondItemViewHolder(

  private val view: View,
  private val context: Context,
  private val fragment: Fragment,
  private val activity: Activity,
  listener: OnViewHolderItemSelected<User?>? = null

) : RecyclerView.ViewHolder(view), Binder<User> {

  private val phone2Tv:   TextView = view.findViewById(R.id.phone2Tv)
  private val network2Tv: TextView = view.findViewById(R.id.network2Tv)
  private val menu: ImageButton = view.findViewById(R.id.menu2)
  private lateinit var popup: PopupMenu

  private var balanceBool:           Boolean = false
  private var bankRechargeBool:      Boolean = false
  private var codeRechargeBool:      Boolean = false
  private var buyDataBool:           Boolean = false
  private var payDSTvBillBool:       Boolean = false
  private var payElectricityBillBool:Boolean = false

  private val util: Util = Util()

  private var currentUser: User? = null

  init {
    listener?.let { l ->
      view.setOnClickListener { l(currentUser) }
    }
  }

  private fun setNullBanks(model: User) {

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

  }

  private fun init(model: User, view: View) {

    balanceBool = true
    bankRechargeBool = true
    codeRechargeBool = true
    buyDataBool = true
    payDSTvBillBool = true
    payElectricityBillBool = true

    setNullBanks(model)

    if (model.phone == null || model.phone == "" || model.network == null || model.network == "") {
      balanceBool = false
    }

    if (model.bank1 == null || model.bank1 == "Choose Bank") {
      bankRechargeBool = false
    }

    if (model.network == null || model.network == "Choose Network") {
      buyDataBool = false
      codeRechargeBool = false
    }

    if (
      (model.smartCardNumber1 == null || model.smartCardNumber1 == "") &&
      (model.smartCardNumber2 == null || model.smartCardNumber2 == "") &&
      (model.smartCardNumber3 == null || model.smartCardNumber3 == "") &&
      (model.smartCardNumber4 == null || model.smartCardNumber4 == "")
    ) {
      payDSTvBillBool = false
    }

    if (
      (model.meterNumber1 == null || model.meterNumber1 == "") &&
      (model.meterNumber2 == null || model.meterNumber2 == "") &&
      (model.meterNumber3 == null || model.meterNumber3 == "")
    ) {
      payElectricityBillBool = false
    }

    initPopup(model, view)

  }

  private fun initPopup(user: User, view: View) {

    popup = PopupMenu(context, view)

    popup.menu.add(R.id.user_menu, R.id.airtime_balance, 1, util.menuIconWithText(
        ResourcesCompat.getDrawable(view.resources, R.drawable.ic_account_balance_black_24dp, null)!!,
        view.resources.getString(R.string.airtime_balance))).isEnabled = balanceBool

    popup.menu.add(R.id.user_menu, R.id.data_balance, 2, util.menuIconWithText(
        ResourcesCompat.getDrawable(view.resources, R.drawable.ic_internet_24dp, null)!!,
        view.resources.getString(R.string.data_balance))).isEnabled = balanceBool

    popup.menu.add(R.id.user_menu, R.id.bank_recharge, 3, util.menuIconWithText(
        ResourcesCompat.getDrawable(view.resources, R.drawable.ic_battery_charger_icon_7, null)!!,
        view.resources.getString(R.string.bank_recharge))).isEnabled = bankRechargeBool

    popup.menu.add(R.id.user_menu, R.id.code_recharge, 4, util.menuIconWithText(
        ResourcesCompat.getDrawable(view.resources, R.drawable.ic_baseline_battery_saver_24, null)!!,
        view.resources.getString(R.string.code_recharge))).isEnabled = codeRechargeBool

    popup.menu.add(R.id.user_menu, R.id.buy_data, 5, util.menuIconWithText(
        ResourcesCompat.getDrawable(view.resources, R.drawable.ic_add_shopping_cart_black_24dp, null)!!,
        view.resources.getString(R.string.buy_data))).isEnabled = buyDataBool

    popup.menu.add(R.id.user_menu, R.id.dstv_bill, 6, util.menuIconWithText(
        ResourcesCompat.getDrawable(view.resources, R.drawable.ic_live_tv_black_24dp, null)!!,
        view.resources.getString(R.string.dstv_gotv_bill))).isEnabled = payDSTvBillBool

    popup.menu.add(R.id.user_menu, R.id.electricity_bill, 7, util.menuIconWithText(
        ResourcesCompat.getDrawable(view.resources, R.drawable.ic_lightbulb_outline_black_24dp, null)!!,
        view.resources.getString(R.string.electricity_bill))).isEnabled = payElectricityBillBool

    popup.setOnMenuItemClickListener(UserMenuItemClickListener(context, fragment, activity, user, "2"))

  }

  private fun showPopup() {

    popup.show()

  }

  override fun bind(model: User) {

    currentUser = model

    model.run {

      if (phone != null) {
        phone2Tv.text = phone
      }
      else {
        phone2Tv.visibility = View.INVISIBLE
      }

      if (network != null) {
        network2Tv.text = network
      }
      else {
        network2Tv.visibility = View.INVISIBLE
      }

      menu.setOnClickListener {

        init(model, it)
        showPopup()

      }

    }

  }

}