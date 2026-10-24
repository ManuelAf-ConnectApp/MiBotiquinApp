package com.connectapp.data.repository

import com.connectapp.data.datasource.CatalogueHttpClient
import com.connectapp.data.datasource.CatalogueResponse
import com.connectapp.domain.model.AppError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class CatalogueRepositoryTest {
    private class Client(var response: CatalogueResponse) : CatalogueHttpClient {
        val requested = mutableListOf<String>()
        override suspend fun presentation(nationalCode: String): CatalogueResponse { requested += nationalCode; return response }
    }
    @Test fun matchingPresentationIsVerifiedWithoutInferringPrescriptionOrStock() = runBlocking<Unit> {
        val client = Client(CatalogueResponse(200, """{"cn":"123456","nombre":"TEST 500 MG, 20 COMPRIMIDOS","nregistro":"12345","comerc":true,"dosis":"500 mg","unidades":20}"""))
        val found = CatalogueRepositoryImpl(client).presentation("123456").getOrThrow()!!
        assertEquals("123456", found.nationalCode)
        assertEquals("TEST 500 MG, 20 COMPRIMIDOS", found.name)
        assertEquals(listOf("123456"), client.requested)
        assertEquals(true, found.commercialized)
        assertTrue(found.retrievedAt > 0)
    }
    @Test fun unknownAndUnavailableAreDistinctAndMismatchedCodesAreNeverAccepted() = runBlocking<Unit> {
        val client = Client(CatalogueResponse(404, ""))
        val repository = CatalogueRepositoryImpl(client)
        assertNull(repository.presentation("123456").getOrThrow())
        for (response in listOf(CatalogueResponse(503,""), CatalogueResponse(200,"[]"), CatalogueResponse(200,"""{"cn":"654321","nombre":"Other"}"""), CatalogueResponse(200,"""{"cn":"123456","nombre":""}"""))) {
            client.response = response
            assertIs<AppError.CatalogueUnavailable>(repository.presentation("123456").exceptionOrNull())
        }
    }
    @Test fun invalidInputCannotReachNetworkAndCancellationPropagates() = runBlocking<Unit> {
        val client = Client(CatalogueResponse(200,"{}"))
        assertIs<AppError.InvalidMedicationCode>(CatalogueRepositoryImpl(client).presentation("123456&user=someone").exceptionOrNull())
        assertTrue(client.requested.isEmpty())
        val cancelled = object : CatalogueHttpClient {
            override suspend fun presentation(nationalCode: String): CatalogueResponse = throw CancellationException("Cancelled")
        }
        assertFailsWith<CancellationException> { CatalogueRepositoryImpl(cancelled).presentation("123456") }
    }
}
