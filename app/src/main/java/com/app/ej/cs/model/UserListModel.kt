package com.app.ej.cs.model

import com.app.ej.cs.common.mvp.Model
import com.app.ej.cs.repository.entity.User

/**
 * Model for the UserList
 */

data class UserListModel(var userList: MutableList<User>) : Model