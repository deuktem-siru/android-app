package com.example.deuktemsiru_buyer.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deuktemsiru_buyer.R
import com.example.deuktemsiru_buyer.data.SampleData
import com.example.deuktemsiru_buyer.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var storeAdapter: StoreAdapter
    private var currentCategory = "전체"
    private val allStores = SampleData.stores.toMutableList()

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

        setupRecyclerView()
        setupCategoryChips()
        filterStores("전체")
    }

    private fun setupRecyclerView() {
        storeAdapter = StoreAdapter(
            stores = allStores,
            onStoreClick = { store ->
                findNavController().navigate(
                    R.id.action_home_to_storeDetail,
                    bundleOf("storeId" to store.id)
                )
            },
            onWishlistClick = { store ->
                val index = allStores.indexOfFirst { it.id == store.id }
                if (index >= 0) {
                    allStores[index] = allStores[index].copy(isWishlisted = !allStores[index].isWishlisted)
                    storeAdapter.updateStores(allStores.toList())
                }
            }
        )

        binding.rvStores.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = storeAdapter
        }
    }

    private fun setupCategoryChips() {
        val chips = mapOf(
            binding.chipAll to "전체",
            binding.chipBakery to "베이커리",
            binding.chipLunchbox to "도시락",
            binding.chipSalad to "샐러드",
            binding.chipCafe to "카페"
        )

        chips.forEach { (chip, category) ->
            chip.setOnClickListener {
                currentCategory = category
                chips.forEach { (c, _) ->
                    c.setBackgroundResource(R.drawable.bg_chip_unselected)
                    c.setTextColor(Color.parseColor("#1A1A1A"))
                }
                chip.setBackgroundResource(R.drawable.bg_chip_selected)
                chip.setTextColor(Color.WHITE)
                filterStores(category)
            }
        }
    }

    private fun filterStores(category: String) {
        val filtered = if (category == "전체") {
            allStores.toList()
        } else {
            allStores.filter { it.category == category }
        }

        if (filtered.isEmpty()) {
            binding.rvStores.visibility = View.GONE
            binding.llEmpty.visibility = View.VISIBLE
        } else {
            binding.rvStores.visibility = View.VISIBLE
            binding.llEmpty.visibility = View.GONE
            storeAdapter.updateStores(filtered)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
