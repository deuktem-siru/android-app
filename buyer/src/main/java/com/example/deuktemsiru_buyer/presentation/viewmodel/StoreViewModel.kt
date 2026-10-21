package com.example.deuktemsiru_buyer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.deuktemsiru_buyer.domain.model.Result
import com.example.deuktemsiru_buyer.domain.model.Store
import com.example.deuktemsiru_buyer.domain.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class StoreViewModel(private val repository: StoreRepository) : ViewModel(), StoreRepository {
    private val _stores = MutableStateFlow<List<Store>>(emptyList())
    val stores = _stores.asStateFlow()
    private val _selected = MutableStateFlow<Store?>(null)
    val selected = _selected.asStateFlow()

    override suspend fun getStores(category: String?): Result<List<Store>> = repository.getStores(category).also {
        if (it is Result.Success) _stores.value = it.data
    }
    override suspend fun getStore(storeId: Long): Result<Store> = repository.getStore(storeId).also {
        if (it is Result.Success) _selected.value = it.data
    }
    override suspend fun getWishlist(): Result<List<Store>> = repository.getWishlist().also {
        if (it is Result.Success) _stores.value = it.data
    }
    override suspend fun toggleWishlist(storeId: Long): Result<Boolean> = repository.toggleWishlist(storeId).also {
        if (it is Result.Success) {
            val value = it.data
            _stores.value = _stores.value.map { store -> if (store.id == storeId) store.copy(isWishlisted = value) else store }
            _selected.value = _selected.value?.let { store -> if (store.id == storeId) store.copy(isWishlisted = value) else store }
        }
    }
}
