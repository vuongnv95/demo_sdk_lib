package com.vht_iot.vsmartsdk.sdk_config

import android.app.Application

data class SDKConfigData(
    val application: Application,
    val appId: String,
    val adminId: String,
    val adminPassword: String
)
