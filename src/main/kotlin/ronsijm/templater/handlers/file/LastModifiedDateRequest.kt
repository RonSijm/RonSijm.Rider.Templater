package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandRequest
import ronsijm.templater.handlers.ParamDescription

data class LastModifiedDateRequest(
    @ParamDescription("Date format string")
    val format: String = "yyyy-MM-dd HH:mm",
    @ParamDescription("File path (uses current file if not specified)")
    val path: String? = null
) : CommandRequest

