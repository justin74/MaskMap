package com.justin.huang.maskmap.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.justin.huang.maskmap.viewModel.DrugStoreViewModel
import com.justin.huang.maskmap.viewModel.MaskMapViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(DrugStoreViewModel::class)
    abstract fun bindDrugStoreViewModel(drugStoreViewModel: DrugStoreViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: MaskMapViewModelFactory): ViewModelProvider.Factory
}