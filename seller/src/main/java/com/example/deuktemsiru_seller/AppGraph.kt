package com.example.deuktemsiru_seller

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.deuktemsiru_seller.data.remote.RetrofitClient
import com.example.deuktemsiru_seller.data.repository.RemoteAppRepository
import com.example.deuktemsiru_seller.domain.sales.SalesModel
import com.example.deuktemsiru_seller.presentation.viewmodel.AppViewModel
import com.example.deuktemsiru_seller.presentation.viewmodel.SalesViewModel

object AppGraph {
    val repository = RemoteAppRepository { RetrofitClient.api }
    private val factory = viewModelFactory {
        initializer { AppViewModel(repository) }
        initializer { SalesViewModel(SalesModel(repository) { RetrofitClient.memberId }) }
    }

    fun sales(owner: ViewModelStoreOwner) = ViewModelProvider(owner, factory)[SalesViewModel::class.java]
    fun model(owner: ViewModelStoreOwner) = ViewModelProvider(owner, factory)[AppViewModel::class.java]
}
