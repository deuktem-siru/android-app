package com.example.deuktemsiru_buyer.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.deuktemsiru_buyer.R
import com.example.deuktemsiru_buyer.databinding.FragmentMypageBinding

class MyPageFragment : Fragment() {

    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvNickname.text = "정음식"
        binding.tvGrade.text = "🌱 새싹"
        binding.tvTotalSavings.text = "87,400원"
        binding.tvSavedFood.text = "23개"
        binding.tvMonthSavings.text = "12,300원"
        binding.tvCarbon.text = "4.6kg"
        binding.tvNextGrade.text = "7개 더 구하면 🌳"
        binding.tvCouponCount.text = "2"
        binding.tvPoints.text = "1,200P"
        binding.tvWishlistCount.text = "5"

        // Progress bar (76%)
        binding.progressGrade.post {
            val width = binding.progressGrade.parent
            val parentWidth = (binding.progressGrade.parent as View).width
            binding.progressGrade.layoutParams.width = (parentWidth * 0.76f).toInt()
            binding.progressGrade.requestLayout()
        }

        binding.menuOrders.setOnClickListener {
            Toast.makeText(requireContext(), "주문 내역", Toast.LENGTH_SHORT).show()
        }

        binding.menuPlaces.setOnClickListener {
            Toast.makeText(requireContext(), "자주 가는 위치", Toast.LENGTH_SHORT).show()
        }

        binding.menuNotifications.setOnClickListener {
            Toast.makeText(requireContext(), "알림 설정", Toast.LENGTH_SHORT).show()
        }

        binding.menuSupport.setOnClickListener {
            Toast.makeText(requireContext(), "고객 문의", Toast.LENGTH_SHORT).show()
        }

        binding.menuOwner.setOnClickListener {
            findNavController().navigate(R.id.action_mypage_to_ownerRegister)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
