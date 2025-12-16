package ronsijm.templater.handlers.date

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.modules.date.DateUtils
import java.time.LocalDate

@RegisterHandler(
    module = "date",
    description = "Returns yesterday's date",
    example = "yesterday(\"YYYY-MM-DD\")",
    pure = true
)
class YesterdayHandler : CommandHandler<YesterdayRequest, String> {
    override fun handle(request: YesterdayRequest, context: TemplateContext): String {
        return DateUtils.formatDate(LocalDate.now().minusDays(1), request.format)
    }
}

