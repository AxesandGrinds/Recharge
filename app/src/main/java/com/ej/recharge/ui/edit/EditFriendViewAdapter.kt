package com.ej.recharge.ui.edit

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ej.recharge.common.events.OnViewHolderItemSelected
import com.ej.recharge.R
import com.ej.recharge.model.FriendListModel
import com.ej.recharge.presenter.PickContactListener
import com.ej.recharge.presenter.ReorderContactListener
import com.ej.recharge.repository.entity.Friend
import com.ej.recharge.repository.entity.UserAndFriendInfo
import com.google.gson.Gson
import kotlinx.android.synthetic.main.edit_friend_list_item_layout.view.*


class EditFriendViewAdapter(
        private val model: FriendListModel,
        private val activity: Activity,
//        private var showDeleteCheckBox: Boolean = false,
        private val pickContactListener: PickContactListener,
        private val editFragment: EditFragment? = null,
        private val listener: OnViewHolderItemSelected<Friend?>? = null
) :

  RecyclerView.Adapter<EditFriendItemViewHolder>() {

    private val PREFNAME: String = "local_user"

    fun moveItem(from: Int, to: Int) {

        val gson = Gson()
        val sharedPref = activity.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        val tempFromAccountNumber1: String? = allInfoUnsaved.friendList?.get(from)?.accountNumber1
        val tempFromAccountNumber2: String? = allInfoUnsaved.friendList?.get(from)?.accountNumber2
        val tempFromAccountNumber3: String? = allInfoUnsaved.friendList?.get(from)?.accountNumber3
        val tempFromAccountNumber4: String? = allInfoUnsaved.friendList?.get(from)?.accountNumber4

        val tempFromBank1: String? = allInfoUnsaved.friendList?.get(from)?.bank1
        val tempFromBank2: String? = allInfoUnsaved.friendList?.get(from)?.bank2
        val tempFromBank3: String? = allInfoUnsaved.friendList?.get(from)?.bank3
        val tempFromBank4: String? = allInfoUnsaved.friendList?.get(from)?.bank4

        val tempFromNetwork1: String? = allInfoUnsaved.friendList?.get(from)?.network1
        val tempFromNetwork2: String? = allInfoUnsaved.friendList?.get(from)?.network2
        val tempFromNetwork3: String? = allInfoUnsaved.friendList?.get(from)?.network3

        val tempFromPhone1: String? = allInfoUnsaved.friendList?.get(from)?.phone1
        val tempFromPhone2: String? = allInfoUnsaved.friendList?.get(from)?.phone2
        val tempFromPhone3: String? = allInfoUnsaved.friendList?.get(from)?.phone3

        val fromFriend = allInfoUnsaved.friendList?.get(from)

        val tempFromIndex: Int? = allInfoUnsaved.friendList?.get(from)?.index
        val tempFromDescription: String? = allInfoUnsaved.friendList?.get(from)?.description

        allInfoUnsaved.friendList!![from].index = allInfoUnsaved.friendList!![to].index
        allInfoUnsaved.friendList!![from].description = allInfoUnsaved.friendList!![to].description

        allInfoUnsaved.friendList!![to].index = tempFromIndex!!
        allInfoUnsaved.friendList!![to].description = tempFromDescription!!

        allInfoUnsaved.friendList?.removeAt(from)
        fromFriend?.let { allInfoUnsaved.friendList?.add(to, it) }

        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)

        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.apply()

    }

  @SuppressLint("ClickableViewAccessibility")
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditFriendItemViewHolder {

    val itemView = LayoutInflater
      .from(parent.context)
      .inflate(R.layout.edit_friend_list_item_layout, parent, false)

      val viewHolder = EditFriendItemViewHolder(itemView, activity, listener, pickContactListener)

      viewHolder.itemView.reorderHandleView.setOnTouchListener {

              view, event ->

          if (event.actionMasked == MotionEvent.ACTION_DOWN) {
              editFragment?.startDragging(viewHolder)
          }

          return@setOnTouchListener true

      }

    return viewHolder

  }

  override fun getItemCount(): Int {

    return when (model.friendList?.size) {
      0 -> {

        0

      }
      else -> {

        model.friendList!!.size

      }

    }

  }

  override fun onBindViewHolder(holderEdit: EditFriendItemViewHolder, position: Int) {

    val friends: List<Friend> = listOf(

            Friend(
                index = 0,
                description = "Friend 1",

                folded = false,

                name = null,
                phone1 = null,
                phone2 = null,
                phone3 = null,

                network1 = null,
                network2 = null,
                network3 = null,

                bank1 = null,
                bank2 = null,
                bank3 = null,
                bank4 = null,

                accountNumber1 = null,
                accountNumber2 = null,
                accountNumber3 = null,
                accountNumber4 = null,
                showDeleteCheckBox = false,
                deleteCheckBox = false,
            ),

            Friend(
                index = 1,
                description = "Friend 2",

                folded = false,

                name = null,
                phone1 = null,
                phone2 = null,
                phone3 = null,

                network1 = null,
                network2 = null,
                network3 = null,

                bank1 = null,
                bank2 = null,
                bank3 = null,
                bank4 = null,

                accountNumber1 = null,
                accountNumber2 = null,
                accountNumber3 = null,
                accountNumber4 = null,
                showDeleteCheckBox = false,
                deleteCheckBox = false,
            )


    )

    return when (model.friendList?.size) {

      0 -> {

      }
      else -> {

        holderEdit.bind(model.friendList!![position])

        holderEdit.itemView.setOnLongClickListener {
          pickContactListener.deleteContact(position)
          true
        }

      }

    }

  }

}