package com.justin.huang.maskmap.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.justin.huang.maskmap.api.MaskApiService
import com.justin.huang.maskmap.data.asDatabaseModel
import com.justin.huang.maskmap.db.DrugStoreDao
import timber.log.Timber
import javax.inject.Inject

class MaskMapWorker @Inject constructor(
    context: Context,
    workerParams: WorkerParameters,
    private val maskApiService: MaskApiService,
    private val drugStoreDao: DrugStoreDao
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        Timber.e("doWork")
        return try {
            val maskPoints = maskApiService.getMaskPoints()
            drugStoreDao.insertDrugStores(maskPoints.asDatabaseModel())
            Result.success()
        } catch (e: Exception) {
            Timber.e("get mask points error = $e")
            Result.failure()
        }
    }

    class Factory @Inject constructor(
        private val maskApiService: MaskApiService,
        private val drugStoreDao: DrugStoreDao
    ) : ChildWorkerFactory {
        override fun create(context: Context, params: WorkerParameters): ListenableWorker {
            return MaskMapWorker(context, params, maskApiService, drugStoreDao)
        }
    }
}