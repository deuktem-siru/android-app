package com.example.deuktemsiru_seller

import android.content.Intent
import com.deuktemsiru.common.showPushNotification
import com.example.deuktemsiru_seller.data.remote.Push
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class DeuktemsiruMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) = Push.registerToken(token)

    override fun onMessageReceived(message: RemoteMessage) = showPushNotification(
        message, Push.CHANNEL_ID, R.drawable.ic_bell,
        Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
    )
}
