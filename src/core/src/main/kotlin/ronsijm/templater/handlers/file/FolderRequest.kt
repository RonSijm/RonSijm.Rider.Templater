package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandRequest
import ronsijm.templater.handlers.ParamDescription

data class FolderRequest(
    @ParamDescription("If true, returns full folder path; if false, returns folder name")
    val relative: Boolean = false
) : CommandRequest
