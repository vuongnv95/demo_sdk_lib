package com.vht_iot.vsmartsdk.network.connect

import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.gson.Gson
import com.vht_iot.vsmartsdk.utils.isNetworkAvailable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.Interceptor
import okhttp3.Response
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.nio.charset.Charset

@ExperimentalCoroutinesApi
class NetworkInterceptor constructor(
    private val networkEvent: NetworkEvent,
    private val context: Context,
    private val gson: Gson
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!context.isNetworkAvailable()) {
            networkEvent.publish(NetworkState.NO_INTERNET)
            throw NetworkException()
        } else {
            try {
                val response = chain.proceed(request)
                val responseBody = response.body
                val source = responseBody?.source()
                source?.request(Long.MAX_VALUE)
                val buffer = source?.buffer()
                val responseBodyString = buffer?.clone()?.readString(Charset.forName("UTF-8"))
                val errorResponse = gson.fromJson(responseBodyString, ApiException::class.java)
                when (response.code) {
                    in 200..299 -> {
                        networkEvent.publish(NetworkState.CONNECTED_INTERNET)
                    }
                    //TODO
                    in 400..409 -> {

                    }
                    HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    }
                    else -> {
                        networkEvent.publish(
                            NetworkState.GENERIC(errorResponse)
                        )
                    }
                }

                return response
            } catch (e: ConnectException) {
                networkEvent.publish(NetworkState.NO_CONNECT_INTERNET)
            } catch (e: SocketTimeoutException) {
                networkEvent.publish(NetworkState.CONNECTION_LOST)
            } catch (e: Exception) {
                if ("Canceled" != e.message) {
                    networkEvent.publish(NetworkState.ERROR)
                }
            }
            return chain.proceed(request)
        }
    }
}