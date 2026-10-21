package com.example.deuktemsiru_seller.presentation.view.order

import com.example.deuktemsiru_seller.domain.order.OrderModel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.deuktemsiru_seller.MainActivity
import com.example.deuktemsiru_seller.R
import com.example.deuktemsiru_seller.data.repository.SessionManager
import com.example.deuktemsiru_seller.data.repository.appSession
import com.example.deuktemsiru_seller.databinding.FragmentOrderBinding
import com.example.deuktemsiru_seller.databinding.ItemOrderCompletedBinding
import com.example.deuktemsiru_seller.databinding.ItemOrderNewBinding
import com.example.deuktemsiru_seller.databinding.ItemOrderPreparingBinding
import com.example.deuktemsiru_seller.domain.model.OrderStatus
import com.example.deuktemsiru_seller.domain.model.OrderApiResponse
import com.example.deuktemsiru_seller.data.remote.RetrofitClient
import com.example.deuktemsiru_seller.domain.model.UpdateOrderStatusRequest
import com.example.deuktemsiru_seller.util.LocalNotificationHelper
import com.example.deuktemsiru_seller.data.repository.NotificationPreferences
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

    private val screenModel by lazy { com.example.deuktemsiru_seller.AppGraph.model(this) }

    private var _binding: FragmentOrderBinding? = null
    private val binding get() = _binding!!

    private var currentTab = 0
    private val model = OrderModel()
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
        session = requireContext().appSession
        setupTabs()
        val mainActivity = activity as? MainActivity
        val pendingTab = mainActivity?.pendingOrderTab
        mainActivity?.pendingOrderTab = null
        loadOrders(selectTabAfterLoad = pendingTab ?: currentTab)
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
            val newOrders = screenModel.getOrders(RetrofitClient.memberId).data ?: emptyList()
            val hasNewPendingOrders = model.hasNewPendingOrders(newOrders)
            val shouldRefreshVisibleList = !silent || model.visibleOrderIdsChanged(currentTab, newOrders)

            model.orders = newOrders
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
                model.orders = emptyList()
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
            0 -> showNewOrders(model.ordersForTab(0))
            1 -> showConfirmedOrders(model.ordersForTab(1))
            3 -> showCompletedOrders(model.ordersForTab(3))
            // index 2 = tabPickup은 UI에서 숨김 처리되어 있으므로 아무것도 하지 않음
        }
    }

    private fun updateTabCounts() {
        binding.tabNew.text = getString(R.string.tab_new_order_count, model.ordersForTab(0).size)
        binding.tabPreparing.text = getString(R.string.tab_preparing_count, model.ordersForTab(1).size)
        binding.tabDone.text = getString(R.string.tab_completed_count, model.ordersForTab(3).size)
    }

    private fun showNewOrders(orders: List<OrderApiResponse>) {
        renderOrders(orders, "새로운 주문이 없어요") { order ->
            val itemBinding = ItemOrderNewBinding.inflate(layoutInflater, binding.orderListContainer, false)
            bindOrderCard(order, itemBinding.root)
            itemBinding.tvOrderTime.text = getString(R.string.just_arrived)
            val buttons = listOf(itemBinding.btnAccept, itemBinding.btnReject)
            itemBinding.btnAccept.setOnClickListener {
                setButtonsEnabled(buttons, false)
                updateStatus(order.id, OrderStatus.Confirmed, onSuccess = { updatedOrder ->
                    model.replace(updatedOrder)
                    if (NotificationPreferences(requireContext()).isEnabled("new_order")) {
                        LocalNotificationHelper.show(requireContext(), "🛍️ 주문 수락됨", order.orderNumber ?: "")
                    }
                    toast("${order.orderNumber ?: "주문"} 수락 완료")
                    loadOrders(selectTabAfterLoad = 1)
                }, onFailure = {
                    setButtonsEnabled(buttons, true)
                })
            }
            itemBinding.btnReject.setOnClickListener {
                setButtonsEnabled(buttons, false)
                updateStatus(order.id, OrderStatus.Cancelled, onSuccess = { updatedOrder ->
                    model.replace(updatedOrder)
                    toast("${order.orderNumber ?: "주문"} 거절됨")
                    loadOrders()
                }, onFailure = {
                    setButtonsEnabled(buttons, true)
                })
            }
            itemBinding.root
        }
    }

    private fun showConfirmedOrders(orders: List<OrderApiResponse>) {
        renderOrders(orders, "준비중인 주문이 없어요") { order ->
            val itemBinding = ItemOrderPreparingBinding.inflate(layoutInflater, binding.orderListContainer, false)
            bindOrderCard(order, itemBinding.root)
            itemBinding.tvElapsedTime.text = elapsedLabel(order.createdAt)
            // READY 상태 없음 → 픽업 준비 완료 버튼 숨김
            itemBinding.btnReady.visibility = View.GONE
            itemBinding.root
        }
    }

    private fun showCompletedOrders(orders: List<OrderApiResponse>) {
        renderOrders(orders, "완료된 주문이 없어요") { order ->
            val itemBinding = ItemOrderCompletedBinding.inflate(layoutInflater, binding.orderListContainer, false)
            bindOrderCard(order, itemBinding.root)
            itemBinding.root
        }
    }

    private fun bindOrderCard(order: OrderApiResponse, root: View) {
        root.findViewById<TextView>(R.id.tv_order_number).text = order.displayNumber()
        root.findViewById<TextView>(R.id.tv_pickup_time).text = order.pickupTime.orEmpty()
        root.findViewById<TextView>(R.id.tv_menu_summary).text = order.menuSummary()
        root.findViewById<TextView>(R.id.tv_total_amount).text = order.totalAmount.toWon()
        root.setOnClickListener { showDetail(order) }
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
                val updatedOrder = screenModel.updateOrderStatus(
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

    private fun setButtonsEnabled(buttons: List<Button>, enabled: Boolean) {
        buttons.forEach { it.isEnabled = enabled }
    }

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
