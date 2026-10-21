package com.example.deuktemsiru_seller.data.repository

import android.content.Context

class NotificationPreferences(context: Context) {
    private val settings = context.getSharedPreferences("notif_settings", Context.MODE_PRIVATE)
    private val phrases = context.getSharedPreferences("notification_phrases", Context.MODE_PRIVATE)

    fun isEnabled(key: String): Boolean = settings.getBoolean(key, key in setOf("new_order", "pickup_complete"))

    fun setEnabled(key: String, enabled: Boolean) {
        settings.edit().putBoolean(key, enabled).apply()
    }

    fun loadPhrases(): MutableList<String> =
        phrases.getString("phrases", null)?.split("|")?.filter { it.isNotBlank() }?.toMutableList()
            ?: mutableListOf("마감 특가!", "오늘만 할인", "선착순!")

    fun savePhrases(values: List<String>) {
        phrases.edit().putString("phrases", values.joinToString("|")).apply()
    }
}
