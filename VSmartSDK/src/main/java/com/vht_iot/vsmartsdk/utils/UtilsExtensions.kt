package com.vht_iot.vsmartsdk.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

fun Context.isNetworkAvailable(): Boolean {
    val manager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val connection = manager.activeNetworkInfo
    return connection != null && connection.isConnectedOrConnecting
}

fun Context.isWifiAvailable(): Boolean {
    val connManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    val wifi = connManager!!.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
    return wifi!!.isConnected
}

@SuppressLint("NewApi")
fun Context.isNetworkOnline(): Boolean {
    var isOnline = false
    try {
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            manager.getNetworkCapabilities(manager.activeNetwork) // need ACCESS_NETWORK_STATE permission
        isOnline =
            capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return isOnline
}