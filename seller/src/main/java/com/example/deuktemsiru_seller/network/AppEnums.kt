package com.example.deuktemsiru_seller.network

import androidx.annotation.ColorRes
import com.example.deuktemsiru_seller.R

enum class OrderStatus(val apiValue: String) {
    Pending("PENDING"),
    Confirmed("CONFIRMED"),
    PickedUp("PICKED_UP"),
    Cancelled("CANCELLED"),
    Completed("COMPLETED"),
    Unknown("");

    companion object {
        fun from(value: String?): OrderStatus =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: Unknown
    }
}

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

enum class SaleStatus(val apiValue: String) {
    Available("AVAILABLE"),
    Paused("PAUSED"),
    SoldOut("SOLD_OUT"),
    Expired("EXPIRED"),
    Unknown("");

    val isFinal: Boolean get() = this == Expired

    companion object {
        fun from(value: String?): SaleStatus =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: Unknown
    }
}

fun SaleStatus.badgeStyle(fallback: String = apiValue): BadgeStyle =
    when (this) {
        SaleStatus.Available -> BadgeStyle("● 판매중", R.drawable.bg_status_available, R.color.success)
        SaleStatus.Paused -> BadgeStyle("● 판매중지", R.drawable.bg_status_soldout, R.color.warning)
        SaleStatus.SoldOut -> BadgeStyle("● 품절", R.drawable.bg_status_soldout, R.color.warning)
        SaleStatus.Expired -> BadgeStyle("종료", R.drawable.bg_status_expired, R.color.text_muted)
        else -> BadgeStyle(fallback, R.drawable.bg_status_expired, R.color.text_muted)
    }

enum class NotificationTarget(val apiValue: String) {
    Regular("REGULAR"),
    Nearby("NEARBY"),
}

enum class SalesPeriod(val apiValue: String, val title: String) {
    Day("DAY", "일간 매출"),
    Week("WEEK", "주간 매출"),
    Month("MONTH", "월간 매출");

    companion object {
        fun from(value: String): SalesPeriod =
            entries.firstOrNull { it.apiValue == value } ?: Day
    }
}
