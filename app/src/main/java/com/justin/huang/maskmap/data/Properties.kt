package com.justin.huang.maskmap.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Properties(
    val id: String,
    val address: String,
    val available: String,
    val county: String,
    val cunli: String,
    val custom_note: String,
    val mask_adult: Int,
    val mask_child: Int,
    val name: String,
    val note: String,
    val phone: String,
    val service_periods: String,
    val town: String,
    val updated: String,
    val website: String
)