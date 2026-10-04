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
}
