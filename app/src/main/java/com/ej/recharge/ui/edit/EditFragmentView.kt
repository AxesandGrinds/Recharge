package com.ej.recharge.ui.edit

import com.ej.recharge.model.FriendListModel
import com.ej.recharge.common.mvp.View
import com.ej.recharge.model.UserListModel

interface EditFragmentView : View<UserListModel, FriendListModel> {


  fun displayUserMain(userList: UserListModel)

  fun displayUserSecond(userList: UserListModel)

  fun displayFriends(friendList: FriendListModel)

}