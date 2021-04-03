package com.app.ej.cs.model

import com.app.ej.cs.common.mvp.Model
import com.app.ej.cs.repository.entity.Friend
import com.app.ej.cs.repository.entity.Permission

/**
 * Model for the NewsList
 */

data class PermissionListModel(var permissionList: List<Permission>) : Model