package com.example.deuktemsiru_seller.domain.model

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
