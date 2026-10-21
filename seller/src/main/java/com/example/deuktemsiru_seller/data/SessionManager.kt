package com.example.deuktemsiru_seller.data

import com.deuktemsiru.common.securePrefs
import android.content.Context
import android.util.Log
import com.example.deuktemsiru_seller.network.RetrofitClient
import com.kakao.sdk.user.UserApiClient

class SessionManager(context: Context) {
    private val prefs = securePrefs(context, "seller_session")

    init {
        restoreToken()
        RetrofitClient.onTokenRefreshed = { newAccessToken ->
            accessToken = newAccessToken
        }
    }

    var isMockSession: Boolean
        get() = prefs.getBoolean("isMockSession", false)
        set(value) {
            prefs.edit().putBoolean("isMockSession", value).apply()
            RetrofitClient.isMockSession = value
        }

    var memberId: Long
        get() = prefs.getLong("memberId", -1L)
        set(value) {
            prefs.edit().putLong("memberId", value).apply()
            RetrofitClient.memberId = value
        }

    var nickname: String
        get() = prefs.getString("nickname", "") ?: ""
        set(value) { prefs.edit().putString("nickname", value).apply() }

    var accessToken: String
        get() = prefs.getString("accessToken", "") ?: ""
        set(value) {
            prefs.edit().putString("accessToken", value).apply()
            RetrofitClient.accessToken = value.takeIf { it.isNotBlank() }
        }

    var refreshToken: String
        get() = prefs.getString("refreshToken", "") ?: ""
        set(value) {
            prefs.edit().putString("refreshToken", value).apply()
            RetrofitClient.refreshToken = value.takeIf { it.isNotBlank() }
        }

    fun isLoggedIn() = memberId > 0L && accessToken.isNotBlank()

    fun restoreToken() {
        RetrofitClient.accessToken = accessToken.takeIf { it.isNotBlank() }
        RetrofitClient.refreshToken = refreshToken.takeIf { it.isNotBlank() }
        RetrofitClient.memberId = memberId
        RetrofitClient.isMockSession = isMockSession
    }

    fun clear() {
        // 카카오 세션까지 끊어야 다음 로그인에서 계정을 다시 고를 수 있습니다.
        UserApiClient.instance.logout { error ->
            if (error != null) Log.w("SessionManager", "카카오 로그아웃 실패", error)
        }
        prefs.edit().clear().apply()
        RetrofitClient.accessToken = null
        RetrofitClient.refreshToken = null
        RetrofitClient.memberId = -1L
        RetrofitClient.isMockSession = false
    }
}

val Context.appSession: SessionManager
    get() = (applicationContext as com.example.deuktemsiru_seller.DeuktemsiruSellerApp).session
