package com.example.deuktemsiru_seller.util

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/** Pads the view by the system bars so content is not drawn under status/nav bars. */
fun View.applySystemBarInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
        insets
    }
}
