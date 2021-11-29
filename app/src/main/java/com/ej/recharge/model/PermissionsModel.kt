package com.ej.recharge.model

import com.ej.recharge.common.mvp.Model
import com.ej.recharge.repository.entity.Friend

data class PermissionsModel(val friend: Friend) : Model