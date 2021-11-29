package com.ej.recharge.repository

import com.ej.recharge.repository.entity.Friend
import com.ej.recharge.repository.entity.User

interface UsersAndFriendsRepository {

  fun userById(id: Int): User?

  fun userList(): MutableList<User>

  fun userInsert(user: User)

  fun userInsert(userList: MutableList<User>) = userList.forEach { userInsert(it) }

  fun friendById(id: Int): Friend?

  fun friendList(): MutableList<Friend>

  fun friendListUnsaved(): MutableList<Friend>

  fun friendInsert(friend: Friend)

  fun friendInsert(friendList: MutableList<Friend>) = friendList.forEach { friendInsert(it) }

}