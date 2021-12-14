package com.vht_iot.vsmartsdk.network.data

import java.io.Serializable

data class VOrganizationResponse(
    val description: String = "",
    val group_id: String = "",
    val id: String = "",
    val name: String = "",
    val org_id: String = "",
    val project_id: String = ""
):Serializable