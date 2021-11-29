package com.ej.recharge.ui.edit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ej.recharge.common.events.OnViewHolderItemSelected
import com.ej.recharge.R
import com.ej.recharge.model.UserListModel
import com.ej.recharge.repository.entity.User

class EditUserSecondViewAdapter(
        private val model: UserListModel,
        private val listener: OnViewHolderItemSelected<User?>? = null
) :

  RecyclerView.Adapter<EditUserSecondItemViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditUserSecondItemViewHolder {

    val itemView = LayoutInflater
      .from(parent.context)
      .inflate(R.layout.edit_user_second_item_layout, parent, false)

    return EditUserSecondItemViewHolder(itemView, listener)

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

  override fun onBindViewHolder(holderEdit: EditUserSecondItemViewHolder, position: Int) {

      when (model.userList.size) {

          0 -> {

              holderEdit.bind(

                  User(
                      uid= "",
                      index = 1,
                      description = "User 2",
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