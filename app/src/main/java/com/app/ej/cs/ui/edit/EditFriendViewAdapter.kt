package com.app.ej.cs.ui.edit

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.ej.cs.common.events.OnViewHolderItemSelected
import com.app.ej.cs.R
import com.app.ej.cs.model.FriendListModel
import com.app.ej.cs.presenter.PickContactListener
import com.app.ej.cs.presenter.ReorderContactListener
import com.app.ej.cs.repository.entity.Friend
import com.app.ej.cs.repository.entity.UserAndFriendInfo
import com.google.gson.Gson
import kotlinx.android.synthetic.main.edit_friend_list_item_layout.view.*

/**
 * The Adapter for the RecyclerView of the news
 */
class EditFriendViewAdapter(
        private val model: FriendListModel,
        private val activity: Activity,
        private val pickContactListener: PickContactListener,
        private val editFragment: EditFragment? = null,
        private val listener: OnViewHolderItemSelected<Friend?>? = null
) :

  RecyclerView.Adapter<EditFriendItemViewHolder>() {


    private val PREFNAME: String = "local_user"

    fun moveItem(from: Int, to: Int) {

//        val fromEmoji = emojis[from]
//
//        emojis.removeAt(from)
//
//        if (to < from) {
//            emojis.add(to, fromEmoji)
//        } else {
//            emojis.add(to - 1, fromEmoji)
//        }

        val gson = Gson()
        val sharedPref = activity.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        var allInfoJsonUnsaved = sharedPref.getString("allInfoUnsaved", "defaultAll")!!
        val allInfoUnsaved = gson.fromJson(allInfoJsonUnsaved, UserAndFriendInfo::class.java)

        val tempFromName: String? = allInfoUnsaved.friendList?.get(from)?.name
        val tempFromDescription: String? = allInfoUnsaved.friendList?.get(from)?.description

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


        allInfoUnsaved.friendList!![from].index = to
        allInfoUnsaved.friendList!![from].name = allInfoUnsaved.friendList!![to].name
        allInfoUnsaved.friendList!![from].description = allInfoUnsaved.friendList!![to].description

//        allInfoUnsaved.friendList!![from].accountNumber1 = allInfoUnsaved.friendList!![to].accountNumber1
//        allInfoUnsaved.friendList!![from].accountNumber2 = allInfoUnsaved.friendList!![to].accountNumber2
//        allInfoUnsaved.friendList!![from].accountNumber3 = allInfoUnsaved.friendList!![to].accountNumber3
//        allInfoUnsaved.friendList!![from].accountNumber4 = allInfoUnsaved.friendList!![to].accountNumber4
//
//        allInfoUnsaved.friendList!![from].bank1 = allInfoUnsaved.friendList!![to].bank1
//        allInfoUnsaved.friendList!![from].bank2 = allInfoUnsaved.friendList!![to].bank2
//        allInfoUnsaved.friendList!![from].bank3 = allInfoUnsaved.friendList!![to].bank3
//        allInfoUnsaved.friendList!![from].bank4 = allInfoUnsaved.friendList!![to].bank4
//
//        allInfoUnsaved.friendList!![from].network1 = allInfoUnsaved.friendList!![to].network1
//        allInfoUnsaved.friendList!![from].network2 = allInfoUnsaved.friendList!![to].network2
//        allInfoUnsaved.friendList!![from].network3 = allInfoUnsaved.friendList!![to].network3
//
//
//        allInfoUnsaved.friendList!![from].phone1 = allInfoUnsaved.friendList!![to].phone1
//        allInfoUnsaved.friendList!![from].phone2 = allInfoUnsaved.friendList!![to].phone2
//        allInfoUnsaved.friendList!![from].phone3 = allInfoUnsaved.friendList!![to].phone3

        allInfoUnsaved.friendList!![to].index = from
        allInfoUnsaved.friendList!![to].name = tempFromName
        allInfoUnsaved.friendList!![to].description = tempFromDescription!!

//        allInfoUnsaved.friendList!![to].accountNumber1 = tempFromAccountNumber1
//        allInfoUnsaved.friendList!![to].accountNumber2 = tempFromAccountNumber2
//        allInfoUnsaved.friendList!![to].accountNumber3 = tempFromAccountNumber3
//        allInfoUnsaved.friendList!![to].accountNumber4 = tempFromAccountNumber4
//
//        allInfoUnsaved.friendList!![to].bank1 = tempFromBank1
//        allInfoUnsaved.friendList!![to].bank2 = tempFromBank2
//        allInfoUnsaved.friendList!![to].bank3 = tempFromBank3
//        allInfoUnsaved.friendList!![to].bank4 = tempFromBank4
//
//        allInfoUnsaved.friendList!![to].network1 = tempFromNetwork1
//        allInfoUnsaved.friendList!![to].network2 = tempFromNetwork2
//        allInfoUnsaved.friendList!![to].network3 = tempFromNetwork3
//
//        allInfoUnsaved.friendList!![to].phone1 = tempFromPhone1
//        allInfoUnsaved.friendList!![to].phone2 = tempFromPhone2
//        allInfoUnsaved.friendList!![to].phone3 = tempFromPhone3

        val fromFriend = allInfoUnsaved.friendList?.get(from)

        allInfoUnsaved.friendList?.removeAt(from)

        if (to < from) {
            fromFriend?.let { allInfoUnsaved.friendList?.add(to, it) }
        } else {
            fromFriend?.let { allInfoUnsaved.friendList?.add(to - 1, it) }
        }

        allInfoJsonUnsaved = gson.toJson(allInfoUnsaved)  // json string

        val editor = sharedPref!!.edit()
        editor.putString("allInfoUnsaved", allInfoJsonUnsaved)
        editor.putString("allInfoSaved", allInfoJsonUnsaved)
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
              // 2. When we detect touch-down event, we call the
              //    startDragging(...) method we prepared above
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
      else -> { // 0 case

        model.friendList!!.size

      }

    }

  }

  override fun onBindViewHolder(holderEdit: EditFriendItemViewHolder, position: Int) {

//    model.friendList?.get(position)?.let { holderEdit.bind(it) }

//    holderEdit.bind(model.friendList[position])

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
            )


    )

    return when (model.friendList?.size) {

      0 -> {

//        holderEdit.bind(friends[position])

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