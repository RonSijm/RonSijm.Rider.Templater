package ronsijm.templater.debug

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ronsijm.templater.debug.DebugAction
import ronsijm.templater.debug.DebugBreakpoint
import ronsijm.templater.debug.ExecutionStep
import ronsijm.templater.debug.ExecutionTrace
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference


@Service(Service.Level.PROJECT)
class TemplaterDebugService(private val project: Project) : Disposable {


    private val breakpoints = ConcurrentHashMap<String, MutableSet<Int>>()


    private var currentSession: ActiveDebugSession? = null


    private val listeners = mutableListOf<DebugEventListener>()


    fun getBreakpoints(file: VirtualFile): Set<Int> {
        return breakpoints[file.path]?.toSet() ?: emptySet()
    }


    fun toggleBreakpoint(file: VirtualFile, lineNumber: Int): Boolean {
        val fileBreakpoints = breakpoints.getOrPut(file.path) { mutableSetOf() }
        val added = if (lineNumber in fileBreakpoints) {
            fileBreakpoints.remove(lineNumber)
            false
        } else {
            fileBreakpoints.add(lineNumber)
            true
        }
        notifyBreakpointsChanged(file)
        return added
    }


    fun hasBreakpoint(file: VirtualFile, lineNumber: Int): Boolean {
        return breakpoints[file.path]?.contains(lineNumber) ?: false
    }


    fun clearBreakpoints(file: VirtualFile) {
        breakpoints.remove(file.path)
        notifyBreakpointsChanged(file)
    }


    fun startSession(file: VirtualFile): ActiveDebugSession {
        stopSession()

        val session = ActiveDebugSession(file, getBreakpoints(file))
        currentSession = session
        notifySessionStarted(session)
        return session
    }


    fun getCurrentSession(): ActiveDebugSession? = currentSession


    fun stopSession() {
        currentSession?.let { session ->
            session.stop()
            notifySessionEnded(session)
        }
        currentSession = null
    }


    fun isDebugging(): Boolean = currentSession?.isActive ?: false


    fun addListener(listener: DebugEventListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: DebugEventListener) {
        listeners.remove(listener)
    }

    private fun notifyBreakpointsChanged(file: VirtualFile) {
        listeners.forEach { it.onBreakpointsChanged(file, getBreakpoints(file)) }
    }

    private fun notifySessionStarted(session: ActiveDebugSession) {
        listeners.forEach { it.onSessionStarted(session) }
    }

    private fun notifySessionEnded(session: ActiveDebugSession) {
        listeners.forEach { it.onSessionEnded(session) }
    }

    override fun dispose() {
        stopSession()
        listeners.clear()
        breakpoints.clear()
    }

    companion object {
        fun getInstance(project: Project): TemplaterDebugService {
            return project.getService(TemplaterDebugService::class.java)
        }
    }
}

