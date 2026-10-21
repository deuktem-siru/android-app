package com.example.deuktemsiru_buyer.network

import com.deuktemsiru.common.PushRegistration

object Push : PushRegistration(
    canRegister = { !RetrofitClient.accessToken.isNullOrBlank() },
    register = { RetrofitClient.api.registerFcmToken(FcmTokenRequest(it)) },
)
