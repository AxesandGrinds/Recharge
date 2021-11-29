package com.ej.recharge.repository.entity

import com.google.gson.annotations.SerializedName

/**
 * The Entity for the Users
 */

data class User(

        @SerializedName("uid")
        var uid:   String,

        @SerializedName("index")
        var index: Int,

        @SerializedName("description")
        var description: String,

        @SerializedName("folded")
        var folded: Boolean,

        @SerializedName("created")
        var created: String?,

        @SerializedName("name")
        var name:    String?,

        @SerializedName("email")
        var email:   String?,

        @SerializedName("phone")
        var phone:   String?,

        @SerializedName("network")
        var network: String?,

        @SerializedName("pin")
        var pin: String?,

        @SerializedName("bank1")
        var bank1: String?,

        @SerializedName("bank2")
        var bank2: String?,

        @SerializedName("bank3")
        var bank3: String?,

        @SerializedName("bank4")
        var bank4: String?,

        @SerializedName("smartCardNumber1")
        var smartCardNumber1: String?,

        @SerializedName("smartCardNumber2")
        var smartCardNumber2: String?,

        @SerializedName("smartCardNumber3")
        var smartCardNumber3: String?,

        @SerializedName("smartCardNumber4")
        var smartCardNumber4: String?,

        @SerializedName("meterNumber1")
        var meterNumber1:     String?,

        @SerializedName("meterNumber2")
        var meterNumber2:     String?,

        @SerializedName("meterNumber3")
        var meterNumber3:     String?,

        ) {

    constructor() : this(
            "", 0, "", false, null,
            null, null, null, null, null,
            null, null, null, null,
            null, null, null,
            null, null, null, null)
}

data class UsersInfo(
        var userList: MutableList<User> = mutableListOf()
)