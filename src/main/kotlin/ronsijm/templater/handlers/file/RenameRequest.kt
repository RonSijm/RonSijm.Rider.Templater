package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandRequest
import ronsijm.templater.handlers.ParamDescription

data class RenameRequest(
    @ParamDescription("New file name")
    val newName: String
) : CommandRequest

