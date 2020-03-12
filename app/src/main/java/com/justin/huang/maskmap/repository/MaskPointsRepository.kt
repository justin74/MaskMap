package com.justin.huang.maskmap.repository

import com.justin.huang.maskmap.api.MaskApiService
import com.justin.huang.maskmap.data.asDatabaseModel
import com.justin.huang.maskmap.db.DrugStoreDao
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaskPointsRepository @Inject constructor(
    private val maskApiService: MaskApiService,
    private val drugStoreDao: DrugStoreDao
) {
    suspend fun fetchMaskPoints() {
        Timber.e("fetchMaskPoints")
        val maskPoints = maskApiService.getMaskPoints()
        drugStoreDao.insertDrugStores(maskPoints.asDatabaseModel())
    }
}