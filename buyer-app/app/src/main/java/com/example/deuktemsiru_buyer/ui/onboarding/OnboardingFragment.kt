package com.example.deuktemsiru_buyer.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.deuktemsiru_buyer.R
import com.example.deuktemsiru_buyer.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateStartButton()

        binding.cbAll.setOnCheckedChangeListener { _, isChecked ->
            binding.cbTerms.isChecked = isChecked
            binding.cbPrivacy.isChecked = isChecked
            updateStartButton()
        }

        binding.cbTerms.setOnCheckedChangeListener { _, _ ->
            updateAllCheckbox()
            updateStartButton()
        }

        binding.cbPrivacy.setOnCheckedChangeListener { _, _ ->
            updateAllCheckbox()
        }

        binding.tvTermsDetail.setOnClickListener {
            Toast.makeText(requireContext(), "이용약관 전문입니다.", Toast.LENGTH_SHORT).show()
        }

        binding.tvPrivacyDetail.setOnClickListener {
            Toast.makeText(requireContext(), "개인정보 처리방침 전문입니다.", Toast.LENGTH_SHORT).show()
        }

        binding.btnStart.setOnClickListener {
            if (binding.cbTerms.isChecked) {
                findNavController().navigate(R.id.action_onboarding_to_home)
            }
        }
    }

    private fun updateAllCheckbox() {
        val allChecked = binding.cbTerms.isChecked && binding.cbPrivacy.isChecked
        binding.cbAll.setOnCheckedChangeListener(null)
        binding.cbAll.isChecked = allChecked
        binding.cbAll.setOnCheckedChangeListener { _, isChecked ->
            binding.cbTerms.isChecked = isChecked
            binding.cbPrivacy.isChecked = isChecked
            updateStartButton()
        }
    }

    private fun updateStartButton() {
        val enabled = binding.cbTerms.isChecked
        binding.btnStart.alpha = if (enabled) 1.0f else 0.4f
        binding.btnStart.isEnabled = enabled
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
