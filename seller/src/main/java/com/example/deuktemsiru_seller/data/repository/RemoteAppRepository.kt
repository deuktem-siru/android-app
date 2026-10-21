package com.example.deuktemsiru_seller.data.repository

import com.example.deuktemsiru_seller.domain.repository.AppRepository
import com.example.deuktemsiru_seller.domain.model.*
import com.example.deuktemsiru_seller.data.remote.ApiService
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class RemoteAppRepository(private val api: () -> ApiService) : AppRepository {
    override suspend fun kakaoLogin(req: KakaoLoginRequest): ApiResponse<LoginData> =
        api().kakaoLogin(req)
    override suspend fun debugLogin(req: DebugLoginRequest): ApiResponse<LoginData> =
        api().debugLogin(req)
    override suspend fun logout(memberId: Long): ApiResponse<Unit> =
        api().logout(memberId)
    override suspend fun registerFcmToken(req: FcmTokenRequest): ApiResponse<Unit> =
        api().registerFcmToken(req)
    override suspend fun getMyInfo(memberId: Long): ApiResponse<MemberMeResponse> =
        api().getMyInfo(memberId)
    override suspend fun getSaleItems(sellerId: Long): ApiResponse<List<SaleItemApiResponse>> =
        api().getSaleItems(sellerId)
    override suspend fun createSaleItem(sellerId: Long, req: SaleItemCreateRequest): ApiResponse<SaleItemApiResponse> =
        api().createSaleItem(sellerId, req)
    override suspend fun updateSaleStatus(id: Long, sellerId: Long, req: UpdateSaleStatusRequest): ApiResponse<SaleItemApiResponse> =
        api().updateSaleStatus(id, sellerId, req)
    override suspend fun updateSaleItem(id: Long, sellerId: Long, req: UpdateSaleItemRequest): ApiResponse<SaleItemApiResponse> =
        api().updateSaleItem(id, sellerId, req)
    override suspend fun cancelSaleItem(id: Long, sellerId: Long): ApiResponse<Unit> =
        api().cancelSaleItem(id, sellerId)
    override suspend fun getMenus(sellerId: Long): ApiResponse<List<MenuItemApiResponse>> =
        api().getMenus(sellerId)
    override suspend fun addMenuWithImage(sellerId: Long, name: String, originalPrice: String, description: String?, allergenInfo: String?, image: UploadFile?): ApiResponse<MenuItemApiResponse> =
        api().addMenuWithImage(sellerId, name.toRequestBody("text/plain".toMediaType()), originalPrice.toRequestBody("text/plain".toMediaType()), description?.toRequestBody("text/plain".toMediaType()), allergenInfo?.toRequestBody("text/plain".toMediaType()), image?.toPart())
    override suspend fun updateMenu(menuItemId: Long, sellerId: Long, req: MenuItemUpdateRequest): ApiResponse<MenuItemApiResponse> =
        api().updateMenu(menuItemId, sellerId, req)
    override suspend fun deleteMenu(menuItemId: Long, sellerId: Long): ApiResponse<Unit> =
        api().deleteMenu(menuItemId, sellerId)
    override suspend fun getOrders(sellerId: Long): ApiResponse<List<OrderApiResponse>> =
        api().getOrders(sellerId)
    override suspend fun updateOrderStatus(orderId: Long, sellerId: Long, req: UpdateOrderStatusRequest): ApiResponse<OrderApiResponse> =
        api().updateOrderStatus(orderId, sellerId, req)
    override suspend fun confirmPickup(orderId: Long, sellerId: Long, req: ConfirmPickupRequest): ApiResponse<OrderApiResponse> =
        api().confirmPickup(orderId, sellerId, req)
    override suspend fun verifyPickupCode(sellerId: Long, code: String): ApiResponse<OrderApiResponse> =
        api().verifyPickupCode(sellerId, code)
    override suspend fun getMyStore(sellerId: Long): ApiResponse<StoreApiResponse> =
        api().getMyStore(sellerId)
    override suspend fun updateStore(sellerId: Long, req: UpdateStoreRequest): ApiResponse<StoreApiResponse> =
        api().updateStore(sellerId, req)
    override suspend fun sendNotification(sellerId: Long, req: SendNotificationRequest): ApiResponse<NotificationApiResponse> =
        api().sendNotification(sellerId, req)
    override suspend fun getNotifications(sellerId: Long): ApiResponse<List<NotificationApiResponse>> =
        api().getNotifications(sellerId)
    override suspend fun getSettlements(sellerId: Long, year: Int, month: Int): ApiResponse<SettlementListResponse> =
        api().getSettlements(sellerId, year, month)
    override suspend fun requestWithdrawal(sellerId: Long, req: SettlementWithdrawRequest): ApiResponse<SettlementItem> =
        api().requestWithdrawal(sellerId, req)
    override suspend fun getSales(sellerId: Long, period: String, date: String?): ApiResponse<SalesApiResponse> =
        api().getSales(sellerId, period, date)
}

private fun UploadFile.toPart() = MultipartBody.Part.createFormData(fieldName, name, bytes.toRequestBody(contentType?.toMediaTypeOrNull()))
