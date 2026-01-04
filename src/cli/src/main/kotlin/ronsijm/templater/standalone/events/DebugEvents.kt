package ronsijm.templater.standalone.events

import java.io.File

enum class DebugState {
    IDLE,
    RUNNING,
    PAUSED,
    STOPPED
}

data class DebugStateChangedEvent(
    val state: DebugState,
    val file: File?
) : AppEvent

data class DebugSessionStartedEvent(
    val file: File,
    val breakpoints: Set<Int>
) : AppEvent

data class DebugSessionStoppedEvent(
    val file: File?,
    val result: String?
) : AppEvent

data class BreakpointHitEvent(
    val lineNumber: Int,
    val variables: Map<String, Any?>
) : AppEvent

data class DebugStepEvent(
    val lineNumber: Int,
    val variables: Map<String, Any?>
) : AppEvent

data class VariablesChangedEvent(
    val variables: Map<String, Any?>
) : AppEvent

data class CurrentDebugLineChangedEvent(
    val lineNumber: Int?
) : AppEvent

