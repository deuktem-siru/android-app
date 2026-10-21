package com.example.deuktemsiru_buyer.data

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CartManagerTest {
    @Before fun setUp() = CartManager.clear()
    @After fun tearDown() = CartManager.clear()

    private fun add(storeId: Long = 10L) = CartManager.add(
        storeId, "가게", "🍽", 37.0, 126.0,
        CartItem(1L, "메뉴", "🍽", 1000, 700),
    )

    @Test fun `adding the same menu increases its quantity and rejects another store`() {
        assertTrue(add())
        assertTrue(add())
        assertFalse(add(storeId = 20L))
        assertEquals(10L, CartManager.storeId)
        assertEquals(2, CartManager.totalCount)
        assertEquals(1400, CartManager.totalPrice)
    }

    @Test fun `decreasing the last item resets all cart metadata`() {
        add()
        CartManager.addServerCartItemId(1L, 99L)
        CartManager.decreaseQuantity(1L)
        assertTrue(CartManager.isEmpty)
        assertEquals(0L, CartManager.storeId)
        assertEquals("", CartManager.storeName)
        assertEquals("", CartManager.storeEmoji)
        assertEquals(0.0, CartManager.storeLat, 0.0)
        assertEquals(0.0, CartManager.storeLng, 0.0)
        assertTrue(CartManager.serverCartItemIds.isEmpty())
    }

    @Test fun `server replacement replaces both the items and their server identifiers`() {
        add()
        CartManager.replaceFromServer(20L, "다른 가게", 38.0, 127.0,
            listOf(CartItem(2L, "새 메뉴", "🍽", 2000, 1000, quantity = 3)), mapOf(2L to 100L))
        assertEquals(20L, CartManager.storeId)
        assertEquals(3, CartManager.totalCount)
        assertEquals(3000, CartManager.totalPrice)
        assertEquals(mapOf(2L to 100L), CartManager.serverCartItemIds)
    }
}
