package com.example.deuktemsiru_buyer.data

data class CartItem(
    val menuId: Long,
    val menuName: String,
    val emoji: String,
    val originalPrice: Int,
    val discountedPrice: Int,
    val pickupStart: String = "",
    val pickupEnd: String = "",
    var quantity: Int = 1,
)

object CartManager {
    @Volatile var serverCartItemIds: Map<Long, Long> = emptyMap()
        private set
    @Volatile var storeId: Long = 0L
        private set
    @Volatile var storeName: String = ""
        private set
    @Volatile var storeEmoji: String = ""
        private set
    @Volatile var storeLat: Double = 0.0
        private set
    @Volatile var storeLng: Double = 0.0
        private set
    private val _items = mutableListOf<CartItem>()

    val items: List<CartItem> get() = synchronized(this) { _items.toList() }

    @Synchronized
    fun add(storeId: Long, storeName: String, storeEmoji: String, storeLat: Double, storeLng: Double, item: CartItem): Boolean {
        if (this.storeId != 0L && this.storeId != storeId) return false
        this.storeId = storeId
        this.storeName = storeName
        this.storeEmoji = storeEmoji
        this.storeLat = storeLat
        this.storeLng = storeLng
        val existing = _items.find { it.menuId == item.menuId }
        if (existing != null) existing.quantity++ else _items.add(item.copy())
        return true
    }

    @Synchronized
    fun remove(menuId: Long) {
        _items.removeAll { it.menuId == menuId }
        if (_items.isEmpty()) clearInternal()
    }

    @Synchronized
    fun increaseQuantity(menuId: Long) {
        _items.find { it.menuId == menuId }?.quantity++
    }

    @Synchronized
    fun decreaseQuantity(menuId: Long) {
        val item = _items.find { it.menuId == menuId } ?: return
        if (item.quantity <= 1) {
            remove(menuId)
        } else {
            item.quantity--
        }
    }

    @Synchronized
    fun addServerCartItemId(productId: Long, cartItemId: Long) {
        serverCartItemIds = serverCartItemIds + (productId to cartItemId)
    }

    @Synchronized
    fun clear() { clearInternal() }

    private fun clearInternal() {
        storeId = 0L
        storeName = ""
        storeEmoji = ""
        storeLat = 0.0
        storeLng = 0.0
        _items.clear()
        serverCartItemIds = emptyMap()
    }

    @Synchronized
    fun replaceFromServer(
        storeId: Long,
        storeName: String,
        storeLat: Double,
        storeLng: Double,
        items: List<CartItem>,
        serverIds: Map<Long, Long>,
    ) {
        clearInternal()
        this.storeId = storeId
        this.storeName = storeName
        storeEmoji = "🛍️"
        this.storeLat = storeLat
        this.storeLng = storeLng
        _items.addAll(items)
        serverCartItemIds = serverIds
    }

    val totalPrice: Int get() = synchronized(this) { _items.sumOf { it.discountedPrice * it.quantity } }
    val totalCount: Int get() = synchronized(this) { _items.sumOf { it.quantity } }
    val isEmpty: Boolean get() = synchronized(this) { _items.isEmpty() }
}
