package com.app.ej.cs.presenter

import android.content.Intent

interface AddFriendDoneListener {

    fun contactPicked(data: Intent?, index: Int)

}