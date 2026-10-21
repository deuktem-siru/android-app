package com.example.deuktemsiru_buyer.presentation.view.map

import com.example.deuktemsiru_buyer.presentation.widget.map.MapStoreAdapter

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deuktemsiru_buyer.R
import com.example.deuktemsiru_buyer.domain.model.Store
import com.example.deuktemsiru_buyer.domain.model.storeCategoryFilters
import com.example.deuktemsiru_buyer.databinding.FragmentMapBinding
import com.example.deuktemsiru_buyer.data.remote.RetrofitClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.example.deuktemsiru_buyer.util.bindCategorySelection
import com.example.deuktemsiru_buyer.util.bindSearch
import com.example.deuktemsiru_buyer.domain.model.filterStores
import com.example.deuktemsiru_buyer.util.MapViewLifecycleDelegate
import com.example.deuktemsiru_buyer.domain.model.Result
import com.example.deuktemsiru_buyer.util.getCurrentLocation
import com.example.deuktemsiru_buyer.util.hasLocationPermission
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Locale

private val SIHEUNG = LatLng(37.3799, 126.8031)

class MapFragment : Fragment(), OnMapReadyCallback {

    private val screenModel by lazy { com.example.deuktemsiru_buyer.AppGraph.model(this) }

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private val mapLifecycle = MapViewLifecycleDelegate { if (::mapView.isInitialized) mapView else null }
    private var googleMap: GoogleMap? = null
    private var loadedStores: List<Store> = emptyList()
    private var currentCategory = "전체"
    private lateinit var mapStoreAdapter: MapStoreAdapter
    private val repository by lazy { com.example.deuktemsiru_buyer.AppGraph.stores(this) }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            enableMyLocation()
            moveToCurrentLocation()
        } else {
            Snackbar.make(binding.root, "위치 권한이 필요합니다.", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        val koConfig = Configuration(requireContext().resources.configuration)
        koConfig.setLocale(Locale.forLanguageTag("ko-KR"))
        val koContext = requireContext().createConfigurationContext(koConfig)
        mapView = MapView(koContext)
        binding.mapContainer.addView(mapView)
        mapView.onCreate(savedInstanceState)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView.getMapAsync(this)
        requestLocationPermissions()
        setupBottomSheetCards()
        setupSearch()
        setupCategoryChips()
        loadStores()

        binding.btnMyLocation.setOnClickListener { moveToCurrentLocation() }
        binding.btnZoomIn.setOnClickListener { googleMap?.animateCamera(CameraUpdateFactory.zoomIn()) }
        binding.btnZoomOut.setOnClickListener { googleMap?.animateCamera(CameraUpdateFactory.zoomOut()) }
    }

    override fun onMapReady(map: GoogleMap) {
        if (_binding == null) return
        googleMap = map
        map.uiSettings.isMyLocationButtonEnabled = false
        map.uiSettings.isZoomControlsEnabled = false
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(SIHEUNG, 12f))
        map.setOnInfoWindowClickListener { marker ->
            if (_binding == null) return@setOnInfoWindowClickListener
            (marker.tag as? Long)?.let { storeId ->
                findNavController().navigate(
                    R.id.action_map_to_storeDetail,
                    Bundle().apply { putLong("storeId", storeId) }
                )
            }
        }
        renderStoreMarkers(filteredStores())
        enableMyLocation()
        moveToCurrentLocation()
    }

    private fun requestLocationPermissions() {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (hasLocationPermission) googleMap?.isMyLocationEnabled = true
    }

    @SuppressLint("MissingPermission")
    private fun moveToCurrentLocation() {
        getCurrentLocation(onLocation = ::animateTo)
    }

    private fun animateTo(location: Location) {
        googleMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15f)
        )
    }

    private fun loadStores() {
        // viewLifecycleOwner.lifecycleScope: View가 파괴되면 코루틴도 자동 취소 → _binding null 안전
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                when (val result = repository.getStores()) {
                    is Result.Success -> {
                        loadedStores = result.data
                        if (_binding != null) updateMapStores()
                    }
                    is Result.Error -> if (_binding != null) {
                        Snackbar.make(binding.root, "지도 매장 정보를 불러오지 못했어요.", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun renderStoreMarkers(stores: List<Store>) {
        val map = googleMap ?: return
        if (_binding == null) return

        val storesWithLocation = stores.filter { it.hasValidLocation() }
        map.clear()
        if (storesWithLocation.isEmpty()) return

        val boundsBuilder = LatLngBounds.builder()
        storesWithLocation.forEach { store ->
            val position = LatLng(store.latitude, store.longitude)
            val marker = map.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(store.name)
                    .snippet("${store.discountRate}% ${"%.0f원".format(store.discountedPrice.toFloat())}")
            )
            marker?.tag = store.id
            boundsBuilder.include(position)
        }

        val bounds = boundsBuilder.build()
        binding.mapContainer.post {
            if (_binding == null) return@post
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 96))
        }
    }

    private fun setupBottomSheetCards() {
        mapStoreAdapter = MapStoreAdapter { store ->
            if (_binding == null) return@MapStoreAdapter
            findNavController().navigate(
                R.id.action_map_to_storeDetail,
                Bundle().apply { putLong("storeId", store.id) }
            )
        }
        binding.rvMapStores.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = mapStoreAdapter
        }
    }

    private fun setupSearch() {
        bindSearch(binding.etMapSearch, binding.btnMapSearch) { updateMapStores() }
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
                updateMapStores()
            },
        )
    }

    private fun updateMapStores() {
        if (_binding == null) return
        val stores = filteredStores()
        mapStoreAdapter.submitList(stores)
        renderStoreMarkers(stores)
    }

    private fun filteredStores(): List<Store> {
        val query = _binding?.etMapSearch?.text?.toString()?.trim().orEmpty()
        return loadedStores.filterStores(currentCategory, query)
    }

    // MapView 생명주기: onDestroyView에서는 onDestroy 호출 금지 (탭 재진입 시 크래시 원인)
    override fun onStart() { super.onStart(); mapLifecycle.onStart() }
    override fun onResume() { super.onResume(); mapLifecycle.onResume() }
    override fun onPause() { super.onPause(); mapLifecycle.onPause() }
    override fun onStop() { super.onStop(); mapLifecycle.onStop() }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapLifecycle.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapLifecycle.onLowMemory()
    }

    override fun onDestroyView() {
        googleMap = null
        super.onDestroyView()
        _binding = null
    }

    // MapView의 onDestroy는 Fragment.onDestroy()에서 호출해야 안전
    override fun onDestroy() {
        mapLifecycle.onDestroy()
        super.onDestroy()
    }

    private fun Store.hasValidLocation() = latitude != 0.0 && longitude != 0.0
}

