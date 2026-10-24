package com.connectapp.data.catalogue

import com.connectapp.data.datasource.CatalogueHttpClient
import com.connectapp.data.datasource.CatalogueResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readBytes
import platform.Foundation.*
import platform.darwin.NSObject
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
class IOSCatalogueHttpClient : CatalogueHttpClient {
    override suspend fun presentation(nationalCode: String): CatalogueResponse {
        require(nationalCode.matches(Regex("[0-9]{6}")))
        return suspendCancellableCoroutine { continuation ->
            val configuration = NSURLSessionConfiguration.ephemeralSessionConfiguration()
            configuration.timeoutIntervalForRequest = 10.0
            configuration.timeoutIntervalForResource = 15.0
            val delegate = object : NSObject(), NSURLSessionTaskDelegateProtocol {
                override fun URLSession(session: NSURLSession, task: NSURLSessionTask,
                    willPerformHTTPRedirection: NSHTTPURLResponse, newRequest: NSURLRequest,
                    completionHandler: (NSURLRequest?) -> Unit) {
                    val url = newRequest.URL
                    completionHandler(newRequest.takeIf { url?.scheme == "https" && url.host == "cima.aemps.es" })
                }
            }
            val session = NSURLSession.sessionWithConfiguration(configuration, delegate, null)
            val url = NSURL.URLWithString("https://cima.aemps.es/cima/rest/presentacion/$nationalCode")!!
            val task = session.dataTaskWithURL(url) { data, response, error ->
                val status = (response as? NSHTTPURLResponse)?.statusCode?.toInt() ?: 0
                val body = if (error == null && status == 200 && data != null && data.length <= 1_000_000uL)
                    data.bytes?.readBytes(data.length.toInt())?.decodeToString() ?: "" else ""
                if (continuation.isActive) continuation.resume(CatalogueResponse(status, body))
                session.finishTasksAndInvalidate()
            }
            continuation.invokeOnCancellation { task.cancel(); session.invalidateAndCancel() }
            task.resume()
        }
    }
}
