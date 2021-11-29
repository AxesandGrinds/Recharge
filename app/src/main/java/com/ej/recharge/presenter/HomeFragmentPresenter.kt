package com.ej.recharge.presenter

import com.ej.recharge.ui.home.HomeFragmentView
import com.ej.recharge.common.mvp.Presenter
import com.ej.recharge.model.FriendListModel
import com.ej.recharge.model.UserListModel

interface HomeFragmentPresenter : Presenter<UserListModel, FriendListModel, HomeFragmentView> {

  fun displayHomeDetails()

}