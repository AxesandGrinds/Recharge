package com.app.ej.cs.model

import com.app.ej.cs.common.mvp.Model
import com.app.ej.cs.repository.entity.Friend


/**
 * The Model for the friends
 */

data class PermissionsModel(val friend: Friend) : Model