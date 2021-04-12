package com.app.ej.cs.ui.edit

import com.app.ej.cs.model.FriendListModel
import com.app.ej.cs.common.mvp.View
import com.app.ej.cs.model.UserListModel

interface EditFragmentView : View<UserListModel, FriendListModel> {


  fun displayUserMain(userList: UserListModel)

  fun displayUserSecond(userList: UserListModel)

  fun displayFriends(friendList: FriendListModel)

}