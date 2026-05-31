package com.example.deuktemsiru_seller.ui.order

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.deuktemsiru_seller.MainActivity
import com.example.deuktemsiru_seller.R
import com.example.deuktemsiru_seller.data.SessionManager
import com.example.deuktemsiru_seller.databinding.FragmentOrderBinding
import com.example.deuktemsiru_seller.databinding.ItemOrderCompletedBinding
import com.example.deuktemsiru_seller.databinding.ItemOrderNewBinding
import com.example.deuktemsiru_seller.databinding.ItemOrderPreparingBinding
import com.example.deuktemsiru_seller.network.OrderStatus
import com.example.deuktemsiru_seller.network.OrderApiResponse
import com.example.deuktemsiru_seller.network.RetrofitClient
import com.example.deuktemsiru_seller.network.UpdateOrderStatusRequest
import com.example.deuktemsiru_seller.util.LocalNotificationHelper
import com.example.deuktemsiru_seller.ui.settings.NotificationSettingsActivity
import com.example.deuktemsiru_seller.util.emptyTextView
import com.example.deuktemsiru_seller.util.failureMessage
import com.example.deuktemsiru_seller.util.handleSellerAuthFailure
import com.example.deuktemsiru_seller.util.renderChildren
import com.example.deuktemsiru_seller.util.toast
import com.example.deuktemsiru_seller.util.toWon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.Duration
import java.time.LocalDateTime
import java.time.OffsetDateTime

class OrderFragment : Fragment() {

    private var _binding: FragmentOrderBinding? = null
    private val binding get() = _binding!!

    private var currentTab = 0
    private var allOrders: List<OrderApiResponse> = emptyList()
    private lateinit var session: SessionManager
    private var isPending = false

