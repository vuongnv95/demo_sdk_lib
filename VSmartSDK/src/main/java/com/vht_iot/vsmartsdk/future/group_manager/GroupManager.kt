package com.vht_iot.vsmartsdk.future.group_manager

import android.util.Log
import com.vht_iot.vsmartsdk.network.connect.ApiInterface
import com.vht_iot.vsmartsdk.network.data.ErrorCode
import com.vht_iot.vsmartsdk.network.data.ResultApi
import com.vht_iot.vsmartsdk.sdk_config.SDKConfig
import com.vht_iot.vsmartsdk.utils.HandleError
import com.vht_iot.vsmartsdk.utils.VConfigUtils
import com.vht_iot.vsmartsdk.utils.VDefine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.HttpException

class GroupManager {
    private var job: Job = Job()
    internal var apiInterface: ApiInterface? = null

    init {
        apiInterface = SDKConfig.apiInterface
    }

    val scope = CoroutineScope(Dispatchers.IO + job)
    val mainScope = CoroutineScope(Dispatchers.Main + job)

    companion object {
        @Volatile
        private var instance: GroupManager? = null
        private var TAG = "GroupManager"
        fun getInstance(): GroupManager =
            instance ?: synchronized(this) {
                instance ?: GroupManager()
            }
    }

    fun getGroupByName(
        groupName: String,
        entityType: String,
        sucess: (ResultApi<String>) -> Unit,
        failt: (ResultApi<String>) -> Unit
    ) {
        VDefine.useAddminToken = true
        scope.launch {
            try {
                val groupResponse =
                    apiInterface?.getGroupByName(groupName, entityType)
                if (groupResponse != null) {
                    if (SDKConfig.debugMode) {
                        Log.d(
                            TAG,
                            "getGroupByName() called success : ${groupResponse}"
                        )
                    }
                    if (groupResponse.id.isNotEmpty()){
                        VConfigUtils.GROUP_PARENT = groupResponse.id
                        mainScope.launch {
                            sucess(
                                ResultApi.VSmartSuccess(
                                    ""
                                )
                            )
                        }
                    }else{
                        createGroup(groupName, "", entityType, sucess, failt)
                    }

                } else {
                    mainScope.launch {
                        failt(
                            ResultApi.VSmartError(
                                ErrorCode.ERROR_SERVER,
                                "Không thể lấy thông tin group"
                            )
                        )
                    }
                }

            } catch (e: Exception) {
//                if (e is HttpException) {
//                    if (e.code() == ErrorCode.CODE_404) {
//                        createGroup(groupName, "", entityType, sucess, failt)
//                    }
//                } else {
                    HandleError.handCommonError(e, failt)
//                }
                if (SDKConfig.debugMode) {
                    Log.d(TAG, "getGroupByName() called err :$e")
                }
            }
        }
    }

    private fun createGroup(
        groupName: String,
        orgId: String,
        entityType: String,
        sucess: (ResultApi<String>) -> Unit,
        failt: (ResultApi<String>) -> Unit
    ) {
        VDefine.useAddminToken = true
        scope.launch {
            try {
                val data = mutableMapOf<String, String>()
                data.put(VDefine.ParamApi.PARAM_NAME, groupName)
                data.put(VDefine.ParamApi.PARAM_ENTITY_TYPE, entityType)
                data.put(VDefine.ParamApi.PARAM_ORG_ID, orgId)
                data.put(VDefine.ParamApi.PARAM_PROJECT_ID, SDKConfig.sdkConfigData?.appId ?: "")
                val groupResponse = apiInterface?.createGroup(createBodyMap(data))
                if (groupResponse != null) {
                    if (SDKConfig.debugMode) {
                        Log.d(
                            TAG,
                            "createGroup() called success : ${groupResponse}"
                        )
                    }
                    createRole(groupResponse.id, sucess, failt)
                }
            } catch (e: Exception) {
                HandleError.handCommonError(e, failt)
                if (SDKConfig.debugMode) {
                    Log.d(TAG, "createGroup() called err :$e")
                }
            }
        }
    }

    fun createRole(
        groupId: String,
        sucess: (ResultApi<String>) -> Unit,
        failt: (ResultApi<String>) -> Unit
    ) {
        VDefine.useAddminToken = true
        scope.launch {
            try {
                val data = mutableMapOf<String, String>()
                data.put(VDefine.ParamApi.PARAM_GROUP_ID, groupId)
                data.put(VDefine.ParamApi.PARAM_USER_ID, VConfigUtils.USER_ID)
                val roleResponse = apiInterface?.createRole(createBodyMap(data))
                mainScope.launch {
                    sucess(
                        ResultApi.VSmartSuccess(
                            ""
                        )
                    )
                }
            } catch (e: Exception) {
                HandleError.handCommonError(e, failt)
            }
        }
    }

    private fun createBodyMap(data: MutableMap<String, String>): RequestBody {
        val jsonObject = JSONObject()
        for ((key, value) in data) {
            jsonObject.put(key, value)
        }
        val body: RequestBody =
            RequestBody.create(
                "application/json".toMediaTypeOrNull(), jsonObject.toString()
            )
        return body
    }

    fun onDestroy() {
        job.cancel()
    }

}