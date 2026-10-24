package com.connectapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CatalogPresentation(
    val nationalCode: String, val name: String, val registration: String?,
    val commercialized: Boolean?, val retrievedAt: Long
)
