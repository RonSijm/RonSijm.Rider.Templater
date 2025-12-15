package ronsijm.templater.handlers.web

import com.google.gson.JsonParser
import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.utils.ErrorMessages
import java.net.URI
import java.net.http.HttpRequest

@RegisterHandler(
    module = "web",
    description = "Fetches daily quote from quotable.io API",
    example = "daily_quote()"
)
class DailyQuoteHandler : CommandHandler<DailyQuoteRequest, String> {
    override fun handle(request: DailyQuoteRequest, context: TemplateContext): String {
        return try {
            val httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.quotable.io/random"))
                .GET()
                .build()

            val responseBody = context.services.httpService.send(httpRequest)

            val json = JsonParser.parseString(responseBody).asJsonObject

            val quote = json.get("content").asString
            val author = json.get("author").asString

            formatQuote(quote, author)
        } catch (e: Exception) {
            ErrorMessages.quoteError(e.message ?: "Unknown error")
        }
    }

    private fun formatQuote(quote: String, author: String): String {
        return "> [!quote] Daily Quote\n> $quote\n>\n> — $author"
    }
}

