package com.justin.huang.maskmap.data

import androidx.room.Entity

@Entity(primaryKeys = ["id"])
data class DrugStore(
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
    val website: String,
    val latitude: String,
    val longitude: String
)