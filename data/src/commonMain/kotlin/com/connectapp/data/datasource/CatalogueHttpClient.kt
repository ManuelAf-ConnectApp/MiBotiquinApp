package com.connectapp.data.datasource

data class CatalogueResponse(val status: Int, val body: String)
interface CatalogueHttpClient {
    suspend fun presentation(nationalCode: String): CatalogueResponse
}
