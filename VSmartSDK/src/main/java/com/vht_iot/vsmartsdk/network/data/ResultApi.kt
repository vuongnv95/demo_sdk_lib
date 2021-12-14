package com.vht_iot.vsmartsdk.network.data

sealed class ResultApi<T>(
    val code: Int,
    val data: T?,
    val exception: String?,
    val status: Status
) {
    class Loading<T> : ResultApi<T>(0, null, null, Status.LOADING)
    class VSmartError<T>(code: Int, exception: String) :
        ResultApi<T>(code = code, null, exception, Status.ERROR)

    class VSmartSuccess<T>(data: T?) : ResultApi<T>(200, data, null, Status.SUCCESS)
}

enum class Status {
    LOADING,
    SUCCESS,
    ERROR
}