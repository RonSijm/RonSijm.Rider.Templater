package ronsijm.templater.handlers.web

import ronsijm.templater.handlers.CommandRequest

data class DailyQuoteRequest(
    val dummy: Boolean = false
) : CommandRequest
