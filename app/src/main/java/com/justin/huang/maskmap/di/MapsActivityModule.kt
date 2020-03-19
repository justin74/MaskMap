package com.justin.huang.maskmap.di

import com.justin.huang.maskmap.MapsActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MapsActivityModule {
    @ContributesAndroidInjector(modules = [FragmentBuildersModule::class])
    abstract fun contributeMapsActivity(): MapsActivity
}