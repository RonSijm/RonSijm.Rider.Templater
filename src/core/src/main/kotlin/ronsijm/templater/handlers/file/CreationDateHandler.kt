package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.utils.DateUtils
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@RegisterHandler(
    module = "file",
    description = "Returns the file creation date",
    example = "creation_date(\"YYYY-MM-DD HH:mm\", \"path/to/file.md\")",
    pure = true
)
class CreationDateHandler : CommandHandler<CreationDateRequest, String> {
    override fun handle(request: CreationDateRequest, context: TemplateContext): String {
        val timestamp = context.services.fileOperationService.getCreationDate(request.path)

        val dateTime = if (timestamp != null) {
            Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        } else {
            LocalDateTime.now()
        }

        return DateUtils.formatDateTime(dateTime, request.format)
    }
}
