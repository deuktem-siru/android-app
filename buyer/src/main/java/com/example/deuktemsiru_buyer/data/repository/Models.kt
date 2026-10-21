package com.example.deuktemsiru_buyer.data.repository

import com.example.deuktemsiru_buyer.domain.model.*
import com.example.deuktemsiru_buyer.domain.model.*

import com.example.deuktemsiru_buyer.domain.model.StoreDetailApiResponse
import com.example.deuktemsiru_buyer.domain.model.StoreListItemResponse
import com.example.deuktemsiru_buyer.domain.model.StoreProductItem
import com.example.deuktemsiru_buyer.domain.model.WishlistItemResponse
import com.example.deuktemsiru_buyer.domain.model.CartApiItem
import com.example.deuktemsiru_buyer.domain.model.*

private object StoreDefaults {
    const val WALKING_MINUTES = 5
    const val CLOSE_MINUTES = 60
}



fun StoreDetailApiResponse.toStore(isWishlisted: Boolean = this.isWishlisted): Store {
    val menus = products.map { it.toMenuItem() }
    val rep = menus.firstOrNull { !it.isSoldOut } ?: menus.firstOrNull()
    return Store(
        id = storeId,
        name = name,
        category = categoryToDisplay(categories.firstOrNull() ?: "OTHER"),
        emoji = categoryEmoji(categories.firstOrNull()),
        rating = ratingAvg.toFloat(),
        walkingMinutes = StoreDefaults.WALKING_MINUTES,
        discountRate = rep?.discountRate ?: 0,
        originalPrice = rep?.originalPrice ?: 0,
        discountedPrice = rep?.discountedPrice ?: 0,
        remainingItems = products.sumOf { it.quantityRemaining },
        minutesUntilClose = products.map { minutesUntilClose(it.pickupEnd) }.filter { it > 0 }.minOrNull() ?: 0,
        address = address,
        phone = phone.orEmpty(),
        latitude = latitude,
        longitude = longitude,
        isWishlisted = isWishlisted,
        menus = menus,
    )
}

fun StoreListItemResponse.toStore() = toStoreSummary(
    storeId = storeId,
    name = name,
    category = category,
    ratingAvg = ratingAvg,
    walkingMinutes = (distanceM / 80).coerceAtLeast(1),
    representativeDiscountRate = representativeDiscountRate,
    representativeOriginalPrice = representativeOriginalPrice,
    representativeDiscountPrice = representativeDiscountPrice,
    availableProductCount = availableProductCount,
    representativePickupEnd = representativePickupEnd,
    isWishlisted = isWishlisted,
)

fun WishlistItemResponse.toStore() = toStoreSummary(
    storeId = storeId,
    name = name,
    category = category,
    ratingAvg = ratingAvg,
    walkingMinutes = StoreDefaults.WALKING_MINUTES,
    representativeDiscountRate = representativeDiscountRate,
    representativeOriginalPrice = representativeOriginalPrice,
    representativeDiscountPrice = representativeDiscountPrice,
    availableProductCount = availableProductCount,
    representativePickupEnd = representativePickupEnd,
    isWishlisted = true,
)

private fun toStoreSummary(
    storeId: Long,
    name: String,
    category: String,
    ratingAvg: Double,
    walkingMinutes: Int,
    representativeDiscountRate: Int,
    representativeOriginalPrice: Int,
    representativeDiscountPrice: Int,
    availableProductCount: Int,
    representativePickupEnd: String?,
    isWishlisted: Boolean,
) = Store(
    id = storeId,
    name = name,
    category = categoryToDisplay(category),
    emoji = categoryEmoji(category),
    rating = ratingAvg.toFloat(),
    walkingMinutes = walkingMinutes,
    discountRate = representativeDiscountRate,
    originalPrice = representativeOriginalPrice,
    discountedPrice = representativeDiscountPrice,
    remainingItems = availableProductCount,
    minutesUntilClose = representativePickupEnd?.let { minutesUntilClose(it) } ?: StoreDefaults.CLOSE_MINUTES,
    address = "",
    phone = "",
    isWishlisted = isWishlisted,
)

fun StoreProductItem.toMenuItem(): MenuItem {
    val original = when {
        originalPrice > 0 -> originalPrice
        discountPrice > 0 -> (discountPrice / 0.7).toInt()
        else -> 0
    }
    return MenuItem(
        id = productId,
        name = name,
        emoji = "🍽",
        originalPrice = original,
        discountedPrice = discountPrice,
        discountRate = if (original > 0) ((original - discountPrice) * 100 / original) else 0,
        remainingItems = quantityRemaining,
        pickupStart = pickupStart.orEmpty(),
        pickupEnd = pickupEnd,
        isSoldOut = status != "AVAILABLE" || quantityRemaining <= 0,
    )
}

fun CartApiItem.toCartItem() = CartItem(
    menuId = productId,
    menuName = productName,
    emoji = "🛍️",
    originalPrice = originalPrice,
    discountedPrice = discountPrice,
    pickupStart = pickupStart,
    pickupEnd = pickupEnd,
    quantity = quantity,
)

