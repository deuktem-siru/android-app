package com.example.deuktemsiru_seller.domain.order

import com.example.deuktemsiru_seller.domain.model.OrderApiResponse
import com.example.deuktemsiru_seller.domain.model.OrderStatus

class OrderModel {
    var orders: List<OrderApiResponse> = emptyList()

    private fun statusesForTab(tabIndex: Int): List<OrderStatus> = when (tabIndex) {
        0 -> listOf(OrderStatus.Pending)
        1 -> listOf(OrderStatus.Confirmed)
        3 -> listOf(OrderStatus.PickedUp, OrderStatus.Cancelled)
        else -> emptyList()
    }

    fun ordersForTab(tabIndex: Int, orders: List<OrderApiResponse> = this.orders): List<OrderApiResponse> =
        orders.filter { it.orderStatus in statusesForTab(tabIndex) }

    fun hasNewPendingOrders(newOrders: List<OrderApiResponse>): Boolean {
        val previousPendingIds = this.orders.pendingOrderIds()
        return newOrders.pendingOrderIds().any { it !in previousPendingIds }
    }

    fun visibleOrderIdsChanged(tabIndex: Int, newOrders: List<OrderApiResponse>): Boolean =
        orderIdsForTab(tabIndex, this.orders) != orderIdsForTab(tabIndex, newOrders)

    private fun orderIdsForTab(tabIndex: Int, orders: List<OrderApiResponse>): List<Long> =
        ordersForTab(tabIndex, orders).map { it.id }

    private fun List<OrderApiResponse>.pendingOrderIds(): Set<Long> =
        filter { it.orderStatus == OrderStatus.Pending }.map { it.id }.toSet()

    fun replace(updatedOrder: OrderApiResponse) {
        orders = orders.map { if (it.id == updatedOrder.id) updatedOrder else it }
    }
}
