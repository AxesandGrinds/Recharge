package com.ej.recharge.model

import com.ej.recharge.common.mvp.Model
import com.ej.recharge.repository.entity.Friend
import com.ej.recharge.repository.entity.Permission

data class PermissionListModel(var permissionList: List<Permission>) : Model