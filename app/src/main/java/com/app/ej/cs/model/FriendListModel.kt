package com.app.ej.cs.model

import com.app.ej.cs.common.mvp.Model
import com.app.ej.cs.repository.entity.Friend

data class FriendListModel(var friendList: MutableList<Friend>?) : Model