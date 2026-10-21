package com.example.deuktemsiru_seller.util

import com.example.deuktemsiru_seller.domain.product.PickupTimeState
import com.example.deuktemsiru_seller.domain.product.discountPrice
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import com.example.deuktemsiru_seller.R

fun View.bindDiscountPrice(
    originalPrice: Int,
    initialRate: Int,
    onRateChanged: (Int) -> Unit,
    onPriceChanged: (Int) -> Unit,
) {
    val input = findViewById<EditText>(R.id.et_discount_rate)
    val presets = discountPresetViews(this)
    if (initialRate > 0) input.setText(initialRate.toString())
    presets.forEach { (rate, preset) ->
        preset.text = "$rate%"
        preset.setOnClickListener {
            input.setText(rate.toString())
            input.setSelection(input.text?.length ?: 0)
        }
    }
    input.doOnTextChanged { text, _, _, _ ->
        val rate = text.toString().toIntOrNull() ?: 0
        onRateChanged(rate)
        onPriceChanged(discountPrice(originalPrice, rate))
        refreshDiscountPresets(presets, rate)
    }
    refreshDiscountPresets(presets, initialRate)
}

fun View.bindPickupTime(
    state: PickupTimeState,
    minDurationMinutes: Int,
    highlightSelection: Boolean = false,
) {
    val start = findViewById<View>(R.id.container_pickup_start)
    val end = findViewById<View>(R.id.container_pickup_end)
    val startLabel = findViewById<TextView>(R.id.tv_pickup_start)
    val endLabel = findViewById<TextView>(R.id.tv_pickup_end)
    fun refresh() {
        startLabel.text = state.startLabel
        endLabel.text = state.endLabel
    }
    fun highlight(active: View, inactive: View) {
        if (highlightSelection) {
            active.setBackgroundResource(R.drawable.bg_card_primary_border)
            inactive.setBackgroundResource(R.drawable.bg_rounded_muted)
        }
    }
    start.setOnClickListener {
        highlight(start, end)
        context.showTimePicker(state.startMinutes) {
            state.updateStart(it, minDurationMinutes)
            refresh()
        }
    }
    end.setOnClickListener {
        highlight(end, start)
        context.showTimePicker(state.endMinutes) {
            if (!state.updateEnd(it)) context.toast("마감 시간은 시작 시간보다 늦어야 해요.")
            refresh()
        }
    }
    listOf(R.id.chip_30min to 30, R.id.chip_1hour to 60, R.id.chip_2hour to 120)
        .forEach { (id, minutes) ->
            findViewById<View>(id)?.setOnClickListener {
                state.setDuration(minutes)
                highlight(end, start)
                refresh()
            }
        }
    findViewById<View>(R.id.chip_until_close)?.setOnClickListener {
        state.endMinutes = 21 * 60
        state.ensureEndAfterStart(minDurationMinutes)
        refresh()
    }
    refresh()
}
