package com.app.ej.cs.repository

import com.app.ej.cs.repository.entity.Friend
import com.app.ej.cs.repository.entity.User

/**
 * Repository for the Users And Friends
 */
interface UsersAndFriendsRepository {

  /**
   * Retrieve the News by the Id if present
   */
  fun userById(id: Int): User?

  /**
   * All the news
   */
  fun userList(): MutableList<User>

  /**
   * Insert a news
   */
  fun userInsert(user: User)

  /**
   * Insert a list of news
   */
  fun userInsert(userList: MutableList<User>) = userList.forEach { userInsert(it) }

  /**
   * Retrieve the News by the Id if present
   */
  fun friendById(id: Int): Friend?

  /**
   * All the news
   */
  fun friendList(): MutableList<Friend>

  /**
   * Insert a news
   */
  fun friendInsert(friend: Friend)

  /**
   * Insert a list of news
   */
  fun friendInsert(friendList: MutableList<Friend>) = friendList.forEach { friendInsert(it) }

}