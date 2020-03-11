package com.justin.huang.maskmap.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Geometry(
    val coordinates: List<Double>,
    val type: String
)