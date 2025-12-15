package ronsijm.templater.handlers.date

import ronsijm.templater.handlers.CommandRequest
import ronsijm.templater.handlers.ParamDescription

data class NowRequest(
    @ParamDescription("Date/time format string")
    val format: String = "yyyy-MM-dd HH:mm",
    @ParamDescription("Offset string (e.g., '+7d', '-1M')")
    val offset: String = "",
    @ParamDescription("Reference date/time string")
    val reference: String = "",
    @ParamDescription("Format for parsing reference date/time")
    val referenceFormat: String = ""
) : CommandRequest

