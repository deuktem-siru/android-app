package com.example.deuktemsiru_buyer.presentation.widget.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.deuktemsiru_buyer.R
import com.example.deuktemsiru_buyer.domain.model.Store
import com.example.deuktemsiru_buyer.databinding.ItemMapStoreCardBinding
import com.example.deuktemsiru_buyer.util.formatPrice

class MapStoreAdapter(
    private val onClick: (Store) -> Unit,
) : ListAdapter<Store, MapStoreAdapter.VH>(DiffCallback) {

    inner class VH(private val binding: ItemMapStoreCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(store: Store) {
            val ctx = binding.root.context
            binding.tvEmoji.text = store.emoji
            binding.tvBadge.text = ctx.getString(R.string.label_discount_rate, store.discountRate)
            binding.tvName.text = store.name
            binding.tvTime.text = ctx.getString(R.string.label_minutes_left, store.minutesUntilClose)
            binding.tvPrice.text = store.discountedPrice.formatPrice()

            val (clockIcon, clockColor) = if (store.minutesUntilClose <= 30) {
                R.drawable.ic_clock to ContextCompat.getColor(ctx, R.color.danger)
            } else {
                R.drawable.ic_clock_warning to ContextCompat.getColor(ctx, R.color.warning)
            }
            binding.tvTime.setTextColor(clockColor)
            binding.tvTime.setCompoundDrawablesWithIntrinsicBounds(clockIcon, 0, 0, 0)
            binding.cardRoot.setOnClickListener { onClick(store) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemMapStoreCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object DiffCallback : DiffUtil.ItemCallback<Store>() {
        override fun areItemsTheSame(oldItem: Store, newItem: Store) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Store, newItem: Store) = oldItem == newItem
    }
}
