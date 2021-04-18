package com.app.ej.cs.ui

import android.Manifest
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.ej.cs.R
import com.app.ej.cs.ui.adapter.*
import com.app.ej.cs.utils.PhoneUtil
import com.app.ej.cs.utils.SimUtil
import com.app.ej.cs.utils.Util
import es.dmoral.toasty.Toasty
import java.util.*


class DataBuyForFriendDialog : DialogFragment() {
  
  private var rootView: View? = null
  private var dialogRecyclerView: RecyclerView? = null
  private val amountsList: ArrayList<String>? = null
  private var mtnDataBuyForFriendAdapter: MTNDataBuyForFriendAdapter? = null
  private var gloDataBuyForFriendAdapter: GloDataBuyForFriendAdapter? = null
  private var etisalatDataBuyForFriendAdapter: EtisalatDataBuyForFriendAdapter? = null
  private var airtelDataBuyForFriendAdapter: AirtelDataBuyForFriendAdapter? = null
  private val util: Util = Util()
  private val phoneUtil: PhoneUtil = PhoneUtil()

  private var chosenSimCard = 0
  private var chosenNetwork: String? = null
  private var chosenPhone: String? = null
  
  private fun sendToSimOld(intent: Intent, simNumber: Int) {
    
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    intent.putExtra("com.android.phone.force.slot", true)
    intent.putExtra("Cdma_Supp", true)
    for (s in simSlotName) intent.putExtra(s, simNumber)
    
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

    return object : Dialog(requireActivity(), theme) {

      override fun onBackPressed() {
        dismiss()
      }

    }

  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (arguments != null) {
      chosenSimCard = requireArguments().getInt(Simcard)
      chosenNetwork = requireArguments().getString(Network)
      chosenPhone = requireArguments().getString(FriendPhone)
    }

  }

  override fun onStart() {
    super.onStart()

    val metrics = resources.displayMetrics
    val width = metrics.widthPixels
    val height = metrics.heightPixels
    val dialog = dialog
    if (dialog != null) {

      dialog.window!!.setLayout(6 * width / 7, ViewGroup.LayoutParams.WRAP_CONTENT)

    }

  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?): View? {

    rootView = inflater.inflate(R.layout.fragment_dialog_data_recharge, container, false)

    return rootView

  }

  private fun setAdapter(recyclerView: RecyclerView, network: String?) {

    recyclerView.layoutManager = LinearLayoutManager(context)

    when (network) {
      "Airtel" -> {
        recyclerView.adapter = airtelDataBuyForFriendAdapter
      }
      "Etisalat(9Mobile)" -> {
        recyclerView.adapter = etisalatDataBuyForFriendAdapter
      }
      "Glo Mobile" -> {
        recyclerView.adapter = gloDataBuyForFriendAdapter
      }
      "MTN Nigeria" -> {
        recyclerView.adapter = mtnDataBuyForFriendAdapter
      }
      else -> println("$network: error sharing credit!")
    }

  }

  private fun callCode(code: String, data: String, amount: String) {

    val intent: Intent
    val finalCode = code + Uri.encode("#")
    intent = Intent("android.intent.action.CALL", Uri.parse("tel:$finalCode"))
    intent.setPackage("com.android.server.telecom")

    if (chosenSimCard == 1) phoneUtil.sendToSim(requireContext(), intent, 0)
    else if (chosenSimCard == 2) phoneUtil.sendToSim(requireContext(), intent, 1)

    Log.e("ATTENTION ATTENTION", "chosenSimCard: $chosenSimCard")

    startActivity(intent)
    dismiss()

    Toasty.success(requireContext(),
      "Amount $amount for $data request completed!",
      Toasty.LENGTH_LONG, true).show()

  }

  private fun rechargeData(code: String, data: String, amount: String) {
    callCode(code, data, amount)
  }

  var tempNumber: String? = null
  var tempMessage: String? = null
  var tempData: String? = null
  var tempAmount: String? = null

  var fragment: Fragment? = null

