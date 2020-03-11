package com.justin.huang.maskmap.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MaskPoints(
    val type: String,
    val features: List<Feature>
)