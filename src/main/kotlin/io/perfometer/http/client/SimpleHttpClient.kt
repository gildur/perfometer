package io.perfometer.http.client

import io.perfometer.dsl.RequestBuilder
import io.perfometer.http.HttpResponse
import io.perfometer.http.HttpStatus
import java.net.HttpURLConnection

class SimpleHttpClient(
        private val trustAllCertificates: Boolean,
) : HttpClient {

    override fun executeHttp(request: RequestBuilder): HttpResponse {
        var body: String? = null
        var connection: HttpURLConnection? = null
        try {
            connection = createHttpConnectionForRequest(request)
            connection.connect()
            connection.inputStream.bufferedReader().use {
                body = it.readText()
            }
        } catch (ignored: Exception) {
            ignored.printStackTrace()
        } finally {
            connection?.disconnect()
            val httpStatus = HttpStatus(connection?.responseCode ?: -1)
            return HttpResponse(httpStatus, body)
        }
    }

    private fun createHttpConnectionForRequest(request: RequestBuilder): HttpURLConnection {
        return httpConnection(request.protocol, request.host, request.port, request.pathWithParams()) {
            if (trustAllCertificates) {
                trustAllCertificates()
            }
            method(request.method.name)
            headers(request.headers())
            body(request.body())
            authorization(request.authorization)
        }
    }
}
