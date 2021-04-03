package com.app.ej.cs.presenter.impl

import android.util.Log
import com.app.ej.cs.presenter.ScanFragmentPresenter
import com.app.ej.cs.ui.scan.ScanFragmentView
import com.app.ej.cs.common.mvp.impl.BasePresenter
import com.app.ej.cs.conf.TAG
import com.app.ej.cs.init.InitApp.Companion.appContext
import com.app.ej.cs.model.FriendListModel
import com.app.ej.cs.model.UserListModel
import com.app.ej.cs.repository.UsersAndFriendsRepository
import com.app.ej.cs.repository.impl.MemoryUserAndFriendsRepository
import javax.inject.Inject

/**
 * Presenter for the display of the list
 */
class ScanFragmentPresenterImpl @Inject constructor(
) : BasePresenter<UserListModel, FriendListModel, ScanFragmentView>(),

  ScanFragmentPresenter {

  override fun displayScanDetails() {

    val memoryUserAndFriendsRepository: MemoryUserAndFriendsRepository = MemoryUserAndFriendsRepository(appContext)

    val userMainMutableList = memoryUserAndFriendsRepository.userList() //.subscribe

    val userSecondMutableList = memoryUserAndFriendsRepository.userList() //.subscribe

    val friendsMutableList = memoryUserAndFriendsRepository.friendList() //.subscribe

    view?.displayUserMain(UserListModel(userMainMutableList))

    view?.displayUserSecond(UserListModel(userSecondMutableList))

    view?.displayFriends(FriendListModel(friendsMutableList))

    Log.e(TAG, "In ScanFragmentPresenterImpl using User Repository $memoryUserAndFriendsRepository")
    Log.e("ATTENTION ATTENTION", "userMutableList: ${userMainMutableList.toString()}")
    Log.e("ATTENTION ATTENTION", "friendsMutableList: ${friendsMutableList.toString()}")

  }


}