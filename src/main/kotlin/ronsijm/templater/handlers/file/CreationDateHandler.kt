package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.modules.date.DateUtils
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Handler for getting file creation date
 */
@RegisterHandler(
    module = "file",
    description = "Returns the file creation date",
    example = "creation_date(\"YYYY-MM-DD HH:mm\", \"path/to/file.md\")",
    pure = true
)
class CreationDateHandler : CommandHandler<CreationDateRequest, String> {
    override fun handle(request: CreationDateRequest, context: TemplateContext): String {
        // Get creation date from FileOperationService
        val timestamp = context.services.fileOperationService.getCreationDate(request.path)

        // Convert timestamp to LocalDateTime
        val dateTime = if (timestamp != null) {
            Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        } else {
            // Fallback to current date if timestamp not available
            LocalDateTime.now()
        }

        // Format and return
        return DateUtils.formatDateTime(dateTime, request.format)
    }
}



