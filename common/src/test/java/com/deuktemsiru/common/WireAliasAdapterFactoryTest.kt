package com.deuktemsiru.common

import com.google.gson.GsonBuilder
import org.junit.Assert.*
import org.junit.Test

class WireAliasAdapterFactoryTest {
    data class Item(val id: Long, val selected: Boolean)
    data class Envelope(val items: List<Item>)
    private val gson = GsonBuilder().registerTypeAdapterFactory(WireAliasAdapterFactory(mapOf(
        Item::class.java to mapOf("id" to listOf("productId"), "selected" to listOf("isSelected", "selected")),
    ))).create()

    @Test fun `nested legacy fields decode and current names encode`() {
        val value = gson.fromJson("""{"items":[{"productId":7,"selected":true}]}""", Envelope::class.java)
        assertEquals(Item(7, true), value.items.single())
        val json = gson.toJsonTree(value).asJsonObject["items"].asJsonArray[0].asJsonObject
        assertEquals(7L, json["productId"].asLong)
        assertTrue(json["isSelected"].asBoolean)
        assertFalse(json.has("id"))
        assertFalse(json.has("selected"))
    }

    @Test fun `canonical name takes precedence and null remains null`() {
        val item = gson.fromJson("""{"productId":3,"isSelected":false,"selected":true}""", Item::class.java)
        assertFalse(item.selected)
        assertNull(gson.fromJson("null", Item::class.java))
    }
}
