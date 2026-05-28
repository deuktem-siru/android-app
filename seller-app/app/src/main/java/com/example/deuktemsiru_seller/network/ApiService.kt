package com.example.deuktemsiru_seller.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {

    // ── 인증 ──────────────────────────────────────────────────
    @POST("api/v1/auth/kakao/login")
    suspend fun kakaoLogin(@Body req: KakaoLoginRequest): ApiResponse<LoginData>

    @POST("api/v1/auth/debug/login")
    suspend fun debugLogin(@Body req: DebugLoginRequest): ApiResponse<LoginData>

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body req: TokenRefreshRequest): ApiResponse<TokenData>

    @POST("api/v1/auth/logout")
    suspend fun logout(@Query("memberId") memberId: Long): ApiResponse<Unit>

    // ── 멤버 ──────────────────────────────────────────────────
    @GET("api/v1/members/me")
    suspend fun getMyInfo(@Query("memberId") memberId: Long): ApiResponse<MemberMeResponse>

    // ── 판매 상품 ──────────────────────────────────────────────
    @GET("api/v1/sellers/products")
    suspend fun getSaleItems(@Query("sellerId") sellerId: Long): ApiResponse<List<SaleItemApiResponse>>

    @POST("api/v1/sellers/products")
    suspend fun createSaleItem(
        @Query("sellerId") sellerId: Long,
        @Body req: SaleItemCreateRequest,
    ): ApiResponse<SaleItemApiResponse>

    @Multipart
    @POST("api/v1/sellers/products")
    suspend fun createSaleItemWithImage(
        @Query("sellerId") sellerId: Long,
        @Part("name") name: RequestBody,
        @Part("discountPrice") discountPrice: RequestBody,
        @Part("originalPrice") originalPrice: RequestBody,
        @Part("quantityTotal") quantityTotal: RequestBody,
        @Part("pickupStart") pickupStart: RequestBody,
        @Part("pickupEnd") pickupEnd: RequestBody,
        @Part("availableDate") availableDate: RequestBody,
        @Part("menuItemId") menuItemId: RequestBody?,
        @Part("madeAt") madeAt: RequestBody?,
        @Part("allergenInfo") allergenInfo: RequestBody?,
        @Part images: List<MultipartBody.Part>?,
    ): ApiResponse<SaleItemApiResponse>

    @PATCH("api/v1/sellers/products/{productId}/status")
    suspend fun updateSaleStatus(
        @Query("sellerId") sellerId: Long,
        @Path("productId") id: Long,
        @Body req: UpdateSaleStatusRequest,
    ): ApiResponse<SaleItemApiResponse>

    @PATCH("api/v1/sellers/products/{productId}")
    suspend fun updateSaleItem(
        @Query("sellerId") sellerId: Long,
        @Path("productId") id: Long,
        @Body req: UpdateSaleItemRequest,
    ): ApiResponse<SaleItemApiResponse>

    @DELETE("api/v1/sellers/products/{productId}")
    suspend fun cancelSaleItem(
        @Query("sellerId") sellerId: Long,
        @Path("productId") id: Long,
    ): ApiResponse<Unit>

    // ── 메뉴 마스터 ────────────────────────────────────────────
    @GET("api/v1/sellers/menu-items")
    suspend fun getMenus(@Query("sellerId") sellerId: Long): ApiResponse<List<MenuItemApiResponse>>

    @POST("api/v1/sellers/menu-items")
    suspend fun addMenu(
        @Query("sellerId") sellerId: Long,
        @Body req: MenuItemRequest,
    ): ApiResponse<MenuItemApiResponse>

    @Multipart
    @POST("api/v1/sellers/menu-items")
    suspend fun addMenuWithImage(
        @Query("sellerId") sellerId: Long,
        @Part("name") name: RequestBody,
        @Part("originalPrice") originalPrice: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("allergenInfo") allergenInfo: RequestBody?,
        @Part image: MultipartBody.Part?,
    ): ApiResponse<MenuItemApiResponse>

    @PATCH("api/v1/sellers/menu-items/{menuItemId}")
    suspend fun updateMenu(
        @Query("sellerId") sellerId: Long,
        @Path("menuItemId") menuItemId: Long,
        @Body req: MenuItemUpdateRequest,
    ): ApiResponse<MenuItemApiResponse>

    @DELETE("api/v1/sellers/menu-items/{menuItemId}")
    suspend fun deleteMenu(
        @Query("sellerId") sellerId: Long,
        @Path("menuItemId") menuItemId: Long,
    ): ApiResponse<Unit>

    // ── 주문 ──────────────────────────────────────────────────
    @GET("api/v1/sellers/orders")
    suspend fun getOrders(@Query("sellerId") sellerId: Long): ApiResponse<List<OrderApiResponse>>

    @PATCH("api/v1/sellers/orders/{orderId}/status")
    suspend fun updateOrderStatus(
        @Query("sellerId") sellerId: Long,
        @Path("orderId") orderId: Long,
        @Body req: UpdateOrderStatusRequest,
    ): ApiResponse<OrderApiResponse>

    @PATCH("api/v1/sellers/orders/{orderId}/confirm")
    suspend fun confirmPickup(
        @Query("sellerId") sellerId: Long,
        @Path("orderId") orderId: Long,
        @Body req: ConfirmPickupRequest,
    ): ApiResponse<OrderApiResponse>

    @GET("api/v1/sellers/pickup/verify")
    suspend fun verifyPickupCode(
        @Query("sellerId") sellerId: Long,
        @Query("code") code: String,
    ): ApiResponse<OrderApiResponse>

    // ── 가게 ──────────────────────────────────────────────────
    @GET("api/v1/sellers/stores/my")
    suspend fun getMyStore(@Query("sellerId") sellerId: Long): ApiResponse<StoreApiResponse>

    @PUT("api/v1/sellers/stores/my")
    suspend fun updateStore(
        @Query("sellerId") sellerId: Long,
        @Body req: UpdateStoreRequest,
    ): ApiResponse<StoreApiResponse>

    // ── 알림 ──────────────────────────────────────────────────
    @POST("api/v1/sellers/notifications")
    suspend fun sendNotification(
        @Query("sellerId") sellerId: Long,
        @Body req: SendNotificationRequest,
    ): ApiResponse<NotificationApiResponse>

    @GET("api/v1/sellers/notifications")
    suspend fun getNotifications(@Query("sellerId") sellerId: Long): ApiResponse<List<NotificationApiResponse>>

    // ── 정산 ──────────────────────────────────────────────────
    @GET("api/v1/sellers/settlements")
    suspend fun getSettlements(
        @Query("sellerId") sellerId: Long,
        @Query("year") year: Int,
        @Query("month") month: Int,
    ): ApiResponse<SettlementListResponse>

    @POST("api/v1/sellers/settlements/withdrawals")
    suspend fun requestWithdrawal(
        @Query("sellerId") sellerId: Long,
        @Body req: SettlementWithdrawRequest,
    ): ApiResponse<SettlementItem>

    // ── 매출 ──────────────────────────────────────────────────
    @GET("api/v1/sellers/sales/summary")
    suspend fun getSales(
        @Query("sellerId") sellerId: Long,
        @Query("period") period: String = "DAY",
        @Query("date") date: String? = null,
    ): ApiResponse<SalesApiResponse>
}
