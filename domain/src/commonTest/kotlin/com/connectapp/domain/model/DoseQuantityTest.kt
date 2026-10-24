package com.connectapp.domain.model

import kotlin.test.*

class DoseQuantityTest {
    @Test fun tabletFractionsAndIntegersHaveExactValues() {
        for ((text, minor) in listOf("¼" to 250L, "½" to 500L, "¾" to 750L, "1" to 1000L, "1,25" to 1250L, "2" to 2000L)) {
            val amount = DoseQuantity.parse(text, DoseUnit.TABLET)!!
            assertEquals(minor, amount.minorUnits)
            assertEquals(amount, DoseQuantity.parse(amount.toDecimalString(), DoseUnit.TABLET))
        }
    }

    @Test fun liquidDecimalsDoNotAccumulateFloatingPointError() {
        val amount = DoseQuantity.parse("0,1", DoseUnit.MILLILITER)!!
        assertEquals(1000L, amount.minorUnits * 10)
        assertEquals("0.1", amount.toDecimalString())
        assertEquals(12345L, DoseQuantity.parse("12.345", DoseUnit.MILLILITER)?.minorUnits)
        assertEquals("0.001", DoseQuantity.parse("0.0010", DoseUnit.MILLILITER)?.toDecimalString())
    }

    @Test fun invalidValuesCannotBeRoundedOrInterpretedAsAnotherUnit() {
        for (text in listOf("", "0", "-1", "NaN", "Infinity", "1e2", "1,2.3", ".5", "1/2", "1 mg", "0.0001")) {
            assertNull(DoseQuantity.parse(text, DoseUnit.TABLET), text)
            assertNull(DoseQuantity.parse(text, DoseUnit.MILLILITER), text)
        }
        assertNull(DoseQuantity.parse("0.3", DoseUnit.TABLET))
        assertNotNull(DoseQuantity.parse("0.3", DoseUnit.MILLILITER))
        assertNull(DoseQuantity.parse("½", DoseUnit.MILLILITER))
    }

    @Test fun overflowIsRejectedAndLargestExactLiquidAmountRoundTrips() {
        assertNull(DoseQuantity.parse("9223372036854775.808", DoseUnit.MILLILITER))
        assertNull(DoseQuantity.parse("9999999999999999999999999", DoseUnit.TABLET))
        assertEquals(Long.MAX_VALUE, DoseQuantity.parse("9223372036854775.807", DoseUnit.MILLILITER)?.minorUnits)
        assertEquals("9223372036854775.807", DoseQuantity.fromMinorUnits(Long.MAX_VALUE, DoseUnit.MILLILITER)?.toDecimalString())
    }

    @Test fun stableUnitCodesRecognizeHistoricalLabels() {
        for (value in listOf("Pastillas", "pills", "TABLET", "comprimidos")) assertEquals(DoseUnit.TABLET, DoseUnit.fromStored(value))
        assertEquals(DoseUnit.MILLILITER, DoseUnit.fromStored("mL"))
        assertNull(DoseUnit.fromStored("mg"))
        assertNull(DoseQuantity.fromMinorUnits(300, DoseUnit.TABLET))
    }
}
