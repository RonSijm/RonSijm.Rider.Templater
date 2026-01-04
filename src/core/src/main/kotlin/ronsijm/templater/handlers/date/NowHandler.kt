package ronsijm.templater.handlers.date

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.utils.DateUtils
import java.time.LocalDateTime

@RegisterHandler(
    module = "date",
    description = "Returns current date/time with optional format and offset",
    example = "now(\"YYYY-MM-DD HH:mm\", \"+7d\")",
    pure = true
)
class NowHandler : CommandHandler<NowRequest, String> {
    override fun handle(request: NowRequest, context: TemplateContext): String {

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

        return DateUtils.formatDateTime(resultDateTime, request.format)
    }
}
