package com.vht_iot.vsmartsdk.sdk_config

import com.vht_iot.vsmartsdk.future.user_manager.UserManager
import com.vht_iot.vsmartsdk.network.connect.ApiInterface
import com.vht_iot.vsmartsdk.network.connect.module.ApiModule
import com.vht_iot.vsmartsdk.utils.VConfigUtils

/**
 * khởi tạo thư viện bằng appid, sceretkey
 */
class SDKConfig {

    companion object {

        /**
         * true: debug mode enable
         * false: debug mode disable
         */
        var debugMode = false
        var sdkConfigData: SDKConfigData? = null
        internal var apiInterface: ApiInterface? = null

        fun startWithAppId(sdkConfig: SDKConfigData) {
            VConfigUtils.APP_ID = sdkConfig.appId
            sdkConfigData = sdkConfig
            apiInterface =
                ApiModule.getInstance(sdkConfig.application, "http://125.212.248.229:4437")
                    .provideApiInterface()
            UserManager.getInstance().loginAddmin(
                sdkConfig.application,
                "smarthome@viettel.com.vn", "test1a@",
                sucess = {
                },
                failt = {
                })
        }

        @Volatile
        private var instance: SDKConfig? = null

        fun getInstance(): SDKConfig =
            instance ?: synchronized(this) {
                instance ?: SDKConfig()
            }

    }

    fun onDestroy(){
        UserManager.getInstance().onDestroy()
    }
}