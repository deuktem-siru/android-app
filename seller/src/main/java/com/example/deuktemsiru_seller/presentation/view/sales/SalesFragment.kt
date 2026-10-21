package com.example.deuktemsiru_seller.presentation.view.sales
import com.example.deuktemsiru_seller.presentation.widget.sales.BarChartView
import com.example.deuktemsiru_seller.presentation.widget.sales.PieChartView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.deuktemsiru_seller.R
import com.example.deuktemsiru_seller.data.repository.SessionManager
import com.example.deuktemsiru_seller.data.repository.appSession
import com.example.deuktemsiru_seller.databinding.FragmentSalesBinding
import com.example.deuktemsiru_seller.domain.model.SalesPeriod
import com.example.deuktemsiru_seller.domain.model.TopMenu
import com.example.deuktemsiru_seller.util.emptyTextView
import com.example.deuktemsiru_seller.util.handleSellerAuthFailure
import com.example.deuktemsiru_seller.domain.sales.offsetBy
import com.example.deuktemsiru_seller.util.renderChildren
import com.example.deuktemsiru_seller.util.toast
import com.example.deuktemsiru_seller.util.toWon
import com.example.deuktemsiru_seller.util.visibleIf
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SalesFragment : Fragment() {

    private var _binding: FragmentSalesBinding? = null
    private val binding get() = _binding!!

    private val model by lazy { com.example.deuktemsiru_seller.AppGraph.sales(this) }

    private val currentPeriod get() = model.state.value.period
    private val currentOffset get() = model.state.value.offset
    private val showPieChart get() = model.state.value.pieChart
    private lateinit var session: SessionManager

    private companion object {
        const val TAG = "SalesFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        session = requireContext().appSession

        binding.tabDaily.setOnClickListener { switchPeriod(SalesPeriod.Day) }
        binding.tabWeekly.setOnClickListener { switchPeriod(SalesPeriod.Week) }
        binding.tabMonthly.setOnClickListener { switchPeriod(SalesPeriod.Month) }
        binding.btnToggleChart.setOnClickListener { toggleChartMode() }

        binding.btnPrev.setOnClickListener { if (session.isLoggedIn()) model.move(1) }
        binding.btnNext.setOnClickListener { if (session.isLoggedIn() && currentOffset > 0) model.move(-1) }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                model.state.collect { state ->
                    updateTabs()
                    updatePeriodLabel()
                    renderChartMode()
                    binding.tvLoadingIndicator.visibleIf(state.loading)
                    state.summary?.let { summary ->
                        binding.tvTotalSales.text = summary.total.toWon()
                        binding.barChart.setEntries(summary.entries.map { BarChartView.Entry(it.label, it.amount, it.highlighted) })
                        renderSalesSummary(summary.orderCount, summary.carbonSavedKg, summary.topMenus, summary.entries.map { it.amount })
                    }
                    state.error?.let { error ->
                        if (!handleSellerAuthFailure(error, session)) {
                            if (error is HttpException && error.code() == 404) renderEmptySales()
                            else toast("데이터를 불러올 수 없어요.")
                        }
                        model.errorShown()
                    }
                }
            }
        }
        if (session.isLoggedIn()) model.load()
    }

    private fun switchPeriod(period: SalesPeriod) { model.selectPeriod(period) }

    private fun updateTabs() {
        val tabs = listOf(
            binding.tabDaily to SalesPeriod.Day,
            binding.tabWeekly to SalesPeriod.Week,
            binding.tabMonthly to SalesPeriod.Month,
        )
        tabs.forEach { (tab, period) ->
            if (period == currentPeriod) {
                tab.setTextColor(requireContext().getColor(R.color.white))
                tab.setBackgroundResource(R.drawable.bg_button_primary)
            } else {
                tab.setTextColor(requireContext().getColor(R.color.text_muted))
                tab.background = null
            }
        }
        binding.tvSalesTitle.text = currentPeriod.title
    }

    private fun targetDate(): LocalDate = LocalDate.now().offsetBy(currentPeriod, currentOffset)

    private fun renderEmptySales() {
        binding.tvTotalSales.text = 0.toWon()
        binding.barChart.setEntries(emptyList())
        renderSalesSummary(orderCount = 0, carbonSavedKg = 0f, topMenus = emptyList(), amounts = emptyList())
    }

    private fun renderSalesSummary(
        orderCount: Int,
        carbonSavedKg: Float,
        topMenus: List<TopMenu>,
        amounts: List<Int>,
    ) {
        binding.tvWasteCount.text = "절감 폐기 ${orderCount}개"
        binding.tvCo2Amount.text = "탄소 ${carbonSavedKg.toInt()}kg 절감"
        renderTopMenus(topMenus)
        binding.pieChart.setSlices(topMenus.map { PieChartView.Slice(it.name, it.count) })
        updateInsight(amounts, topMenus)
    }

    private fun updatePeriodLabel() {
        binding.tvPeriodRange.text = when (currentPeriod) {
            SalesPeriod.Day -> {
                targetDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            }
            SalesPeriod.Month -> {
                val month = targetDate()
                "${month.year}년 ${month.monthValue}월"
            }
            else -> {
                val endDay = targetDate()
                val startDay = endDay.minusDays(6)
                val fmt = DateTimeFormatter.ofPattern("MM.dd")
                "${startDay.format(fmt)} – ${endDay.format(fmt)}"
            }
        }
        binding.btnNext.alpha = if (currentOffset > 0) 1f else 0.3f
    }

    private fun toggleChartMode() { model.toggleChart() }

    private fun renderChartMode() {
        binding.containerTopMenus.visibleIf(!showPieChart)
        binding.pieChart.visibleIf(showPieChart)
        binding.btnToggleChart.text = if (showPieChart) "TOP 3 목록" else "파이차트"
    }

    private fun renderTopMenus(menus: List<TopMenu>) {
        binding.containerTopMenus.renderChildren(
            items = menus.withIndex().toList(),
            emptyView = {
                requireContext().emptyTextView("아직 판매 데이터가 없어요", verticalPaddingDp = 14)
            },
        ) { (i, menu) ->
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_top_menu, binding.containerTopMenus, false)
            itemView.findViewById<TextView>(R.id.tvRank).apply {
                text = "${i + 1}"
                setBackgroundResource(if (i == 0) R.drawable.bg_rounded_primary_light else R.drawable.bg_rounded_muted)
                setTextColor(requireContext().getColor(if (i == 0) R.color.primary_dark else R.color.text_muted))
            }
            itemView.findViewById<TextView>(R.id.tvMenuEmoji).text = menu.emoji ?: "🍽️"
            itemView.findViewById<TextView>(R.id.tvMenuName).text = menu.name
            itemView.findViewById<TextView>(R.id.tvMenuCount).text = "${menu.count}건 판매"
            if (i < menus.lastIndex) {
                itemView.findViewById<View>(R.id.dividerMenu).visibility = View.VISIBLE
            }
            itemView
        }
    }

    private fun updateInsight(amounts: List<Int>, topMenus: List<TopMenu>) {
        binding.tvInsight.text = when {
            amounts.all { it == 0 } -> "💡 아직 판매 데이터가 없어요. 첫 번째 메뉴를 등록해보세요!"
            topMenus.isNotEmpty() -> "💡 ${topMenus.first().name}이(가) 가장 잘 팔려요. 재고를 충분히 준비해보세요!"
            else -> "💡 꾸준한 할인 메뉴 등록이 단골 고객을 만들어요!"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
