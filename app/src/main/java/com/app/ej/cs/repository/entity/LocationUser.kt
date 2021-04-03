package com.app.ej.cs.repository.entity

/**
 * The Entity for the Users
 */

data class LocationUser(

        var uid:      String,
        var created:  String,
        var name:     String,
        var email:    String,
        var phone:    String,
        var network:  String,
        var phone2:   String?,
        var network2: String?,

        var longitude: Double,
        var latitude:  Double,

        var address:    String,
        var city:       String,
        var state:     String,
        var country:    String,
        var postalCode: String,
        var knownName:  String,

        )

data class LocationUserInfo(
        var userLocationList: ArrayList<LocationUser> = arrayListOf()
)