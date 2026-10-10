package com.rideclick.app

import org.junit.Assert.*
import org.junit.Test

class TextRequestParserTest {
    @Test fun parsesArabicRequest() {
        val r = TextRequestParser.parse("السعر 6.50 د.أ - الوصول 3 دقائق - المسافة 2.4 كم", Platform.JEENY)
        assertNotNull(r)
        assertEquals(6.5, r!!.price, 0.001)
        assertEquals(3, r.eta)
        assertEquals(2.4, r.distance, 0.001)
    }

    @Test fun rejectsIncompleteText() {
        assertNull(TextRequestParser.parse("السعر 6.50 د.أ فقط", Platform.JEENY))
    }

    @Test fun parsesArabicPriceAndMinuteFormats() {
        for (price in listOf("6.50 د.أ", "6,50 دأ", "دينار 6.50", "6.50 دينار")) {
            for (eta in listOf("3 دقائق", "3 دقيقة", "3 د")) {
                assertRequest("السعر $price - الوصول $eta - المسافة 2.4 كم")
            }
        }
    }

    @Test fun parsesEnglishPriceAndMinuteFormats() {
        for (price in listOf("JOD 6.50", "6.50 JOD", "jod 6,50")) {
            for (eta in listOf("3 min", "3 mins", "3 minute", "3 minutes", "3 MIN")) {
                assertRequest("Price $price - ETA $eta - Distance 2.4 km")
            }
        }
    }

    @Test fun currencyWithoutEtaDoesNotSupplyMinutes() {
        for (price in listOf("6.50 د.أ", "6.50 دأ", "6,50 د.أ", "6.50 د أ", "6.50 JOD")) {
            assertNull(price, TextRequestParser.parse("$price - 2.4 كم", Platform.JEENY))
        }
    }

    @Test fun rejectsDecimalEtaAndPartialMinuteUnits() {
        for (eta in listOf("2.5 min", "2,5 دقائق", "3 minimum", "3 دقائقنا")) {
            assertNull(eta, TextRequestParser.parse("6.50 JOD - $eta - 2.4 km", Platform.JEENY))
        }
    }

    @Test fun ignoresCurrencyEvenWhenEtaAppearsFirst() {
        assertRequest("3 دقائق - 6.50 د.أ - 2.4 كم")
    }

    private fun assertRequest(text: String) {
        val request = TextRequestParser.parse(text, Platform.JEENY)
        assertNotNull(text, request)
        assertEquals(text, 6.5, request!!.price, 0.001)
        assertEquals(text, 3, request.eta)
        assertEquals(text, 2.4, request.distance, 0.001)
        assertEquals(Platform.JEENY, request.platform)
    }
}
