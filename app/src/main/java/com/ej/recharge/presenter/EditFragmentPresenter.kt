package com.ej.recharge.presenter

import com.ej.recharge.model.FriendListModel
import com.ej.recharge.ui.edit.EditFragmentView
import com.ej.recharge.common.mvp.Presenter
import com.ej.recharge.model.UserListModel

interface EditFragmentPresenter : Presenter<UserListModel, FriendListModel, EditFragmentView> {

  fun displayEditDetails()

}