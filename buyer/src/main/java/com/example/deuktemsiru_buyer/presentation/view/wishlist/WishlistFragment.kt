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
import com.example.deuktemsiru_buyer.presentation.widget.home.StoreAdapter
import com.example.deuktemsiru_buyer.domain.model.Result
import com.example.deuktemsiru_buyer.util.bindCategorySelection
import com.example.deuktemsiru_buyer.util.bindSearch
import com.example.deuktemsiru_buyer.domain.model.filterStores
import com.example.deuktemsiru_buyer.util.toast
import kotlinx.coroutines.launch

class WishlistFragment : Fragment() {

    private var _binding: FragmentWishlistBinding? = null
    private val binding get() = _binding!!

    private val allStores = mutableListOf<Store>()
    private var currentCategory = "전체"
    private var loadFailed = false
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
            findNavController().navigate(R.id.onboardingFragment)
            return
        }

        loadWishlist()
    }

    private fun loadWishlist() {
        binding.layoutEmpty.visibility = View.GONE
        binding.progress.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = repository.getWishlist()) {
                is Result.Success -> {
                    loadFailed = false
                    val stores = result.data
                    allStores.clear()
                    allStores.addAll(stores)
                    binding.progress.visibility = View.GONE
                    refresh()
                }
                is Result.Error -> {
                    loadFailed = true
                    binding.progress.visibility = View.GONE
                    binding.layoutEmpty.visibility = View.VISIBLE
                    binding.rvWishlist.visibility = View.GONE
                    binding.tvEmptyTitle.setText(R.string.wishlist_error_title)
                    binding.tvEmptyBody.setText(R.string.load_error_body)
                    binding.btnEmptyAction.setText(R.string.retry_action)
                    binding.btnEmptyAction.setOnClickListener { loadWishlist() }
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
        if (loadFailed || binding.progress.visibility == View.VISIBLE) return
        val query = binding.etWishlistSearch.text?.toString()?.trim().orEmpty()
        val stores = allStores.filterStores(currentCategory, query)
        val filtered = currentCategory != "전체" || query.isNotEmpty()
        binding.tvEmptyTitle.setText(if (filtered) R.string.search_empty_title else R.string.wishlist_empty_title)
        binding.tvEmptyBody.setText(if (filtered) R.string.search_empty_body else R.string.wishlist_empty_body)
        binding.btnEmptyAction.setText(if (filtered) R.string.reset_filters else R.string.browse_stores)
        binding.btnEmptyAction.setOnClickListener {
            if (filtered) {
                currentCategory = "전체"
                binding.etWishlistSearch.setText("")
                setupCategoryChips()
                refresh()
            } else {
                findNavController().navigate(R.id.homeFragment)
            }
        }
        adapter.submitList(stores)
        binding.rvWishlist.visibility = if (stores.isEmpty()) View.GONE else View.VISIBLE
        binding.layoutEmpty.visibility = if (stores.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
