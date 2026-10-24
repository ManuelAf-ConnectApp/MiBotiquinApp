package com.connectapp.domain.repository

import com.connectapp.domain.model.CatalogPresentation

interface CatalogueRepository {
    /** Explicit public lookup; receives no account, history, serial number or image. */
    suspend fun presentation(nationalCode: String): Result<CatalogPresentation?>
}
