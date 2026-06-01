package com.example.deuktemsiru_buyer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.deuktemsiru_buyer.domain.payment.PaymentModel
import com.example.deuktemsiru_buyer.domain.payment.PaymentDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PaymentViewModel(private val payment: PaymentModel) : ViewModel() {
    private val _submitting = MutableStateFlow(false)
    val submitting = _submitting.asStateFlow()

    suspend fun submitOrder(draft: PaymentDraft, pickupTime: String?) {
        check(!_submitting.value) { "주문을 처리하고 있어요." }
        _submitting.value = true
        try { payment.submitOrder(draft, pickupTime) }
        finally { _submitting.value = false }
    }
}
