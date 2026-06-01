package com.example.deuktemsiru_buyer.data.repository

import com.example.deuktemsiru_buyer.domain.model.*

import com.example.deuktemsiru_buyer.domain.repository.AppRepository
import com.example.deuktemsiru_buyer.domain.model.AppError
import com.example.deuktemsiru_buyer.domain.model.Result
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

class RemoteStoreRepository(private val api: AppRepository) : com.example.deuktemsiru_buyer.domain.repository.StoreRepository {

    private val storeCache = mutableMapOf<Long, Store>()

    override suspend fun getStores(category: String?): Result<List<Store>> = safeCall {
        val apiCategory = if (category != null) categoryToApi(category) else null
        api.getStores(category = apiCategory).data?.stores
            ?.map { item ->
                item.toStore().also { storeCache[item.storeId] = it }
            }
            ?: error("Failed to load stores: API returned null data")
    }

    override suspend fun getStore(storeId: Long): Result<Store> = safeCall {
        storeCache[storeId]?.takeIf { it.menus.isNotEmpty() && it.address.isNotBlank() } ?: run {
            val response = api.getStore(storeId).data
                ?: error("Store $storeId not found")
            response.toStore().also { storeCache[storeId] = it }
        }
    }

    override suspend fun toggleWishlist(storeId: Long): Result<Boolean> = safeCall {
        api.toggleWishlist(storeId).data?.isWishlisted ?: false
    }

    override suspend fun getWishlist(): Result<List<Store>> = safeCall {
        api.getWishlist().data?.wishlists?.map { it.toStore() } ?: emptyList()
    }
}

private inline fun <T> safeCall(block: () -> T): Result<T> = try {
    Result.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: HttpException) {
    val appError = when (e.code()) {
        401, 403 -> AppError.AUTH_ERROR
        404 -> AppError.NOT_FOUND
        in 500..599 -> AppError.SERVER_ERROR
        else -> AppError.UNKNOWN
    }
    Result.Error(appError, httpCode = e.code())
} catch (e: Exception) {
    Result.Error(AppError.NETWORK_ERROR)
}
