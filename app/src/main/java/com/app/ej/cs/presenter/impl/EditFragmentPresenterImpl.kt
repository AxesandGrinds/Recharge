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
import com.app.ej.cs.repository.entity.User
import com.app.ej.cs.repository.impl.MemoryUserAndFriendsRepository
import com.app.ej.cs.utils.PhoneUtil
import javax.inject.Inject


/**
 * Presenter for the display of the list
 */

class EditFragmentPresenterImpl @Inject constructor(
) : BasePresenter<UserListModel, FriendListModel, EditFragmentView>(),

  EditFragmentPresenter {

  val phoneUtil: PhoneUtil = PhoneUtil()

  override fun displayEditDetails() {

    val memoryUserAndFriendsRepository: MemoryUserAndFriendsRepository = MemoryUserAndFriendsRepository(InitApp.appContext)

    val usersList: MutableList<User> = memoryUserAndFriendsRepository.userList()

    if (usersList.size == 1 || !phoneUtil.isDualSim(InitApp.appContext)) {

      view?.displayUserMain(UserListModel(mutableListOf(usersList[0])))

    }
    else if (usersList.size == 2 && phoneUtil.isDualSim(InitApp.appContext)) {

      view?.displayUserMain(UserListModel(mutableListOf(usersList[0])))
      view?.displayUserSecond(UserListModel(mutableListOf(usersList[1])))

    }
    else if (usersList.size == 2 && !phoneUtil.isDualSim(InitApp.appContext)) {

      view?.displayUserMain(UserListModel(mutableListOf(usersList[0])))

    }

    val friendsMutableList = memoryUserAndFriendsRepository.friendList()

    view?.displayFriends(FriendListModel(friendsMutableList))

    Log.e(TAG, "In EditFragmentPresenterImpl using User Repository $memoryUserAndFriendsRepository")
    Log.e("ATTENTION ATTENTION", "userMutableList: ${usersList.toString()}")
    Log.e("ATTENTION ATTENTION", "friendsMutableList: ${friendsMutableList.toString()}")

  }

}