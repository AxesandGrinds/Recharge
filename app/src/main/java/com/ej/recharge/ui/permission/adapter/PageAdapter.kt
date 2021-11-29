package com.ej.recharge.ui.permission.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.ej.recharge.ui.permission.fragment.PermissionFragment
import com.ej.recharge.ui.permission.model.PermissionModel


class PagerAdapter(fm: FragmentManager?, permissions: List<PermissionModel>) :
    FragmentStatePagerAdapter(fm!!) {
    private val permissions: List<PermissionModel>
    override fun getItem(position: Int): Fragment {
        return PermissionFragment.newInstance(permissions[position])
    }

    override fun getCount(): Int {
        return permissions.size
    }

    init {
        this.permissions = permissions
    }
}
