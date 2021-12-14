package com.vht_iot.vsmartsdk.network.connect

import android.os.Handler
import android.os.Looper
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

sealed class NetworkState {
    object NO_INTERNET : NetworkState()
    object UNAUTHORIZED : NetworkState()
    object INITIALIZE : NetworkState()
    object ERROR : NetworkState()
    object NOT_FOUND : NetworkState()
    object BAD_REQUEST : NetworkState()
    object CONNECTION_LOST : NetworkState()
    object FORBIDDEN : NetworkState()
    object SERVER_NOT_AVAILABLE : NetworkState()
    object DATA_ERROR : NetworkState()
    object ACCESS_DENY : NetworkState()
    object NO_PERMISSION : NetworkState()
    object NO_CONNECT_INTERNET : NetworkState()
    object CONNECTED_INTERNET : NetworkState()
    data class GENERIC(val exception: ApiException) : NetworkState()
}

class NetworkEvent  constructor() {

    @ExperimentalCoroutinesApi
    private val events: ConflatedBroadcastChannel<NetworkState> by lazy {
        ConflatedBroadcastChannel<NetworkState>().also { channel ->
            channel.offer(NetworkState.INITIALIZE)
        }
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    val observableNetworkState: Flow<NetworkState> = events.asFlow()

    @ExperimentalCoroutinesApi
    fun publish(networkState: NetworkState) {
        Handler(Looper.getMainLooper()).post {
            events.offer(networkState)
        }
    }
}