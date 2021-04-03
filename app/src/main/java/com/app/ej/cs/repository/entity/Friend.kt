package com.app.ej.cs.repository.entity

import android.view.View
import android.widget.AdapterView

/**
 * The Entity for the Friends
 */

data class Friend(

        var index: Int,
        var description: String,

        var folded: Boolean,

        var name:  String?,
        var phone1: String?,
        var phone2: String?,
        var phone3: String?,

        var network1: String?,
        var network2: String?,
        var network3: String?,

        var bank1: String?,
        var bank2: String?,
        var bank3: String?,
        var bank4: String?,

        var accountNumber1: String?,
        var accountNumber2: String?,
        var accountNumber3: String?,
        var accountNumber4: String?,

  ) {

    constructor() : this(
            0, "", false, null, null,
            null, null, null, null, null,
            null, null, null, null, null,
            null, null, null)
}

data class FriendInfo(
        var friendsList: MutableList<Friend> = mutableListOf()
)