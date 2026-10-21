package com.deuktemsiru.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

open class PushRegistration(
    private val canRegister: () -> Boolean,
    private val register: suspend (String) -> Unit,
) {
    val CHANNEL_ID = "deuktemsiru_default"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** minSdk 29라 채널은 항상 필요하고, 같은 ID로 다시 만들면 no-op이다. */
    fun ensureChannel(context: Context) {
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "득템시루 알림", NotificationManager.IMPORTANCE_HIGH),
        )
    }

    fun registerCurrentToken() {
        // ponytail: google-services.json 없이 빌드하면 기본 FirebaseApp이 없다 → 푸시만 조용히 비활성.
        runCatching { FirebaseMessaging.getInstance() }.getOrNull()
            ?.token?.addOnSuccessListener(::registerToken)
    }

    fun registerToken(token: String?) {
        if (token.isNullOrBlank() || !canRegister()) return
        scope.launch { runCatching { register(token) } }
    }
}
