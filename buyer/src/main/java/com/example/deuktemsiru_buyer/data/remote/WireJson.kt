package com.example.deuktemsiru_buyer.data.remote

import com.example.deuktemsiru_buyer.domain.model.*
import com.google.gson.GsonBuilder
import com.deuktemsiru.common.WireAliasAdapterFactory

fun wireGson() = GsonBuilder().registerTypeAdapterFactory(WireAliasAdapterFactory(mapOf(
    StoreListItemResponse::class.java to mapOf(
        "isWishlisted" to listOf("isWishlisted", "wishlisted"),
    ),
    StoreDetailApiResponse::class.java to mapOf(
        "isWishlisted" to listOf("isWishlisted", "wishlisted"),
    ),
    WishlistToggleResponse::class.java to mapOf(
        "isWishlisted" to listOf("isWishlisted", "wishlisted"),
    ),
    MemberApiResponse::class.java to mapOf(
        "isSiruLinked" to listOf("isSiruLinked", "siruLinked"),
    ),
))).create()
