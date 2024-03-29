package com.ej.recharge.repository.entity

import android.view.View
import android.widget.AdapterView
import com.google.gson.annotations.SerializedName

/**
 * The Entity for the Friends
 */

data class UserAndFriend(

        val index: Int,
        val description: String,

        val name:  String?,
        val phone1: String?,
        val phone2: String?,
        val phone3: String?,

        val network1: String?,
        val network2: String?,
        val network3: String?,

        val bank1: String?,
        val bank2: String?,
        val bank3: String?,
        val bank4: String?,

        val accountNumber1: String?,
        val accountNumber2: String?,
        val accountNumber3: String?,
        val accountNumber4: String?,

  )

data class UserAndFriendInfo(

        @SerializedName("usersList")
        var usersList: MutableList<User> = mutableListOf(),

        @SerializedName("friendList")
        var friendList: MutableList<Friend>? = mutableListOf()

)