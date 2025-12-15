package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandRequest
import ronsijm.templater.handlers.ParamDescription

data class ExistsRequest(
    @ParamDescription("File path to check")
    val filepath: String
) : CommandRequest

