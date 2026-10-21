package com.example.deuktemsiru_seller.network

import com.deuktemsiru.common.PushRegistration

object Push : PushRegistration(
    canRegister = { !RetrofitClient.accessToken.isNullOrBlank() && !RetrofitClient.isMockSession },
    register = { RetrofitClient.api.registerFcmToken(FcmTokenRequest(it)) },
)
