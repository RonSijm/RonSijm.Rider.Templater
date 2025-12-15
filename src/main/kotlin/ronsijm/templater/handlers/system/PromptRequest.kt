package ronsijm.templater.handlers.system

import ronsijm.templater.handlers.CommandRequest
import ronsijm.templater.handlers.ParamDescription

data class PromptRequest(
    @ParamDescription("Prompt text to display")
    val promptText: String = "Enter value",
    @ParamDescription("Default value")
    val defaultValue: String? = null,
    @ParamDescription("Whether to throw an error if user cancels")
    val throwOnCancel: Boolean = false,
    @ParamDescription("Whether to allow multiline input")
    val multiline: Boolean = false
) : CommandRequest

