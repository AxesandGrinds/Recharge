package com.ej.recharge.ui.home

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.ej.recharge.common.events.OnViewHolderItemSelected
import com.ej.recharge.R
import com.ej.recharge.model.UserListModel
import com.ej.recharge.repository.entity.User

class HomeUserSecondViewAdapter(
  private val model: UserListModel,
  private val context: Context,
  private val fragment: Fragment,
  private val activity: Activity,
  private val listener: OnViewHolderItemSelected<User?>? = null
) :

  RecyclerView.Adapter<HomeUserSecondItemViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeUserSecondItemViewHolder {

    val itemView = LayoutInflater
      .from(parent.context)
      .inflate(R.layout.home_user_second_item_layout, parent, false)

    return HomeUserSecondItemViewHolder(itemView, context, fragment, activity, listener)

  }

  override fun getItemCount(): Int = model.userList.size

  override fun onBindViewHolder(holderHome: HomeUserSecondItemViewHolder, position: Int) {

    holderHome.bind(model.userList[position])

  }

}