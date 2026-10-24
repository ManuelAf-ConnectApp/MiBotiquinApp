package com.connectapp.data.repository

import com.connectapp.data.datasource.CatalogueHttpClient
import com.connectapp.domain.model.AppError
import com.connectapp.domain.model.CatalogPresentation
import com.connectapp.domain.repository.CatalogueRepository
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.*
import kotlin.time.Clock

class CatalogueRepositoryImpl(private val client: CatalogueHttpClient) : CatalogueRepository {
    override suspend fun presentation(nationalCode: String): Result<CatalogPresentation?> = try {
        if (!nationalCode.matches(Regex("[0-9]{6}"))) throw AppError.InvalidMedicationCode()
        val response = client.presentation(nationalCode)
        val result = when {
            response.status == 404 -> null
            response.status != 200 || response.body.length > 1_000_000 -> throw AppError.CatalogueUnavailable()
            else -> {
                val json = Json.parseToJsonElement(response.body)
                if (json == JsonNull) null else {
                    val record = json.jsonObject
                    val cn = record["cn"]?.jsonPrimitive?.contentOrNull
                    val name = record["nombre"]?.jsonPrimitive?.contentOrNull?.trim()
                    if (cn != nationalCode || name.isNullOrBlank() || name.length > 500) throw AppError.CatalogueUnavailable()
                    CatalogPresentation(cn, name, record["nregistro"]?.jsonPrimitive?.contentOrNull,
                        record["comerc"]?.jsonPrimitive?.booleanOrNull, Clock.System.now().toEpochMilliseconds())
                }
            }
        }
        Result.success(result)
    } catch (e: CancellationException) { throw e
    } catch (e: AppError) { Result.failure(e)
    } catch (e: Exception) { Result.failure(AppError.CatalogueUnavailable()) }
}
