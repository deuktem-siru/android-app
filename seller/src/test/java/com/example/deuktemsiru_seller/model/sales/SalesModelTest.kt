package com.example.deuktemsiru_seller.model.sales

import com.example.deuktemsiru_seller.domain.sales.*

import com.example.deuktemsiru_seller.domain.model.DailySales
import com.example.deuktemsiru_seller.domain.model.SalesApiResponse
import com.example.deuktemsiru_seller.domain.model.SalesPeriod
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class SalesModelTest {
    @Test fun `summary totals all chart points and highlights only the current period`() {
        val response = SalesApiResponse(todayOrderCount = 3,
            salesData = listOf(DailySales("월", 1000), DailySales("화", 2000)))
        val current = periodSummary(response, 0)
        assertEquals(3000, current.total)
        assertEquals(3, current.orderCount)
        assertEquals(0f, current.carbonSavedKg, 0f)
        assertEquals(listOf(false, true), current.entries.map { it.highlighted })
        assertTrue(periodSummary(response, 1).entries.none { it.highlighted })
        assertEquals(0, periodSummary(SalesApiResponse(), 0).total)
    }

    @Test fun `period offsets handle month and year boundaries`() {
        val date = LocalDate.of(2026, 1, 31)
        assertEquals(LocalDate.of(2025, 12, 31), date.offsetBy(SalesPeriod.Month, 1))
        assertEquals(LocalDate.of(2026, 1, 24), date.offsetBy(SalesPeriod.Week, 1))
        assertEquals(LocalDate.of(2026, 1, 30), date.offsetBy(SalesPeriod.Day, 1))
    }
}
