package com.ej.recharge.presenter

interface  PickContactListener {

    fun pickContact(index: Int)
    fun addContactToIndex(index: Int)
    fun removeContactFromIndex(index: Int)
    fun deleteContact(index: Int)
    fun deleteContacts()

}