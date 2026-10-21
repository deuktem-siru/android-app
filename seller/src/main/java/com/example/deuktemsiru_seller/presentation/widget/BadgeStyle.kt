package com.example.deuktemsiru_seller.presentation.widget

import androidx.annotation.ColorRes
import com.example.deuktemsiru_seller.R
import com.example.deuktemsiru_seller.domain.model.OrderStatus
import com.example.deuktemsiru_seller.domain.model.SaleStatus

data class BadgeStyle(
    val text: String,
    val backgroundRes: Int,
    @param:ColorRes val textColorRes: Int,
)

fun OrderStatus.badgeStyle(fallback: String = apiValue): BadgeStyle =
    when (this) {
        OrderStatus.Pending -> BadgeStyle("신규 주문", R.drawable.bg_status_soldout, R.color.warning)
        OrderStatus.Confirmed -> BadgeStyle("준비 중", R.drawable.bg_status_available, R.color.success)
        OrderStatus.PickedUp -> BadgeStyle("픽업 완료", R.drawable.bg_status_expired, R.color.text_muted)
        OrderStatus.Cancelled -> BadgeStyle("취소됨", R.drawable.bg_status_expired, R.color.text_muted)
        else -> BadgeStyle(fallback, R.drawable.bg_status_expired, R.color.text_muted)
    }

fun SaleStatus.badgeStyle(fallback: String = apiValue): BadgeStyle =
    when (this) {
        SaleStatus.Available -> BadgeStyle("● 판매중", R.drawable.bg_status_available, R.color.success)
        SaleStatus.Paused -> BadgeStyle("● 판매중지", R.drawable.bg_status_soldout, R.color.warning)
        SaleStatus.SoldOut -> BadgeStyle("● 품절", R.drawable.bg_status_soldout, R.color.warning)
        SaleStatus.Expired -> BadgeStyle("종료", R.drawable.bg_status_expired, R.color.text_muted)
        else -> BadgeStyle(fallback, R.drawable.bg_status_expired, R.color.text_muted)
    }

