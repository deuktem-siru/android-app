package com.example.deuktemsiru_buyer.presentation.widget.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.deuktemsiru_buyer.R
import com.example.deuktemsiru_buyer.databinding.ItemOrderHistoryBinding
import com.example.deuktemsiru_buyer.domain.model.OrderListItemResponse
import com.example.deuktemsiru_buyer.util.formatPrice
import com.example.deuktemsiru_buyer.util.orderStatusLabel

class OrderHistoryAdapter(
    private val orders: List<OrderListItemResponse>,
    private val onItemClick: (OrderListItemResponse) -> Unit,
) : RecyclerView.Adapter<OrderHistoryAdapter.VH>() {

    inner class VH(val binding: ItemOrderHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemOrderHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount() = orders.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val order = orders[position]
        val b = holder.binding
        b.tvStoreName.text = order.storeName
        b.tvOrderNumber.text = "#${order.orderId}"
        b.tvStatus.text = orderStatusLabel(order.status)
        val (statusBackground, statusTextColor) = when (order.status) {
            "CONFIRMED", "PREPARING", "READY" -> R.drawable.bg_order_status_primary to R.color.primary
            "PICKED_UP", "COMPLETED" -> R.drawable.bg_order_status_success to R.color.success
            "CANCELLED" -> R.drawable.bg_order_status_muted to R.color.text_muted
            else -> R.drawable.bg_order_status_muted to R.color.text_sub
        }
        b.tvStatus.setBackgroundResource(statusBackground)
        b.tvStatus.setTextColor(ContextCompat.getColor(b.root.context, statusTextColor))
        b.tvMenuSummary.text = "주문 상품 ${order.itemCount}개"
        b.tvTotalAmount.text = order.totalPrice.formatPrice()
        b.tvPickupTime.text = "주문: ${order.createdAt.substringBefore('T')}"
        b.tvPickupCode.text = "코드: ${order.pickupCode ?: "----"}"
        b.root.setOnClickListener { onItemClick(order) }
    }
}