    private companion object {
        const val TAG = "OrderFragment"
        const val ORDER_REFRESH_INTERVAL_MS = 30_000L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())
        setupTabs()
        val mainActivity = activity as? MainActivity
        val pendingTab = mainActivity?.pendingOrderTab
        when {
            pendingTab != null -> {
                mainActivity.pendingOrderTab = null
                loadOrders(selectTabAfterLoad = pendingTab)
            }
            mainActivity?.orderCompletedRequested == true -> {
                mainActivity.orderCompletedRequested = false
                loadOrders(selectTabAfterLoad = 3)
            }
            else -> loadOrders()
        }
        binding.btnPickupVerify.setOnClickListener {
            startActivity(Intent(requireContext(), PickupVerifyActivity::class.java))
        }
        childFragmentManager.setFragmentResultListener("order_updated", viewLifecycleOwner) { _, bundle ->
            loadOrders(selectTabAfterLoad = bundle.getInt("next_tab", currentTab))
        }
        startAutoRefresh()
    }

    private fun loadOrders(selectTabAfterLoad: Int = currentTab, silent: Boolean = false) {
        if (!session.isLoggedIn()) return
        viewLifecycleOwner.lifecycleScope.launch {
            refreshOrders(selectTabAfterLoad, silent)
        }
    }

    private fun startAutoRefresh() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    delay(ORDER_REFRESH_INTERVAL_MS)
                    refreshOrders(selectTabAfterLoad = currentTab, silent = true)
                }
            }
        }
    }

    private suspend fun refreshOrders(selectTabAfterLoad: Int, silent: Boolean) {
        try {
            val newOrders = RetrofitClient.api.getOrders(RetrofitClient.memberId).data ?: emptyList()
            val hasNewPendingOrders = hasNewPendingOrders(newOrders)
            val shouldRefreshVisibleList = !silent || visibleOrderIdsChanged(newOrders)

            allOrders = newOrders
            updateTabCounts()
            if (shouldRefreshVisibleList) {
                selectTab(selectTabAfterLoad)
            }
            if (silent && hasNewPendingOrders) {
                toast("새 주문이 도착했어요.")
            }
            (activity as? MainActivity)?.refreshOrderBadge()
        } catch (e: Exception) {
            if (handleSellerAuthFailure(e, session, "다시 로그인해주세요.")) return
            if (e is HttpException && e.code() == 404) {
                allOrders = emptyList()
                updateTabCounts()
                if (!silent) selectTab(selectTabAfterLoad)
                (activity as? MainActivity)?.refreshOrderBadge()
                return
            }
            Log.e(TAG, "Failed to load orders", e)
            if (!silent) toast("주문 목록을 불러오지 못했어요.")
        }
    }

    private fun setupTabs() {
        // 픽업대기 탭은 스펙상 별도 상태 없음 → 숨김
        binding.tabPickup.visibility = View.GONE
        binding.tabNew.setOnClickListener { selectTab(0) }
        binding.tabPreparing.setOnClickListener { selectTab(1) }
        binding.tabDone.setOnClickListener { selectTab(3) }
        updateTabCounts()
    }

    private fun selectTab(index: Int) {
        currentTab = index
        val tabs = listOf(binding.tabNew, binding.tabPreparing, binding.tabPickup, binding.tabDone)
        tabs.forEachIndexed { i, tab ->
            if (i == index) {
                tab.setTextColor(requireContext().getColor(R.color.primary))
                tab.setBackgroundResource(R.drawable.bg_tab_active)
                tab.setPadding(0, 0, 0, 0)
            } else {
                tab.setTextColor(requireContext().getColor(R.color.text_muted))
                tab.background = null
            }
        }
        showTab(index)
    }

    private fun showTab(index: Int) {
        when (index) {
            0 -> showNewOrders(ordersForTab(0))
            1 -> showConfirmedOrders(ordersForTab(1))
            3 -> showCompletedOrders(ordersForTab(3))
            // index 2 = tabPickup은 UI에서 숨김 처리되어 있으므로 아무것도 하지 않음
        }
    }

    private fun statusesForTab(tabIndex: Int): List<OrderStatus> = when (tabIndex) {
        0 -> listOf(OrderStatus.Pending)
        1 -> listOf(OrderStatus.Confirmed)
        3 -> listOf(OrderStatus.PickedUp, OrderStatus.Cancelled)
        else -> emptyList()
    }

    private fun ordersForTab(tabIndex: Int, orders: List<OrderApiResponse> = allOrders): List<OrderApiResponse> =
        orders.filter { it.orderStatus in statusesForTab(tabIndex) }

    private fun updateTabCounts() {
        binding.tabNew.text = getString(R.string.tab_new_order_count, ordersForTab(0).size)
        binding.tabPreparing.text = getString(R.string.tab_preparing_count, ordersForTab(1).size)
        binding.tabDone.text = getString(R.string.tab_completed_count, ordersForTab(3).size)
    }

    private fun showNewOrders(orders: List<OrderApiResponse>) {
        renderOrders(orders, "새로운 주문이 없어요") { order ->
            val itemBinding = ItemOrderNewBinding.inflate(layoutInflater, binding.orderListContainer, false)
            itemBinding.tvOrderNumber.text = order.displayNumber()
            itemBinding.tvOrderTime.text = getString(R.string.just_arrived)
            itemBinding.tvPickupTime.text = order.pickupTime.orEmpty()
            itemBinding.tvMenuSummary.text = order.menuSummary()
            itemBinding.tvTotalAmount.text = order.totalAmount.toWon()
            itemBinding.root.setOnClickListener { showDetail(order) }
            itemBinding.btnAccept.setOnClickListener {
                setButtonsEnabled(listOf(itemBinding.btnAccept, itemBinding.btnReject), false)
                updateStatus(order.id, OrderStatus.Confirmed, onSuccess = { updatedOrder ->
                    replaceOrder(updatedOrder)
                    if (NotificationSettingsActivity.isEnabled(requireContext(), "new_order")) {
                        LocalNotificationHelper.show(requireContext(), "🛍️ 주문 수락됨", order.orderNumber ?: "")
                    }
                    toast("${order.orderNumber ?: "주문"} 수락 완료")
                    loadOrders(selectTabAfterLoad = 1)
                }, onFailure = {
                    setButtonsEnabled(listOf(itemBinding.btnAccept, itemBinding.btnReject), true)
                })
            }
            itemBinding.btnReject.setOnClickListener {
                setButtonsEnabled(listOf(itemBinding.btnAccept, itemBinding.btnReject), false)
                updateStatus(order.id, OrderStatus.Cancelled, onSuccess = { updatedOrder ->
                    replaceOrder(updatedOrder)
                    toast("${order.orderNumber ?: "주문"} 거절됨")
                    loadOrders()
                }, onFailure = {
                    setButtonsEnabled(listOf(itemBinding.btnAccept, itemBinding.btnReject), true)
                })
            }
            itemBinding.root
        }
    }

    private fun showConfirmedOrders(orders: List<OrderApiResponse>) {
        renderOrders(orders, "준비중인 주문이 없어요") { order ->
            val itemBinding = ItemOrderPreparingBinding.inflate(layoutInflater, binding.orderListContainer, false)
            itemBinding.tvOrderNumber.text = order.displayNumber()
            itemBinding.tvElapsedTime.text = elapsedLabel(order.createdAt)
            itemBinding.tvPickupTime.text = order.pickupTime.orEmpty()
            itemBinding.tvMenuSummary.text = order.menuSummary()
            itemBinding.tvTotalAmount.text = order.totalAmount.toWon()
            itemBinding.root.setOnClickListener { showDetail(order) }
            // READY 상태 없음 → 픽업 준비 완료 버튼 숨김
            itemBinding.btnReady.visibility = View.GONE
            itemBinding.root
        }
    }

    private fun showCompletedOrders(orders: List<OrderApiResponse>) {
        renderOrders(orders, "완료된 주문이 없어요") { order ->
            val itemBinding = ItemOrderCompletedBinding.inflate(layoutInflater, binding.orderListContainer, false)
            itemBinding.tvOrderNumber.text = order.displayNumber()
            itemBinding.tvPickupTime.text = order.pickupTime.orEmpty()
            itemBinding.tvMenuSummary.text = order.menuSummary()
            itemBinding.tvTotalAmount.text = order.totalAmount.toWon()
            itemBinding.root.setOnClickListener { showDetail(order) }
            itemBinding.root
        }
    }

    private fun renderOrders(
        orders: List<OrderApiResponse>,
        emptyMessage: String,
        createItemView: (OrderApiResponse) -> View,
    ) {
        binding.orderListContainer.renderChildren(
            items = orders,
            emptyView = { createEmptyView(emptyMessage) },
            itemView = createItemView,
        )
    }

    private fun updateStatus(
        orderId: Long,
        status: OrderStatus,
        onSuccess: (OrderApiResponse) -> Unit,
        onFailure: () -> Unit,
    ) {
        if (isPending) return
        isPending = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val updatedOrder = RetrofitClient.api.updateOrderStatus(
                    sellerId = RetrofitClient.memberId,
                    orderId = orderId,
                    req = UpdateOrderStatusRequest(status.apiValue),
                ).data ?: return@launch
                onSuccess(updatedOrder)
                (activity as? MainActivity)?.refreshOrderBadge()
            } catch (e: Exception) {
                if (handleSellerAuthFailure(e, session, "다시 로그인해주세요.")) return@launch
                onFailure()
                Log.e(TAG, "Failed to update order status. orderId=$orderId status=${status.apiValue}", e)
                toast(e.failureMessage("상태 업데이트"))
                // Intentionally reload the full order list on failure to restore consistent UI state.
                loadOrders()
            } finally {
                isPending = false
            }
        }
    }

    private fun showDetail(order: OrderApiResponse) {
        OrderDetailBottomSheet.newInstance(order).show(childFragmentManager, "order_detail")
    }

    private fun replaceOrder(updatedOrder: OrderApiResponse) {
        allOrders = allOrders.map { if (it.id == updatedOrder.id) updatedOrder else it }
    }

    private fun setButtonsEnabled(buttons: List<Button>, enabled: Boolean) {
        buttons.forEach { it.isEnabled = enabled }
    }

    private fun hasNewPendingOrders(newOrders: List<OrderApiResponse>): Boolean {
        val previousPendingIds = allOrders.pendingOrderIds()
        return newOrders.pendingOrderIds().any { it !in previousPendingIds }
    }

    private fun visibleOrderIdsChanged(newOrders: List<OrderApiResponse>): Boolean =
        orderIdsForTab(currentTab, allOrders) != orderIdsForTab(currentTab, newOrders)

    private fun orderIdsForTab(tabIndex: Int, orders: List<OrderApiResponse>): List<Long> =
        ordersForTab(tabIndex, orders).map { it.id }

    private fun List<OrderApiResponse>.pendingOrderIds(): Set<Long> =
        filter { it.orderStatus == OrderStatus.Pending }.map { it.id }.toSet()

    private fun OrderApiResponse.displayNumber(): String = orderNumber ?: "#$id"

    private fun OrderApiResponse.menuSummary(): String =
        items.joinToString(", ") { item ->
            val emojiPart = item.emoji?.let { "$it " } ?: ""
            "$emojiPart${item.name} × ${item.quantity}"
        }

    private fun elapsedLabel(createdAt: String): String = try {
        val created = runCatching { OffsetDateTime.parse(createdAt) }
            .getOrElse { LocalDateTime.parse(createdAt).atOffset(OffsetDateTime.now().offset) }
        val minutes = Duration.between(created, OffsetDateTime.now()).toMinutes()
        when {
            minutes < 1 -> "방금 전"
            minutes < 60 -> "${minutes}분 경과"
            else -> "${minutes / 60}시간 ${minutes % 60}분 경과"
        }
    } catch (_: Exception) { "" }


    private fun createEmptyView(message: String): View {
        return requireContext().emptyTextView(message, topMarginDp = 48, verticalPaddingDp = 0, centered = true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
