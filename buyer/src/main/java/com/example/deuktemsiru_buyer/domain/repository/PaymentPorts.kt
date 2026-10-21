package com.example.deuktemsiru_buyer.domain.repository

interface PaymentSession {
    var lastOrderId: Long
    suspend fun syncMe(api: AppRepository): Boolean
}
interface CartStore {
    suspend fun loadServerCart(): Boolean
    suspend fun addProduct(productId: Long): Boolean
    suspend fun removeProduct(productId: Long): Boolean
    suspend fun setQuantity(productId: Long, quantity: Int): Boolean
    suspend fun clearServerCart()
}
