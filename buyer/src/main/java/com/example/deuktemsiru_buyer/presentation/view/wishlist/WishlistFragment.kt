package com.example.deuktemsiru_buyer.presentation.view.wishlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deuktemsiru_buyer.R
import com.example.deuktemsiru_buyer.data.repository.appSession
import com.example.deuktemsiru_buyer.domain.model.Store
import com.example.deuktemsiru_buyer.domain.model.storeCategoryFilters
import com.example.deuktemsiru_buyer.databinding.FragmentWishlistBinding
import com.example.deuktemsiru_buyer.data.remote.RetrofitClient
import com.example.deuktemsiru_buyer.presentation.widget.home.StoreAdapter
import com.example.deuktemsiru_buyer.domain.model.Result
import com.example.deuktemsiru_buyer.util.bindCategorySelection
import com.example.deuktemsiru_buyer.util.bindSearch
import com.example.deuktemsiru_buyer.domain.model.filterStores
import com.example.deuktemsiru_buyer.util.toast
import kotlinx.coroutines.launch

class WishlistFragment : Fragment() {

    private val screenModel by lazy { com.example.deuktemsiru_buyer.AppGraph.model(this) }

    private var _binding: FragmentWishlistBinding? = null
    private val binding get() = _binding!!

    private val allStores = mutableListOf<Store>()
    private var currentCategory = "전체"
    private lateinit var adapter: StoreAdapter
    private val repository by lazy { com.example.deuktemsiru_buyer.AppGraph.stores(this) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWishlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupCategoryChips()

        val session = requireContext().appSession
        if (!session.isLoggedIn()) {
            binding.progress.visibility = View.GONE
            binding.layoutEmpty.visibility = View.VISIBLE
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = repository.getWishlist()) {
                is Result.Success -> {
                    val stores = result.data
                    allStores.clear()
                    allStores.addAll(stores)
                    binding.progress.visibility = View.GONE
                    refresh()
                }
                is Result.Error -> {
                    binding.progress.visibility = View.GONE
                    binding.layoutEmpty.visibility = View.VISIBLE
                    toast("찜 목록을 불러오지 못했어요.")
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = StoreAdapter(
            onStoreClick = { store ->
                findNavController().navigate(
                    R.id.action_wishlist_to_storeDetail,
                    Bundle().apply { putLong("storeId", store.id) }
                )
            },
            onWishlistClick = { store ->
                viewLifecycleOwner.lifecycleScope.launch {
                    when (repository.toggleWishlist(store.id)) {
                        is Result.Success -> {
                            allStores.removeAll { it.id == store.id }
                            refresh()
                            toast("찜 목록에서 제거했어요")
                        }
                        is Result.Error ->
                            toast("찜 처리 중 오류가 발생했어요.")
                    }
                }
            }
        )
        binding.rvWishlist.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@WishlistFragment.adapter
        }
    }

    private fun setupCategoryChips() {
        val chips = listOf(
            binding.chipAll,
            binding.chipKorean,
            binding.chipWestern,
            binding.chipCafeDessert,
            binding.chipBakery,
            binding.chipCafe,
        ).zip(storeCategoryFilters).toMap()

        chips.bindCategorySelection(
            fragment = this,
            selected = { currentCategory },
            onSelected = {
                currentCategory = it
                refresh()
            },
        )
    }

    private fun setupSearch() {
        bindSearch(binding.etWishlistSearch, binding.btnWishlistSearch) { refresh() }
    }

    private fun refresh() {
        val query = binding.etWishlistSearch.text?.toString()?.trim().orEmpty()
        val stores = allStores.filterStores(currentCategory, query)
        adapter.submitList(stores)
        binding.rvWishlist.visibility = if (stores.isEmpty()) View.GONE else View.VISIBLE
        binding.layoutEmpty.visibility = if (stores.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
