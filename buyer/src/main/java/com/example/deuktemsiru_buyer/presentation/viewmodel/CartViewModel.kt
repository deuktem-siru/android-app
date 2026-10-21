package com.example.deuktemsiru_buyer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.deuktemsiru_buyer.domain.repository.CartStore

class CartViewModel(repository: CartStore) : ViewModel(), CartStore by repository
