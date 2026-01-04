package ronsijm.templater.debug

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.vfs.VirtualFile
import ronsijm.templater.debug.DebugAction
import ronsijm.templater.debug.DebugBreakpoint
import ronsijm.templater.debug.ExecutionStep
import ronsijm.templater.debug.ExecutionTrace
import ronsijm.templater.script.VariableUpdater
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference


class ActiveDebugSession(
    val file: VirtualFile,
    initialBreakpoints: Set<Int>
) {

    private val lineBreakpoints = initialBreakpoints.toMutableSet()
    private val nodeBreakpoints = mutableSetOf<String>()

    private val pendingAction = AtomicReference<DebugAction?>(null)
    private var waitLatch: CountDownLatch? = null

    @Volatile
    var progressIndicator: ProgressIndicator? = null

    @Volatile
    var isActive: Boolean = true
        private set

    @Volatile
    var currentStep: ExecutionStep? = null
        private set

    @Volatile
    var currentBreakpoint: DebugBreakpoint? = null
        private set

    @Volatile
    var trace: ExecutionTrace? = null
        private set

    @Volatile
    var isPaused: Boolean = false
        private set

    private val stateListeners = mutableListOf<SessionStateListener>()

    @Volatile
    var variableUpdater: VariableUpdater? = null


    fun onBreakpointHit(breakpoint: DebugBreakpoint): DebugAction {
        if (!isActive) return DebugAction.STOP

        currentBreakpoint = breakpoint
        currentStep = breakpoint.step
        trace = breakpoint.trace
        isPaused = true

        notifyPaused(breakpoint)


        waitLatch = CountDownLatch(1)
        try {

            while (isActive && waitLatch?.count ?: 0 > 0) {

                if (progressIndicator?.isCanceled == true) {
                    isActive = false
                    isPaused = false
                    notifyStopped()
                    return DebugAction.STOP
                }


                if (waitLatch?.await(100, TimeUnit.MILLISECONDS) == true) {
                    break
                }
            }
        } catch (e: InterruptedException) {
            isActive = false
            isPaused = false
            return DebugAction.STOP
        }

        isPaused = false
        val action = pendingAction.getAndSet(null) ?: DebugAction.CONTINUE

        if (action == DebugAction.STOP) {
            isActive = false

        } else {
            notifyResumed(action)
        }
        return action
    }

    fun resume(action: DebugAction) {
        pendingAction.set(action)
        waitLatch?.countDown()
    }

    fun continueExecution() = resume(DebugAction.CONTINUE)
    fun stepInto() = resume(DebugAction.STEP_INTO)
    fun stepOver() = resume(DebugAction.STEP_OVER)
    fun stepOut() = resume(DebugAction.STEP_OUT)

    fun stop() {
        isActive = false
        isPaused = false
        resume(DebugAction.STOP)
    }



    @Deprecated("Use hasBreakpointOnNode() for AST node-based breakpoints")
    fun hasBreakpoint(lineNumber: Int): Boolean = lineNumber in lineBreakpoints

    @Deprecated("Use getNodeBreakpoints() for AST node-based breakpoints")
    fun getBreakpoints(): Set<Int> = lineBreakpoints.toSet()



    fun hasBreakpointOnNode(nodeId: String): Boolean = nodeId in nodeBreakpoints
    fun getNodeBreakpoints(): Set<String> = nodeBreakpoints.toSet()

    fun addBreakpointOnNode(nodeId: String) {
        nodeBreakpoints.add(nodeId)
    }

    fun removeBreakpointOnNode(nodeId: String) {
        nodeBreakpoints.remove(nodeId)
    }

    fun updateVariable(name: String, value: String): Boolean {
        return variableUpdater?.updateVariable(name, value) ?: false
    }

    fun addStateListener(listener: SessionStateListener) {
        stateListeners.add(listener)
    }

    fun removeStateListener(listener: SessionStateListener) {
        stateListeners.remove(listener)
    }

    private fun notifyPaused(breakpoint: DebugBreakpoint) {
        stateListeners.forEach { it.onPaused(breakpoint) }
    }

    private fun notifyResumed(action: DebugAction) {
        stateListeners.forEach { it.onResumed(action) }
    }

    private fun notifyStopped() {
        stateListeners.forEach { it.onStopped() }
    }
}


interface SessionStateListener {
    fun onPaused(breakpoint: DebugBreakpoint)
    fun onResumed(action: DebugAction)
    fun onStopped() {}
}

