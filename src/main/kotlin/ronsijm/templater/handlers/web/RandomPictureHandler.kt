package ronsijm.templater.handlers.web

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.utils.ErrorMessages

@RegisterHandler(
    module = "web",
    description = "Gets random picture from Unsplash",
    example = "random_picture('1600x900', 'nature', false)"
)
class RandomPictureHandler : CommandHandler<RandomPictureRequest, String> {
    companion object {
        private const val DEFAULT_WIDTH = "1600"
        private const val DEFAULT_HEIGHT = "900"
        private const val UNSPLASH_BASE_URL = "https://source.unsplash.com/random"
    }

    override fun handle(request: RandomPictureRequest, context: TemplateContext): String {
        return try {
            val (width, height) = parseDimensions(request.size)
            val url = buildUnsplashUrl(width, height, request.query)
            formatMarkdownImage(url, width, height, request.query, request.includeSize)
        } catch (e: Exception) {
            ErrorMessages.pictureError(e.message ?: "Unknown error")
        }
    }

    private fun parseDimensions(size: String): Pair<String, String> {
        return if (size.contains("x")) {
            val parts = size.split("x")
            parts[0] to parts[1]
        } else {
            DEFAULT_WIDTH to DEFAULT_HEIGHT
        }
    }

    private fun buildUnsplashUrl(width: String, height: String, query: String): String {
        val baseUrl = "$UNSPLASH_BASE_URL/${width}x${height}"
        return if (query.isNotEmpty()) {
            "$baseUrl?${query.split(",").joinToString(",")}"
        } else {
            baseUrl
        }
    }

    private fun formatMarkdownImage(url: String, width: String, height: String, query: String, includeSize: Boolean): String {
        return if (includeSize) {
            "![$query]($url|${width}x${height})"
        } else {
            "![$query]($url)"
        }
    }
}

