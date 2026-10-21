package com.example.deuktemsiru_seller

import com.example.deuktemsiru_seller.data.repository.RemoteAppRepository
import com.example.deuktemsiru_seller.data.remote.RetrofitClient
import com.example.deuktemsiru_seller.presentation.viewmodel.AppViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

object AppGraph {
    val repository = RemoteAppRepository { RetrofitClient.api }
    private val factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass == com.example.deuktemsiru_seller.presentation.viewmodel.SalesViewModel::class.java) {
                return com.example.deuktemsiru_seller.presentation.viewmodel.SalesViewModel(com.example.deuktemsiru_seller.domain.sales.SalesModel(repository) { RetrofitClient.memberId }) as T
            }
            require(modelClass == AppViewModel::class.java)
            return AppViewModel(repository) as T
        }
    }
    fun sales(owner: ViewModelStoreOwner) = ViewModelProvider(owner, factory)[com.example.deuktemsiru_seller.presentation.viewmodel.SalesViewModel::class.java]
    fun model(owner: ViewModelStoreOwner): AppViewModel = ViewModelProvider(owner, factory)[AppViewModel::class.java]
}
