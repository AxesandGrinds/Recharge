package com.app.ej.cs.presenter

import com.app.ej.cs.model.FriendListModel
import com.app.ej.cs.ui.edit.EditFragmentView
import com.app.ej.cs.common.mvp.Presenter
import com.app.ej.cs.model.UserListModel

/**
 * The Presenter for the NewsList
 */

interface EditFragmentPresenter : Presenter<UserListModel, FriendListModel, EditFragmentView> {

  /**
   * Invoke in order to display the primary user info
   */
  fun displayEditDetails()

}