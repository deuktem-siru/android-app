package com.example.deuktemsiru_buyer.domain.repository

import com.example.deuktemsiru_buyer.domain.model.Store
import com.example.deuktemsiru_buyer.domain.model.Result

interface StoreRepository {
    suspend fun getStores(category: String? = null): Result<List<Store>>
    suspend fun getStore(storeId: Long): Result<Store>
    suspend fun toggleWishlist(storeId: Long): Result<Boolean>
    suspend fun getWishlist(): Result<List<Store>>
}
