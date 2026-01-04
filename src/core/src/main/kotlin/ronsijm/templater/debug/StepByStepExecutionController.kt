package ronsijm.templater.debug

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference


class StepByStepExecutionController(
    private val stepDelayMilliseconds: Int = 500
) {

    interface ExecutionListener {

        fun onNodeEntered(nodeId: String, lineNumber: Int?)


        fun onNodeExited(nodeId: String)


        fun onPaused()


        fun onResumed()


        fun onCompleted()


        fun onStopped()
    }

    enum class ExecutionState {
        IDLE,
        RUNNING,
        PAUSED,
        STEPPING,
        STOPPED
    }

    private val state = AtomicReference(ExecutionState.IDLE)
    private val currentNodeId = AtomicReference<String?>(null)
    private val shouldStop = AtomicBoolean(false)
    private val stepRequested = AtomicBoolean(false)
    private val listeners = mutableListOf<ExecutionListener>()
    private val lock = Object()

    val currentState: ExecutionState get() = state.get()
    val activeNodeId: String? get() = currentNodeId.get()


    fun addListener(listener: ExecutionListener) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }


    fun removeListener(listener: ExecutionListener) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }


    fun start() {
        state.set(ExecutionState.RUNNING)
        shouldStop.set(false)
        stepRequested.set(false)
    }


    fun pause() {
        if (state.get() == ExecutionState.RUNNING) {
            state.set(ExecutionState.PAUSED)
            notifyPaused()
        }
    }


    fun resume() {
        if (state.get() == ExecutionState.PAUSED) {
            state.set(ExecutionState.RUNNING)
            notifyResumed()
            synchronized(lock) {
                lock.notifyAll()
            }
        }
    }


    fun step() {
        if (state.get() == ExecutionState.PAUSED) {
            stepRequested.set(true)
            state.set(ExecutionState.STEPPING)
            synchronized(lock) {
                lock.notifyAll()
            }
        }
    }


    fun stop() {
        shouldStop.set(true)
        state.set(ExecutionState.STOPPED)
        notifyStopped()
        synchronized(lock) {
            lock.notifyAll()
        }
    }


    fun reset() {
        state.set(ExecutionState.IDLE)
        currentNodeId.set(null)
        shouldStop.set(false)
        stepRequested.set(false)
    }


    fun enterNode(nodeId: String, lineNumber: Int? = null): Boolean {
        if (shouldStop.get()) {
            return false
        }

        currentNodeId.set(nodeId)
        notifyNodeEntered(nodeId, lineNumber)


        when (state.get()) {
            ExecutionState.RUNNING -> {

                if (stepDelayMilliseconds > 0) {
                    try {
                        Thread.sleep(stepDelayMilliseconds.toLong())
                    } catch (e: InterruptedException) {
                        Thread.currentThread().interrupt()
                        return false
                    }
                }
            }
            ExecutionState.PAUSED, ExecutionState.STEPPING -> {

                waitForContinue()
            }
            ExecutionState.STOPPED -> {
                return false
            }
            ExecutionState.IDLE -> {

            }
        }

        return !shouldStop.get()
    }


    fun exitNode(nodeId: String) {
        notifyNodeExited(nodeId)


        if (state.get() == ExecutionState.STEPPING && stepRequested.compareAndSet(true, false)) {
            state.set(ExecutionState.PAUSED)
            notifyPaused()
        }
    }


    fun complete() {
        state.set(ExecutionState.IDLE)
        currentNodeId.set(null)
        notifyCompleted()
    }

    private fun waitForContinue() {
        synchronized(lock) {
            while (state.get() == ExecutionState.PAUSED && !shouldStop.get()) {
                try {
                    lock.wait()
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    break
                }
            }
        }
    }

    private fun notifyNodeEntered(nodeId: String, lineNumber: Int?) {
        synchronized(listeners) {
            listeners.forEach { it.onNodeEntered(nodeId, lineNumber) }
        }
    }

    private fun notifyNodeExited(nodeId: String) {
        synchronized(listeners) {
            listeners.forEach { it.onNodeExited(nodeId) }
        }
    }

    private fun notifyPaused() {
        synchronized(listeners) {
            listeners.forEach { it.onPaused() }
        }
    }

    private fun notifyResumed() {
        synchronized(listeners) {
            listeners.forEach { it.onResumed() }
        }
    }

    private fun notifyCompleted() {
        synchronized(listeners) {
            listeners.forEach { it.onCompleted() }
        }
    }

    private fun notifyStopped() {
        synchronized(listeners) {
            listeners.forEach { it.onStopped() }
        }
    }
}

