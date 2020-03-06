package com.justin.huang.maskmap.di

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.justin.huang.maskmap.MaskMapApp
import dagger.android.AndroidInjection
import dagger.android.HasAndroidInjector

object AppInjector {
    fun init(maskMapApp: MaskMapApp) {
        DaggerAppComponent.builder().application(maskMapApp)
            .build().inject(maskMapApp)
       maskMapApp.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks{
           override fun onActivityPaused(activity: Activity) {

           }

           override fun onActivityStarted(activity: Activity) {

           }

           override fun onActivityDestroyed(activity: Activity) {

           }

           override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {

           }

           override fun onActivityStopped(activity: Activity) {

           }

           override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
               handleActivity(activity)
           }

           override fun onActivityResumed(activity: Activity) {

           }
       })
    }

    private fun handleActivity(activity: Activity) {
        if (activity is HasAndroidInjector) {
            AndroidInjection.inject(activity)
        }
    }
}