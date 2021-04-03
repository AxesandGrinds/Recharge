package com.app.ej.cs.ui.scan

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.app.ej.cs.common.events.OnViewHolderItemSelected
import com.app.ej.cs.R
import com.app.ej.cs.model.FriendListModel
import com.app.ej.cs.repository.entity.Friend
import com.app.ej.cs.repository.entity.User
import com.app.ej.cs.repository.entity.UserAndFriendInfo
import com.google.gson.Gson

/**
 * The Adapter for the RecyclerView of the news
 */
class ScanFriendViewAdapter(
  private val context: Context,
  private val fragment: Fragment,
  private val activity: Activity,
  private val userList: MutableList<User>,
  private val model: FriendListModel,
  private val listener: OnViewHolderItemSelected<Friend?>? = null
) :

  RecyclerView.Adapter<ScanFriendItemViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanFriendItemViewHolder {

    val itemView = LayoutInflater
      .from(parent.context)
      .inflate(R.layout.scan_friend_list_item_layout, parent, false)

    return ScanFriendItemViewHolder(itemView, context, fragment, activity, userList, listener)

  }

  private val PREFNAME: String = "local_user"
  private val gson = Gson()

  override fun getItemCount(): Int {

    var itemCount: Int = model.friendList?.size ?: 0

//    if (model.friendList != null) {
//      itemCount = model.friendList!!.size
//    }

    Log.e("ATTENTION ATTENTION", "ScanFriendViewAdapter: getItemCount(): itemCount: ${itemCount.toString()}")

    Log.e("ATTENTION ATTENTION", "ScanFriendViewAdapter: model.friendList?.size: ${model.friendList?.size.toString()}")


    return itemCount

  }

  override fun onBindViewHolder(holderScan: ScanFriendItemViewHolder, position: Int) {

    holderScan.bind(model.friendList?.get(position)!!)

    Log.e("ATTENTION ATTENTION", "ScanFriendViewAdapter: onBindViewHolder(...): " +
            "allInfo.friendList?.get(position)?.name: ${model.friendList?.get(position)?.name}")

  }

}