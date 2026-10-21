package com.example.deuktemsiru_seller.network

import com.example.deuktemsiru_seller.BuildConfig
import com.deuktemsiru.common.AuthenticatedClient

object RetrofitClient : AuthenticatedClient(
    BuildConfig.BASE_URL.ifBlank { "http://10.0.2.2:8080/" },
    BuildConfig.DEBUG,
    isMockSession = { RetrofitClient.isMockSession },
) {
    @Volatile var memberId: Long = -1L
    var isMockSession = false
    private val realApi: ApiService by lazy { createApi(ApiService::class.java) }
    val api: ApiService get() = if (isMockSession) MockApiService else realApi
}
