package com.example.deuktemsiru_seller.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PickupTimeStateTest {

    @Test
    fun `moving start preserves valid end and applies the screen minimum otherwise`() {
        val state = PickupTimeState(startMinutes = 16 * 60, endMinutes = 18 * 60)
        state.updateStart(17 * 60, minDurationMinutes = 30)
        assertEquals(18 * 60, state.endMinutes)
        state.updateStart(18 * 60, minDurationMinutes = 30)
        assertEquals(18 * 60 + 30, state.endMinutes)
        state.updateStart(19 * 60, minDurationMinutes = 60)
        assertEquals(20 * 60, state.endMinutes)
    }

    @Test
    fun `invalid end leaves the current selection unchanged`() {
        val state = PickupTimeState(startMinutes = 18 * 60, endMinutes = 20 * 60)
        assertFalse(state.updateEnd(17 * 60))
        assertFalse(state.updateEnd(18 * 60))
        assertEquals(20 * 60, state.endMinutes)
        assertTrue(state.updateEnd(19 * 60))
        assertEquals(19 * 60, state.endMinutes)
    }

    @Test
    fun `automatic end stays within the same day`() {
        val state = PickupTimeState()
        state.updateStart(23 * 60 + 45, minDurationMinutes = 30)
        assertEquals(PickupTimeState.LAST_MINUTE_OF_DAY, state.endMinutes)
        state.setDuration(60)
        assertEquals(PickupTimeState.LAST_MINUTE_OF_DAY, state.endMinutes)
    }
}
