package com.connectapp.domain.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet

/** Holds cold-start notification taps until the authenticated UI can consume them. */
class ReminderNavigation {
    data class Request(val eventId: String, val sequence: Long)
    private val mutableRequest = MutableStateFlow<Request?>(null)
    val request = mutableRequest.asStateFlow()
    private val sequence = MutableStateFlow(0L)

    fun open(eventId: String) {
        if (!eventId.matches(Regex("[1-9][0-9]{0,18}:[0-9]{1,19}"))) return
        mutableRequest.value = Request(eventId, sequence.updateAndGet { it + 1 })
    }

    fun consume(request: Request) {
        mutableRequest.compareAndSet(request, null)
    }
}
