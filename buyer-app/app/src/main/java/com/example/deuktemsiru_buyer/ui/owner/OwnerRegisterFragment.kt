package com.example.deuktemsiru_buyer.ui.owner

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.deuktemsiru_buyer.R
import com.example.deuktemsiru_buyer.databinding.FragmentOwnerRegisterBinding
import java.util.Calendar

class OwnerRegisterFragment : Fragment() {

    private var _binding: FragmentOwnerRegisterBinding? = null
    private val binding get() = _binding!!

    private var stockCount = 5
    private var selectedPreset = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOwnerRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvStockCount.text = stockCount.toString()

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.btnMinus.setOnClickListener {
            if (stockCount > 1) {
                stockCount--
                binding.tvStockCount.text = stockCount.toString()
            }
        }

        binding.btnPlus.setOnClickListener {
            stockCount++
            binding.tvStockCount.text = stockCount.toString()
        }

        setupPricePresets()
        setupTimePickers()
        setupValidation()

        binding.btnPreview.setOnClickListener {
            Toast.makeText(requireContext(), "미리보기: ${binding.etMenuName.text}", Toast.LENGTH_SHORT).show()
        }

        binding.btnRegister.setOnClickListener {
            Toast.makeText(requireContext(), "등록되었어요! 단골 손님에게 알림을 보내고 있어요 🔔", Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
        }

        binding.flPhotoMain.setOnClickListener {
            Toast.makeText(requireContext(), "사진 촬영 또는 앨범에서 선택", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupPricePresets() {
        val presets = listOf(
            binding.preset30 to 0.30,
            binding.preset50 to 0.50,
            binding.preset60 to 0.60,
            binding.preset70 to 0.70
        )

        presets.forEachIndexed { index, (btn, rate) ->
            btn.setOnClickListener {
                selectedPreset = index
                updatePresetSelection(presets.map { it.first })
                applyDiscount(rate)
            }
        }
    }

    private fun updatePresetSelection(buttons: List<Button>) {
        buttons.forEachIndexed { index, btn ->
            if (index == selectedPreset) {
                btn.setBackgroundResource(R.drawable.bg_chip_selected)
                btn.setTextColor(android.graphics.Color.WHITE)
            } else {
                btn.setBackgroundResource(R.drawable.bg_chip_unselected)
                btn.setTextColor(android.graphics.Color.parseColor("#1A1A1A"))
            }
        }
    }

    private fun applyDiscount(rate: Double) {
        val originalText = binding.etOriginalPrice.text.toString()
        if (originalText.isNotEmpty()) {
            val original = originalText.toLongOrNull() ?: return
            val discounted = (original * (1 - rate)).toLong()
            binding.etDiscountPrice.setText(discounted.toString())
        }
    }

    private fun setupTimePickers() {
        binding.btnPickupStart.setOnClickListener {
            showTimePicker { h, m ->
                binding.btnPickupStart.text = "%02d:%02d".format(h, m)
            }
        }

        binding.btnPickupEnd.setOnClickListener {
            showTimePicker { h, m ->
                binding.btnPickupEnd.text = "%02d:%02d".format(h, m)
            }
        }
    }

    private fun showTimePicker(onTimeSet: (Int, Int) -> Unit) {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hour, minute -> onTimeSet(hour, minute) },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun setupValidation() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateForm()
                if (selectedPreset >= 0) {
                    selectedPreset = -1
                    updatePresetSelection(listOf(binding.preset30, binding.preset50, binding.preset60, binding.preset70))
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        binding.etOriginalPrice.addTextChangedListener(watcher)
        binding.etDiscountPrice.addTextChangedListener(watcher)
    }

    private fun validateForm() {
        val hasPrice = binding.etOriginalPrice.text.isNotEmpty() &&
                binding.etDiscountPrice.text.isNotEmpty()
        binding.btnRegister.alpha = if (hasPrice) 1.0f else 0.5f
        binding.btnRegister.isEnabled = hasPrice
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
