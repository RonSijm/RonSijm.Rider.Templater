package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandRequest
import ronsijm.templater.handlers.ParamDescription

data class MoveRequest(
    @ParamDescription("New file path")
    val newPath: String
) : CommandRequest

