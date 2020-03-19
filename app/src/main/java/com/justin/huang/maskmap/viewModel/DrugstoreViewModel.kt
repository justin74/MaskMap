package com.justin.huang.maskmap.viewModel

import androidx.lifecycle.*
import com.justin.huang.maskmap.db.Drugstore
import com.justin.huang.maskmap.repository.MaskPointsRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class DrugstoreViewModel @Inject constructor(private val repository: MaskPointsRepository) :
    ViewModel() {
    val drugStores: LiveData<List<Drugstore>> = repository.getDrugstoreList()

    init {
        viewModelScope.launch {
            Timber.d("viewModel init")
            repository.fetchMaskPoints()
        }
    }
}