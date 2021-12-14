package com.vht_iot.vsmartsdk.future.organization

import android.util.Log
import com.vht_iot.vsmartsdk.network.connect.ApiArrayOrgResponse
import com.vht_iot.vsmartsdk.network.connect.ApiInterface
import com.vht_iot.vsmartsdk.network.data.ResultApi
import com.vht_iot.vsmartsdk.network.data.VOrganizationResponse
import com.vht_iot.vsmartsdk.sdk_config.SDKConfig
import com.vht_iot.vsmartsdk.utils.HandleError
import com.vht_iot.vsmartsdk.utils.VConfigUtils
import com.vht_iot.vsmartsdk.utils.VDefine
import com.vht_iot.vsmartsdk.utils.createBodyMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class OrganizationManager {

    private var job: Job = Job()
    internal var apiInterface: ApiInterface? = null

    init {
        apiInterface = SDKConfig.apiInterface
    }

    val scope = CoroutineScope(Dispatchers.IO + job)
    val mainScope = CoroutineScope(Dispatchers.Main + job)

    companion object {
        @Volatile
        private var instance: OrganizationManager? = null
        private var TAG = "OrganizationManager"
        fun getInstance(): OrganizationManager =
            instance ?: synchronized(this) {
                instance ?: OrganizationManager()
            }
    }

    fun createOrganizations(
        groupId: String,
        orgId: String,
        name: String,
        description: String,
        sucess: (ResultApi<VOrganizationResponse>) -> Unit,
        failt: (ResultApi<String>) -> Unit
    ) {
        scope.launch {
            try {
                val data = mutableMapOf<String, String>()
                data.put(
                    VDefine.ParamApi.PARAM_GROUP_ID,
                    if (groupId.isEmpty()) VConfigUtils.GROUP_PARENT else groupId
                )
                data.put(VDefine.ParamApi.PARAM_ORG_ID, orgId)
                data.put(VDefine.ParamApi.PARAM_NAME, name)
                data.put(VDefine.ParamApi.PARAM_DESCRIPTION, description)
                data.put(VDefine.ParamApi.PARAM_PROJECT_ID, SDKConfig.sdkConfigData?.appId ?: "")
                val groupResponse = apiInterface?.createOrganizations(createBodyMap(data))
                groupResponse?.let {
                    if (SDKConfig.debugMode) {
                        Log.d(
                            TAG,
                            "createOrganizations() called success : ${groupResponse}"
                        )
                    }
                    mainScope.launch {
                        sucess(
                            ResultApi.VSmartSuccess(
                                it
                            )
                        )
                    }
                }

            } catch (e: Exception) {
                HandleError.handCommonError(e, failt)
                if (SDKConfig.debugMode) {
                    Log.d(TAG, "createOrganizations() called err :$e")
                }
            }
        }
    }

    fun getOrganizations(
        sucess: (ResultApi<ApiArrayOrgResponse<VOrganizationResponse>>) -> Unit,
        failt: (ResultApi<String>) -> Unit
    ) {
        scope.launch {
            try {
                val data = HashMap<String,String>()
                data.put(VDefine.ParamApi.PARAM_PROJECT_ID, SDKConfig.sdkConfigData?.appId ?: "")
                val groupResponse = apiInterface?.getOrganizations(data)
                groupResponse?.let {
                    if (SDKConfig.debugMode) {
                        Log.d(
                            TAG,
                            "createOrganizations() called success : ${groupResponse}"
                        )
                    }
                    mainScope.launch {
                        sucess(
                            ResultApi.VSmartSuccess(
                                it
                            )
                        )
                    }
                }

            } catch (e: Exception) {
                HandleError.handCommonError(e, failt)
                if (SDKConfig.debugMode) {
                    Log.d(TAG, "createOrganizations() called err :$e")
                }
            }
        }
    }

}