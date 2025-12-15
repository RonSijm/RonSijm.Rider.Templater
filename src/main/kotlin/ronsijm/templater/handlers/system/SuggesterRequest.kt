package ronsijm.templater.handlers.system

import ronsijm.templater.handlers.CommandRequest
import ronsijm.templater.handlers.ParamDescription

data class SuggesterRequest(
    @ParamDescription("List of text items to display")
    val textItems: List<Any>,
    @ParamDescription("List of actual items (defaults to textItems)")
    val items: List<Any>? = null,
    @ParamDescription("Whether to throw an error if user cancels")
    val throwOnCancel: Boolean = false,
    @ParamDescription("Placeholder text")
    val placeholder: String = "",
    @ParamDescription("Maximum number of items to display")
    val limit: Int? = null
) : CommandRequest

