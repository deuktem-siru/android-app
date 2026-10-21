package com.example.deuktemsiru_buyer.data.remote

import com.example.deuktemsiru_buyer.domain.model.*

import com.deuktemsiru.common.PushRegistration

object Push : PushRegistration(
    canRegister = { !RetrofitClient.accessToken.isNullOrBlank() },
    register = { RetrofitClient.api.registerFcmToken(FcmTokenRequest(it)) },
)
