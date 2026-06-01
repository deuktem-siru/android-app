package com.example.deuktemsiru_seller.model.order

import com.example.deuktemsiru_seller.domain.order.*

import com.example.deuktemsiru_seller.domain.model.OrderApiResponse
import org.junit.Assert.*
import org.junit.Test

class OrderModelTest {
    private fun order(id: Long, status: String) = OrderApiResponse(id = id, status = status, totalAmount = 1000, items = emptyList())

    @Test fun `order item names omit promotional prefixes`() {
        fun item(name: String) = com.example.deuktemsiru_seller.domain.model.OrderItemApiResponse(
            name = name, quantity = 1, price = 1000,
        )
        assertEquals("반찬 3종", item("오늘 마감할인 오늘의 반찬 3종").displayName)
        assertEquals("소금빵", item("오늘 마감할인 소금빵").displayName)
        assertEquals("소금빵", item("소금빵").displayName)
    }

    @Test fun `tabs and new order detection follow status transitions`() {
        val model = OrderModel()
        val pending = order(1, "PENDING")
        model.orders = listOf(pending, order(2, "CANCELLED"), order(3, "PICKED_UP"))
        assertEquals(listOf(1L), model.ordersForTab(0).map { it.id })
        assertEquals(listOf(2L, 3L), model.ordersForTab(3).map { it.id })
        assertFalse(model.hasNewPendingOrders(model.orders))
        assertTrue(model.hasNewPendingOrders(model.orders + order(4, "PENDING")))
        model.replace(pending.copy(status = "CONFIRMED"))
        assertTrue(model.ordersForTab(0).isEmpty())
        assertEquals(listOf(1L), model.ordersForTab(1).map { it.id })
        assertTrue(model.visibleOrderIdsChanged(1, emptyList()))
    }
}
