package com.deuktemsiru.common

import android.content.Context
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient

fun Throwable.isKakaoLoginCancelled() = this is ClientError && reason == ClientErrorCause.Cancelled

fun Context.loginWithKakao(onToken: (String) -> Unit, onError: (Throwable) -> Unit) {
    val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        when {
            error != null -> onError(error)
            token != null -> onToken(token.accessToken)
        }
    }
    val client = UserApiClient.instance
    if (client.isKakaoTalkLoginAvailable(this)) {
        client.loginWithKakaoTalk(this) { token, error ->
            if (error != null && !error.isKakaoLoginCancelled()) {
                client.loginWithKakaoAccount(this, callback = callback)
            } else callback(token, error)
        }
    } else client.loginWithKakaoAccount(this, callback = callback)
}
