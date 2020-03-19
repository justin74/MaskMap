package com.justin.huang.maskmap.di

import com.justin.huang.maskmap.fragment.DrugstoreDetailFragment
import com.justin.huang.maskmap.fragment.MapsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@SuppressWarnings("unused")
@Module
abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeMapsFragment(): MapsFragment

    @ContributesAndroidInjector
    abstract fun contributeDrugstoreDetailFragment(): DrugstoreDetailFragment
}