package ronsijm.templater.handlers.web

import ronsijm.templater.handlers.CommandRequest
import ronsijm.templater.handlers.ParamDescription

data class RequestRequest(
    @ParamDescription("URL to request")
    val url: String,
    @ParamDescription("JSON path to extract from response")
    val jsonPath: String = ""
) : CommandRequest
