package com.example.deuktemsiru_seller.domain.model


import java.io.Serializable

// ── 공통 응답 래퍼 ────────────────────────────────────────────
typealias ApiResponse<T> = com.deuktemsiru.common.ApiResponse<T>

// ── 인증 ──────────────────────────────────────────────────────
data class KakaoLoginRequest(
    val kakaoAccessToken: String,
    val role: String = "SELLER",
)

data class DebugLoginRequest(
    val role: String = "SELLER",
    val email: String? = null,
)

typealias MemberSummary = com.deuktemsiru.common.MemberSummary

typealias LoginData = com.deuktemsiru.common.LoginData

typealias TokenData = com.deuktemsiru.common.TokenData

// ── 멤버 ──────────────────────────────────────────────────────
data class MemberMeResponse(
    val memberId: Long,
    val email: String? = null,
    val nickname: String,
    val role: String,
)

// ── 판매 상품(SaleItem) ───────────────────────────────────────
data class SaleItemApiResponse(
    val id: Long,
    val menuItemId: Long? = null,
    val name: String,
    val emoji: String? = null,
    val originalPrice: Int,
    val discountedPrice: Int,
    val discountRate: Int? = null,
    val remainingItems: Int,
    val totalItems: Int,
    val status: String,
    // 명세서: pickupStart + pickupEnd 분리. 기존 pickupTimeSlot도 허용(mock 호환)
    val pickupStart: String? = null,
    val pickupEnd: String? = null,
    val pickupTimeSlot: String? = null,
) : Serializable {
    val displayPickupTime: String
        get() = pickupTimeSlot
            ?: if (pickupStart != null && pickupEnd != null) "$pickupStart~$pickupEnd"
            else pickupStart ?: pickupEnd ?: ""

    val saleStatus: SaleStatus get() = SaleStatus.from(status)
}

// 상품 목록 래퍼 (GET /sellers/products → data.products)
data class SaleItemCreateRequest(
    val menuItemId: Long? = null,
    val name: String,
    val discountPrice: Int,
    val originalPrice: Int,
    val quantityTotal: Int,
    val madeAt: String? = null,
    val pickupStart: String,
    val pickupEnd: String,
    val availableDate: String,
    val allergenInfo: String? = null,
)

data class UpdateSaleStatusRequest(val status: String)
data class UpdateSaleItemRequest(
    val originalPrice: Int? = null,
    val discountPrice: Int? = null,
    val quantityRemaining: Int? = null,
)

// ── 메뉴 ──────────────────────────────────────────────────────
data class MenuItemApiResponse(
    val id: Long,
    val name: String,
    val emoji: String? = null,
    val imageUrl: String? = null,
    val originalPrice: Int,
) {
    val displayEmoji: String get() = emoji ?: "🍽️"
}

data class MenuItemUpdateRequest(
    val name: String? = null,
    val originalPrice: Int? = null,
)

// ── 주문 ──────────────────────────────────────────────────────
data class OrderItemApiResponse(
    val productId: Long? = null,
    val menuItemId: Long? = null,
    val name: String,
    val emoji: String? = null,
    val quantity: Int,
    val price: Int,
) : Serializable

data class OrderApiResponse(
    val id: Long,
    val orderNumber: String? = null,
    val storeId: Long? = null,
    val storeName: String? = null,
    val customerName: String? = null,
    val status: String,
    val pickupCode: String? = null,
    val pickupTime: String? = null,
    val totalAmount: Int,
    val createdAt: String = "",
    val items: List<OrderItemApiResponse>,
) : Serializable {
    val orderStatus: OrderStatus get() = OrderStatus.from(status)
}

// 주문 목록 래퍼 (GET /sellers/orders → data.orders)
data class UpdateOrderStatusRequest(val status: String)

data class ConfirmPickupRequest(val pickupCode: String)

// ── 가게 ──────────────────────────────────────────────────────
data class StoreApiResponse(
    // 명세서 v2 필드
    val storeId: Long? = null,
    val name: String,
    val isActive: Boolean? = null,
    val isVerified: Boolean? = null,
    val todayProductCount: Int? = null,
    val pendingOrderCount: Int? = null,
    val ratingAvg: Float? = null,
    val reviewCount: Int? = null,
    // 확장 필드 (가게 상세/수정에서 추가 반환될 수 있음)
    val id: Long? = null,
    val category: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val closingTime: String? = null,
    val description: String? = null,
) {
    val actualId: Long get() = storeId ?: id ?: 0L
}

data class UpdateStoreRequest(
    val address: String? = null,
    val phone: String? = null,
    val closingTime: String? = null,
    val description: String? = null,
)

// ── 알림 ──────────────────────────────────────────────────────
data class SendNotificationRequest(
    val message: String,
    val targetType: String = "REGULAR",
    val radiusKm: Int? = null,
)

data class NotificationApiResponse(
    val id: Long,
    val storeId: Long,
    val storeName: String,
    val message: String,
    val sentAt: String,
    val recipientCount: Int,
)

// ── 매출 ──────────────────────────────────────────────────────
data class DailySales(val date: String, val amount: Int)

data class TopMenu(
    val name: String,
    val emoji: String? = null,
    val count: Int,
)

data class SalesApiResponse(
    val todaySales: Int = 0,
    val todayOrderCount: Int = 0,
    val salesData: List<DailySales> = emptyList(),
    val topMenus: List<TopMenu> = emptyList(),
    val carbonSavedKg: Float? = null,
)

data class SettlementItem(
    val settlementId: Long,
    val periodStart: String,
    val periodEnd: String,
    val totalSales: Int,
    val platformFee: Int,
    val settlementAmount: Int,
    val status: String,
    val settledAt: String?,
)

data class SettlementListResponse(
    val settlements: List<SettlementItem> = emptyList(),
)

data class SettlementWithdrawRequest(
    val year: Int,
    val month: Int,
)

data class FcmTokenRequest(val token: String, val deviceInfo: String = "ANDROID")
