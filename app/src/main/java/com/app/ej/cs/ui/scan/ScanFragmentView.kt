package com.app.ej.cs.ui.scan

import com.app.ej.cs.model.FriendListModel
import com.app.ej.cs.common.mvp.View
import com.app.ej.cs.model.UserListModel

interface ScanFragmentView : View<UserListModel, FriendListModel> {

  fun displayUserMain(userList: UserListModel)

  fun displayUserSecond(userList: UserListModel)

  fun displayFriends(friendList: FriendListModel)

}