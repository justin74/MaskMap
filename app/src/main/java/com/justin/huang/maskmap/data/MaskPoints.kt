package com.justin.huang.maskmap.data

import com.justin.huang.maskmap.db.DrugStore
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MaskPoints(
    val type: String,
    val features: List<Feature>
)

fun MaskPoints.asDatabaseModel(): List<DrugStore> {
    return features.map {
        DrugStore(
            id = it.properties.id,
            address = it.properties.address,
            available = it.properties.available,
            county = it.properties.county,
            cunli = it.properties.cunli,
            customNote = it.properties.custom_note,
            maskAdult = it.properties.mask_adult,
            maskChild = it.properties.mask_child,
            name = it.properties.name,
            note = it.properties.note,
            phone = it.properties.phone,
            servicePeriods = it.properties.service_periods,
            town = it.properties.town,
            updated = it.properties.updated,
            website = it.properties.website,
            longitude = it.geometry.coordinates[0],
            latitude = it.geometry.coordinates[1]
        )
    }
}