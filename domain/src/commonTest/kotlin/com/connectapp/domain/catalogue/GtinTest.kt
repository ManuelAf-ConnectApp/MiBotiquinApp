package com.connectapp.domain.catalogue

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GtinTest {
    @Test fun acceptsValidGtinsAndGs1Ai01() {
        assertEquals("9501101530003", Gtin.normalize("9501101530003"))
        assertEquals("09501101530003", Gtin.normalize("(01)09501101530003(10)LOT"))
    }
    @Test fun rejectsBadCheckDigitsAndUnsupportedLengths() {
        assertNull(Gtin.normalize("9501101530004"))
        assertNull(Gtin.normalize("123456"))
    }
}
