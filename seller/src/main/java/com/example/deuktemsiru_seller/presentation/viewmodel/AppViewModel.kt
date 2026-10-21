package com.example.deuktemsiru_seller.presentation.viewmodel

import com.example.deuktemsiru_seller.domain.repository.AppRepository
import com.example.deuktemsiru_seller.domain.model.*
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
    override suspend fun logout(memberId: Long): ApiResponse<Unit> =
        request { repository.logout(memberId) }
    override suspend fun registerFcmToken(req: FcmTokenRequest): ApiResponse<Unit> =
        request { repository.registerFcmToken(req) }
    override suspend fun getMyInfo(memberId: Long): ApiResponse<MemberMeResponse> =
        request { repository.getMyInfo(memberId) }
    override suspend fun getSaleItems(sellerId: Long): ApiResponse<List<SaleItemApiResponse>> =
        request { repository.getSaleItems(sellerId) }
    override suspend fun createSaleItem(sellerId: Long, req: SaleItemCreateRequest): ApiResponse<SaleItemApiResponse> =
        request { repository.createSaleItem(sellerId, req) }
    override suspend fun updateSaleStatus(id: Long, sellerId: Long, req: UpdateSaleStatusRequest): ApiResponse<SaleItemApiResponse> =
        request { repository.updateSaleStatus(id, sellerId, req) }
    override suspend fun updateSaleItem(id: Long, sellerId: Long, req: UpdateSaleItemRequest): ApiResponse<SaleItemApiResponse> =
        request { repository.updateSaleItem(id, sellerId, req) }
    override suspend fun cancelSaleItem(id: Long, sellerId: Long): ApiResponse<Unit> =
        request { repository.cancelSaleItem(id, sellerId) }
    override suspend fun getMenus(sellerId: Long): ApiResponse<List<MenuItemApiResponse>> =
        request { repository.getMenus(sellerId) }
    override suspend fun addMenuWithImage(sellerId: Long, name: String, originalPrice: String, description: String?, allergenInfo: String?, image: UploadFile?): ApiResponse<MenuItemApiResponse> =
        request { repository.addMenuWithImage(sellerId, name, originalPrice, description, allergenInfo, image) }
    override suspend fun updateMenu(menuItemId: Long, sellerId: Long, req: MenuItemUpdateRequest): ApiResponse<MenuItemApiResponse> =
        request { repository.updateMenu(menuItemId, sellerId, req) }
    override suspend fun deleteMenu(menuItemId: Long, sellerId: Long): ApiResponse<Unit> =
        request { repository.deleteMenu(menuItemId, sellerId) }
    override suspend fun getOrders(sellerId: Long): ApiResponse<List<OrderApiResponse>> =
        request { repository.getOrders(sellerId) }
    override suspend fun updateOrderStatus(orderId: Long, sellerId: Long, req: UpdateOrderStatusRequest): ApiResponse<OrderApiResponse> =
        request { repository.updateOrderStatus(orderId, sellerId, req) }
    override suspend fun confirmPickup(orderId: Long, sellerId: Long, req: ConfirmPickupRequest): ApiResponse<OrderApiResponse> =
        request { repository.confirmPickup(orderId, sellerId, req) }
    override suspend fun verifyPickupCode(sellerId: Long, code: String): ApiResponse<OrderApiResponse> =
        request { repository.verifyPickupCode(sellerId, code) }
    override suspend fun getMyStore(sellerId: Long): ApiResponse<StoreApiResponse> =
        request { repository.getMyStore(sellerId) }
    override suspend fun updateStore(sellerId: Long, req: UpdateStoreRequest): ApiResponse<StoreApiResponse> =
        request { repository.updateStore(sellerId, req) }
    override suspend fun sendNotification(sellerId: Long, req: SendNotificationRequest): ApiResponse<NotificationApiResponse> =
        request { repository.sendNotification(sellerId, req) }
    override suspend fun getNotifications(sellerId: Long): ApiResponse<List<NotificationApiResponse>> =
        request { repository.getNotifications(sellerId) }
    override suspend fun getSettlements(sellerId: Long, year: Int, month: Int): ApiResponse<SettlementListResponse> =
        request { repository.getSettlements(sellerId, year, month) }
    override suspend fun requestWithdrawal(sellerId: Long, req: SettlementWithdrawRequest): ApiResponse<SettlementItem> =
        request { repository.requestWithdrawal(sellerId, req) }
    override suspend fun getSales(sellerId: Long, period: String, date: String?): ApiResponse<SalesApiResponse> =
        request { repository.getSales(sellerId, period, date) }
}
