package ronsijm.templater.handlers.date

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.modules.date.DateUtils
import java.time.LocalDate

@RegisterHandler(
    module = "date",
    description = "Returns weekday name with optional format and offset",
    example = "weekday(\"YYYY-MM-DD\", 0)"
)
class WeekdayHandler : CommandHandler<WeekdayRequest, String> {
    override fun handle(request: WeekdayRequest, context: TemplateContext): String {
        val baseDate = if (request.reference.isNotEmpty() && request.referenceFormat.isNotEmpty()) {
            try {
                DateUtils.parseDate(request.reference, request.referenceFormat)
            } catch (e: Exception) {
                LocalDate.now()
            }
        } else {
            LocalDate.now()
        }

        val currentDayOfWeek = baseDate.dayOfWeek.value // Monday = 1, Sunday = 7

        val targetDate = if (request.weekday >= 0) {
            // Positive or zero: find this/next occurrence
            val daysToAdd = request.weekday - (currentDayOfWeek - 1)
            baseDate.plusDays(daysToAdd.toLong())
        } else {
            // Negative: go back
            baseDate.plusDays(request.weekday.toLong())
        }

        return DateUtils.formatDate(targetDate, request.format)
    }
}

