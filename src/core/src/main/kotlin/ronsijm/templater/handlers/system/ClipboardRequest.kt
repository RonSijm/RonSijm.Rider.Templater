package ronsijm.templater.handlers.system

import ronsijm.templater.handlers.CommandRequest

data class ClipboardRequest(
    val dummy: Boolean = false
) : CommandRequest
