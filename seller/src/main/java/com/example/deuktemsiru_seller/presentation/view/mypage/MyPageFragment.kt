package com.example.deuktemsiru_seller.presentation.view.mypage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.deuktemsiru_seller.MainActivity
import com.example.deuktemsiru_seller.data.repository.appSession
import com.example.deuktemsiru_seller.databinding.FragmentMyPageBinding
import com.example.deuktemsiru_seller.presentation.view.settings.NotificationSettingsActivity
import com.example.deuktemsiru_seller.presentation.view.store.StoreFragment

class MyPageFragment : Fragment() {

    private var _binding: FragmentMyPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val session = requireContext().appSession

        binding.tvStoreName.text = session.nickname.ifBlank { "내 가게" }
        binding.tvStoreEmoji.text = "🏪"

        binding.itemStoreInfo.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.example.deuktemsiru_seller.R.id.fragment_container, StoreFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.itemAccountInfo.setOnClickListener {
            startActivity(Intent(requireContext(), AccountInfoActivity::class.java))
        }

        binding.itemNotification.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationSettingsActivity::class.java))
        }

        binding.itemSettlement.setOnClickListener {
            startActivity(Intent(requireContext(), SettlementActivity::class.java))
        }

        binding.itemCustomerService.setOnClickListener {
            showInfoDialog("고객센터", "운영시간: 평일 09:00 – 18:00\n전화: 1588-0000\n이메일: help@siru.co.kr")
        }

        binding.itemPolicy.setOnClickListener {
            showInfoDialog(
                "서비스 이용약관",
                "득템시루 판매자 서비스 이용약관\n\n판매자는 본 서비스를 통해 마감 할인 상품을 등록하고 관리할 수 있습니다.\n\n자세한 약관은 공식 웹사이트를 참고해주세요.",
            )
        }

        binding.itemLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("로그아웃")
                .setMessage("로그아웃 하시겠어요?")
                .setPositiveButton("로그아웃") { _, _ ->
                    (activity as? MainActivity)?.logout()
                }
                .setNegativeButton("취소", null)
                .show()
        }

        binding.tvVersion.text = "득템시루 판매자 v1.0  |  Member #${session.memberId}"
    }

    private fun showInfoDialog(title: String, message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("확인", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
