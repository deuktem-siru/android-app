package com.example.deuktemsiru_buyer.presentation.viewmodel

import com.example.deuktemsiru_buyer.domain.model.Store
import com.example.deuktemsiru_buyer.domain.repository.StoreRepository
import com.example.deuktemsiru_buyer.domain.model.AppError
import com.example.deuktemsiru_buyer.domain.model.Result
import com.example.deuktemsiru_buyer.domain.model.filterStores
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HomeState(
    val stores: List<Store> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val authError: Boolean = false,
    val selectedCategory: String = "전체",
    val searchQuery: String = "",
) {
    val filteredStores: List<Store> get() = stores.filterStores(selectedCategory, searchQuery)
}

class HomeViewModel(
    private val repository: StoreRepository,
) : androidx.lifecycle.ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch { loadStores(showLoading = false) }
    }

    fun toggle(store: Store) { viewModelScope.launch { toggleWishlist(store) } }

    suspend fun loadStores(showLoading: Boolean = true) {
        _uiState.update { it.copy(isLoading = showLoading, error = null) }
        when (val result = repository.getStores()) {
            is Result.Success -> _uiState.update { it.copy(stores = result.data, isLoading = false) }
            is Result.Error -> {
                val isAuth = result.error == AppError.AUTH_ERROR
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        authError = isAuth,
                        error = if (isAuth) null else errorMessage(result.error, result.httpCode),
                    )
                }
            }
        }
    }

    fun selectCategory(category: String) = _uiState.update { it.copy(selectedCategory = category) }

    fun updateSearch(query: String) = _uiState.update { it.copy(searchQuery = query) }

    suspend fun toggleWishlist(store: Store) {
        val optimisticValue = !store.isWishlisted
        updateWishlistState(store.id, optimisticValue)
        when (val result = repository.toggleWishlist(store.id.toLong())) {
            is Result.Success -> {
                updateWishlistState(store.id, result.data)
            }
            is Result.Error -> {
                updateWishlistState(store.id, store.isWishlisted)
                _uiState.update { it.copy(error = "찜 처리 중 오류가 발생했어요.") }
            }
        }
    }

    private fun updateWishlistState(storeId: Long, isWishlisted: Boolean) = _uiState.update { state ->
        state.copy(
            stores = state.stores.map {
                if (it.id == storeId) it.copy(isWishlisted = isWishlisted) else it
            },
        )
    }

    fun errorShown() {
        _uiState.update { it.copy(error = null) }
    }

    fun authErrorHandled() {
        _uiState.update { it.copy(authError = false) }
    }

    // AUTH_ERROR never reaches here — callers surface it through the authError flag instead.
    private fun errorMessage(error: AppError, httpCode: Int): String = when (error) {
        AppError.NOT_FOUND -> "데이터를 찾을 수 없어요."
        AppError.SERVER_ERROR -> "서버에 일시적인 문제가 있어요. 잠시 후 다시 시도해주세요."
        AppError.NETWORK_ERROR -> "네트워크에 연결할 수 없어요."
        AppError.UNKNOWN, AppError.AUTH_ERROR -> "네트워크 오류가 발생했어요. ($httpCode)"
    }
}
