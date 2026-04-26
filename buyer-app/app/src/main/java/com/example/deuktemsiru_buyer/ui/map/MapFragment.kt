package com.example.deuktemsiru_buyer.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.deuktemsiru_buyer.R
import com.example.deuktemsiru_buyer.data.SampleData
import com.example.deuktemsiru_buyer.databinding.FragmentMapBinding
import com.example.deuktemsiru_buyer.databinding.ItemMapStoreCardBinding

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        populateBottomSheetCards()

        binding.btnMyLocation.setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "현재 위치로 이동합니다", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun populateBottomSheetCards() {
        SampleData.stores.forEach { store ->
            val cardBinding = ItemMapStoreCardBinding.inflate(
                LayoutInflater.from(requireContext()),
                binding.llMapStores,
                false
            )
            cardBinding.tvEmoji.text = store.emoji
            cardBinding.tvBadge.text = "${store.discountRate}%"
            cardBinding.tvName.text = store.name
            cardBinding.tvTime.text = "${store.minutesUntilClose}분 후 마감"
            cardBinding.tvPrice.text = com.example.deuktemsiru_buyer.data.SampleData.formatPrice(store.discountedPrice)

            if (store.minutesUntilClose <= 30) {
                cardBinding.tvTime.setTextColor(android.graphics.Color.parseColor("#FF3B30"))
                cardBinding.tvTime.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_clock, 0, 0, 0)
            } else {
                cardBinding.tvTime.setTextColor(android.graphics.Color.parseColor("#FF8800"))
                cardBinding.tvTime.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_clock_warning, 0, 0, 0)
            }

            cardBinding.cardRoot.setOnClickListener {
                findNavController().navigate(
                    R.id.action_map_to_storeDetail,
                    bundleOf("storeId" to store.id)
                )
            }

            binding.llMapStores.addView(cardBinding.root)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
