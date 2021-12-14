package com.vht_iot.vsmartsdk.future.user_manager

import android.content.Context
import android.util.Log
import com.vht_iot.vsmartsdk.future.group_manager.GroupManager
import com.vht_iot.vsmartsdk.network.connect.ApiInterface
import com.vht_iot.vsmartsdk.network.data.ErrorCode
import com.vht_iot.vsmartsdk.network.data.ResultApi
import com.vht_iot.vsmartsdk.network.data.VOTPPhoneResponse
import com.vht_iot.vsmartsdk.sdk_config.SDKConfig
import com.vht_iot.vsmartsdk.utils.HandleError
import com.vht_iot.vsmartsdk.utils.VConfigUtils
import com.vht_iot.vsmartsdk.utils.VDefine
import com.vht_iot.vsmartsdk.utils.createBodyMap
import com.viettel.vht.core.pref.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject

/**
 * Quản lý thông tin người dùng.
 */
class UserManager() {
    private var job = Job()
    internal var apiInterface: ApiInterface? = null

    init {
        apiInterface = SDKConfig.apiInterface
    }

    val scope = CoroutineScope(Dispatchers.IO + job)
    val mainScope = CoroutineScope(Dispatchers.Main + job)

    companion object {
        @Volatile
        private var instance: UserManager? = null
        private var TAG = "UserManager"
        fun getInstance(): UserManager =
            instance ?: synchronized(this) {
                instance ?: UserManager()
            }
    }

    /**
     * logout account user.
     */
    fun logout(
        sucess: (ResultApi<String>) -> Unit,
        failt: (ResultApi<String>) -> Unit
    ) {
        scope.launch {
            try {
                apiInterface?.logout()
                mainScope.launch {
                    sucess(ResultApi.VSmartSuccess(""))
                }
                if (SDKConfig.debugMode) {
                    Log.d(TAG, "logout() called success")
                }
            } catch (e: Exception) {
                HandleError.handCommonError(e, failt)
                if (SDKConfig.debugMode) {
                    Log.d(TAG, "logout() called error : ${e}")
                }
            }
        }
    }

    /**
     * login with sub user.
     */
    fun login(
        context: Context,
        phone: String,
        pass: String,
        sucess: (ResultApi<String>) -> Unit,
        failt: (ResultApi<String>) -> Unit
    ) {
        scope.launch {
            try {
                val data = mutableMapOf<String, String>()
                data.put(VDefine.ParamApi.PARAM_PROJECT_ID, VConfigUtils.APP_ID)
                data.put(VDefine.ParamApi.PARAM_IDENTIFIER, phone)
                data.put(VDefine.ParamApi.PARAM_PASSWORD, pass)
                val body = createBodyMap(data)
                val loginResponse = apiInterface?.login(body)
                loginResponse?.let {
                    AppPreferences.getInstance(context).setUserToken(it.token, it.deviceToken)
                    if (SDKConfig.debugMode) {
                        Log.d(TAG, "login() called success : ${it}")
                    }
//                    sucess(ResultApi.VSmartSuccess(loginResponse))

                    //create group
                    VConfigUtils.USER_ID = loginResponse.userId
                    GroupManager.getInstance().getGroupByName(
                        phone,
                        VDefine.EntityType.ORGANIZATION,
                        sucess, failt
                    )
                }
//                sucess(ResultApi.VSmartSuccess(loginResponse))
            } catch (e: Exception) {
                HandleError.handCommonError(e, failt)
                if (SDKConfig.debugMode) {
                    Log.d(TAG, "login() called err :$e")
                }
            }
        }
    }

    fun loginAddmin(
        context: Context,
        phone: String,
        pass: String,
        sucess: (ResultApi<String>) -> Unit,
        failt: (ResultApi<String>) -> Unit
    ) {
        scope.launch {
            try {
                val data = mutableMapOf<String, String>()
                data.put(VDefine.ParamApi.PARAM_IDENTIFIER, phone)
                data.put(VDefine.ParamApi.PARAM_PASSWORD, pass)
                val body = createBodyMap(data)
                val loginResponse = apiInterface?.login(body)
                loginResponse?.let {
                    AppPreferences.getInstance(context).setAddminToken(it.token)
                    mainScope.launch {
                        sucess(ResultApi.VSmartSuccess(it.token))
                    }
                }
            } catch (e: Exception) {
                HandleError.handCommonError(e, failt)
            }
        }
    }

//    /**
//     * when receive code from phone, handle verify to server.
//     */
//    fun sendVerificationCode(
//        phone: String,
//        type: String,
//        sucess: (ResultApi<String>) -> Unit,
//        failt: (ResultApi<String>) -> Unit
//    ) {
//        job = CoroutineScope(Dispatchers.IO).launch {
//            try {
//                apiInterface?.sendVerificationCode(phone, type)
//                sucess(ResultApi.VSmartSuccess(""))
//                if (SDKConfig.debugMode) {
//                    Log.d(TAG, "sendVerificationCode() called success")
//                }
//            } catch (e: Exception) {
//                if (SDKConfig.debugMode) {
//                    Log.d(TAG, "sendVerificationCode() called err :$e")
//                }
//                HandleError.handCommonError(e, failt)
//            }
//        }
//    }
//

