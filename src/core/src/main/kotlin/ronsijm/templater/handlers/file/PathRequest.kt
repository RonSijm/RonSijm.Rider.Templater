package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandRequest
import ronsijm.templater.handlers.ParamDescription

data class PathRequest(
    @ParamDescription("If true, returns relative path; if false, returns absolute path")
    val relative: Boolean = false
) : CommandRequest
