package com.ej.recharge.model

import com.ej.recharge.common.mvp.Model
import com.ej.recharge.repository.entity.User

data class UsersModel(val user: User) : Model