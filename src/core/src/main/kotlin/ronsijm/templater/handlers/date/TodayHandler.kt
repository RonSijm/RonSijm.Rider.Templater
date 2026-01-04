package ronsijm.templater.handlers.date

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.utils.DateUtils
import java.time.LocalDate

@RegisterHandler(
    module = "date",
    description = "Returns today's date",
    example = "today(\"YYYY-MM-DD\")",
    pure = true
)
class TodayHandler : CommandHandler<TodayRequest, String> {
    override fun handle(request: TodayRequest, context: TemplateContext): String {
        return DateUtils.formatDate(LocalDate.now(), request.format)
    }
}
