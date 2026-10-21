package com.example.deuktemsiru_seller.data.remote

import com.example.deuktemsiru_seller.domain.model.*
import com.google.gson.GsonBuilder
import com.deuktemsiru.common.WireAliasAdapterFactory

fun wireGson() = GsonBuilder().registerTypeAdapterFactory(WireAliasAdapterFactory(mapOf(
    SaleItemApiResponse::class.java to mapOf(
        "id" to listOf("productId"),
        "discountedPrice" to listOf("discountPrice"),
        "remainingItems" to listOf("quantityRemaining"),
        "totalItems" to listOf("quantityTotal"),
    ),
    MenuItemApiResponse::class.java to mapOf(
        "id" to listOf("menuItemId"),
    ),
    OrderItemApiResponse::class.java to mapOf(
        "name" to listOf("productName"),
        "price" to listOf("unitPrice"),
    ),
    OrderApiResponse::class.java to mapOf(
        "id" to listOf("orderId"),
        "totalAmount" to listOf("totalPrice"),
    ),
    TopMenu::class.java to mapOf(
        "name" to listOf("productName", "name"),
        "count" to listOf("soldCount", "count"),
    ),
    SalesApiResponse::class.java to mapOf(
        "todaySales" to listOf("totalAmount"),
        "todayOrderCount" to listOf("totalOrders"),
        "salesData" to listOf("chartData"),
        "topMenus" to listOf("topProducts", "topMenus"),
    ),
))).create()
