package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandRequest
import ronsijm.templater.handlers.ParamDescription

data class IncludeRequest(
    @ParamDescription("File path to include")
    val includeLink: String
) : CommandRequest

