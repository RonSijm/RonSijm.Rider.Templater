package ronsijm.templater.handlers.date

import ronsijm.templater.handlers.CommandRequest
import ronsijm.templater.handlers.ParamDescription

data class WeekdayRequest(
    @ParamDescription("Date format string")
    val format: String = "yyyy-MM-dd",
    @ParamDescription("Weekday offset (0 = Monday, 1 = Tuesday, etc.)")
    val weekday: Int = 0,
    @ParamDescription("Reference date string")
    val reference: String = "",
    @ParamDescription("Format for parsing reference date")
    val referenceFormat: String = ""
) : CommandRequest
