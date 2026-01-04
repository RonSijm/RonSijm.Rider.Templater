package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandRequest

data class ContentRequest(
    val dummy: Boolean = false
) : CommandRequest
