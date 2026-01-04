package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandRequest
import ronsijm.templater.handlers.ParamDescription

data class CreateNewRequest(
    @ParamDescription("Template content for the new file")
    val template: String,
    @ParamDescription("Filename for the new file")
    val filename: String? = null,
    @ParamDescription("Whether to open the new file after creation")
    val openNew: Boolean = false,
    @ParamDescription("Folder to create the file in")
    val folder: String? = null
) : CommandRequest
