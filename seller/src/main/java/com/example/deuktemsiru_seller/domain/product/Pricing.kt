package com.example.deuktemsiru_seller.domain.product

fun discountPrice(originalPrice: Int, rate: Int): Int = originalPrice * (100 - rate) / 100
