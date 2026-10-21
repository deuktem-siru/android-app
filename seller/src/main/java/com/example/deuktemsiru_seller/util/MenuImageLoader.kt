package com.example.deuktemsiru_seller.util

import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.URL

fun ImageView.loadMenuImage(
    imageUrl: String?,
    fallbackEmoji: TextView,
    lifecycleOwner: LifecycleOwner,
) {
    val resolvedUrl = imageUrl?.trim()?.takeIf { it.isNotBlank() }?.let(::resolveImageUrl)
    tag = resolvedUrl

    visibility = View.GONE
    fallbackEmoji.visibility = View.VISIBLE
    setImageDrawable(null)
    if (resolvedUrl == null) return

    lifecycleOwner.lifecycleScope.launch {
        val bitmap = withContext(Dispatchers.IO) {
            runCatching {
                URL(resolvedUrl).openConnection().apply {
                    connectTimeout = 5_000
                    readTimeout = 5_000
                }.getInputStream().use(BitmapFactory::decodeStream)
            }.getOrNull()
        }

        if (tag != resolvedUrl) return@launch
        if (bitmap == null) {
            visibility = View.GONE
            fallbackEmoji.visibility = View.VISIBLE
        } else {
            setImageBitmap(bitmap)
            fallbackEmoji.visibility = View.GONE
            visibility = View.VISIBLE
        }
    }
}

private fun resolveImageUrl(imageUrl: String): String {
    val uri = URI(imageUrl)
    return if (uri.isAbsolute) {
        imageUrl
    } else {
        URI(RetrofitClient.BASE_URL).resolve(imageUrl).toString()
    }
}
