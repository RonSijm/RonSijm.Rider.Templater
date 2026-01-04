package ronsijm.templater.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import java.util.concurrent.CompletableFuture


class IntelliJSystemOperationsService(
    private val project: Project
) : SystemOperationsService {

    override fun prompt(
        promptText: String,
        defaultValue: String?,
        multiLine: Boolean,
        password: Boolean
    ): String? {
        if (ApplicationManager.getApplication().isDispatchThread) {
            return Messages.showInputDialog(
                project,
                promptText,
                "Templater Input",
                Messages.getQuestionIcon(),
                defaultValue,
                null
            )
        }

        val future = CompletableFuture<String?>()
        ApplicationManager.getApplication().invokeAndWait({
            val result = Messages.showInputDialog(
                project,
                promptText,
                "Templater Input",
                Messages.getQuestionIcon(),
                defaultValue,
                null
            )
            future.complete(result)
        }, ModalityState.defaultModalityState())

        return future.get()
    }

    override fun suggester(
        textItems: List<*>,
        values: List<*>,
        throwOnCancel: Boolean,
        placeholder: String,
        limit: Int?
    ): Any? {
        val labelStrings = textItems.map { it.toString() }

        val selectedIndex = showPopupChooser(
            labelStrings,
            if (placeholder.isNotEmpty()) placeholder else "Select an option"
        )

        return if (selectedIndex >= 0 && selectedIndex < values.size) {
            values[selectedIndex]
        } else {
            if (throwOnCancel) {
                throw IllegalStateException("Suggester was cancelled")
            }
            null
        }
    }

    override fun multiSuggester(
        textItems: List<*>,
        values: List<*>,
        throwOnCancel: Boolean,
        placeholder: String,
        limit: Int?
    ): List<*>? {
        val labelStrings = textItems.map { it.toString() }
        val selected = mutableListOf<Any?>()

        var shouldContinue = true
        while (shouldContinue) {
            val title = if (placeholder.isNotEmpty()) {
                "$placeholder (Select items, cancel when done)"
            } else {
                "Select items (cancel when done)"
            }

            val selectedIndex = showPopupChooser(labelStrings, title)

            if (selectedIndex >= 0 && selectedIndex < values.size) {
                selected.add(values[selectedIndex])
            } else {
                shouldContinue = false
            }
        }

        return if (selected.isEmpty() && throwOnCancel) {
            throw IllegalStateException("Multi-suggester was cancelled")
        } else {
            selected
        }
    }


    private fun showPopupChooser(items: List<String>, title: String): Int {
        val showDialog: () -> Int = {
            Messages.showChooseDialog(
                project,
                title,
                "Templater",
                Messages.getQuestionIcon(),
                items.toTypedArray(),
                items.firstOrNull() ?: ""
            )
        }

        return if (ApplicationManager.getApplication().isDispatchThread) {
            showDialog()
        } else {
            val future = CompletableFuture<Int>()
            ApplicationManager.getApplication().invokeAndWait({
                future.complete(showDialog())
            }, ModalityState.defaultModalityState())
            future.get()
        }
    }
}