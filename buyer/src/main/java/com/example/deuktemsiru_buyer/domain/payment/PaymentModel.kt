package com.example.deuktemsiru_buyer.domain.payment

import com.example.deuktemsiru_buyer.domain.model.CartManager
import com.example.deuktemsiru_buyer.domain.repository.CartStore
import com.example.deuktemsiru_buyer.domain.repository.PaymentSession
import com.example.deuktemsiru_buyer.domain.model.Store
import com.example.deuktemsiru_buyer.domain.repository.AppRepository
import com.example.deuktemsiru_buyer.domain.model.CreateOrderRequest
import com.example.deuktemsiru_buyer.domain.model.OrderItemRequest

class PaymentModel(
    private val api: AppRepository,
    private val session: PaymentSession,
    private val cartRepository: CartStore,
) {
    suspend fun submitOrder(draft: PaymentDraft, pickupTime: String?) {
        val order = api.createOrder(CreateOrderRequest(items = draft.orderItems, pickupTime = pickupTime)).data
            ?: throw IllegalStateException("Empty order response")
        session.lastOrderId = order.orderId
        if (draft.fromCart) {
            cartRepository.clearServerCart()
            CartManager.clear()
        }
        session.syncMe(api)
    }
}

fun cartPaymentDraft(storeId: Long): PaymentDraft {
    val items = CartManager.items
    return PaymentDraft(
        storeId = storeId,
        menuId = 0L,
        storeName = CartManager.storeName,
        menuSummary = "장바구니 메뉴 ${CartManager.totalCount}개",
        pickupStart = items.mapNotNull { it.pickupStart.takeIf(String::isNotBlank) }.maxOrNull(),
        pickupEnd = items.mapNotNull { it.pickupEnd.takeIf(String::isNotBlank) }.minOrNull(),
        originalTotal = items.sumOf { it.originalPrice * it.quantity },
        discountedTotal = CartManager.totalPrice,
        itemCount = CartManager.totalCount,
        orderItems = items.map { OrderItemRequest(productId = it.menuId, quantity = it.quantity) },
        fromCart = true,
    )
}

fun storePaymentDraft(store: Store, menuId: Long, fallbackTotal: Int): PaymentDraft {
    val menu = store.menus.firstOrNull { it.id == menuId && !it.isSoldOut }
        ?: store.menus.firstOrNull { !it.isSoldOut }
    return PaymentDraft(
        storeId = store.id,
        menuId = menuId,
        storeName = store.name,
        menuSummary = menu?.name ?: "주문 가능한 메뉴 없음",
        pickupStart = menu?.pickupStart,
        pickupEnd = menu?.pickupEnd,
        originalTotal = menu?.originalPrice?.takeIf { it > 0 } ?: menu?.discountedPrice ?: fallbackTotal,
        discountedTotal = menu?.discountedPrice ?: fallbackTotal,
        itemCount = 1,
        orderItems = menu?.let { listOf(OrderItemRequest(productId = it.id, quantity = 1)) }.orEmpty(),
        fromCart = false,
    )
}

data class PaymentDraft(
    val storeId: Long,
    val menuId: Long,
    val storeName: String,
    val menuSummary: String,
    val pickupStart: String?,
    val pickupEnd: String?,
    val originalTotal: Int,
    val discountedTotal: Int,
    val itemCount: Int,
    val orderItems: List<OrderItemRequest>,
    val fromCart: Boolean,
)
