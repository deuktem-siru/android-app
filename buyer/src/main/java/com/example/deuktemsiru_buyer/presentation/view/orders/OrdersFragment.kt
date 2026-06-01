package com.example.deuktemsiru_buyer.presentation.view.orders

import com.example.deuktemsiru_buyer.presentation.widget.orders.OrderHistoryAdapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.deuktemsiru_buyer.R
import kotlinx.coroutines.CancellationException
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deuktemsiru_buyer.data.repository.appSession
import com.example.deuktemsiru_buyer.databinding.FragmentOrdersBinding
import com.example.deuktemsiru_buyer.domain.model.OrderListItemResponse
import com.example.deuktemsiru_buyer.util.toast
import kotlinx.coroutines.launch

class OrdersFragment : Fragment() {

    private val screenModel by lazy { com.example.deuktemsiru_buyer.AppGraph.model(this) }

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val session = requireContext().appSession
        if (!session.isLoggedIn()) return

        binding.btnEmptyAction.setOnClickListener { findNavController().navigate(R.id.homeFragment) }
        loadOrders()
    }

    private fun loadOrders() {
        binding.llEmpty.visibility = View.GONE
        binding.progress.visibility = View.VISIBLE
        binding.btnEmptyAction.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val orders = screenModel.getOrders().data.orEmpty()
                if (orders.isEmpty()) {
                    binding.rvOrders.visibility = View.GONE
                    binding.llEmpty.visibility = View.VISIBLE
                    binding.tvEmptyTitle.setText(R.string.orders_empty_title)
                    binding.tvEmptyBody.setText(R.string.orders_empty_body)
                    binding.btnEmptyAction.setText(R.string.browse_stores)
                    binding.btnEmptyAction.setOnClickListener { findNavController().navigate(R.id.homeFragment) }
                } else {
                    binding.llEmpty.visibility = View.GONE
                    binding.rvOrders.visibility = View.VISIBLE
                    binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
                    binding.rvOrders.adapter = OrderHistoryAdapter(orders) { order ->
                        showOrderDetail(order)
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                binding.rvOrders.visibility = View.GONE
                binding.llEmpty.visibility = View.VISIBLE
                binding.tvEmptyTitle.setText(R.string.orders_error_title)
                binding.tvEmptyBody.setText(R.string.load_error_body)
                binding.btnEmptyAction.setText(R.string.retry_action)
                binding.btnEmptyAction.setOnClickListener { loadOrders() }
            } finally {
                _binding?.progress?.visibility = View.GONE
                _binding?.btnEmptyAction?.isEnabled = true
            }
        }
    }

    private fun showOrderDetail(order: OrderListItemResponse) {
        OrderDetailBottomSheet.newInstance(order.orderId) { loadOrders() }
            .show(childFragmentManager, "order_detail")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

