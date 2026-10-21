package com.example.deuktemsiru_seller.domain.repository

import com.example.deuktemsiru_seller.domain.model.*

interface AppRepository {
    suspend fun kakaoLogin(req: KakaoLoginRequest): ApiResponse<LoginData>
    suspend fun debugLogin(req: DebugLoginRequest): ApiResponse<LoginData>
    suspend fun logout(memberId: Long): ApiResponse<Unit>
    suspend fun registerFcmToken(req: FcmTokenRequest): ApiResponse<Unit>
    suspend fun getMyInfo(memberId: Long): ApiResponse<MemberMeResponse>
    suspend fun getSaleItems(sellerId: Long): ApiResponse<List<SaleItemApiResponse>>
    suspend fun createSaleItem(sellerId: Long, req: SaleItemCreateRequest): ApiResponse<SaleItemApiResponse>
    suspend fun updateSaleStatus(id: Long, sellerId: Long, req: UpdateSaleStatusRequest): ApiResponse<SaleItemApiResponse>
    suspend fun updateSaleItem(id: Long, sellerId: Long, req: UpdateSaleItemRequest): ApiResponse<SaleItemApiResponse>
    suspend fun cancelSaleItem(id: Long, sellerId: Long): ApiResponse<Unit>
    suspend fun getMenus(sellerId: Long): ApiResponse<List<MenuItemApiResponse>>
    suspend fun addMenuWithImage(sellerId: Long, name: String, originalPrice: String, description: String?, allergenInfo: String?, image: UploadFile?): ApiResponse<MenuItemApiResponse>
    suspend fun updateMenu(menuItemId: Long, sellerId: Long, req: MenuItemUpdateRequest): ApiResponse<MenuItemApiResponse>
    suspend fun deleteMenu(menuItemId: Long, sellerId: Long): ApiResponse<Unit>
    suspend fun getOrders(sellerId: Long): ApiResponse<List<OrderApiResponse>>
    suspend fun updateOrderStatus(orderId: Long, sellerId: Long, req: UpdateOrderStatusRequest): ApiResponse<OrderApiResponse>
    suspend fun confirmPickup(orderId: Long, sellerId: Long, req: ConfirmPickupRequest): ApiResponse<OrderApiResponse>
    suspend fun verifyPickupCode(sellerId: Long, code: String): ApiResponse<OrderApiResponse>
    suspend fun getMyStore(sellerId: Long): ApiResponse<StoreApiResponse>
    suspend fun updateStore(sellerId: Long, req: UpdateStoreRequest): ApiResponse<StoreApiResponse>
    suspend fun sendNotification(sellerId: Long, req: SendNotificationRequest): ApiResponse<NotificationApiResponse>
    suspend fun getNotifications(sellerId: Long): ApiResponse<List<NotificationApiResponse>>
    suspend fun getSettlements(sellerId: Long, year: Int, month: Int): ApiResponse<SettlementListResponse>
    suspend fun requestWithdrawal(sellerId: Long, req: SettlementWithdrawRequest): ApiResponse<SettlementItem>
    suspend fun getSales(sellerId: Long, period: String = "DAY", date: String? = null): ApiResponse<SalesApiResponse>
}
