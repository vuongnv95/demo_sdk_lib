package com.vht_iot.vsmartsdk.network.data

data class DeviceResponse(
    val devices: List<Device> = listOf(),
    val limit: Int = 0,
    val offset: Int = 0,
    val total: Int = 0
)