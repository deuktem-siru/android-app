package com.example.deuktemsiru_buyer.ui.pickup

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.deuktemsiru_buyer.data.SampleData
import com.example.deuktemsiru_buyer.databinding.FragmentPickupBinding

class PickupFragment : Fragment() {

    private var _binding: FragmentPickupBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())
    private var remainingSeconds = 42 * 60
    private var timerRunnable: Runnable? = null

    private val pickupCode = "A42K"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPickupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvPickupTime.text = "5시 30분까지"
        binding.tvPickupCode.text = pickupCode.chunked(1).joinToString(" ")
        binding.tvStoreName.text = "파리바게뜨 정왕점"
        binding.tvStoreAddress.text = "경기도 시흥시 정왕동 1234-56"
        binding.tvOrderMenu.text = "아침 세트 A"
        binding.tvPaidPrice.text = SampleData.formatPrice(5000)

        startCountdown()

        binding.llCode.setOnLongClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("픽업코드", pickupCode))
            Toast.makeText(requireContext(), "픽업 코드가 복사되었어요", Toast.LENGTH_SHORT).show()
            true
        }

        binding.btnDirections.setOnClickListener {
            Toast.makeText(requireContext(), "지도 앱으로 연결됩니다", Toast.LENGTH_SHORT).show()
        }

        binding.btnCall.setOnClickListener {
            Toast.makeText(requireContext(), "031-123-4567", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCountdown() {
        timerRunnable = object : Runnable {
            override fun run() {
                if (remainingSeconds > 0) {
                    val mins = remainingSeconds / 60
                    val secs = remainingSeconds % 60
                    binding.tvCountdown.text = "%02d:%02d".format(mins, secs)
                    remainingSeconds--
                    handler.postDelayed(this, 1000)
                } else {
                    binding.tvCountdown.text = "00:00"
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
