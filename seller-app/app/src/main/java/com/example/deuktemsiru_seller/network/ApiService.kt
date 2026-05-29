package com.example.deuktemsiru_seller.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
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
        @Path("productId") id: Long,
        @Query("sellerId") sellerId: Long,
        @Body req: UpdateSaleStatusRequest,
    ): ApiResponse<SaleItemApiResponse>

    @POST("api/v1/sellers/products/{productId}/status")
    suspend fun updateSaleStatusViaPost(
        @Path("productId") id: Long,
        @Query("sellerId") sellerId: Long,
        @Body req: UpdateSaleStatusRequest,
    ): ApiResponse<SaleItemApiResponse>

    @PATCH("api/v1/sellers/products/{productId}")
    suspend fun updateSaleItem(
        @Path("productId") id: Long,
        @Query("sellerId") sellerId: Long,
        @Body req: UpdateSaleItemRequest,
    ): ApiResponse<SaleItemApiResponse>

    @POST("api/v1/sellers/products/{productId}")
    suspend fun updateSaleItemViaPost(
        @Path("productId") id: Long,
        @Query("sellerId") sellerId: Long,
        @Body req: UpdateSaleItemRequest,
    ): ApiResponse<SaleItemApiResponse>

    @DELETE("api/v1/sellers/products/{productId}")
    suspend fun cancelSaleItem(
        @Path("productId") id: Long,
        @Query("sellerId") sellerId: Long,
    ): Response<ResponseBody>

    @POST("api/v1/sellers/products/{productId}/delete")
    suspend fun cancelSaleItemViaPost(
        @Path("productId") id: Long,
        @Query("sellerId") sellerId: Long,
    ): Response<ResponseBody>

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

    @POST("api/v1/sellers/menu-items/{menuItemId}")
    suspend fun updateMenu(
        @Path("menuItemId") menuItemId: Long,
        @Query("sellerId") sellerId: Long,
        @Body req: MenuItemUpdateRequest,
    ): ApiResponse<MenuItemApiResponse>

    @DELETE("api/v1/sellers/menu-items/{menuItemId}")
    suspend fun deleteMenu(
        @Path("menuItemId") menuItemId: Long,
        @Query("sellerId") sellerId: Long,
    ): Response<ResponseBody>

    @POST("api/v1/sellers/menu-items/{menuItemId}/delete")
    suspend fun deleteMenuViaPost(
        @Path("menuItemId") menuItemId: Long,
        @Query("sellerId") sellerId: Long,
    ): Response<ResponseBody>

    // ── 주문 ──────────────────────────────────────────────────
    @GET("api/v1/sellers/orders")
    suspend fun getOrders(@Query("sellerId") sellerId: Long): ApiResponse<List<OrderApiResponse>>

    @PATCH("api/v1/sellers/orders/{orderId}/status")
    suspend fun updateOrderStatus(
        @Path("orderId") orderId: Long,
        @Query("sellerId") sellerId: Long,
        @Body req: UpdateOrderStatusRequest,
    ): ApiResponse<OrderApiResponse>

    @PATCH("api/v1/sellers/orders/{orderId}/confirm")
    suspend fun confirmPickup(
        @Path("orderId") orderId: Long,
        @Query("sellerId") sellerId: Long,
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

suspend fun ApiService.updateSaleStatusCompat(
    sellerId: Long,
    id: Long,
    req: UpdateSaleStatusRequest,
): ApiResponse<SaleItemApiResponse> =
    runCatching {
        withPostFallback(
            primary = { updateSaleStatus(id, sellerId, req) },
            fallback = { updateSaleStatusViaPost(id, sellerId, req) },
        )
    }.getOrElse { error ->
        val legacyPauseRequest = UpdateSaleStatusRequest(SaleStatus.SoldOut.apiValue)
        if (req.status == SaleStatus.Paused.apiValue && error.canRetryWithLegacyPauseStatus()) {
            withPostFallback(
                primary = { updateSaleStatus(id, sellerId, legacyPauseRequest) },
                fallback = { updateSaleStatusViaPost(id, sellerId, legacyPauseRequest) },
            )
        } else {
            throw error
        }
    }

suspend fun ApiService.updateSaleItemCompat(
    sellerId: Long,
    id: Long,
    req: UpdateSaleItemRequest,
): ApiResponse<SaleItemApiResponse> =
    withPostFallback(
        primary = { updateSaleItem(id, sellerId, req) },
        fallback = { updateSaleItemViaPost(id, sellerId, req) },
    )

suspend fun ApiService.cancelSaleItemCompat(
    sellerId: Long,
    id: Long,
): Unit =
    withRawPostFallback(
        primary = { cancelSaleItem(id, sellerId).throwIfNotSuccessful() },
        fallback = { cancelSaleItemViaPost(id, sellerId).throwIfNotSuccessful() },
    )

suspend fun ApiService.deleteMenuCompat(
    sellerId: Long,
    menuItemId: Long,
): Unit =
    withRawPostFallback(
        primary = { deleteMenu(menuItemId, sellerId).throwIfNotSuccessful() },
        fallback = { deleteMenuViaPost(menuItemId, sellerId).throwIfNotSuccessful() },
    )

private suspend fun <T> withPostFallback(
    primary: suspend () -> ApiResponse<T>,
    fallback: suspend () -> ApiResponse<T>,
): ApiResponse<T> =
    runCatching { primary() }.getOrElse { error ->
        if (error.isEndpointCompatibilityError()) fallback() else throw error
    }

private suspend fun withRawPostFallback(
    primary: suspend () -> Unit,
    fallback: suspend () -> Unit,
) {
    runCatching { primary() }.getOrElse { error ->
        if (error.isEndpointCompatibilityError()) fallback() else throw error
    }
}

private fun Response<ResponseBody>.throwIfNotSuccessful() {
    if (!isSuccessful) throw retrofit2.HttpException(this)
}

private fun Throwable.isEndpointCompatibilityError(): Boolean =
    this is retrofit2.HttpException && code() in setOf(404, 405, 501)

private fun Throwable.canRetryWithLegacyPauseStatus(): Boolean =
    this is retrofit2.HttpException && code() in setOf(400, 409, 500)
