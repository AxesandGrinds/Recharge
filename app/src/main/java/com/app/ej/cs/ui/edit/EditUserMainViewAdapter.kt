package com.app.ej.cs.ui.edit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.ej.cs.common.events.OnViewHolderItemSelected
import com.app.ej.cs.R
import com.app.ej.cs.model.UserListModel
import com.app.ej.cs.repository.entity.User
import com.google.firebase.auth.FirebaseUser

/**
 * The Adapter for the RecyclerView of the news
 */
class EditUserMainViewAdapter(
  private val model: UserListModel,
  private val firebaseUser: FirebaseUser,
  private val listener: OnViewHolderItemSelected<User?>? = null
) :

  RecyclerView.Adapter<EditUserMainItemViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditUserMainItemViewHolder {

    val itemView = LayoutInflater
      .from(parent.context)
      .inflate(R.layout.edit_user_main_item_layout, parent, false)

    return EditUserMainItemViewHolder(itemView, firebaseUser, listener)

  }

  override fun getItemCount(): Int {

    return when (model.userList.size) {
        2 -> {

          1

        }
        1 -> {

          1

        }
        else -> { // 0 case

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
                      folded = false,
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