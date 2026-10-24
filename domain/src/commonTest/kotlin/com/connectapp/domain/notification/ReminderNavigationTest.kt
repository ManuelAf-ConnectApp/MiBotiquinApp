package com.connectapp.domain.notification

import kotlin.test.*

class ReminderNavigationTest {
    @Test fun coldStartRetainsReferenceUntilExplicitlyConsumed() {
        val navigation = ReminderNavigation()
        navigation.open("17:1770000000000")
        val request = navigation.request.value!!
        assertEquals("17:1770000000000", request.eventId)
        navigation.consume(request)
        assertNull(navigation.request.value)
    }

    @Test fun repeatedOrDifferentTapsCannotBeLostByConsumingAnEarlierRequest() {
        val navigation = ReminderNavigation()
        navigation.open("17:1770000000000")
        val old = navigation.request.value!!
        navigation.open("17:1770000000000")
        val repeat = navigation.request.value!!
        assertNotEquals(old, repeat)
        navigation.consume(old)
        assertEquals(repeat, navigation.request.value)
        navigation.open("18:1770000000000")
        navigation.consume(repeat)
        assertEquals("18:1770000000000", navigation.request.value?.eventId)
    }

    @Test fun malformedPayloadCannotReplacePendingDestination() {
        val navigation = ReminderNavigation()
        navigation.open("17:1770000000000")
        val expected = navigation.request.value
        for (value in listOf("", "17", "https://example.com", "-1:2", "0:1", "1:2:3")) navigation.open(value)
        assertEquals(expected, navigation.request.value)
    }
}
