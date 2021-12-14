package com.vht_iot.vsmartsdk.network.data

data class VGroupResponse(
    val id: String = "",
    val level: String = "",
    val name: String = "",
    val org_name: String = "",
    val organization: String = "",
    val parents: String = "",
    val project_id: String = "",
    val subgroups: Any = Any()
)