package com.example.deuktemsiru_seller.presentation.view.store

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.domain.model.MenuItemApiResponse
import com.example.deuktemsiru_seller.util.toast
import kotlinx.coroutines.launch

fun Context.confirmDeleteMenu(
    menu: MenuItemApiResponse,
    owner: LifecycleOwner,
    delete: suspend () -> Unit,
    onDeleted: () -> Unit,
    onFailure: (Throwable) -> Unit,
) {
    AlertDialog.Builder(this)
        .setTitle("메뉴 삭제")
        .setMessage("${menu.name}을(를) 삭제할까요?")
        .setPositiveButton("삭제") { _, _ ->
            owner.lifecycleScope.launch {
                runCatching { delete() }
                    .onSuccess {
                        toast("메뉴가 삭제됐어요")
                        onDeleted()
                    }.onFailure(onFailure)
            }
        }
        .setNegativeButton("취소", null)
        .show()
}
