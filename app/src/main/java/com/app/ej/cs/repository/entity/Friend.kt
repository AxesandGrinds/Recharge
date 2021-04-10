package com.app.ej.cs.repository.entity

import android.view.View
import android.widget.AdapterView
import com.google.gson.annotations.SerializedName

/**
 * The Entity for the Friends
 */

data class Friend(

        @SerializedName("index")
        var index: Int,

        @SerializedName("description")
        var description: String,

        @SerializedName("folded")
        var folded: Boolean,

        @SerializedName("name")
        var name:  String?,

        @SerializedName("phone1")
        var phone1: String?,

        @SerializedName("phone2")
        var phone2: String?,

        @SerializedName("phone3")
        var phone3: String?,

        @SerializedName("network1")
        var network1: String?,

        @SerializedName("network2")
        var network2: String?,

        @SerializedName("network3")
        var network3: String?,

        @SerializedName("bank1")
        var bank1: String?,

        @SerializedName("bank2")
        var bank2: String?,

        @SerializedName("bank3")
        var bank3: String?,

        @SerializedName("bank4")
        var bank4: String?,

        @SerializedName("accountNumber1")
        var accountNumber1: String?,

        @SerializedName("accountNumber2")
        var accountNumber2: String?,

        @SerializedName("accountNumber3")
        var accountNumber3: String?,

        @SerializedName("accountNumber4")
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