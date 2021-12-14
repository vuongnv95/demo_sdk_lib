package com.vht_iot.vsmartsdk.network.connect

import com.vht_iot.vsmartsdk.network.data.*
import com.vht_iot.vsmartsdk.network.data.response.LoginResponse
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiInterface {
    @POST("/api/login")
    suspend fun login(@Body requestBody: RequestBody): LoginResponse

    @DELETE("/v2/user/logout")
    suspend fun logout(): ApiObjectResponse<Any>

    @GET("/api/devices")
    suspend fun getListDevice(): DeviceResponse

    @POST("/api/users/otp/register")
    suspend fun sendVerificationCodeRegister(@Body requestBody: RequestBody): VOTPPhoneResponse

    @POST("/api/users/otp/register")
    suspend fun sendVerificationCodeForgetPassword(@Body requestBody: RequestBody): VOTPPhoneResponse

    @PUT("/api/users/otp/newpassword")
    suspend fun registerUserWithPhone(@Body requestBody: RequestBody): Any

    @POST("/api/users")
    suspend fun registerUserWithEmail(@Body requestBody: RequestBody): RegisterWithEmailResponse


    // group manager

    @GET("/api/groups/{groupName}/{entityType}")
    suspend fun getGroupByName(
        @Path("groupName") groupName: String,
        @Path("entityType") entityType: String
    ): VGroupResponse

    @POST("/api/groups")
    suspend fun createGroup(@Body requestBody: RequestBody): VGroupResponse

    @POST("/api/roles/customer")
    suspend fun createRole(@Body requestBody: RequestBody): Any

    // organizations manager

    @POST("/api/organizations")
    suspend fun createOrganizations(@Body requestBody: RequestBody): VOrganizationResponse

    @GET("/api/organizations")
    suspend fun getOrganizations(
        @QueryMap data: Map<String, String>
    ): ApiArrayOrgResponse<VOrganizationResponse>

}