package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandRequest
import ronsijm.templater.handlers.ParamDescription

data class CursorRequest(
    @ParamDescription("Cursor order/position")
    val order: Int? = null
) : CommandRequest
