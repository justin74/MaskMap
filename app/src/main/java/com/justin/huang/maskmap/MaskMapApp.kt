package com.justin.huang.maskmap

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.justin.huang.maskmap.di.AppInjector
import com.justin.huang.maskmap.worker.MaskMapWorkerFactory
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import timber.log.Timber
import javax.inject.Inject

class MaskMapApp : Application(), HasAndroidInjector {
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var maskWorkerFactory: MaskMapWorkerFactory

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        AppInjector.init(this)
        WorkManager.initialize(this, Configuration.Builder().setWorkerFactory(maskWorkerFactory).build())
    }

    override fun androidInjector() = dispatchingAndroidInjector
}