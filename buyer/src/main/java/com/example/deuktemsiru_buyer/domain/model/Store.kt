package com.example.deuktemsiru_buyer.domain.model

data class Store(
    val id: Long,
    val name: String,
    val category: String,
    val emoji: String,
    val rating: Float,
    val walkingMinutes: Int,
    val discountRate: Int,
    val originalPrice: Int,
    val discountedPrice: Int,
    val remainingItems: Int,
    val minutesUntilClose: Int,
    val address: String,
    val phone: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    var isWishlisted: Boolean = false,
    val menus: List<MenuItem> = emptyList()
)

data class MenuItem(
    val id: Long,
    val name: String,
    val emoji: String,
    val originalPrice: Int,
    val discountedPrice: Int,
    val discountRate: Int,
    val remainingItems: Int,
    val pickupStart: String = "",
    val pickupEnd: String = "",
    val isSoldOut: Boolean = false
)