    /**
     * when receive code from phone, handle verify to server.
     */
    fun sendVerificationCode(
        phone: String,
        type: Int,
        sucess: (ResultApi<String>) -> Unit,
        failt: (ResultApi<String>) -> Unit
    ) {
        scope.launch {
            try {
                VDefine.useAddminToken = true
                val data = mutableMapOf<String, String>()
                data.put(VDefine.ParamApi.PARAM_PHONE, phone)
                data.put(VDefine.ParamApi.PARAM_PROJECT_ID, SDKConfig.sdkConfigData?.appId ?: "")
                val body = createBodyMap(data)
                var verifyCodeResponse: VOTPPhoneResponse? = null
                when (type) {
                    VDefine.OTPType.RESET_PASSWORD ->
                        verifyCodeResponse = apiInterface?.sendVerificationCodeForgetPassword(body)
                    else->{
                        verifyCodeResponse = apiInterface?.sendVerificationCodeRegister(body)
                    }
                }

                if (verifyCodeResponse != null) {
                    if (SDKConfig.debugMode) {
                        Log.d(TAG, "registerUser() called success : ${verifyCodeResponse}")
                    }
                    mainScope.launch {
                        sucess(
                            ResultApi.VSmartSuccess("")
                        )
                    }
                } else {
                    mainScope.launch {
                        failt(
                            ResultApi.VSmartError(
                                ErrorCode.ERROR_SERVER,
                                "Không thể lấy thông tin code"
                            )
                        )
                    }

                }

            } catch (e: Exception) {
                HandleError.handCommonError(e, failt)
                if (SDKConfig.debugMode) {
                    Log.d(TAG, "registerUser() called err :$e")
                }
            }
        }
    }

    /**
     * use otp register to register account with phone.
     * handle register sub user with phone.
     */
    fun register(
        phone: String,
        password: String,
        otp: String,
        sucess: (ResultApi<String>) -> Unit,
        failt: (ResultApi<String>) -> Unit
    ) {
        scope.launch {
            try {
                VDefine.useAddminToken = true
                val data = mutableMapOf<String, String>()
                data.put(VDefine.ParamApi.PARAM_PHONE, phone)
                data.put(VDefine.ParamApi.PARAM_PROJECT_ID, SDKConfig.sdkConfigData?.appId ?: "")
                data.put(VDefine.ParamApi.PARAM_PASSWORD, password)
                data.put(VDefine.ParamApi.PARAM_OTP, otp)
                val body = createBodyMap(data)
                val passwordResponse = apiInterface?.registerUserWithPhone(body)
                if (SDKConfig.debugMode) {
                    Log.d(TAG, "setPassUser() called success${passwordResponse}")
                }
                mainScope.launch {
                    sucess(
                        ResultApi.VSmartSuccess("")
                    )
                }

            } catch (e: Exception) {
                HandleError.handCommonError(e, failt)
                if (SDKConfig.debugMode) {
                    Log.d(TAG, "setPassUser() called err :$e")
                }
            }
        }
    }


    /**
     * handle register sub user with email
     */
    fun registerUserWithEmail(
        email: String,
        pass: String,
        sucess: (ResultApi<String>) -> Unit,
        failt: (ResultApi<String>) -> Unit
    ) {
        scope.launch {
            try {
                val data = mutableMapOf<String, String>()
                data.put(VDefine.ParamApi.PARAM_EMAIL, email)
                data.put(VDefine.ParamApi.PARAM_PROJECT_ID, SDKConfig.sdkConfigData?.appId ?: "")
                data.put(VDefine.ParamApi.PARAM_PASSWORD, pass)
                val body = createBodyMap(data)
                VDefine.PARAM_IDENTIFIER
                val registerResponse = apiInterface?.registerUserWithEmail(body)
                if (registerResponse != null) {
                    if (SDKConfig.debugMode) {
                        Log.d(TAG, "registerUser() called success : ${registerResponse}")
                    }
                    mainScope.launch {
                        sucess(
                            ResultApi.VSmartSuccess(
                                registerResponse.identity_id
                            )
                        )
                    }
                } else {
                    mainScope.launch {
                        failt(
                            ResultApi.VSmartError(
                                ErrorCode.ERROR_SERVER,
                                "Không thể thực hiện đăng kí"
                            )
                        )
                    }

                }

            } catch (e: Exception) {
                HandleError.handCommonError(e, failt)
                if (SDKConfig.debugMode) {
                    Log.d(TAG, "registerUser() called err :$e")
                }
            }
        }
    }

    fun onDestroy() {
        job.cancel()
    }
}