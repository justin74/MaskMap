package com.justin.huang.maskmap.di

import com.justin.huang.maskmap.worker.ChildWorkerFactory
import com.justin.huang.maskmap.worker.MaskMapWorker
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@SuppressWarnings("unused")
@Module
abstract class WorkerModule {
    @Binds
    @IntoMap
    @WorkerKey(MaskMapWorker::class)
    abstract fun bindMaskMapWorker(factory: MaskMapWorker.Factory): ChildWorkerFactory
}