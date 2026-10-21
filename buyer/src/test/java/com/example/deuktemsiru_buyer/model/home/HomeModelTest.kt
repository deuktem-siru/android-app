package com.example.deuktemsiru_buyer.model.home

import com.example.deuktemsiru_buyer.presentation.viewmodel.*

import com.example.deuktemsiru_buyer.data.repository.RemoteStoreRepository
import com.example.deuktemsiru_buyer.domain.model.ApiResponse
import com.example.deuktemsiru_buyer.domain.repository.AppRepository
import com.example.deuktemsiru_buyer.domain.model.StoreListItemResponse
import com.example.deuktemsiru_buyer.domain.model.StoreListResponse
import java.lang.reflect.Proxy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class HomeViewModelTest {
    private fun api(call: (String) -> Any?): AppRepository = Proxy.newProxyInstance(
        AppRepository::class.java.classLoader, arrayOf(AppRepository::class.java),
    ) { _, method, _ -> call(method.name) } as AppRepository

    @Test fun `search state filters stores and failed wishlist updates roll back`() = runBlocking {
        val response = ApiResponse(200, "", StoreListResponse(listOf(
            StoreListItemResponse(1, "빵집", null, 100, "BAKERY", 0.0, 0, 1),
        ), false))
        val model = HomeViewModel(RemoteStoreRepository(api { method ->
            if (method == "getStores") response else throw IllegalStateException("offline")
        }))
        model.loadStores()
        model.selectCategory("베이커리")
        model.updateSearch("빵")
        assertEquals(1, model.uiState.value.filteredStores.size)
        model.toggleWishlist(model.uiState.value.stores.single())
        assertFalse(model.uiState.value.stores.single().isWishlisted)
        assertNotNull(model.uiState.value.error)
        model.errorShown()
        assertNull(model.uiState.value.error)
        model.updateSearch("없는 가게")
        assertTrue(model.uiState.value.filteredStores.isEmpty())
    }

    @Test fun `destroyed controller work propagates cancellation`() = runBlocking {
        val model = HomeViewModel(RemoteStoreRepository(api { throw CancellationException("view destroyed") }))
        try {
            model.loadStores()
            fail("Cancellation must propagate")
        } catch (_: CancellationException) {
            assertNull(model.uiState.value.error)
        }
    }
}
