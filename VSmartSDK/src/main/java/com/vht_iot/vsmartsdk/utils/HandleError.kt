package com.vht_iot.vsmartsdk.utils

import android.util.Log
import com.vht_iot.vsmartsdk.network.connect.NetworkException
import com.vht_iot.vsmartsdk.network.data.ErrorCode
import com.vht_iot.vsmartsdk.network.data.ResultApi
import com.vht_iot.vsmartsdk.sdk_config.SDKConfig
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class HandleError {

    companion object {
        val handler = CoroutineExceptionHandler { _, exception ->
            if (SDKConfig.debugMode) {
                Log.d("HandleError", "CoroutineExceptionHandler ${exception}")
            }
        }
        val scope = CoroutineScope(Dispatchers.Main + handler)

        fun handCommonError(e: Exception, failt: (ResultApi<String>) -> Unit) {
            if (e is NetworkException) {
                scope.launch {
                    failt(ResultApi.VSmartError(ErrorCode.CODE_NETWORK, "NetworkException"))
                }
            } else {
                if (e is HttpException) {
                    scope.launch {
                        failt(
                            ResultApi.VSmartError(
                                e.code(),
                                e.response()?.errorBody()?.byteString()?.utf8() ?: ""
                            )
                        )
                    }

                } else {
                    scope.launch {
                        failt(
                            ResultApi.VSmartError(
                                ErrorCode.ERROR_SERVER,
                                e.localizedMessage.toString()
                            )
                        )
                    }
                }
            }
        }
    }
}