package com.ej.recharge.presenter.impl

import android.util.Log
import com.ej.recharge.presenter.HomeFragmentPresenter
import com.ej.recharge.ui.home.HomeFragmentView
import com.ej.recharge.common.mvp.impl.BasePresenter
import com.ej.recharge.conf.TAG
import com.ej.recharge.init.InitApp
import com.ej.recharge.model.FriendListModel
import com.ej.recharge.model.UserListModel
import com.ej.recharge.repository.entity.User
import com.ej.recharge.repository.impl.MemoryUserAndFriendsRepository
import com.ej.recharge.utils.PhoneUtil
import javax.inject.Inject

class HomeFragmentPresenterImpl @Inject constructor(
) : BasePresenter<UserListModel, FriendListModel, HomeFragmentView>(),

  HomeFragmentPresenter {

  val phoneUtil: PhoneUtil = PhoneUtil()

  override fun displayHomeDetails() {

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