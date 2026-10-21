package com.example.deuktemsiru_seller.presentation.view.mypage

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.databinding.ActivitySettlementBinding
import com.example.deuktemsiru_seller.data.remote.RetrofitClient
import com.example.deuktemsiru_seller.domain.model.SettlementWithdrawRequest
import com.example.deuktemsiru_seller.util.applySystemBarInsets
import com.example.deuktemsiru_seller.util.toast
import com.example.deuktemsiru_seller.util.toWon
import kotlinx.coroutines.launch
import java.time.LocalDate

class SettlementActivity : AppCompatActivity() {

    private val screenModel by lazy { com.example.deuktemsiru_seller.AppGraph.model(this) }

    private lateinit var binding: ActivitySettlementBinding
    private var currentOffset = 0
    private var currentSettlementAmount: Int? = null
    private var currentStatus: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettlementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.applySystemBarInsets()

        binding.btnBack.setOnClickListener { finish() }

        binding.btnPrevMonth.setOnClickListener {
            currentOffset++
            loadSettlement()
        }

        binding.btnNextMonth.setOnClickListener {
            if (currentOffset > 0) {
                currentOffset--
                loadSettlement()
            }
        }

        binding.btnWithdraw.setOnClickListener { showWithdrawDialog() }

        loadSettlement()
    }

    private fun loadSettlement() {
        val target = LocalDate.now().minusMonths(currentOffset.toLong())
        binding.tvMonthLabel.text = "${target.year}년 ${target.monthValue}월"
        binding.btnNextMonth.alpha = if (currentOffset > 0) 1f else 0.3f

        binding.tvTotalSales.text = "—"
        binding.tvCommission.text = "—"
        binding.tvSettlementAmount.text = "—"
        binding.tvSettlementStatus.text = "—"
        currentSettlementAmount = null
        currentStatus = null

        lifecycleScope.launch {
            runCatching {
                val settlementItem = screenModel.getSettlements(
                    sellerId = RetrofitClient.memberId,
                    year = target.year,
                    month = target.monthValue,
                ).data?.settlements?.firstOrNull()

                val total = settlementItem?.totalSales ?: 0
                val commission = settlementItem?.platformFee ?: 0
                val settlement = settlementItem?.settlementAmount ?: 0

                currentSettlementAmount = settlement
                currentStatus = settlementItem?.status
                binding.tvTotalSales.text = total.toWon()
                binding.tvCommission.text = commission.toWon()
                binding.tvSettlementAmount.text = settlement.toWon()
                binding.tvSettlementStatus.text = statusLabel(settlementItem?.status)
            }.onFailure {
                toast("데이터를 불러올 수 없어요.")
            }
        }
    }

    private fun showWithdrawDialog() {
        val numericAmount = currentSettlementAmount
        if (numericAmount == null) {
            toast("정산 데이터를 불러오는 중이에요.")
            return
        }
        if (numericAmount <= 0) {
            toast("출금 가능한 금액이 없어요.")
            return
        }
        if (currentStatus == "COMPLETED") {
            toast("이미 지급이 완료된 정산이에요.")
            return
        }
        val amount = numericAmount.toWon()
        AlertDialog.Builder(this)
            .setTitle("출금 신청")
            .setMessage("정산 예정금액 ${amount}을 출금 신청하시겠어요?\n\n영업일 기준 3–5일 이내 등록된 계좌로 입금됩니다.")
            .setPositiveButton("신청하기") { _, _ ->
                val target = LocalDate.now().minusMonths(currentOffset.toLong())
                lifecycleScope.launch {
                    runCatching {
                        screenModel.requestWithdrawal(
                            RetrofitClient.memberId,
                            SettlementWithdrawRequest(target.year, target.monthValue)
                        )
                    }.onSuccess {
                        toast("출금 신청이 완료됐어요.")
                        loadSettlement()
                    }.onFailure {
                        toast("출금 신청에 실패했어요.")
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun statusLabel(status: String?) = when (status) {
        "COMPLETED" -> "지급 완료"
        "REJECTED" -> "반려됨 — 다시 신청할 수 있어요."
        "PENDING" -> "신청 대기"
        else -> "—"
    }
}
