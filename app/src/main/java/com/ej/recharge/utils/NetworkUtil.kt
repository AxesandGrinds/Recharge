package com.ej.recharge.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class NetworkUtil {

    private val TYPE_WIFI                    = 1
    private val TYPE_MOBILE                  = 2
    private var TYPE_NOT_CONNECTED           = 0
    private val NETWORK_STATUS_NOT_CONNECTED = 0
    private val NETWORK_STAUS_WIFI           = 1
    private val NETWORK_STATUS_MOBILE        = 2

    fun getConnectivityStatus(context: Context): Int {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        if (null != activeNetwork) {
            if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) return TYPE_WIFI
            if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) return TYPE_MOBILE
        }
        return TYPE_NOT_CONNECTED
    }

    fun getConnectivityStatusString(context: Context): Int {
        val conn = getConnectivityStatus(context)
        var status = 0
        if (conn == TYPE_WIFI) {
            status = NETWORK_STAUS_WIFI
        } else if (conn == TYPE_MOBILE) {
            status = NETWORK_STATUS_MOBILE
        } else if (conn == TYPE_NOT_CONNECTED) {
            status = NETWORK_STATUS_NOT_CONNECTED
        }
        return status
    }

    fun isOnline(context: Context) : Boolean {

        var result: Boolean = false

        val cm =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (cm != null) {

                val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)

                if (capabilities != null) {

                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                        return true
                    }
                    else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                        return true
                    }
                    else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                        return true
                    }

                }

            }
            else {

                if (cm != null) {

                    val activeNetwork = cm.activeNetworkInfo

                    if (activeNetwork != null) {

                        if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {

                            result = isConnected(context)

                        }
                        else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {

                            result = isConnected(context)

                        }

                    }

                }

                return result

            }


        }
        return false

    }


    fun isConnected(context: Context): Boolean {

        val cm = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo

        if (activeNetwork != null && activeNetwork.isConnected) {

            try {

                val url = URL("http://www.google.com/")
                val urlc = url.openConnection() as HttpURLConnection
                urlc.setRequestProperty("User-Agent", "test")
                urlc.setRequestProperty("Connection", "close")
                urlc.connectTimeout = 1000
                urlc.connect()
                return when (urlc.responseCode) {
                    200 -> {
                        true
                    }
                    else -> {
                        false
                    }
                }

            }
            catch (e: IOException) {
                Log.i("Error", "Error checking internet connection", e)
                return false
            }

        }
        else
            return false

    }






}
