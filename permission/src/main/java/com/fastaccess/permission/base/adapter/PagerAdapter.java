package com.fastaccess.permission.base.adapter;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.fastaccess.permission.base.fragment.PermissionFragment;
import com.fastaccess.permission.base.model.PermissionModel;

import java.util.List;


public class PagerAdapter extends FragmentStatePagerAdapter {

    private List<PermissionModel> permissions;

    public PagerAdapter(FragmentManager fm, List<PermissionModel> permissions) {
        super(fm);
        this.permissions = permissions;
    }

    @Override public Fragment getItem(int position) {
        return PermissionFragment.newInstance(permissions.get(position));
    }

    @Override public int getCount() {
        return permissions.size();
    }
}
