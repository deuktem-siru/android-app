package com.deuktemsiru.common

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage

fun Context.showPushNotification(message: RemoteMessage, channelId: String, icon: Int, intent: Intent) {
    val notification = NotificationCompat.Builder(this, channelId)
        .setSmallIcon(icon)
        .setContentTitle(message.notification?.title ?: message.data["title"] ?: "득템시루")
        .setContentText(message.notification?.body ?: message.data["body"] ?: "")
        .setAutoCancel(true)
        .setContentIntent(
            PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            ),
        )
        .build()
    getSystemService(NotificationManager::class.java)
        ?.notify(System.currentTimeMillis().toInt(), notification)
}
