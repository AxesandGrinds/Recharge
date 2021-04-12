package com.app.ej.cs.ui.scan

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.app.ej.cs.common.events.OnViewHolderItemSelected
import com.app.ej.cs.R
import com.app.ej.cs.model.UserListModel
import com.app.ej.cs.repository.entity.User

class ScanUserMainViewAdapter(
  private val model: UserListModel,
  private val context: Context,
  private val fragment: Fragment,
  private val activity: Activity,
  private val listener: OnViewHolderItemSelected<User?>? = null
) :

  RecyclerView.Adapter<ScanUserMainItemViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanUserMainItemViewHolder {

    val itemView = LayoutInflater
      .from(parent.context)
      .inflate(R.layout.scan_user_main_item_layout, parent, false)

    return ScanUserMainItemViewHolder(itemView, context, fragment, activity, listener)

  }

  override fun getItemCount(): Int = model.userList.size

  override fun onBindViewHolder(holderScan: ScanUserMainItemViewHolder, position: Int) {

    holderScan.bind(model.userList[position])

  }

}