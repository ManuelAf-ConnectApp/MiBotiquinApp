package com.connectapp.domain.catalogue

import kotlin.test.*

class SpanishMedicationCodeTest {
    @Test fun explicitNationalCodeRetainsLeadingZeroes() {
        assertEquals("001234", SpanishMedicationCode.nationalCode(" 001234 "))
        assertNull(SpanishMedicationCode.nationalCode("123456.8"))
    }
    @Test fun supportedSpanishEanAndNtinExtractOnlyNationalCode() {
        assertEquals("123456", SpanishMedicationCode.nationalCode("8470001234568"))
        assertEquals("123456", SpanishMedicationCode.nationalCode("08470001234568"))
        assertNull(SpanishMedicationCode.nationalCode("8470001234569"))
        assertNull(SpanishMedicationCode.nationalCode("08470001234569"))
    }
    @Test fun gs1DataMatrixDoesNotExposeOrUploadLotAndSerialFields() {
        assertEquals("123456", SpanishMedicationCode.nationalCode("]d201084700012345681726103110LOT\u001D21PRIVATE-SERIAL"))
        assertEquals("123456", SpanishMedicationCode.nationalCode("(01)08470001234568(17)261031(10)LOT(21)SERIAL"))
    }
    @Test fun foreignOrAmbiguousCodesAreNotGuessed() {
        for (value in listOf("4006381333931", "https://example.com/123456", "(01)04006381333931(712)1234568", "x8470001234568", "", "12345", "1234567")) {
            assertNull(SpanishMedicationCode.nationalCode(value), value)
        }
    }
}
