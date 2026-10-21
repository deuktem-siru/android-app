package com.example.deuktemsiru_buyer.data.repository

import com.example.deuktemsiru_buyer.domain.repository.AppRepository
import com.example.deuktemsiru_buyer.domain.model.*
import com.example.deuktemsiru_buyer.data.remote.ApiService

class RemoteAppRepository(private val api: () -> ApiService) : AppRepository {
    override suspend fun kakaoLogin(req: KakaoLoginRequest): ApiResponse<LoginData> =
        api().kakaoLogin(req)
    override suspend fun debugLogin(req: DebugLoginRequest): ApiResponse<LoginData> =
        api().debugLogin(req)
    override suspend fun logout(): ApiResponse<Unit> =
        api().logout()
    override suspend fun registerFcmToken(req: FcmTokenRequest): ApiResponse<Unit> =
        api().registerFcmToken(req)
    override suspend fun linkSiru(req: SiruLinkRequest): ApiResponse<MemberApiResponse> =
        api().linkSiru(req)
    override suspend fun getStores(latitude: Double, longitude: Double, radius: Int, category: String?, keyword: String?, sort: String): ApiResponse<StoreListResponse> =
        api().getStores(latitude, longitude, radius, category, keyword, sort)
    override suspend fun getStore(storeId: Long): ApiResponse<StoreDetailApiResponse> =
        api().getStore(storeId)
    override suspend fun toggleWishlist(storeId: Long): ApiResponse<WishlistToggleResponse> =
        api().toggleWishlist(storeId)
    override suspend fun getWishlist(): ApiResponse<WishlistListResponse> =
        api().getWishlist()
    override suspend fun createOrder(req: CreateOrderRequest): ApiResponse<CreateOrderResponse> =
        api().createOrder(req)
    override suspend fun getOrders(): ApiResponse<List<OrderListItemResponse>> =
        api().getOrders()
    override suspend fun getOrder(orderId: Long): ApiResponse<OrderDetailResponse> =
        api().getOrder(orderId)
    override suspend fun cancelOrder(orderId: Long): ApiResponse<OrderDetailResponse> =
        api().cancelOrder(orderId)
    override suspend fun addToCart(req: CartAddRequest): ApiResponse<CartApiItem> =
        api().addToCart(req)
    override suspend fun getCart(): ApiResponse<CartResponse> =
        api().getCart()
    override suspend fun removeCartItem(cartItemId: Long): ApiResponse<Unit> =
        api().removeCartItem(cartItemId)
    override suspend fun updateCartItem(cartItemId: Long, req: CartUpdateRequest): ApiResponse<CartApiItem> =
        api().updateCartItem(cartItemId, req)
    override suspend fun clearCart(): ApiResponse<Unit> =
        api().clearCart()
    override suspend fun getMe(): ApiResponse<MemberApiResponse> =
        api().getMe()
    override suspend fun getMyStats(): ApiResponse<MemberStatsResponse> =
        api().getMyStats()
    override suspend fun getNotificationSettings(): ApiResponse<NotificationSettingsResponse> =
        api().getNotificationSettings()
    override suspend fun updateNotificationSettings(req: UpdateNotificationSettingsRequest): ApiResponse<NotificationSettingsResponse> =
        api().updateNotificationSettings(req)
}
