package ronsijm.templater.handlers.date

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.modules.date.DateUtils
import ronsijm.templater.utils.ErrorMessages
import java.time.LocalDateTime

@RegisterHandler(
    module = "date",
    description = "Returns current date/time with optional format and offset",
    example = "now(\"YYYY-MM-DD HH:mm\", \"+7d\")"
)
class NowHandler : CommandHandler<NowRequest, String> {
    override fun handle(request: NowRequest, context: TemplateContext): String {
        return try {
            val baseDateTime = if (request.reference.isNotEmpty() && request.referenceFormat.isNotEmpty()) {
                try {
                    DateUtils.parseDateTime(request.reference, request.referenceFormat)
                } catch (e: Exception) {
                    LocalDateTime.now()
                }
            } else {
                LocalDateTime.now()
            }

            val resultDateTime = if (request.offset.isNotEmpty()) {
                DateUtils.applyDateTimeOffset(baseDateTime, request.offset)
            } else {
                baseDateTime
            }

            DateUtils.formatDateTime(resultDateTime, request.format)
        } catch (e: Exception) {
            ErrorMessages.commandError(e.message ?: "Unknown error")
        }
    }
}

