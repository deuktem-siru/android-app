package com.example.deuktemsiru_buyer.data.remote

import com.example.deuktemsiru_buyer.domain.model.*

import com.example.deuktemsiru_buyer.BuildConfig
import com.deuktemsiru.common.AuthenticatedClient

object RetrofitClient : AuthenticatedClient(
    BuildConfig.BASE_URL.ifBlank { "http://10.0.2.2:8080/" },
    BuildConfig.DEBUG,
) {
    val api: ApiService by lazy { createApi(ApiService::class.java, wireGson()) }
}
