package com.justin.huang.maskmap.data

data class Feature(
    val geometry: Geometry,
    val properties: Properties,
    val type: String
)