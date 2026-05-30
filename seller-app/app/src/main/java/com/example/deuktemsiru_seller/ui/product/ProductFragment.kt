package com.example.deuktemsiru_seller.ui.product

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.R
import com.example.deuktemsiru_seller.data.SessionManager
import com.example.deuktemsiru_seller.databinding.FragmentProductBinding
import com.example.deuktemsiru_seller.databinding.ItemSaleItemBinding
import com.example.deuktemsiru_seller.network.RetrofitClient
import com.example.deuktemsiru_seller.network.SaleItemApiResponse
import com.example.deuktemsiru_seller.network.SaleStatus
import com.example.deuktemsiru_seller.network.UpdateSaleItemRequest
import com.example.deuktemsiru_seller.network.UpdateSaleStatusRequest
import com.example.deuktemsiru_seller.network.badgeStyle
import com.example.deuktemsiru_seller.util.emptyTextView
import com.example.deuktemsiru_seller.util.failureMessage
import com.example.deuktemsiru_seller.util.handleSellerAuthFailure
import com.example.deuktemsiru_seller.util.renderChildren
import com.example.deuktemsiru_seller.util.toast
import com.example.deuktemsiru_seller.util.toWon
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ProductFragment : Fragment() {

    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var session: SessionManager

    private companion object {
        const val TAG = "ProductFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())
        binding.btnAddProduct.setOnClickListener {
            startActivity(Intent(requireContext(), ProductListingActivity::class.java))
        }
        loadSaleItems()
    }

    override fun onResume() {
        super.onResume()
        loadSaleItems()
    }

    private fun loadSaleItems() {
        if (!session.isLoggedIn()) return
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                val items = RetrofitClient.api.getSaleItems(RetrofitClient.memberId).data ?: emptyList()
                renderItems(items)
            }.onFailure { error ->
                if (handleSellerAuthFailure(error, session)) return@onFailure
                if (error is HttpException && error.code() == 404) {
                    renderItems(emptyList())
                    return@onFailure
                }
                Log.e(TAG, "Failed to load sale items", error)
                toast("상품 목록을 불러올 수 없어요")
            }
        }
    }

    private fun renderItems(items: List<SaleItemApiResponse>) {
        binding.saleItemsContainer.renderChildren(
            items = items,
            emptyView = ::emptyView,
        ) { item ->
            val itemBinding = ItemSaleItemBinding.inflate(layoutInflater, binding.saleItemsContainer, false)
            itemBinding.tvItemEmoji.text = item.emoji
            itemBinding.tvItemName.text = item.name
            itemBinding.tvItemDetail.text =
                "${item.discountedPrice.toWon()} · 잔여 ${item.remainingItems}/${item.totalItems}개 · ${item.displayPickupTime}"

            val badge = item.saleStatus.badgeStyle(item.status)
            itemBinding.tvItemStatus.text = badge.text
            itemBinding.tvItemStatus.setTextColor(requireContext().getColor(badge.textColorRes))
            itemBinding.tvItemStatus.setBackgroundResource(badge.backgroundRes)

            fun styleButton(btn: TextView, isActive: Boolean) {
                val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)
                if (isActive) {
                    btn.setBackgroundResource(R.drawable.bg_button_primary)
                    btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                } else {
                    btn.setBackgroundResource(R.drawable.bg_button_outline)
                    btn.setTextColor(primaryColor)
                }
            }

            itemBinding.btnEdit.setOnClickListener { showEditDialog(item) }
            itemBinding.btnStatusAvailable.setOnClickListener { updateStatus(item.id, SaleStatus.Available.apiValue) }
            itemBinding.btnStatusSoldout.setOnClickListener { updateStatus(item.id, SaleStatus.Paused.apiValue) }
            val isFinal = item.saleStatus.isFinal
            val isLegacyPaused = item.saleStatus == SaleStatus.SoldOut && item.remainingItems > 0
            val isStatusChangeable = !isFinal && (item.saleStatus != SaleStatus.SoldOut || isLegacyPaused)
            itemBinding.btnCancel.text = if (isFinal) "삭제" else "취소"
            itemBinding.btnCancel.setOnClickListener { confirmCancel(item) }
            styleButton(itemBinding.btnStatusAvailable, item.saleStatus == SaleStatus.Available)
            styleButton(itemBinding.btnStatusSoldout, item.saleStatus == SaleStatus.Paused || isLegacyPaused)
            styleButton(itemBinding.btnCancel, false)
            itemBinding.btnStatusAvailable.isEnabled = isStatusChangeable
            itemBinding.btnStatusSoldout.isEnabled = isStatusChangeable
            itemBinding.btnCancel.isEnabled = true
            itemBinding.btnStatusAvailable.alpha = if (isStatusChangeable) 1f else 0.35f
            itemBinding.btnStatusSoldout.alpha = if (isStatusChangeable) 1f else 0.35f
            itemBinding.btnCancel.alpha = 1f

            val isUnavailable = item.saleStatus in setOf(SaleStatus.Paused, SaleStatus.SoldOut)
            itemBinding.tvItemEmoji.alpha = if (isUnavailable) 0.35f else 1f
            itemBinding.tvItemName.alpha = if (isUnavailable) 0.55f else 1f
            itemBinding.tvItemDetail.alpha = if (isUnavailable) 0.45f else 1f
            itemBinding.tvItemName.paintFlags = if (isUnavailable) {
                itemBinding.tvItemName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                itemBinding.tvItemName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            itemBinding.root
        }
    }

    private fun updateStatus(itemId: Long, status: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                RetrofitClient.api.updateSaleStatus(itemId, RetrofitClient.memberId, UpdateSaleStatusRequest(status))
                loadSaleItems()
            }.onFailure { error ->
                if (handleSellerAuthFailure(error, session)) return@onFailure
                Log.e(TAG, "Failed to update sale status. itemId=$itemId status=$status", error)
                toast(error.failureMessage("상태 변경"))
            }
        }
    }


    private fun showEditDialog(item: SaleItemApiResponse) {
        val dialogView = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
        }
        val etPrice = android.widget.EditText(requireContext()).apply {
            hint = "할인가 (원)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(item.discountedPrice.toString())
        }
        val etQty = android.widget.EditText(requireContext()).apply {
            hint = "잔여 수량"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(item.remainingItems.toString())
        }
        val tvPickup = android.widget.TextView(requireContext()).apply {
            text = "픽업시간: ${item.displayPickupTime}"
            textSize = 13f
            setTextColor(requireContext().getColor(R.color.text_sub))
            setPadding(0, 12, 0, 4)
        }
        dialogView.addView(etPrice)
        dialogView.addView(etQty)
        dialogView.addView(tvPickup)

        AlertDialog.Builder(requireContext())
            .setTitle("상품 수정")
            .setView(dialogView)
            .setPositiveButton("저장") { _, _ ->
                val newPrice = etPrice.text?.toString()?.toIntOrNull()
                val newQty = etQty.text?.toString()?.toIntOrNull()
                if (newPrice == null || newPrice <= 0) {
                    toast("올바른 가격을 입력해주세요")
                    return@setPositiveButton
                }
                if (newQty == null || newQty < 0) {
                    toast("올바른 수량을 입력해주세요")
                    return@setPositiveButton
                }
                viewLifecycleOwner.lifecycleScope.launch {
                    runCatching {
                        RetrofitClient.api.updateSaleItem(
                            item.id,
                            RetrofitClient.memberId,
                            UpdateSaleItemRequest(discountPrice = newPrice, quantityRemaining = newQty),
                        )
                        toast("수정됐어요")
                        loadSaleItems()
                    }.onFailure {
                        toast("수정에 실패했어요")
                    }
                }
            }
            .setNegativeButton("닫기", null)
            .show()
    }

    private fun confirmCancel(item: SaleItemApiResponse) {
        val isFinal = item.saleStatus.isFinal
        val actionLabel = if (isFinal) "삭제" else "취소"
        AlertDialog.Builder(requireContext())
            .setTitle("상품 $actionLabel")
            .setMessage("${item.name} 상품을 ${actionLabel}할까요?")
            .setPositiveButton("${actionLabel}하기") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    runCatching {
                        if (isFinal) {
                            RetrofitClient.api.cancelSaleItem(item.id, RetrofitClient.memberId)
                        } else {
                            RetrofitClient.api.updateSaleStatus(
                                item.id,
                                RetrofitClient.memberId,
                                UpdateSaleStatusRequest(SaleStatus.Expired.apiValue),
                            )
                        }
                        toast("상품이 ${actionLabel}됐어요")
                        loadSaleItems()
                    }.onFailure { error ->
                        if (handleSellerAuthFailure(error, session)) return@onFailure
                        Log.e(TAG, "Failed to cancel/delete sale item. itemId=${item.id}", error)
                        toast(error.failureMessage(actionLabel))
                    }
                }
            }
            .setNegativeButton("닫기", null)
            .show()
    }


    private fun emptyView(): View = requireContext().emptyTextView(
        message = "오늘 등록된 상품이 없어요\n아래 '+ 상품 등록' 버튼을 눌러 등록해보세요",
        verticalPaddingDp = 60,
        centered = true,
    ).apply {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
