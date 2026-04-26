package com.example.deuktemsiru_buyer.ui.payment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.deuktemsiru_buyer.R
import com.example.deuktemsiru_buyer.data.SampleData
import com.example.deuktemsiru_buyer.databinding.FragmentPaymentBinding

class PaymentFragment : Fragment() {

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    private var selectedSlot = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val storeId = arguments?.getInt("storeId") ?: 1
        val totalPrice = arguments?.getInt("totalPrice") ?: 5900
        val store = SampleData.getStoreById(storeId) ?: SampleData.stores.first()

        binding.tvStoreName.text = store.name
        binding.tvMenuName.text = "${store.menus.firstOrNull()?.name ?: "메뉴"} 외 ${(store.menus.size - 1).coerceAtLeast(0)}개"

        val originalTotal = store.menus.filter { !it.isSoldOut }.sumOf { it.originalPrice }
        val discountAmount = originalTotal - totalPrice
        val extraDiscount = 1000
        val finalPrice = (totalPrice - extraDiscount).coerceAtLeast(100)

        binding.tvOrderPrice.text = SampleData.formatPrice(originalTotal.coerceAtLeast(totalPrice))
        binding.tvDiscount.text = "-${SampleData.formatPrice(discountAmount)}"
        binding.tvExtraDiscount.text = "-${SampleData.formatPrice(extraDiscount)}"
        binding.tvFinalPrice.text = SampleData.formatPrice(finalPrice)
        binding.tvSavingsMessage.text = "${SampleData.formatPrice(discountAmount + extraDiscount)}을 절약하고 음식 ${store.menus.filter { !it.isSoldOut }.size}개를 구해요"
        binding.btnPay.text = "${SampleData.formatPrice(finalPrice)} 결제하고 음식 구하기"

        setupTimeSlots()

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.btnPay.setOnClickListener {
            binding.btnPay.isEnabled = false
            binding.btnPay.text = "결제 중..."
            binding.root.postDelayed({
                findNavController().navigate(R.id.action_payment_to_pickup)
            }, 1500)
        }
    }

    private fun setupTimeSlots() {
        val slots = listOf(binding.slot1, binding.slot2, binding.slot3, binding.slot4)
        slots.forEachIndexed { index, slot ->
            slot.setOnClickListener {
                selectedSlot = index + 1
                updateSlotSelection(slots)
            }
        }
        updateSlotSelection(slots)
    }

    private fun updateSlotSelection(slots: List<Button>) {
        slots.forEachIndexed { index, slot ->
            if (index + 1 == selectedSlot) {
                slot.setBackgroundResource(R.drawable.bg_time_slot_selected)
                slot.setTextColor(Color.parseColor("#FF5C2E"))
            } else {
                slot.setBackgroundResource(R.drawable.bg_time_slot)
                slot.setTextColor(Color.parseColor("#1A1A1A"))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
