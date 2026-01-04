package ronsijm.templater.standalone.debug

import ronsijm.templater.debug.*
import ronsijm.templater.parser.*
import ronsijm.templater.script.VariableUpdater
import ronsijm.templater.services.ServiceContainer
import ronsijm.templater.services.mock.NullAppModuleProvider
import ronsijm.templater.standalone.services.TemplateExecutionService
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class StandaloneDebugSession(
    val file: File,
    initialBreakpoints: Set<Int>
) {
    private val breakpoints = initialBreakpoints.toMutableSet()
    private val pendingAction = AtomicReference<DebugAction?>(null)
    private var waitLatch: CountDownLatch? = null

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

    private val stateListeners = mutableListOf<StandaloneSessionStateListener>()

    @Volatile
    var variableUpdater: VariableUpdater? = null

    @Volatile
    private var result: String? = null

    @Volatile
    private var debugParser: DebuggingTemplateParser? = null

    fun getResult(): String? = result

    fun execute(content: String) {
        try {

            val contextResult = TemplateExecutionService.createContext(content, file)
            val frontmatterLineOffset = TemplateExecutionService.calculateFrontmatterOffset(content)

            println("[DEBUG] StandaloneDebugSession.execute()")
            println("[DEBUG]   Breakpoints from UI: $breakpoints")
            println("[DEBUG]   Frontmatter offset: $frontmatterLineOffset")
            println("[DEBUG]   startInStepMode: ${breakpoints.isEmpty()}")


            this.debugParser = DebuggingTemplateParser(validateSyntax = true, services = contextResult.services)


            debugParser!!.startDebugSession(
                onBreakpoint = { breakpoint ->
                    println("[DEBUG] Breakpoint hit! Line: ${breakpoint.step.displayLineNumber}, Type: ${breakpoint.step.type}")
                    onBreakpointHit(breakpoint)
                },
                onComplete = { trace -> this.trace = trace },
                startInStepMode = breakpoints.isEmpty()
            )




            breakpoints.forEach { documentLineNumber ->
                val contentLineNumber = documentLineNumber - frontmatterLineOffset
                println("[DEBUG] Adding breakpoint: document line $documentLineNumber -> content line $contentLineNumber")
                if (contentLineNumber > 0) {
                    debugParser!!.addBreakpoint(contentLineNumber)
                } else {
                    println("[DEBUG] Skipping breakpoint at document line $documentLineNumber (in frontmatter)")
                }
            }

            println("[DEBUG] Breakpoints in parser: ${debugParser!!.getBreakpoints()}")


            variableUpdater = debugParser!!.getVariableUpdater()


            val parseResult = debugParser!!.parse(
                content = contextResult.frontmatterResult.content,
                context = contextResult.context,
                appModuleProvider = NullAppModuleProvider,
                documentLineOffset = frontmatterLineOffset
            )


            this.result = parseResult.result


            if (parseResult.wasStopped) {
                notifyStopped()
            } else {

                notifyStopped()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            this.result = "Error: ${e.message}"
            notifyStopped()
        }
    }

    fun onBreakpointHit(breakpoint: DebugBreakpoint): DebugAction {
        if (!isActive) return DebugAction.STOP

        currentBreakpoint = breakpoint
        currentStep = breakpoint.step
        trace = breakpoint.trace
        isPaused = true



        waitLatch = CountDownLatch(1)

        notifyPaused(breakpoint)


        while (isActive && waitLatch!!.count > 0) {
            waitLatch!!.await(100, TimeUnit.MILLISECONDS)
        }

        isPaused = false
        val action = pendingAction.getAndSet(null) ?: DebugAction.STOP

        if (action != DebugAction.STOP) {
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

    fun hasBreakpoint(lineNumber: Int): Boolean = lineNumber in breakpoints
    fun getBreakpoints(): Set<Int> = breakpoints.toSet()


    fun addBreakpoint(lineNumber: Int) {
        breakpoints.add(lineNumber)

        debugParser?.addBreakpoint(lineNumber)
    }


    fun removeBreakpoint(lineNumber: Int) {
        breakpoints.remove(lineNumber)

        debugParser?.removeBreakpoint(lineNumber)
    }

    fun updateVariable(name: String, value: String): Boolean {
        return variableUpdater?.updateVariable(name, value) ?: false
    }

    fun addStateListener(listener: StandaloneSessionStateListener) {
        stateListeners.add(listener)
    }

    fun removeStateListener(listener: StandaloneSessionStateListener) {
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

interface StandaloneSessionStateListener {
    fun onPaused(breakpoint: DebugBreakpoint)
    fun onResumed(action: DebugAction)
    fun onStopped() {}
}

