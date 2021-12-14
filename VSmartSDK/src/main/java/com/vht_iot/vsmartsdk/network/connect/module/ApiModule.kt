package com.vht_iot.vsmartsdk.network.connect.module

import android.content.Context
import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.vht_iot.vsmartsdk.network.connect.ApiInterface
import com.vht_iot.vsmartsdk.network.connect.NetworkEvent
import com.vht_iot.vsmartsdk.network.connect.NetworkInterceptor
import com.vht_iot.vsmartsdk.network.connect.TokenAuthenticator
import com.vht_iot.vsmartsdk.utils.VDefine
import com.viettel.vht.core.pref.AppPreferences
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

class ApiModule(private val context: Context, private val url: String) {


    companion object {
        @Volatile
        private var instance: ApiModule? = null

        fun getInstance(context: Context, url: String): ApiModule =
            instance ?: synchronized(this) {
                instance ?: ApiModule(context = context, url)
            }
    }


    fun provideGson(): Gson {
        return GsonBuilder().setLenient().create()
    }

    fun provideApiInterface(
    )
            : ApiInterface {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(provideDispatchServerHttpClient())
            .addConverterFactory(GsonConverterFactory.create(provideGson()))
            .build()
        return retrofit.create(ApiInterface::class.java)
    }

    fun provideDispatchServerHttpClient(
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
            .cache(provideCache())
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val request =
                    if (!TextUtils.isEmpty(
                            AppPreferences.getInstance(context).getAddminToken()
                        ) && VDefine.useAddminToken
                    ) {
                        VDefine.useAddminToken = false
                        chain.request()
                            .newBuilder()
                            .header("Content-Type", "application/json")
                            .header("Content-Type", "text/plain; charset=utf-8")
                            .addHeader(
                                "Authorization",
                                AppPreferences.getInstance(context).getAddminToken()!!
                            )
                            .build()
                    } else if (!TextUtils.isEmpty(AppPreferences.getInstance(context).getToken())) {
                        chain.request()
                            .newBuilder()
                            .header("Content-Type", "application/json")
                            .header("Content-Type", "text/plain; charset=utf-8")
                            .addHeader(
                                "Authorization", AppPreferences.getInstance(context).getToken()!!
                            )
                            .build()
                    } else {
                        chain.request()
                            .newBuilder()
                            .header("Content-Type", "application/json")
                            .header("Content-Type", "text/plain; charset=utf-8")
                            .build()
                    }

                chain.proceed(request)
            })
            .addInterceptor(loggingInterceptor)
            .addInterceptor(providerNetworkInterceptor())
            .authenticator(providerAuthenticator())
            .connectTimeout(VDefine.ConfigNetwork.DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(VDefine.ConfigNetwork.DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(VDefine.ConfigNetwork.DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .build()
    }


    fun provideCache(): Cache {
        val cacheSize = 10 * 1024 * 1024.toLong() // 10 MB
        val httpCacheDirectory = File(context.cacheDir, "http-cache")
        return Cache(httpCacheDirectory, cacheSize)
    }

    fun providerNetworkEvent() =
        NetworkEvent()

    fun providerNetworkInterceptor(
    ) = NetworkInterceptor(providerNetworkEvent(), context, provideGson())

    fun providerAuthenticator(
    ) = TokenAuthenticator(
        AppPreferences.getInstance(context),
        provideGson(),
        providerNetworkEvent()
    )
}

class NullOnEmptyConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        val delegate: Converter<ResponseBody, Any> =
            retrofit.nextResponseBodyConverter(this, type, annotations)
        return Converter { body -> if (body.contentLength() == 0L) null else delegate.convert(body) }
    }
}