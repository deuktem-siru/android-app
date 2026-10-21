package com.example.deuktemsiru_buyer.domain.repository

import com.example.deuktemsiru_buyer.domain.model.*

interface AppRepository {
    suspend fun kakaoLogin(req: KakaoLoginRequest): ApiResponse<LoginData>
    suspend fun debugLogin(req: DebugLoginRequest): ApiResponse<LoginData>
    suspend fun logout(): ApiResponse<Unit>
    suspend fun registerFcmToken(req: FcmTokenRequest): ApiResponse<Unit>
    suspend fun linkSiru(req: SiruLinkRequest): ApiResponse<MemberApiResponse>
    suspend fun getStores(latitude: Double = 37.3799, longitude: Double = 126.8031, radius: Int = 20000, category: String? = null, keyword: String? = null, sort: String = "distance"): ApiResponse<StoreListResponse>
    suspend fun getStore(storeId: Long): ApiResponse<StoreDetailApiResponse>
    suspend fun toggleWishlist(storeId: Long): ApiResponse<WishlistToggleResponse>
    suspend fun getWishlist(): ApiResponse<WishlistListResponse>
    suspend fun createOrder(req: CreateOrderRequest): ApiResponse<CreateOrderResponse>
    suspend fun getOrders(): ApiResponse<List<OrderListItemResponse>>
    suspend fun getOrder(orderId: Long): ApiResponse<OrderDetailResponse>
    suspend fun cancelOrder(orderId: Long): ApiResponse<OrderDetailResponse>
    suspend fun addToCart(req: CartAddRequest): ApiResponse<CartApiItem>
    suspend fun getCart(): ApiResponse<CartResponse>
    suspend fun removeCartItem(cartItemId: Long): ApiResponse<Unit>
    suspend fun updateCartItem(cartItemId: Long, req: CartUpdateRequest): ApiResponse<CartApiItem>
    suspend fun clearCart(): ApiResponse<Unit>
    suspend fun getMe(): ApiResponse<MemberApiResponse>
    suspend fun getMyStats(): ApiResponse<MemberStatsResponse>
    suspend fun getNotificationSettings(): ApiResponse<NotificationSettingsResponse>
    suspend fun updateNotificationSettings(req: UpdateNotificationSettingsRequest): ApiResponse<NotificationSettingsResponse>
}
