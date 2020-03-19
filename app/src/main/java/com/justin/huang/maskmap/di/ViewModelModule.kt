package com.justin.huang.maskmap.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.justin.huang.maskmap.viewModel.DrugstoreViewModel
import com.justin.huang.maskmap.viewModel.MaskMapViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(DrugstoreViewModel::class)
    abstract fun bindDrugStoreViewModel(drugstoreViewModel: DrugstoreViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: MaskMapViewModelFactory): ViewModelProvider.Factory
}