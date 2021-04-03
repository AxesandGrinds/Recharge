package com.app.ej.cs.repository.entity

/**
 * The Entity for the Users
 */

data class User(


        var uid:   String,
        var index: Int,
        var description: String,

        var folded: Boolean,

        var created: String?,
        var name:    String?,
        var email:   String?,
        var phone:   String?,
        var network: String?,

        var pin: String?,

        var bank1: String?,
        var bank2: String?,
        var bank3: String?,
        var bank4: String?,

        var smartCardNumber1: String?,
        var smartCardNumber2: String?,
        var smartCardNumber3: String?,
        var smartCardNumber4: String?,

        var meterNumber1:     String?,
        var meterNumber2:     String?,
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