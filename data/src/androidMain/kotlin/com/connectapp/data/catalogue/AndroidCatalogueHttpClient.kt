package com.connectapp.data.catalogue

import com.connectapp.data.datasource.CatalogueHttpClient
import com.connectapp.data.datasource.CatalogueResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

class AndroidCatalogueHttpClient : CatalogueHttpClient {
    override suspend fun presentation(nationalCode: String): CatalogueResponse = withContext(Dispatchers.IO) {
        require(nationalCode.matches(Regex("[0-9]{6}")))
        val connection = URL("https://cima.aemps.es/cima/rest/presentacion/$nationalCode").openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 10_000; connection.readTimeout = 10_000
            connection.instanceFollowRedirects = false; connection.useCaches = false
            connection.setRequestProperty("Accept", "application/json")
            val status = connection.responseCode
            if (status != 200) CatalogueResponse(status, "") else {
                val bytes = ByteArrayOutputStream()
                connection.inputStream.use { input ->
                    val buffer = ByteArray(8192)
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        require(bytes.size() + read <= 1_000_000)
                        bytes.write(buffer, 0, read)
                    }
                }
                CatalogueResponse(status, bytes.toString("UTF-8"))
            }
        } finally { connection.disconnect() }
    }
}
