package com.justin.huang.maskmap.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justin.huang.maskmap.db.DrugStore
import com.justin.huang.maskmap.repository.MaskPointsRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class DrugStoreViewModel @Inject constructor(private val repository: MaskPointsRepository) :
    ViewModel() {
    val drugStoreList: LiveData<List<DrugStore>> = repository.getDrugStoreList()

    init {
        viewModelScope.launch {
            Timber.d("viewModel init")
            repository.fetchMaskPoints()
        }
    }
}