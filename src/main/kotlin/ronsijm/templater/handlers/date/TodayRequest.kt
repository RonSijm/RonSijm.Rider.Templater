package ronsijm.templater.handlers.date

import ronsijm.templater.handlers.CommandRequest
import ronsijm.templater.handlers.ParamDescription

data class TodayRequest(
    @ParamDescription("Date format string")
    val format: String = "yyyy-MM-dd"
) : CommandRequest

