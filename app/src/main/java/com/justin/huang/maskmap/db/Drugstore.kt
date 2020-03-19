package com.justin.huang.maskmap.db

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["id"])
data class Drugstore (
    val id: String,
    val address: String,
    val available: String,
    val county: String,
    val cunli: String,
    @ColumnInfo(name = "custom_note")
    val customNote: String,
    @ColumnInfo(name = "mask_adult")
    val maskAdult: Int,
    @ColumnInfo(name = "mask_child")
    val maskChild: Int,
    val name: String,
    val note: String,
    val phone: String,
    @ColumnInfo(name = "service_periods")
    val servicePeriods: String,
    val town: String,
    val updated: String,
    val website: String,
    // 經度
    val longitude: Double,
    // 緯度
    val latitude: Double
)

//fun List<DrugStoreEntity>.asDomainModel(): List<DrugStore> {
//    return map {
//        DrugStore(
//
//        )
//    }
//}