package com.example.deuktemsiru_seller.domain.sales

import com.example.deuktemsiru_seller.domain.repository.AppRepository
import com.example.deuktemsiru_seller.domain.model.SalesApiResponse
import com.example.deuktemsiru_seller.domain.model.SalesPeriod
import com.example.deuktemsiru_seller.domain.model.TopMenu
import kotlinx.coroutines.CancellationException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SalesModel(private val api: AppRepository, private val sellerId: () -> Long) {
    suspend fun load(period: SalesPeriod, offset: Int, today: LocalDate = LocalDate.now()): SalesSummary? {
        val target = today.offsetBy(period, offset)
        if (period != SalesPeriod.Day) {
            val sales = api.getSales(sellerId(), period.apiValue, target.toString()).data ?: return null
            return periodSummary(sales, offset)
        }
        val salesByDate = listOf(2L, 1L, 0L).map { daysAgo ->
            val date = target.minusDays(daysAgo)
            val sales = try {
                api.getSales(sellerId(), SalesPeriod.Day.apiValue, date.toString()).data
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                null
            }
            date to sales
        }
        val entries = salesByDate.mapIndexed { index, (date, sales) ->
            val label = if (offset == 0) listOf("그제", "어제", "오늘")[index]
                else date.format(DateTimeFormatter.ofPattern("M/d"))
            SalesEntry(label, sales?.salesData?.firstOrNull()?.amount ?: 0, index == 2)
        }
        val latest = salesByDate.last().second
        return SalesSummary(entries.last().amount, latest?.todayOrderCount ?: 0, latest?.carbonSavedKg ?: 0f,
            latest?.topMenus.orEmpty(), entries)
    }
}

data class SalesEntry(val label: String, val amount: Int, val highlighted: Boolean)

data class SalesSummary(
    val total: Int,
    val orderCount: Int,
    val carbonSavedKg: Float,
    val topMenus: List<TopMenu>,
    val entries: List<SalesEntry>,
)

internal fun periodSummary(sales: SalesApiResponse, offset: Int) = SalesSummary(
    total = sales.salesData.sumOf { it.amount },
    orderCount = sales.todayOrderCount,
    carbonSavedKg = sales.carbonSavedKg ?: 0f,
    topMenus = sales.topMenus,
    entries = sales.salesData.mapIndexed { index, point ->
        SalesEntry(point.date, point.amount, offset == 0 && index == sales.salesData.lastIndex)
    },
)

fun LocalDate.offsetBy(period: SalesPeriod, offset: Int): LocalDate = when (period) {
    SalesPeriod.Day -> minusDays(offset.toLong())
    SalesPeriod.Month -> minusMonths(offset.toLong())
    SalesPeriod.Week -> minusWeeks(offset.toLong())
}
