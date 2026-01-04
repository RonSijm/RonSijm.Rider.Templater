package ronsijm.templater.standalone.events

import java.io.File

data class FileSelectedEvent(
    val file: File
) : AppEvent

data class FileOpenedInTabEvent(
    val file: File
) : AppEvent

data class FileOpenedInWindowEvent(
    val file: File
) : AppEvent

data class FileSavedEvent(
    val file: File
) : AppEvent

data class FolderOpenedEvent(
    val folder: File
) : AppEvent

data class EditorContentChangedEvent(
    val content: String,
    val file: File?
) : AppEvent

data class BreakpointsChangedEvent(
    val breakpoints: Set<Int>,
    val file: File?
) : AppEvent

