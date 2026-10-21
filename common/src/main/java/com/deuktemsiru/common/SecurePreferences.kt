package com.deuktemsiru.common

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

fun securePrefs(context: Context, name: String): SharedPreferences {
    val appContext = context.applicationContext
    val legacy = appContext.getSharedPreferences(name, Context.MODE_PRIVATE)
    if (legacy.contains("accessToken") || legacy.contains("refreshToken")) {
        check(appContext.deleteSharedPreferences(name)) { "암호화되지 않은 기존 세션을 삭제하지 못했습니다." }
    }
    val masterKey = MasterKey.Builder(appContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    return EncryptedSharedPreferences.create(
        appContext,
        name,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
}
