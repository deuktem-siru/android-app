package com.example.deuktemsiru_buyer.ui.detail

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deuktemsiru_buyer.R
import com.example.deuktemsiru_buyer.data.SampleData
import com.example.deuktemsiru_buyer.databinding.FragmentStoreDetailBinding

class StoreDetailFragment : Fragment() {

    private var _binding: FragmentStoreDetailBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())
    private var remainingSeconds = 0
    private var timerRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val storeId = arguments?.getInt("storeId") ?: 1
        val store = SampleData.getStoreById(storeId) ?: SampleData.stores.first()

        binding.tvStoreName.text = store.name
        binding.tvRating.text = store.rating.toString()
        binding.tvWalk.text = "도보 ${store.walkingMinutes}분"
        binding.tvPickupRange.text = "17:00 - 18:30"

        val totalPrice = store.menus.filter { !it.isSoldOut }
            .minByOrNull { it.discountedPrice }?.discountedPrice ?: store.discountedPrice
        binding.btnReserve.text = "${SampleData.formatPrice(totalPrice)} 예약하기"

        setupMenuList(store)
        startTimer(store.minutesUntilClose)

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.btnShare.setOnClickListener {
            Toast.makeText(requireContext(), "링크가 복사되었어요.", Toast.LENGTH_SHORT).show()
        }

        var wishlisted = store.isWishlisted
        binding.btnWishlist.setImageResource(
            if (wishlisted) R.drawable.ic_heart_filled else R.drawable.ic_heart
        )
        binding.btnWishlistBottom.setImageResource(
            if (wishlisted) R.drawable.ic_heart_filled else R.drawable.ic_heart
        )

        val wishlistToggle = View.OnClickListener {
            wishlisted = !wishlisted
            val res = if (wishlisted) R.drawable.ic_heart_filled else R.drawable.ic_heart
            binding.btnWishlist.setImageResource(res)
            binding.btnWishlistBottom.setImageResource(res)
            val msg = if (wishlisted) "찜 목록에 추가했어요 💝" else "찜 목록에서 제거했어요"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }
        binding.btnWishlist.setOnClickListener(wishlistToggle)
        binding.btnWishlistBottom.setOnClickListener(wishlistToggle)

        val allSoldOut = store.menus.all { it.isSoldOut }
        if (allSoldOut) {
            binding.btnReserve.text = "알림 신청"
            binding.btnReserve.setBackgroundResource(R.drawable.bg_surface_card)
        }

        binding.btnReserve.setOnClickListener {
            if (!allSoldOut) {
                findNavController().navigate(
                    R.id.action_storeDetail_to_payment,
                    bundleOf(
                        "storeId" to store.id,
                        "totalPrice" to totalPrice
                    )
                )
            } else {
                Toast.makeText(requireContext(), "다음 입고 시 알림을 보내드릴게요!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupMenuList(store: com.example.deuktemsiru_buyer.data.Store) {
        val adapter = MenuAdapter(
            menus = store.menus,
            onMenuClick = { menu ->
                Toast.makeText(requireContext(), "${menu.name} 선택", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvMenus.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
    }

    private fun startTimer(minutes: Int) {
        remainingSeconds = minutes * 60
        timerRunnable = object : Runnable {
            override fun run() {
                if (remainingSeconds > 0) {
                    val mins = remainingSeconds / 60
                    val secs = remainingSeconds % 60
                    binding.tvTimer.text = "%02d:%02d".format(mins, secs)
                    remainingSeconds--
                    handler.postDelayed(this, 1000)
                } else {
                    binding.tvTimer.text = "00:00"
                }
            }
        }
        handler.post(timerRunnable!!)
    }

    override fun onDestroyView() {
        timerRunnable?.let { handler.removeCallbacks(it) }
        super.onDestroyView()
        _binding = null
    }
}
