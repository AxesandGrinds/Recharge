package com.ej.recharge.model

import com.ej.recharge.common.mvp.Model
import com.ej.recharge.repository.entity.User

data class UserListModel(var userList: MutableList<User>) : Model