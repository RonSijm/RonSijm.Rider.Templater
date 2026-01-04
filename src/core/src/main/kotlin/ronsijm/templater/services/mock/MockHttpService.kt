package ronsijm.templater.services.mock

import ronsijm.templater.services.HttpService

class MockHttpService(private val responses: MutableMap<String, String> = mutableMapOf()) : HttpService {
    override fun send(request: java.net.http.HttpRequest) = responses[request.uri().toString()] ?: "{}"
    fun addResponse(url: String, response: String) { responses[url] = response }
}
