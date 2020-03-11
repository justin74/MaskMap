package com.justin.huang.maskmap.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Feature(
    val geometry: Geometry,
    val properties: Properties,
    val type: String
)