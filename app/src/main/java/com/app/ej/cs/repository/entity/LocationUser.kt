package com.app.ej.cs.repository.entity

import com.google.gson.annotations.SerializedName

/**
 * The Entity for the Users
 */

data class LocationUser(

        @SerializedName("uid")
        var uid:      String?,

        @SerializedName("created")
        var created:  String?,

        @SerializedName("name")
        var name:     String?,

        @SerializedName("email")
        var email:    String?,

        @SerializedName("phone")
        var phone:    String?,

        @SerializedName("network")
        var network:  String?,

        @SerializedName("phone2")
        var phone2:   String?,

        @SerializedName("network2")
        var network2: String?,

        @SerializedName("longitude")
        var longitude: Double?,

        @SerializedName("latitude")
        var latitude:  Double?,

        @SerializedName("address")
        var address:    String?,

        @SerializedName("city")
        var city:       String?,

        @SerializedName("state")
        var state:     String?,

        @SerializedName("country")
        var country:    String?,

        @SerializedName("postalCode")
        var postalCode: String?,

        @SerializedName("knownName")
        var knownName:  String?,

        ) {

        constructor() : this(
                "", "", "", "", "",
                "", null, null, null, null,
                "", "", "", "",
                "", "")
}

data class LocationUserInfo(
        var userLocationList: ArrayList<LocationUser> = arrayListOf()
)