package ronsijm.templater.handlers.web

import ronsijm.templater.handlers.CommandRequest
import ronsijm.templater.handlers.ParamDescription

data class RandomPictureRequest(
    @ParamDescription("Image size (e.g., '1600x900')")
    val size: String = "1600x900",
    @ParamDescription("Search query")
    val query: String = "",
    @ParamDescription("Whether to include size in markdown output")
    val includeSize: Boolean = false
) : CommandRequest

