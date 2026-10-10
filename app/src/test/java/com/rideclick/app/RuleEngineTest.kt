package com.rideclick.app

import org.junit.Assert.*
import org.junit.Test

class RuleEngineTest {
    private val rules = Rules()

    @Test fun acceptsMatchingRequest() {
        val r = RideRequest(Platform.JEENY, 6.0, 3, 2.0)
        assertTrue(RuleEngine.evaluate(r, rules, setOf(Platform.JEENY)).accepted)
    }

    @Test fun rejectsLowPrice() {
        val r = RideRequest(Platform.JEENY, 3.0, 3, 2.0)
        assertFalse(RuleEngine.evaluate(r, rules, setOf(Platform.JEENY)).accepted)
    }

    @Test fun rejectsHighEta() {
        val r = RideRequest(Platform.JEENY, 6.0, 6, 2.0)
        assertFalse(RuleEngine.evaluate(r, rules, setOf(Platform.JEENY)).accepted)
    }

    @Test fun rejectsLowPricePerKm() {
        val r = RideRequest(Platform.JEENY, 4.0, 3, 3.0)
        assertFalse(RuleEngine.evaluate(r, rules.copy(minPricePerKm = 1.5), setOf(Platform.JEENY)).accepted)
    }

    @Test fun acceptsPricePerKmAboveDefaultMinimum() {
        val r = RideRequest(Platform.JEENY, 4.0, 3, 3.0)
        assertTrue(RuleEngine.evaluate(r, rules, setOf(Platform.JEENY)).accepted)
    }

    @Test fun acceptsPricePerKmAtMinimum() {
        val r = RideRequest(Platform.JEENY, 4.0, 3, 2.0)
        assertTrue(RuleEngine.evaluate(r, rules.copy(minPricePerKm = 2.0), setOf(Platform.JEENY)).accepted)
    }

    @Test fun rejectsPricePerKmJustBelowMinimum() {
        val r = RideRequest(Platform.JEENY, 4.0, 3, 2.0)
        assertFalse(RuleEngine.evaluate(r, rules.copy(minPricePerKm = 2.01), setOf(Platform.JEENY)).accepted)
    }
}
