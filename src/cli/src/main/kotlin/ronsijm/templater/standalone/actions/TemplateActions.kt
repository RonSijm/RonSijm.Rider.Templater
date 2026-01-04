package ronsijm.templater.standalone.actions

import ronsijm.templater.standalone.events.EventBus
import ronsijm.templater.standalone.events.TemplateExecutionStartedEvent
import ronsijm.templater.standalone.events.TemplateExecutionCompletedEvent
import ronsijm.templater.standalone.events.TemplateExecutionFailedEvent
import ronsijm.templater.standalone.services.TemplateExecutionService
import javax.swing.KeyStroke
import javax.swing.SwingUtilities

class RunTemplateAction(
    private val context: ActionContext
) : Action {

    override val name = "Run Template"
    override val description = "Execute the template and show results"
    override val accelerator: KeyStroke = KeyStroke.getKeyStroke("F5")

    override fun isEnabled(): Boolean {
        return context.getCurrentFile() != null && !context.isDebugging()
    }

    override fun execute() {
        val file = context.getCurrentFile() ?: return
        val content = context.getEditorContent()

        EventBus.publish(TemplateExecutionStartedEvent(file, content))

        Thread {
            val result = TemplateExecutionService.execute(content, file)

            SwingUtilities.invokeLater {
                if (result.success) {
                    EventBus.publish(TemplateExecutionCompletedEvent(file, result.output))
                } else {
                    EventBus.publish(TemplateExecutionFailedEvent(file, result.error ?: Exception("Unknown error")))
                }
            }
        }.start()
    }
}

class StartDebugAction(
    private val context: ActionContext,
    private val onStartDebug: () -> Unit
) : Action {

    override val name = "Start Debugging"
    override val description = "Start debugging the template with breakpoints"
    override val accelerator: KeyStroke = KeyStroke.getKeyStroke("F9")

    override fun isEnabled(): Boolean {
        return context.getCurrentFile() != null && !context.isDebugging()
    }

    override fun execute() {
        if (isEnabled()) {
            onStartDebug()
        }
    }
}

class StopDebugAction(
    private val context: ActionContext,
    private val onStopDebug: () -> Unit
) : Action {

    override val name = "Stop Debugging"
    override val description = "Stop the current debug session"
    override val accelerator: KeyStroke = KeyStroke.getKeyStroke("shift F5")

    override fun isEnabled(): Boolean {
        return context.isDebugging()
    }

    override fun execute() {
        if (isEnabled()) {
            onStopDebug()
        }
    }
}

class ToggleBreakpointAction(
    private val onToggle: () -> Unit
) : Action {

    override val name = "Toggle Breakpoint"
    override val description = "Toggle breakpoint at current line"
    override val accelerator: KeyStroke = KeyStroke.getKeyStroke("F9")

    override fun execute() {
        onToggle()
    }
}

