package ronsijm.templater.handlers.web

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext
import java.net.URI
import java.net.http.HttpRequest

@RegisterHandler(
    module = "web",
    description = "Makes HTTP requests with optional JSON path extraction",
    example = "request('https://api.example.com/data', 'result.value')"
)
class RequestHandler : CommandHandler<RequestRequest, String> {
    companion object {
        private const val ERROR_INVALID_JSON_PATH = "Invalid JSON path"
    }

    override fun handle(request: RequestRequest, context: TemplateContext): String {
        require(request.url.isNotEmpty()) { "URL required for tp.web.request" }

        val responseBody = fetchUrl(request.url, context)

        return if (request.jsonPath.isEmpty()) {
            responseBody
        } else {
            extractJsonPath(responseBody, request.jsonPath)
        }
    }

    private fun fetchUrl(url: String, context: TemplateContext): String {
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build()

        return context.services.httpService.send(httpRequest)
    }

    private fun extractJsonPath(jsonString: String, path: String): String {
        val json = JsonParser.parseString(jsonString)
        val pathParts = path.split(".")

        var current = json
        for (part in pathParts) {
            current = navigateJsonPath(current, part)
                ?: throw IllegalArgumentException(ERROR_INVALID_JSON_PATH)
        }

        return formatJsonValue(current)
    }

    private fun navigateJsonPath(element: JsonElement, part: String): JsonElement? {
        return when {
            part.toIntOrNull() != null && element.isJsonArray -> {
                val index = part.toInt()
                val array = element.asJsonArray
                if (index >= 0 && index < array.size()) {
                    array.get(index)
                } else {
                    null
                }
            }
            element.isJsonObject -> {
                element.asJsonObject.get(part)
            }
            else -> null
        }
    }

    private fun formatJsonValue(element: JsonElement): String {
        return when {
            element.isJsonPrimitive -> element.asString
            element.isJsonObject -> element.toString()
            element.isJsonArray -> element.toString()
            else -> element.toString()
        }
    }
}
