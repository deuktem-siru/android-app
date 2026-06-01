package com.example.deuktemsiru_seller.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deuktemsiru_seller.domain.model.SalesPeriod
import com.example.deuktemsiru_seller.domain.sales.SalesModel
import com.example.deuktemsiru_seller.domain.sales.SalesSummary
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SalesState(
    val period: SalesPeriod = SalesPeriod.Day,
    val offset: Int = 0,
    val pieChart: Boolean = false,
    val loading: Boolean = false,
    val summary: SalesSummary? = null,
    val error: Throwable? = null,
)

class SalesViewModel(private val sales: SalesModel) : ViewModel() {
    private val _state = MutableStateFlow(SalesState())
    val state = _state.asStateFlow()
    private var loadJob: Job? = null

    fun selectPeriod(period: SalesPeriod) {
        _state.value = _state.value.copy(period = period, offset = 0)
        load()
    }
    fun move(delta: Int) {
        _state.value = _state.value.copy(offset = (_state.value.offset + delta).coerceAtLeast(0))
        load()
    }
    fun toggleChart() { _state.value = _state.value.copy(pieChart = !_state.value.pieChart) }
    fun errorShown() { _state.value = _state.value.copy(error = null) }
    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch { refresh() }
    }
    suspend fun refresh() {
        val selection = _state.value
        _state.value = selection.copy(loading = true, error = null)
        try {
            val summary = sales.load(selection.period, selection.offset)
            _state.value = _state.value.copy(summary = summary, loading = false)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            _state.value = _state.value.copy(error = error, loading = false)
        }
    }
}
