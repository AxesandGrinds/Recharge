package com.app.ej.cs.presenter

import com.app.ej.cs.model.FriendsModel
import com.app.ej.cs.ui.scan.ScanFragmentView
import com.app.ej.cs.common.mvp.Presenter
import com.app.ej.cs.model.FriendListModel
import com.app.ej.cs.model.UserListModel

/**
 * The Presenter for the News Detail
 */
interface ScanFragmentPresenter : Presenter<UserListModel, FriendListModel, ScanFragmentView> {

  /**
   * Invoke in order to display the primary and secondary user info and friend's user info
   */
  fun displayScanDetails()

}