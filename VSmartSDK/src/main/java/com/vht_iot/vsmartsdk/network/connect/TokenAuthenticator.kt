package com.vht_iot.vsmartsdk.network.connect

import com.google.gson.Gson
import com.vht_iot.vsmartsdk.network.data.response.LoginResponse
import com.vht_iot.vsmartsdk.utils.VDefine
import com.viettel.vht.core.pref.RxPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class TokenAuthenticator constructor(
    private val rxPreferences: RxPreferences,
    private val gson: Gson,
    private val networkEvent: NetworkEvent
) : Authenticator {

    @ExperimentalCoroutinesApi
    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshResult = refreshToken("${VDefine.ConfigSDK.BASE_URL}${VDefine.EndPointBE.LOGIN}")
        if (refreshResult)
            return response.request.newBuilder()
                .header("Authorization", rxPreferences.getToken() ?: "")
                .build()
        else {
            networkEvent.publish(NetworkState.UNAUTHORIZED)
            return null
        }
    }

    @Throws(IOException::class)
    fun refreshToken(url: String): Boolean {
        val refreshUrl = URL(url)
        val urlConnection = refreshUrl.openConnection() as HttpURLConnection
        urlConnection.apply {
            doInput = true
            setRequestProperty("Content-Type", "application/json")
            requestMethod = "POST"
            useCaches = false
        }
        var jsonObject = JSONObject()
        jsonObject.put(VDefine.PARAM_IDENTIFIER, rxPreferences.getEmail())
        jsonObject.put(VDefine.PARAM_PASSWORD, rxPreferences.getPassword())
        val urlParameters = jsonObject.toString()
        urlConnection.doOutput = true
        DataOutputStream(urlConnection.outputStream).apply {
            writeBytes(urlParameters)
            flush()
            close()
        }
        val responseCode = urlConnection.responseCode
        if (responseCode == 200) {
            val input = BufferedReader(InputStreamReader(urlConnection.inputStream))
            val response = StringBuffer()
            while (true) {
                val inputLine: String? = input.readLine() ?: break
                response.append(inputLine)
            }
            input.close()
            var refreshTokenResult: LoginResponse?
            try {
                refreshTokenResult =
                    Gson().fromJson(
                        response.toString(),
                        LoginResponse::class.java
                    )
            } catch (e: java.lang.Exception) {
                return false
            }
            if (refreshTokenResult != null) {
                rxPreferences.setUserToken(
                    refreshTokenResult.token,
                    refreshTokenResult.deviceToken
                )
            }
            return true
        } else
            return false
    }
}