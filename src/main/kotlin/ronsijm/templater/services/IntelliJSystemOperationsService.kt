package ronsijm.templater.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

/**
 * Real implementation of SystemOperationsService using IntelliJ Platform APIs
 * Shows actual dialogs to the user
 */
class IntelliJSystemOperationsService(
    private val project: Project
) : SystemOperationsService {
    
    override fun prompt(
        promptText: String,
        defaultValue: String?,
        multiLine: Boolean,
        password: Boolean
    ): String? {
        val result = Messages.showInputDialog(
            project,
            promptText,
            "Templater Input",
            Messages.getQuestionIcon(),
            defaultValue,
            null
        )
        return result
    }
    
    @Suppress("DEPRECATION")
    override fun suggester(
        textItems: List<*>,
        values: List<*>,
        throwOnCancel: Boolean,
        placeholder: String,
        limit: Int?
    ): Any? {
        val labelStrings = textItems.map { it.toString() }.toTypedArray()
        val selectedIndex = Messages.showChooseDialog(
            project,
            if (placeholder.isNotEmpty()) placeholder else "Select an option:",
            "Templater Suggester",
            Messages.getQuestionIcon(),
            labelStrings,
            labelStrings.firstOrNull()
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
    
    @Suppress("DEPRECATION")
    override fun multiSuggester(
        textItems: List<*>,
        values: List<*>,
        throwOnCancel: Boolean,
        placeholder: String,
        limit: Int?
    ): List<*>? {
        // IntelliJ doesn't have a built-in multi-select dialog
        // Show multiple dialogs until user cancels
        val labelStrings = textItems.map { it.toString() }.toTypedArray()
        val selected = mutableListOf<Any?>()

        var shouldContinue = true
        while (shouldContinue) {
            val selectedIndex = Messages.showChooseDialog(
                project,
                if (placeholder.isNotEmpty()) "$placeholder (Select items, cancel when done)" 
                else "Select items (cancel when done):",
                "Templater Multi-Suggester",
                Messages.getQuestionIcon(),
                labelStrings,
                labelStrings.firstOrNull()
            )
            
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
}

