package com.app.ej.cs.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View.OnTouchListener
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.app.ej.cs.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


class MTNDataBuyForFriendAdapter(
  private val context: Context,
  private val onDataAmountClickListener: OnMTNDataAmountClickListener
) : RecyclerView.Adapter<MTNDataBuyForFriendAdapter.ViewHolder?>() {

  private var mFirestore: FirebaseFirestore? = null
  private var mAuth: FirebaseAuth? = null
  private var mCurrentUser: FirebaseUser? = null
  private var mMyUserId: String? = null
  private val DEBUG_TAG = "Motion Event Debug"
//  private var code: List<String>? = null
  private var amount_values: List<String>? = null
  private lateinit var data_values: List<String>
  private lateinit var data_full_values: List<String>
  private var valid_values: List<String>? = null

  interface OnMTNDataAmountClickListener {
    fun onMTNDataAmountClicked(code: String, data: String, fullData: String, amount: String)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_data_recharge, parent, false)

    mFirestore = FirebaseFirestore.getInstance()
    mAuth = FirebaseAuth.getInstance()
    mCurrentUser = mAuth!!.currentUser
    mMyUserId = mCurrentUser!!.uid

    return ViewHolder(view)

  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

  override fun getItemViewType(position: Int): Int {
    return position
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onBindViewHolder(holder: ViewHolder, position: Int) {

//    code = listOf(*context.resources.getStringArray(R.array.mtn_sms_code_array))
    amount_values = listOf(*context.resources.getStringArray(R.array.mtn_amount_array))
    data_values = listOf(*context.resources.getStringArray(R.array.mtn_data_transfer_array))
    data_full_values = listOf(*context.resources.getStringArray(R.array.mtn_full_data_array))
    valid_values = listOf(*context.resources.getStringArray(R.array.mtn_valid_array))


    holder.amount.text = amount_values!![position]
    holder.data.text = data_values[position]
    holder.validity.text = valid_values!![position]
    holder.mView.alpha = 1f

    val gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {

      override fun onSingleTapConfirmed(e: MotionEvent): Boolean {

        colorCountDown(position, 200)

        return true

      }

      override fun onLongPress(e: MotionEvent) {
        super.onLongPress(e)
      }

      override fun onDoubleTap(e: MotionEvent): Boolean {
        return super.onDoubleTap(e)
      }

    })

    holder.mView.setOnTouchListener(OnTouchListener {

        view, event ->

      val action = event.actionMasked

      if (gestureDetector.onTouchEvent(event)) {

        return@OnTouchListener true

      }

      when (action) {

        MotionEvent.ACTION_DOWN -> {

          Log.e(DEBUG_TAG, "Action was DOWN")
          holder.mView.setBackgroundResource(R.color.colorPrimary3)
          true

        }
        MotionEvent.ACTION_MOVE -> {

          Log.e(DEBUG_TAG, "Action was MOVE")
          holder.mView.setBackgroundResource(R.color.colorPrimary3)
          true

        }
        MotionEvent.ACTION_UP -> {

          Log.e(DEBUG_TAG, "Action was UP")
          holder.mView.setBackgroundResource(R.color.white)
          true

        }
        MotionEvent.ACTION_CANCEL -> {

          Log.e(DEBUG_TAG, "Action was CANCEL")
          holder.mView.setBackgroundResource(R.color.white)
          true

        }
        MotionEvent.ACTION_OUTSIDE -> {

          holder.mView.setBackgroundResource(R.color.white)

          Log.d(
            DEBUG_TAG, "Movement occurred outside bounds " +
                    "of current screen element")
          true

        }
        else -> false

      }

    })

  }

  override fun getItemCount(): Int {
    return data_values.size
  }

  private fun colorCountDown(position: Int, duration: Long) {

    Handler().postDelayed({

      object : CountDownTimer(duration, 50) {
        override fun onTick(millisUntilFinished: Long) {}
        override fun onFinish() {
          
          onDataAmountClickListener.onMTNDataAmountClicked(
            "", data_values[position],
            data_full_values[position], amount_values!![position])
          
        }

      }.start()

    }, 0)

  }

  inner class ViewHolder public constructor(var mView: View) : RecyclerView.ViewHolder(mView) {

    var container: ConstraintLayout
    var amount: TextView
    var data: TextView
    var validity: TextView
    var naira: ImageView

    init {

      container = mView.findViewById(R.id.container)
      naira = mView.findViewById(R.id.naira)
      amount = mView.findViewById(R.id.name)
      data = mView.findViewById(R.id.phone)
      validity = mView.findViewById(R.id.validity)

    }

  }

  init {
//    data_values = Arrays.asList(*context.resources.getStringArray(R.array.mtn_data_options_array))
    data_values = listOf(*context.resources.getStringArray(R.array.mtn_data_array))
  }

}