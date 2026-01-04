package ronsijm.templater.handlers.date

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.utils.DateUtils
import java.time.LocalDate

@RegisterHandler(
    module = "date",
    description = "Returns tomorrow's date",
    example = "tomorrow(\"YYYY-MM-DD\")",
    pure = true
)
class TomorrowHandler : CommandHandler<TomorrowRequest, String> {
    override fun handle(request: TomorrowRequest, context: TemplateContext): String {
        return DateUtils.formatDate(LocalDate.now().plusDays(1), request.format)
    }
}
