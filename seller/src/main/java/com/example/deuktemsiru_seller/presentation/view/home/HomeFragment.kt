package com.example.deuktemsiru_seller.presentation.view.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.R
import com.example.deuktemsiru_seller.MainActivity
import com.example.deuktemsiru_seller.data.repository.SessionManager
import com.example.deuktemsiru_seller.data.repository.appSession
import com.example.deuktemsiru_seller.databinding.FragmentHomeBinding
import com.example.deuktemsiru_seller.databinding.ItemActiveMenuBinding
import com.example.deuktemsiru_seller.domain.model.OrderStatus
import com.example.deuktemsiru_seller.domain.model.SaleItemApiResponse
import com.example.deuktemsiru_seller.domain.model.SaleStatus
import com.example.deuktemsiru_seller.data.remote.RetrofitClient
import com.example.deuktemsiru_seller.presentation.view.order.PickupVerifyActivity
import com.example.deuktemsiru_seller.presentation.view.product.ProductListingActivity
import com.example.deuktemsiru_seller.presentation.view.product.SaleItemDetailBottomSheet
import com.example.deuktemsiru_seller.util.emptyTextView
import com.example.deuktemsiru_seller.util.renderChildren
import com.example.deuktemsiru_seller.util.toWon
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private val screenModel by lazy { com.example.deuktemsiru_seller.AppGraph.model(this) }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var session: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        session = requireContext().appSession
        binding.tvStoreName.text = session.nickname.ifBlank { "내 가게" }

        val mainActivity = activity as? MainActivity
        binding.cardSales.setOnClickListener { mainActivity?.navigateToOrder() }
        listOf(binding.sectionSalesCount, binding.tvTodayOrders).forEach {
            it.setOnClickListener { mainActivity?.navigateToOrderCompleted() }
        }
        binding.sectionWaste.setOnClickListener { mainActivity?.navigateToSales() }
        listOf(binding.sectionLoyal, binding.btnSendNotification, binding.btnBell).forEach {
            it.setOnClickListener { mainActivity?.navigateToNotification() }
        }
        binding.btnRegisterMenu.setOnClickListener {
            startActivity(Intent(requireContext(), ProductListingActivity::class.java))
        }
        binding.sectionOrderNew.setOnClickListener {
            mainActivity?.navigateToOrderTab(0)
        }
        binding.sectionOrderPreparing.setOnClickListener {
            mainActivity?.navigateToOrderTab(1)
        }
        binding.sectionOrderPickup.setOnClickListener {
            mainActivity?.navigateToOrderTab(3)
        }
        binding.btnPickupVerifyHome.setOnClickListener {
            startActivity(Intent(requireContext(), PickupVerifyActivity::class.java))
        }
        binding.tvViewAllMenus.setOnClickListener {
            mainActivity?.navigateToProduct()
        }

    }

    override fun onResume() {
        super.onResume()
        if (::session.isInitialized && session.isLoggedIn()) {
            loadStore()
            loadStats()
        }
    }

    private fun loadStore() {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                val store = screenModel.getMyStore(RetrofitClient.memberId).data
                if (store != null) {
                    binding.tvStoreName.text = store.name
                    binding.tvClosingTime.text = "마감 ${store.closingTime}"
                }
            }
            // 활성 판매 상품 목록 로드
            runCatching {
                val items = screenModel.getSaleItems(RetrofitClient.memberId).data ?: emptyList()
                renderActiveSaleItems(items)
            }.onFailure {
                renderActiveSaleItems(emptyList())
            }
        }
    }

    private fun renderActiveSaleItems(items: List<SaleItemApiResponse>) {
        val activeItems = items.filter { it.saleStatus == SaleStatus.Available && it.remainingItems > 0 }
        binding.activeMenuContainer.renderChildren(
            items = activeItems,
            emptyView = ::createEmptyMenuView,
        ) { item ->
            val itemBinding = ItemActiveMenuBinding.inflate(layoutInflater, binding.activeMenuContainer, false)
            itemBinding.tvMenuEmoji.text = item.emoji
            itemBinding.tvMenuName.text = item.name
            itemBinding.tvRemaining.text = getString(R.string.remaining_items, item.remainingItems)
            itemBinding.tvPrice.text = item.discountedPrice.toWon()
            itemBinding.tvTimeBadge.text = item.displayPickupTime
            itemBinding.tvTimeBadge.setTextColor(requireContext().getColor(R.color.warning))
            itemBinding.tvTimeBadge.setBackgroundResource(R.drawable.bg_rounded_warning)
            itemBinding.root.setOnClickListener { showSaleItemDetail(item) }
            itemBinding.root
        }
    }

    private fun showSaleItemDetail(item: SaleItemApiResponse) {
        SaleItemDetailBottomSheet.newInstance(item).show(parentFragmentManager, "sale_item_detail")
    }

    private fun createEmptyMenuView(): View = requireContext().emptyTextView(
        message = getString(R.string.empty_menus),
        verticalPaddingDp = 24,
        centered = true,
    ).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
    }

    private fun loadStats() {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                val sales = screenModel.getSales(RetrofitClient.memberId).data
                if (sales != null) {
                    binding.tvTodaySales.text = sales.todaySales.toWon()
                    binding.tvTodayOrders.text = "${sales.todayOrderCount}건"
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                val orders = screenModel.getOrders(RetrofitClient.memberId).data ?: emptyList()
                val newCount = orders.count { it.orderStatus == OrderStatus.Pending }
                val preparingCount = orders.count { it.orderStatus == OrderStatus.Confirmed }
                val pickupCount = orders.count { it.orderStatus == OrderStatus.PickedUp }
                binding.tvReservationNew.text = newCount.toString()
                binding.tvReservationPreparing.text = preparingCount.toString()
                binding.tvReservationPickup.text = pickupCount.toString()
            }.onFailure { }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
