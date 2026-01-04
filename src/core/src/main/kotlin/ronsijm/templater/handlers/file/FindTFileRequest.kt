package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandRequest
import ronsijm.templater.handlers.ParamDescription

data class FindTFileRequest(
    @ParamDescription("Filename to search for")
    val filename: String
) : CommandRequest
