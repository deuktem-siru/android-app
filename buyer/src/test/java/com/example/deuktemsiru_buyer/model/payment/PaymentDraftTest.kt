package com.example.deuktemsiru_buyer.model.payment

import com.example.deuktemsiru_buyer.domain.payment.*

import com.example.deuktemsiru_buyer.domain.model.CartItem
import com.example.deuktemsiru_buyer.domain.model.CartManager
import com.example.deuktemsiru_buyer.domain.model.MenuItem
import com.example.deuktemsiru_buyer.domain.model.StoreListItemResponse
import com.example.deuktemsiru_buyer.data.repository.toStore
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PaymentDraftTest {
    @Before fun setUp() = CartManager.clear()
    @After fun tearDown() = CartManager.clear()

    @Test fun `cart draft preserves quantities prices and the shared pickup window`() {
        CartManager.add(1, "가게", "", 37.0, 127.0,
            CartItem(10, "빵", "", 2000, 1000, "16:00", "18:00", 2))
        CartManager.add(1, "가게", "", 37.0, 127.0,
            CartItem(20, "커피", "", 3000, 2000, "17:00", "19:00"))
        val draft = cartPaymentDraft(1)
        assertEquals(7000, draft.originalTotal)
        assertEquals(4000, draft.discountedTotal)
        assertEquals(3, draft.itemCount)
        assertEquals("17:00", draft.pickupStart)
        assertEquals("18:00", draft.pickupEnd)
        assertEquals(listOf(2, 1), draft.orderItems.map { it.quantity })
        assertTrue(draft.fromCart)
    }

    @Test fun `sold out selection falls back to an available menu and empty store creates no order items`() {
        val store = StoreListItemResponse(1, "가게", null, 100, "BAKERY", 0.0, 0, 1).toStore()
        val soldOut = MenuItem(10, "품절", "", 2000, 1000, 50, 0, isSoldOut = true)
        val available = soldOut.copy(id = 20, name = "판매중", remainingItems = 1, isSoldOut = false)
        val draft = storePaymentDraft(store.copy(menus = listOf(soldOut, available)), 10, 0)
        assertEquals(20L, draft.orderItems.single().productId)
        assertEquals(1000, draft.discountedTotal)
        assertFalse(draft.fromCart)
        assertTrue(storePaymentDraft(store, 10, 3000).orderItems.isEmpty())
    }
}
