package com.app.ej.cs.presenter.impl

import android.util.Log
import com.app.ej.cs.model.FriendListModel
import com.app.ej.cs.presenter.EditFragmentPresenter
import com.app.ej.cs.ui.edit.EditFragmentView
import com.app.ej.cs.common.mvp.impl.BasePresenter
import com.app.ej.cs.conf.TAG
import com.app.ej.cs.init.InitApp
import com.app.ej.cs.model.UserListModel
import com.app.ej.cs.repository.UsersAndFriendsRepository
import com.app.ej.cs.repository.impl.MemoryUserAndFriendsRepository
import javax.inject.Inject


/**
 * Presenter for the display of the list
 */

class EditFragmentPresenterImpl @Inject constructor(
) : BasePresenter<UserListModel, FriendListModel, EditFragmentView>(),

  EditFragmentPresenter {


  override fun displayEditDetails() {

    val memoryUserAndFriendsRepository: MemoryUserAndFriendsRepository = MemoryUserAndFriendsRepository(InitApp.appContext)

    val userMainMutableList = memoryUserAndFriendsRepository.userList()

    val userSecondMutableList = memoryUserAndFriendsRepository.userList()

    val friendsMutableList = memoryUserAndFriendsRepository.friendList()

    view?.displayUserMain(UserListModel(userMainMutableList))

    view?.displayUserSecond(UserListModel(userSecondMutableList))

    view?.displayFriends(FriendListModel(friendsMutableList))

    Log.e(TAG, "In EditFragmentPresenterImpl using User Repository $memoryUserAndFriendsRepository")
    Log.e("ATTENTION ATTENTION", "userMainMutableList: ${userMainMutableList.toString()}")
    Log.e("ATTENTION ATTENTION", "friendsMutableList: ${friendsMutableList.toString()}")

  }

}