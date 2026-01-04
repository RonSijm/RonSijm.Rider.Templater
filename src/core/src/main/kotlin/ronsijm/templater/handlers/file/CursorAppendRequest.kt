package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandRequest
import ronsijm.templater.handlers.ParamDescription

data class CursorAppendRequest(
    @ParamDescription("Content to append at cursor position")
    val content: String
) : CommandRequest