  // TODO Use with future update
  private fun rechargeDataMTNSmsAuto(code: String, data: String, amount: String) {

    val number = "131"
    tempNumber = number
    tempMessage = code
    tempData = data
    tempAmount = amount

    val smsManager: SmsManager
    val SENT = "SMS_SENT"
    val DELIVERED = "SMS_DELIVERED"

    val sentPI: PendingIntent = PendingIntent
      .getBroadcast(context, 0, Intent(SENT), 0)

    val deliveredPI: PendingIntent = PendingIntent
      .getBroadcast(context, 0, Intent(DELIVERED), 0)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

      val localSubscriptionManager = requireContext().getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

      if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) ==
        PackageManager.PERMISSION_GRANTED) {

        var simCardsLength = localSubscriptionManager.activeSubscriptionInfoCount

        if (simCardsLength == 0) {

          simCardsLength = localSubscriptionManager.activeSubscriptionInfoList.size

          Log.e("ATTENTION ATTENTION",
            "getActiveSubscriptionInfoList().size(): " + localSubscriptionManager.activeSubscriptionInfoList.size)

        }

        if (simCardsLength == 0) {

          val manager = requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
          simCardsLength = manager.phoneCount
          Log.e("ATTENTION ATTENTION", "simCardsLength: $simCardsLength")

        }

        val localList = localSubscriptionManager.activeSubscriptionInfoList

        if (simCardsLength > 1) {

          val simInfo1 = localList[0] as SubscriptionInfo
          val simInfo2 = localList[1] as SubscriptionInfo
          val sim1 = simInfo1.subscriptionId
          val sim2 = simInfo2.subscriptionId

          smsManager = when (chosenSimCard) {
              1 -> {
                  SmsManager.getSmsManagerForSubscriptionId(sim1)
              }
              2 -> {
                  SmsManager.getSmsManagerForSubscriptionId(sim2)
              }
              else -> {
                  SmsManager.getSmsManagerForSubscriptionId(sim2)
              }
          }

          Log.e("ATTENTION ATTENTION", "chosenSimCard: $chosenSimCard")
          smsManager.sendTextMessage(number, null, code, sentPI, deliveredPI)

          val msg = "Amount $amount for $data request completed! Press \"send\" to complete."

          util.onShowMessageSuccess(msg, requireContext())

        }
        else if (simCardsLength == 1) {

          val simInfo1 = localList[0] as SubscriptionInfo
          val sim1 = simInfo1.subscriptionId

          if (chosenSimCard == 1) {

            smsManager = SmsManager.getSmsManagerForSubscriptionId(sim1)
            smsManager.sendTextMessage(number, null, code, sentPI, deliveredPI)
            val msg = "Amount $amount for $data request completed! Press \"send\" to complete."
            util.onShowMessageSuccess(msg, requireContext())

          }
          else if (chosenSimCard == 2) {

            val msg = "You only have one sim card. Please update " +
                    "your settings with appropriate info and " +
                    "set \"phone 1\" to the correct number."
            util.onShowErrorMessage(msg, requireContext())

          }

          Log.e("ATTENTION ATTENTION", "chosenSimCard: $chosenSimCard")

        }

      }
      else {

        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.SEND_SMS), RequestSMSPermissionCode)

      }

    }
    else {

      if (chosenSimCard == 1) {
        SimUtil.sendSMS(requireContext(), 0, number, null, code, sentPI, deliveredPI)
      }
      else if (chosenSimCard == 2) {
        SimUtil.sendSMS(requireContext(), 1, number, null, code, sentPI, deliveredPI)
      }

      val msg = "Amount $amount for $data request completed! Press \"send\" to complete."
      util.onShowMessageSuccess(msg, requireContext())

      Log.e("ATTENTION ATTENTION", "chosenSimCard: $chosenSimCard")

    }

  }

  override fun onRequestPermissionsResult(RC: Int, per: Array<String>, PResult: IntArray) {

    when (RC) {

      RequestSMSPermissionCode -> if (PResult.isNotEmpty() && PResult[0] == PackageManager.PERMISSION_GRANTED) {

        val smsManager = SmsManager.getDefault()

        smsManager.sendTextMessage(tempNumber, null,
          tempMessage, null, null)

        dismiss()

        Toasty.success(requireContext(),
          "Amount $tempAmount for $tempData request completed!",
          Toasty.LENGTH_LONG, true).show()

      }
      else {

        Toasty.info(requireContext(),
          "Permission Canceled, Now your application cannot .",
          Toasty.LENGTH_LONG).show()

      }

    }

  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    dialogRecyclerView = rootView!!.findViewById<View>(R.id.dialogRecyclerView) as RecyclerView

    fragment = this

    mtnDataBuyForFriendAdapter = MTNDataBuyForFriendAdapter(requireContext(), object : MTNDataBuyForFriendAdapter.OnMTNDataAmountClickListener {

        override fun onMTNDataAmountClicked(code: String, data: String, fullData: String, amount: String) {

          Log.e("ATTENTION ATTENTION", "MTN Data Code: $code, Data: $data, fullData: $fullData, Amount: $amount")

          phoneUtil.buyFriendDataMTNUSSD(
            requireContext(),
            fragment!!,
            requireActivity(),
            chosenSimCard.toString(),
            code,
            fullData,
            chosenPhone!!)

          dismiss()
          
        }

      })

    etisalatDataBuyForFriendAdapter = EtisalatDataBuyForFriendAdapter(requireContext(), object : EtisalatDataBuyForFriendAdapter.OnEtisalatDataAmountClickListener {

        override fun onEtisalatDataAmountClicked(code: String, data: String, fullData: String, amount: String) {

          Log.e("ATTENTION ATTENTION", "9Mobile Data Code: $code, Data: $data, fullData: $fullData, Amount: $amount")

          phoneUtil.buyFriendData9MobileUSSD(
            requireContext(),
            fragment!!,
            requireActivity(),
            chosenSimCard.toString(),
            code,
            fullData,
            chosenPhone!!)

          dismiss()

        }

      })

    gloDataBuyForFriendAdapter = GloDataBuyForFriendAdapter(requireContext(), object : GloDataBuyForFriendAdapter.OnGloDataAmountClickListener {

        override fun onGloDataAmountClicked(code: String, data: String, fullData: String, amount: String) {

          Log.e("ATTENTION ATTENTION", "9Mobile Data Code: $code, Data: $data, fullData: $fullData, Amount: $amount")

          phoneUtil.buyFriendDataGloUSSD(
            requireContext(),
            fragment!!,
            requireActivity(),
            chosenSimCard.toString(),
            code,
            fullData,
            chosenPhone!!)

          dismiss()


        }

      })

    airtelDataBuyForFriendAdapter =
      AirtelDataBuyForFriendAdapter(requireContext(), object : AirtelDataBuyForFriendAdapter.OnAirtelDataValidityClickListener {

        override fun onAirtelDataValidityClicked(validityOption: String, value: String) {

          Log.e("ATTENTION ATTENTION", "Airtel Data Validity: $validityOption, USSD Option: $value")

          phoneUtil.buyFriendDataAirtelUSSD(
            requireContext(),
            fragment!!,
            requireActivity(),
            chosenSimCard.toString(),
            validityOption,
            value,
            chosenPhone!!)

          dismiss()

        }

      })

    setAdapter(dialogRecyclerView!!, chosenNetwork)

  }

  companion object {

    const val RequestSMSPermissionCode = 1

    private const val Network = "network"
    private const val Simcard = "whichSimCard"
    private const val FriendPhone = "friendPhone"
    private const val FriendName = "friendName"

    private val simSlotName = arrayOf(
      "extra_asus_dial_use_dualsim",
      "com.android.phone.extra.slot",
      "slot",
      "simslot",
      "sim_slot",
      "subscription",
      "Subscription",
      "phone",
      "com.android.phone.DialingMode",
      "simSlot",
      "slot_id",
      "simId",
      "simnum",
      "phone_type",
      "slotId",
      "slotIdx")

    fun newInstance(
      network: String?,
      whichSimCard: Int,
      friendName: String?,
      friendPhone: String?): DataBuyForFriendDialog {

      val fragment = DataBuyForFriendDialog()
      val args = Bundle()
      args.putInt(Simcard, whichSimCard)
      args.putString(Network, network)
      args.putString(FriendName, friendName)
      args.putString(FriendPhone, friendPhone)
      fragment.arguments = args
      return fragment

    }

    private const val REQUEST_CALL_PHONE_PERMISSION = 19

  }

}