package com.app.ej.cs.ui.edit

import com.app.ej.cs.model.FriendListModel
import com.app.ej.cs.common.mvp.View
import com.app.ej.cs.model.UserListModel


/**
 * This is a View in a MVP architecture for Friend's Main Item With list
 */
interface EditFragmentView : View<UserListModel, FriendListModel> {

  /**
   * Displays the list of user in the View
   */
  fun displayUserMain(userList: UserListModel)

  /**
   * Displays the list of news in the View
   */
  fun displayUserSecond(userList: UserListModel)

  /**
   * Displays the list of news in the View
   */
  fun displayFriends(friendList: FriendListModel)

}