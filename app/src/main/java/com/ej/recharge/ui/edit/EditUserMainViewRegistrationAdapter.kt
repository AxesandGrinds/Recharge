package com.ej.recharge.ui.edit

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ej.recharge.common.events.OnViewHolderItemSelected
import com.ej.recharge.R
import com.ej.recharge.model.UserListModel
import com.ej.recharge.repository.entity.User
import com.google.firebase.auth.FirebaseUser

class EditUserMainViewRegistrationAdapter(
    private val model: UserListModel,
    private val activity: Activity,
    private val firebaseUser: FirebaseUser?,
    private val listener: OnViewHolderItemSelected<User?>? = null
) :

  RecyclerView.Adapter<EditUserMainItemViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditUserMainItemViewHolder {

    val itemView = LayoutInflater
      .from(parent.context)
      .inflate(R.layout.edit_user_main_item_layout_registration, parent, false)

    return EditUserMainItemViewHolder(itemView, activity, firebaseUser, listener)

  }

  override fun getItemCount(): Int {

    return when (model.userList.size) {
        2 -> {

          1

        }
        1 -> {

          1

        }
        else -> {

          1

        }
    }

  }

  override fun onBindViewHolder(holderEdit: EditUserMainItemViewHolder, position: Int) {

      when (model.userList.size) {

          0 -> {

              holderEdit.bind(

                  User(
                      uid= "",
                      index = 0,
                      description = "User 1",
                      folded = true,
                      created = null,
                      name = "",
                      email = "",
                      phone = "",
                      network = null,
                      pin = "",
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

          }
          else -> {

              holderEdit.bind(model.userList[0])

          }

      }

  }

}