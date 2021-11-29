package com.ej.recharge.ui.home

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.ej.recharge.common.events.OnViewHolderItemSelected
import com.ej.recharge.R
import com.ej.recharge.model.FriendListModel
import com.ej.recharge.repository.entity.Friend
import com.ej.recharge.repository.entity.User
import com.google.gson.Gson

class HomeFriendViewAdapter(
  private val context: Context,
  private val fragment: Fragment,
  private val activity: Activity,
  private val userList: MutableList<User>,
  private val model: FriendListModel,
  private val listener: OnViewHolderItemSelected<Friend?>? = null
) :

  RecyclerView.Adapter<HomeFriendItemViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeFriendItemViewHolder {

    val itemView = LayoutInflater
      .from(parent.context)
      .inflate(R.layout.home_friend_list_item_layout, parent, false)

    return HomeFriendItemViewHolder(itemView, context, fragment, activity, userList, listener)

  }

  private val PREFNAME: String = "local_user"
  private val gson = Gson()

  override fun getItemCount(): Int {

    var itemCount: Int = model.friendList?.size ?: 0

    Log.e("ATTENTION ATTENTION", "HomeFriendViewAdapter: getItemCount(): itemCount: ${itemCount.toString()}")

    Log.e("ATTENTION ATTENTION", "HomeFriendViewAdapter: model.friendList?.size: ${model.friendList?.size.toString()}")

    return itemCount

  }

  override fun onBindViewHolder(holderHome: HomeFriendItemViewHolder, position: Int) {

    holderHome.bind(model.friendList?.get(position)!!)

    Log.e("ATTENTION ATTENTION", "HomeFriendViewAdapter: onBindViewHolder(...): " +
            "allInfo.friendList?.get(position)?.name: ${model.friendList?.get(position)?.name}")

  }

}