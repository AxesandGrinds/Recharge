package com.app.ej.cs.presenter

import android.content.Intent

interface CheckLoggedIn {

    fun startLogin(i: Intent?)

    fun startRegister(i: Intent?)

}