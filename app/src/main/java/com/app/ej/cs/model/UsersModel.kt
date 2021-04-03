package com.app.ej.cs.model

import com.app.ej.cs.common.mvp.Model
import com.app.ej.cs.repository.entity.User


/**
 * The Model for the users
 */

data class UsersModel(val user: User) : Model