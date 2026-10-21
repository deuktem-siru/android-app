package com.example.deuktemsiru_buyer.domain.model

import java.util.Calendar

val storeCategoryFilters = listOf("전체", "한식", "양식", "카페·디저트", "베이커리", "카페")

internal fun categoryEmoji(category: String?) = when (category) {
    "BAKERY" -> "🥐"
    "CAFE" -> "☕"
    "RESTAURANT" -> "🍱"
    "GROCERY" -> "🥦"
    else -> "🍽"
}

private val categoryLabels = mapOf(
    "BAKERY" to "베이커리",
    "RESTAURANT" to "음식점",
    "CAFE" to "카페",
    "GROCERY" to "식료품",
    "OTHER" to "기타",
)

private val categoryAliases = mapOf(
    "한식" to "RESTAURANT",
    "양식" to "RESTAURANT",
    "카페·디저트" to "CAFE",
)

fun categoryToDisplay(category: String) = categoryLabels[category] ?: category

fun categoryToApi(display: String) =
    categoryAliases[display] ?: categoryLabels.entries.firstOrNull { it.value == display }?.key

internal fun minutesUntilClose(closingTime: String): Int {
    val time = closingTime.substringAfter("T", closingTime).substringBefore(".")
    val parts = time.split(":")
    val closeHour = parts.getOrNull(0)?.toIntOrNull() ?: return 60
    val closeMin = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val now = Calendar.getInstance()
    val nowMins = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    val closeMins = closeHour * 60 + closeMin
    return if (closeMins >= nowMins) closeMins - nowMins
           else 24 * 60 - nowMins + closeMins
}

fun List<Store>.filterStores(category: String, query: String): List<Store> {
    val apiCategory = if (category == "전체") null else categoryToApi(category)
    return filter { store ->
        val matchesCategory = apiCategory == null || categoryToApi(store.category) == apiCategory
        val matchesQuery = query.isBlank() ||
            store.name.contains(query, ignoreCase = true) ||
            store.category.contains(query, ignoreCase = true) ||
            store.address.contains(query, ignoreCase = true) ||
            store.menus.any { it.name.contains(query, ignoreCase = true) }
        matchesCategory && matchesQuery
    }
}
