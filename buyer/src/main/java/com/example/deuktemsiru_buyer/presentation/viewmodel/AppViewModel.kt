package com.example.deuktemsiru_buyer.presentation.viewmodel

import com.example.deuktemsiru_buyer.domain.repository.AppRepository
import com.example.deuktemsiru_buyer.domain.model.*
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RequestState(val activeRequests: Int = 0, val error: String? = null)

class AppViewModel(private val repository: AppRepository) : ViewModel(), AppRepository {
    private val _requests = MutableStateFlow(RequestState())
    val requests = _requests.asStateFlow()

    private suspend fun <T> request(block: suspend () -> T): T {
        _requests.update { it.copy(activeRequests = it.activeRequests + 1, error = null) }
        try { return block() }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) { _requests.update { it.copy(error = error.message) }; throw error }
        finally { _requests.update { it.copy(activeRequests = it.activeRequests - 1) } }
    }
    override suspend fun kakaoLogin(req: KakaoLoginRequest): ApiResponse<LoginData> =
        request { repository.kakaoLogin(req) }
    override suspend fun debugLogin(req: DebugLoginRequest): ApiResponse<LoginData> =
        request { repository.debugLogin(req) }
    override suspend fun logout(): ApiResponse<Unit> =
        request { repository.logout() }
    override suspend fun registerFcmToken(req: FcmTokenRequest): ApiResponse<Unit> =
        request { repository.registerFcmToken(req) }
    override suspend fun linkSiru(req: SiruLinkRequest): ApiResponse<MemberApiResponse> =
        request { repository.linkSiru(req) }
    override suspend fun getStores(latitude: Double, longitude: Double, radius: Int, category: String?, keyword: String?, sort: String): ApiResponse<StoreListResponse> =
        request { repository.getStores(latitude, longitude, radius, category, keyword, sort) }
    override suspend fun getStore(storeId: Long): ApiResponse<StoreDetailApiResponse> =
        request { repository.getStore(storeId) }
    override suspend fun toggleWishlist(storeId: Long): ApiResponse<WishlistToggleResponse> =
        request { repository.toggleWishlist(storeId) }
    override suspend fun getWishlist(): ApiResponse<WishlistListResponse> =
        request { repository.getWishlist() }
    override suspend fun createOrder(req: CreateOrderRequest): ApiResponse<CreateOrderResponse> =
        request { repository.createOrder(req) }
    override suspend fun getOrders(): ApiResponse<List<OrderListItemResponse>> =
        request { repository.getOrders() }
    override suspend fun getOrder(orderId: Long): ApiResponse<OrderDetailResponse> =
        request { repository.getOrder(orderId) }
    override suspend fun cancelOrder(orderId: Long): ApiResponse<OrderDetailResponse> =
        request { repository.cancelOrder(orderId) }
    override suspend fun addToCart(req: CartAddRequest): ApiResponse<CartApiItem> =
        request { repository.addToCart(req) }
    override suspend fun getCart(): ApiResponse<CartResponse> =
        request { repository.getCart() }
    override suspend fun removeCartItem(cartItemId: Long): ApiResponse<Unit> =
        request { repository.removeCartItem(cartItemId) }
    override suspend fun updateCartItem(cartItemId: Long, req: CartUpdateRequest): ApiResponse<CartApiItem> =
        request { repository.updateCartItem(cartItemId, req) }
    override suspend fun clearCart(): ApiResponse<Unit> =
        request { repository.clearCart() }
    override suspend fun getMe(): ApiResponse<MemberApiResponse> =
        request { repository.getMe() }
    override suspend fun getMyStats(): ApiResponse<MemberStatsResponse> =
        request { repository.getMyStats() }
    override suspend fun getNotificationSettings(): ApiResponse<NotificationSettingsResponse> =
        request { repository.getNotificationSettings() }
    override suspend fun updateNotificationSettings(req: UpdateNotificationSettingsRequest): ApiResponse<NotificationSettingsResponse> =
        request { repository.updateNotificationSettings(req) }
}
