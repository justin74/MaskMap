package com.justin.huang.maskmap.viewModel

import androidx.lifecycle.*
import com.justin.huang.maskmap.db.DrugStore
import com.justin.huang.maskmap.repository.MaskPointsRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class DrugStoreViewModel @Inject constructor(private val repository: MaskPointsRepository) :
    ViewModel() {
    val drugStores: LiveData<List<DrugStore>> = repository.getDrugStoreList()

    init {
        fetchMaskPoints()
    }

    fun fetchMaskPoints() {
        viewModelScope.launch {
            Timber.e("fetch mask points")
            repository.fetchMaskPoints()
        }
    }
}