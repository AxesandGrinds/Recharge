package com.app.ej.cs.presenter

import com.app.ej.cs.model.FriendListModel
import com.app.ej.cs.ui.edit.EditFragmentView
import com.app.ej.cs.common.mvp.Presenter
import com.app.ej.cs.model.UserListModel

interface EditFragmentPresenter : Presenter<UserListModel, FriendListModel, EditFragmentView> {

  fun displayEditDetails()

}