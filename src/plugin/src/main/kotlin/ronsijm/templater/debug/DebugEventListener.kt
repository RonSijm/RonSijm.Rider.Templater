package ronsijm.templater.debug

import com.intellij.openapi.vfs.VirtualFile


interface DebugEventListener {

    fun onBreakpointsChanged(file: VirtualFile, breakpoints: Set<Int>) {}


    fun onSessionStarted(session: ActiveDebugSession) {}


    fun onSessionEnded(session: ActiveDebugSession) {}
}

