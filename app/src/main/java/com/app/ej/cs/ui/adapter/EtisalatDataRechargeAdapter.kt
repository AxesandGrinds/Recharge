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
import androidx.recyclerview.widget.RecyclerView
import com.app.ej.cs.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*


/**
 * Created by Victor Lekweuwa on 22/2/18.
 */
class EtisalatDataRechargeAdapter(
  private val context: Context,
  private val onDataAmountClickListener: OnEtisalatDataAmountClickListener
) : RecyclerView.Adapter<EtisalatDataRechargeAdapter.ViewHolder?>() {

  private var mFirestore: FirebaseFirestore? = null
  private var mAuth: FirebaseAuth? = null
  private var mCurrentUser: FirebaseUser? = null
  private var mMyUserId: String? = null
  private val DEBUG_TAG = "Motion Even Debug Debugs"
  private var code: List<String>? = null
  private var amount_values: List<String>? = null
  private var data_values: List<String>
  private var valid_values: List<String>? = null
  private var gestureDetector: GestureDetector? = null

  interface OnEtisalatDataAmountClickListener {
    fun onEtisalatDataAmountClicked(code: String, data: String, amount: String)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_data_recharge, parent, false)

    mFirestore = Firebase.firestore
    mAuth = Firebase.auth
    mCurrentUser = mAuth!!.currentUser
    mMyUserId = mCurrentUser!!.uid
    gestureDetector = GestureDetector(context, SingleTapConfirm())

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

    code = listOf(*context.resources.getStringArray(R.array.etisalat_code_array))
    amount_values = listOf(*context.resources.getStringArray(R.array.etisalat_amount_array))
    data_values = listOf(*context.resources.getStringArray(R.array.etisalat_data_array))
    valid_values = listOf(*context.resources.getStringArray(R.array.etisalat_valid_array))

    holder.amount.text = amount_values!![position]
    holder.data.text = data_values[position]
    holder.validity.text = valid_values!![position]
    holder.mView.alpha = 1f // or 0.5f

    holder.mView.setOnTouchListener(OnTouchListener {

        view, event ->

      val action = event.actionMasked

      // single tap
      if (gestureDetector!!.onTouchEvent(event)) {

        //holder.mView.setAlpha(0f);
        holder.mView.setBackgroundResource(R.color.colorPrimary3)
        colorCountDown(position, 200)

        //onDataAmountClickListener.onEtisalatDataAmountClicked(code.get(position), data_values.get(position), amount_values.get(position));
        return@OnTouchListener true

      }

      when (action) {

        MotionEvent.ACTION_DOWN -> {

          Log.d(DEBUG_TAG, "Action was DOWN")
          holder.mView.setBackgroundResource(R.color.colorPrimary3)
          //holder.mView.setAlpha(0f);
          true

        }
        MotionEvent.ACTION_MOVE -> {

          Log.d(DEBUG_TAG, "Action was MOVE")
          //holder.mView.setAlpha(1f);
          holder.mView.setBackgroundResource(R.color.colorPrimary3)
          true

        }
        MotionEvent.ACTION_UP -> {

          Log.d(DEBUG_TAG, "Action was UP")
          //holder.mView.setAlpha(1f);
          holder.mView.setBackgroundResource(R.color.accent)
          true

        }
        MotionEvent.ACTION_CANCEL -> {
          Log.d(DEBUG_TAG, "Action was CANCEL")
          //holder.mView.setAlpha(1f);
          holder.mView.setBackgroundResource(R.color.accent)
          true
        }
        MotionEvent.ACTION_OUTSIDE -> {

          Log.d(DEBUG_TAG, "Movement occurred outside bounds " +
                    "of current screen element")
          true

        }
        else -> false

      }

    })

    /*holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onDataAmountClickListener.onEtisalatDataAmountClicked(code.get(position), data_values.get(position), amount_values.get(position));

            }
        });*/
  }

  override fun getItemCount(): Int {
    return data_values.size
  }

  private inner class SingleTapConfirm : SimpleOnGestureListener() {
    override fun onSingleTapUp(event: MotionEvent): Boolean {
      return true
    }
  }

  fun colorCountDown(position: Int, duration: Long) {
    Handler().postDelayed({ // count down timer start
      object : CountDownTimer(duration, 50) {
        override fun onTick(millisUntilFinished: Long) {}
        override fun onFinish() {
          onDataAmountClickListener.onEtisalatDataAmountClicked(
            code!![position],
            data_values[position], amount_values!![position]
          )
        }
      }.start()
    }, 0)
  }

  inner class ViewHolder public constructor(var mView: View) : RecyclerView.ViewHolder(mView) {

    var amount: TextView
    var data: TextView
    var validity: TextView
    var naira: ImageView

    init {

      naira = mView.findViewById(R.id.naira)
      amount = mView.findViewById(R.id.name)
      data = mView.findViewById(R.id.phone)
      validity = mView.findViewById(R.id.network)

    }

  }

  init {
    data_values = listOf(*context.resources.getStringArray(R.array.etisalat_data_array))
  }

}