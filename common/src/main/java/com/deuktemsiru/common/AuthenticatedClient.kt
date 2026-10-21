package com.deuktemsiru.common

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.net.URI

open class AuthenticatedClient(
    val BASE_URL: String,
    isDebug: Boolean,
    private val isMockSession: () -> Boolean = { false },
) {
    @Volatile var accessToken: String? = null
    @Volatile var refreshToken: String? = null
    var onTokenRefreshed: ((String) -> Unit)? = null
    var onSessionExpired: (() -> Unit)? = null

    init { requireSecureBaseUrl(BASE_URL, isDebug) }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                accessToken?.takeIf { it.isNotBlank() }?.let {
                    requestBuilder.addHeader("Authorization", "Bearer $it")
                }
                chain.proceed(requestBuilder.build())
            }
            .authenticator(TokenRefreshAuthenticator())
            .build()
    }

    protected fun <T> createApi(service: Class<T>): T = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(service)

    private inner class TokenRefreshAuthenticator : Authenticator {
        override fun authenticate(route: Route?, response: Response): Request? {
            if (response.request.header("Authorization").isNullOrBlank()) return null
            if (responseCount(response) >= 2 || isMockSession()) return null

            val savedRefreshToken = refreshToken?.takeIf { it.isNotBlank() } ?: return null
            val newAccessToken = synchronized(this) {
                val currentRequestToken = response.request.header("Authorization")?.removePrefix("Bearer ")
                if (!accessToken.isNullOrBlank() && accessToken != currentRequestToken) {
                    accessToken
                } else {
                    refreshTokenSync(savedRefreshToken)?.also {
                        accessToken = it
                        onTokenRefreshed?.invoke(it)
                    }
                }
            } ?: return null

            return response.request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        }

        private fun refreshTokenSync(refreshToken: String): String? = try {
            val url = URI(BASE_URL).resolve("api/v1/auth/refresh").toURL()
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 10_000
                readTimeout = 10_000
            }
            try {
                conn.outputStream.use { it.write(Gson().toJson(mapOf("refreshToken" to refreshToken)).toByteArray()) }
                if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                    if (isDefinitiveRefreshFailure(conn.responseCode)) {
                        accessToken = null
                        this@AuthenticatedClient.refreshToken = null
                        onSessionExpired?.invoke()
                    }
                    null
                } else {
                    val type = TypeToken.getParameterized(ApiResponse::class.java, TokenData::class.java).type
                    Gson().fromJson<ApiResponse<TokenData>?>(conn.inputStream.bufferedReader().use { it.readText() }, type)
                        ?.data?.accessToken
                }
            } finally { conn.disconnect() }
        } catch (e: Exception) {
            Log.e("TokenRefresh", "Token refresh failed", e)
            null
        }

        private fun responseCount(response: Response) =
            generateSequence(response) { it.priorResponse }.count()
    }
}

fun requireSecureBaseUrl(baseUrl: String, isDebug: Boolean) {
    require(isDebug || URI(baseUrl).scheme.equals("https", ignoreCase = true)) {
        "릴리스 빌드는 HTTPS 백엔드만 사용할 수 있습니다. local.properties의 BACKEND_BASE_URL을 확인하세요."
    }
}

fun isDefinitiveRefreshFailure(statusCode: Int): Boolean =
    statusCode == HttpURLConnection.HTTP_BAD_REQUEST || statusCode == HttpURLConnection.HTTP_UNAUTHORIZED
