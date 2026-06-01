package com.example.deuktemsiru_seller.data.remote

import com.example.deuktemsiru_seller.domain.model.*

import com.deuktemsiru.common.PushRegistration

object Push : PushRegistration(
    canRegister = { !RetrofitClient.accessToken.isNullOrBlank() && !RetrofitClient.isMockSession },
    register = { RetrofitClient.api.registerFcmToken(FcmTokenRequest(it)) },
)
