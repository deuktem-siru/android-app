package com.example.deuktemsiru_buyer.data.repository

import com.example.deuktemsiru_buyer.domain.model.*

import com.example.deuktemsiru_buyer.domain.repository.AppRepository
import com.example.deuktemsiru_buyer.domain.model.CartAddRequest
import com.example.deuktemsiru_buyer.domain.model.CartUpdateRequest

class CartRepository(
    private val api: AppRepository,
    private val session: SessionManager,
) : com.example.deuktemsiru_buyer.domain.repository.CartStore {
    override suspend fun loadServerCart(): Boolean {
        if (!session.isLoggedIn()) return false
        return runCatching {
            val items = api.getCart().data?.items.orEmpty()
            if (items.isEmpty()) {
                CartManager.clear()
                return@runCatching true
            }

            val first = items.first()
            CartManager.replaceFromServer(
                storeId = first.storeId,
                storeName = first.storeName,
                storeLat = first.storeLatitude,
                storeLng = first.storeLongitude,
                items = items.map { it.toCartItem() },
                serverIds = items.associate { it.productId to it.cartItemId },
            )
            true
        }.getOrDefault(false)
    }

    override suspend fun addProduct(productId: Long): Boolean {
        if (!session.isLoggedIn()) return true
        return runCatching {
            val item = api.addToCart(CartAddRequest(productId = productId, quantity = 1)).data
            if (item != null) CartManager.addServerCartItemId(item.productId, item.cartItemId)
            item != null
        }.getOrDefault(false)
    }

    override suspend fun removeProduct(productId: Long): Boolean =
        withServerCartItem(productId) { api.removeCartItem(it) }

    override suspend fun setQuantity(productId: Long, quantity: Int): Boolean =
        withServerCartItem(productId) { api.updateCartItem(it, CartUpdateRequest(quantity)) }

    override suspend fun clearServerCart() {
        if (session.isLoggedIn()) runCatching { api.clearCart() }
    }

    private suspend fun withServerCartItem(productId: Long, action: suspend (Long) -> Unit): Boolean {
        val cartItemId = CartManager.serverCartItemIds[productId] ?: return true
        if (!session.isLoggedIn()) return true
        return runCatching { action(cartItemId) }
            .onFailure { loadServerCart() }
            .isSuccess
    }
}
