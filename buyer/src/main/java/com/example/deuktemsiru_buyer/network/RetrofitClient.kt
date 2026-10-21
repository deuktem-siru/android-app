package com.example.deuktemsiru_buyer.network

import com.example.deuktemsiru_buyer.BuildConfig
import com.deuktemsiru.common.AuthenticatedClient

object RetrofitClient : AuthenticatedClient(
    BuildConfig.BASE_URL.ifBlank { "http://10.0.2.2:8080/" },
    BuildConfig.DEBUG,
) {
    val api: ApiService by lazy { createApi(ApiService::class.java) }
}
