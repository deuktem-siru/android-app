package com.example.deuktemsiru_buyer.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeLabelTest {
    @Test fun pickupLabelsKeepTheSame24HourTimeAsTheSlot() {
        assertEquals("22:30", "22:30".toHourMinute())
        assertEquals("00:00", "00:00:00".toHourMinute())
        assertEquals("18:30", "2026-11-11T18:30:00".toHourMinute())
    }
}
