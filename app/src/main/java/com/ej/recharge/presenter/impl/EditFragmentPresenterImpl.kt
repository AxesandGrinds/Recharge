package com.ej.recharge.presenter.impl

import android.util.Log
import com.ej.recharge.model.FriendListModel
import com.ej.recharge.presenter.EditFragmentPresenter
import com.ej.recharge.ui.edit.EditFragmentView
import com.ej.recharge.common.mvp.impl.BasePresenter
import com.ej.recharge.conf.TAG
import com.ej.recharge.init.InitApp
import com.ej.recharge.model.UserListModel
import com.ej.recharge.repository.UsersAndFriendsRepository
import com.ej.recharge.repository.entity.User
import com.ej.recharge.repository.impl.MemoryUserAndFriendsRepository
import com.ej.recharge.utils.PhoneUtil
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

    val friendsMutableList = memoryUserAndFriendsRepository.friendListUnsaved()

    view?.displayFriends(FriendListModel(friendsMutableList))

    Log.e(TAG, "In EditFragmentPresenterImpl using User Repository $memoryUserAndFriendsRepository")
    Log.e("ATTENTION ATTENTION", "userMutableList: ${usersList.toString()}")
    Log.e("ATTENTION ATTENTION", "friendsMutableList: ${friendsMutableList.toString()}")

  }

}