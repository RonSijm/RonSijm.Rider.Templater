package ronsijm.templater.services

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

interface HttpService {
    fun send(request: HttpRequest): String
}

class DefaultHttpService(
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()
) : HttpService {
    override fun send(request: HttpRequest) = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body()
}

