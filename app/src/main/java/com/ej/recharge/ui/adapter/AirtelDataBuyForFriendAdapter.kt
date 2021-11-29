package com.ej.recharge.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View.OnTouchListener
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ej.recharge.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*


class AirtelDataBuyForFriendAdapter(
  private val context: Context,
  private val onDataValidityClickListener: OnAirtelDataValidityClickListener
) : RecyclerView.Adapter<AirtelDataBuyForFriendAdapter.ViewHolder?>() {

  private var mFirestore: FirebaseFirestore? = null
  private var mAuth: FirebaseAuth? = null
  private var mCurrentUser: FirebaseUser? = null
  private var mMyUserId: String? = null
  private val DEBUG_TAG = "Motion Event Debug"
  private var code: List<String>? = null

  private var valid_values: List<String> = listOf(
    "3",
    "2",
    "1",
    "4"
  )

  private var valid_options: List<String> = listOf(
    "Daily",
    "Weekly",
    "Monthly",
    "Mega Packs"
  )

  interface OnAirtelDataValidityClickListener {
    fun onAirtelDataValidityClicked(validityOption: String, value: String)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

    val view: View = LayoutInflater.from(parent.context).inflate(
      R.layout.item_data_gift_airtel_valid_options,
      parent,
      false
    )

    mFirestore = Firebase.firestore
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

    code = listOf(*context.resources.getStringArray(R.array.airtel_code_array))

    holder.validity.text = valid_options[position]
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

          Log.e(
            DEBUG_TAG, "Movement occurred outside bounds " +
                    "of current screen element"
          )
          true

        }
        else -> false

      }

    })


  }

  override fun getItemCount(): Int {
    return valid_options.size
  }

  private fun colorCountDown(position: Int, duration: Long) {

    Handler().postDelayed({

      object : CountDownTimer(duration, 50) {

        override fun onTick(millisUntilFinished: Long) {}

        override fun onFinish() {

          onDataValidityClickListener
            .onAirtelDataValidityClicked(
              valid_options[position], valid_values[position]
            )

        }

      }.start()

    }, 0)

  }

  inner class ViewHolder public constructor(var mView: View)
    : RecyclerView.ViewHolder(mView) {

    var validity: TextView

    init {
      validity = mView.findViewById(R.id.validity)
    }

  }

}