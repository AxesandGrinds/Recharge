package com.app.ej.cs.repository

import com.app.ej.cs.repository.entity.Friend
import com.app.ej.cs.repository.entity.User

interface UsersAndFriendsRepository {

  fun userById(id: Int): User?

  fun userList(): MutableList<User>

  fun userInsert(user: User)

  fun userInsert(userList: MutableList<User>) = userList.forEach { userInsert(it) }

  fun friendById(id: Int): Friend?

  fun friendList(): MutableList<Friend>

  fun friendInsert(friend: Friend)

  fun friendInsert(friendList: MutableList<Friend>) = friendList.forEach { friendInsert(it) }

}