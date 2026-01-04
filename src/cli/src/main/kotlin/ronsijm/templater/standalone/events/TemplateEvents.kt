package ronsijm.templater.standalone.events

import java.io.File

data class TemplateExecutionStartedEvent(
    val file: File,
    val content: String
) : AppEvent

data class TemplateExecutionCompletedEvent(
    val file: File,
    val result: String
) : AppEvent

data class TemplateExecutionFailedEvent(
    val file: File,
    val error: Throwable
) : AppEvent

data class RenderContentChangedEvent(
    val content: String,
    val isHtml: Boolean
) : AppEvent

