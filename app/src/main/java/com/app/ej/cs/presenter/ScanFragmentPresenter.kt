package com.app.ej.cs.presenter

import com.app.ej.cs.model.FriendsModel
import com.app.ej.cs.ui.scan.ScanFragmentView
import com.app.ej.cs.common.mvp.Presenter
import com.app.ej.cs.model.FriendListModel
import com.app.ej.cs.model.UserListModel

interface ScanFragmentPresenter : Presenter<UserListModel, FriendListModel, ScanFragmentView> {

  fun displayScanDetails()

}