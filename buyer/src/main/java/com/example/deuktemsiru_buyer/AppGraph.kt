package com.example.deuktemsiru_buyer

import com.example.deuktemsiru_buyer.data.repository.RemoteAppRepository
import com.example.deuktemsiru_buyer.data.remote.RetrofitClient
import com.example.deuktemsiru_buyer.presentation.viewmodel.AppViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

object AppGraph {
    val repository = RemoteAppRepository { RetrofitClient.api }
    private val factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass == com.example.deuktemsiru_buyer.presentation.viewmodel.HomeViewModel::class.java) {
                return com.example.deuktemsiru_buyer.presentation.viewmodel.HomeViewModel(com.example.deuktemsiru_buyer.data.repository.RemoteStoreRepository(repository)) as T
            }
            if (modelClass == com.example.deuktemsiru_buyer.presentation.viewmodel.StoreViewModel::class.java) {
                return com.example.deuktemsiru_buyer.presentation.viewmodel.StoreViewModel(com.example.deuktemsiru_buyer.data.repository.RemoteStoreRepository(repository)) as T
            }
            require(modelClass == AppViewModel::class.java)
            return AppViewModel(repository) as T
        }
    }
    fun homeModel(owner: ViewModelStoreOwner) = ViewModelProvider(owner, factory)[com.example.deuktemsiru_buyer.presentation.viewmodel.HomeViewModel::class.java]
    fun stores(owner: ViewModelStoreOwner) = ViewModelProvider(owner, factory)[com.example.deuktemsiru_buyer.presentation.viewmodel.StoreViewModel::class.java]
    fun cart(owner: ViewModelStoreOwner, session: com.example.deuktemsiru_buyer.data.repository.SessionManager) =
        scoped(owner, com.example.deuktemsiru_buyer.presentation.viewmodel.CartViewModel::class.java) {
            com.example.deuktemsiru_buyer.presentation.viewmodel.CartViewModel(com.example.deuktemsiru_buyer.data.repository.CartRepository(repository, session))
        }
    fun payment(owner: ViewModelStoreOwner, session: com.example.deuktemsiru_buyer.data.repository.SessionManager) =
        scoped(owner, com.example.deuktemsiru_buyer.presentation.viewmodel.PaymentViewModel::class.java) {
            com.example.deuktemsiru_buyer.presentation.viewmodel.PaymentViewModel(com.example.deuktemsiru_buyer.domain.payment.PaymentModel(repository, session, com.example.deuktemsiru_buyer.data.repository.CartRepository(repository, session)))
        }
    private fun <T : ViewModel> scoped(owner: ViewModelStoreOwner, type: Class<T>, create: () -> T): T =
        ViewModelProvider(owner, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <M : ViewModel> create(modelClass: Class<M>): M = create() as M
        })[type]
    fun model(owner: ViewModelStoreOwner): AppViewModel = ViewModelProvider(owner, factory)[AppViewModel::class.java]
}
