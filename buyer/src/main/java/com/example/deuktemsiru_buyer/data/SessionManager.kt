package com.example.deuktemsiru_buyer.data

import com.deuktemsiru.common.securePrefs
import android.content.Context
import android.content.SharedPreferences
import com.example.deuktemsiru_buyer.network.ApiService
import com.example.deuktemsiru_buyer.network.LoginData
import com.example.deuktemsiru_buyer.network.RetrofitClient
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SessionManager(context: Context) {
    private val prefs = securePrefs(context, "buyer_session")

    init {
        restoreToken()
        RetrofitClient.onTokenRefreshed = { newAccessToken ->
            accessToken = newAccessToken
        }
    }

    var memberId: Long     by prefs.pref("memberId",    getter = { getLong(it, -1L) },      setter = { k, v -> putLong(k, v) })
    var nickname: String   by prefs.pref("nickname",    getter = { getString(it, "") ?: "" }, setter = { k, v -> putString(k, v) })
    var lastOrderId: Long  by prefs.pref("lastOrderId", getter = { getLong(it, -1L) },       setter = { k, v -> putLong(k, v) })
    var isSiruLinked: Boolean by prefs.pref("isSiruLinked", getter = { getBoolean(it, false) }, setter = { k, v -> putBoolean(k, v) })
    var siruBalance: Int   by prefs.pref("siruBalance", getter = { getInt(it, 0) },           setter = { k, v -> putInt(k, v) })

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

    fun saveLogin(loginData: LoginData) {
        memberId = loginData.member.memberId
        nickname = loginData.member.nickname
        accessToken = loginData.accessToken
        refreshToken = loginData.refreshToken
    }

    suspend fun syncMe(api: ApiService): Boolean =
        runCatching { api.getMe().data }
            .getOrNull()
            ?.let {
                isSiruLinked = it.isSiruLinked
                siruBalance = it.siruBalance
                true
            } ?: false

    fun restoreToken() {
        RetrofitClient.accessToken = accessToken.takeIf { it.isNotBlank() }
        RetrofitClient.refreshToken = refreshToken.takeIf { it.isNotBlank() }
    }

    fun clear() {
        prefs.edit().clear().apply()
        RetrofitClient.accessToken = null
        RetrofitClient.refreshToken = null
    }
}

// --- SharedPreferences generic property delegate ---

private fun <T> SharedPreferences.pref(
    key: String,
    getter: SharedPreferences.(String) -> T,
    setter: SharedPreferences.Editor.(String, T) -> SharedPreferences.Editor,
): ReadWriteProperty<Any?, T> = object : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = getter(key)
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        edit().setter(key, value).apply()
    }
}

val Context.appSession: SessionManager
    get() = (applicationContext as com.example.deuktemsiru_buyer.DeuktemsiruBuyerApp).session
