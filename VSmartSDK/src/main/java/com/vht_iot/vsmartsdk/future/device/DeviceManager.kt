package com.vht_iot.vsmartsdk.future.device

import com.vht_iot.vsmartsdk.network.connect.ApiInterface
import com.vht_iot.vsmartsdk.network.data.DeviceResponse
import com.vht_iot.vsmartsdk.network.data.ResultApi
import com.vht_iot.vsmartsdk.sdk_config.SDKConfig
import com.vht_iot.vsmartsdk.utils.HandleError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Quản lý thông tin thiết bị.
 */
class DeviceManager()  {
    private var job: Job? = null
    internal var apiInterface: ApiInterface? = null

    init {
        apiInterface = SDKConfig.apiInterface
    }


    companion object {
        @Volatile
        private var instance: DeviceManager? = null

        fun getInstance(): DeviceManager =
            instance ?: synchronized(this) {
                instance ?: DeviceManager()
            }
    }

    /**
     * when receive code from phone, handle verify to server.
     */
    fun getListDevice(
        sucess: (ResultApi<DeviceResponse>) -> Unit,
        failt: (ResultApi<String>) -> Unit
    ) {
        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = apiInterface?.getListDevice()
                sucess(ResultApi.VSmartSuccess(data))
            } catch (e: Exception) {
                HandleError.handCommonError(e, failt)
            }
        }
    }

    fun onDestroy() {
        job?.cancel()
    }
}