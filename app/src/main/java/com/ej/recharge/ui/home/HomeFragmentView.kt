package com.ej.recharge.ui.home

import com.ej.recharge.model.FriendListModel
import com.ej.recharge.common.mvp.View
import com.ej.recharge.model.UserListModel

interface HomeFragmentView : View<UserListModel, FriendListModel> {

  fun displayUserMain(userList: UserListModel)

  fun displayUserSecond(userList: UserListModel)

  fun displayFriends(friendList: FriendListModel)

}