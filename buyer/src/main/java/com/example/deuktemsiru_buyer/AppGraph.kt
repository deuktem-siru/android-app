package com.example.deuktemsiru_buyer

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.deuktemsiru_buyer.data.remote.RetrofitClient
import com.example.deuktemsiru_buyer.data.repository.CartRepository
import com.example.deuktemsiru_buyer.data.repository.RemoteAppRepository
import com.example.deuktemsiru_buyer.data.repository.RemoteStoreRepository
import com.example.deuktemsiru_buyer.data.repository.SessionManager
import com.example.deuktemsiru_buyer.domain.payment.PaymentModel
import com.example.deuktemsiru_buyer.presentation.viewmodel.AppViewModel
import com.example.deuktemsiru_buyer.presentation.viewmodel.CartViewModel
import com.example.deuktemsiru_buyer.presentation.viewmodel.HomeViewModel
import com.example.deuktemsiru_buyer.presentation.viewmodel.PaymentViewModel
import com.example.deuktemsiru_buyer.presentation.viewmodel.StoreViewModel

object AppGraph {
    val repository = RemoteAppRepository { RetrofitClient.api }
    private val factory = viewModelFactory {
        initializer { AppViewModel(repository) }
        initializer { HomeViewModel(RemoteStoreRepository(repository)) }
        initializer { StoreViewModel(RemoteStoreRepository(repository)) }
    }

    fun model(owner: ViewModelStoreOwner) = ViewModelProvider(owner, factory)[AppViewModel::class.java]
    fun homeModel(owner: ViewModelStoreOwner) = ViewModelProvider(owner, factory)[HomeViewModel::class.java]
    fun stores(owner: ViewModelStoreOwner) = ViewModelProvider(owner, factory)[StoreViewModel::class.java]

    fun cart(owner: ViewModelStoreOwner, session: SessionManager) = ViewModelProvider(owner, viewModelFactory {
        initializer { CartViewModel(CartRepository(repository, session)) }
    })[CartViewModel::class.java]

    fun payment(owner: ViewModelStoreOwner, session: SessionManager) = ViewModelProvider(owner, viewModelFactory {
        initializer { PaymentViewModel(PaymentModel(repository, session, CartRepository(repository, session))) }
    })[PaymentViewModel::class.java]
}
