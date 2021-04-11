package com.app.ej.cs.ui.edit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.ej.cs.common.events.OnViewHolderItemSelected
import com.app.ej.cs.R
import com.app.ej.cs.model.UserListModel
import com.app.ej.cs.repository.entity.User

/**
 * The Adapter for the RecyclerView of the user's secondary information
 */
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
            else -> { // 0 case

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