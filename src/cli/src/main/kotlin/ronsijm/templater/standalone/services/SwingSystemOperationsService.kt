package ronsijm.templater.standalone.services

import ronsijm.templater.services.SystemOperationsService
import javax.swing.*
import java.awt.Component

class SwingSystemOperationsService(
    private val parentComponent: Component? = null
) : SystemOperationsService {

    override fun prompt(
        promptText: String,
        defaultValue: String?,
        multiLine: Boolean,
        password: Boolean
    ): String? {
        return if (SwingUtilities.isEventDispatchThread()) {
            showPromptDialog(promptText, defaultValue, multiLine, password)
        } else {
            var result: String? = null
            SwingUtilities.invokeAndWait {
                result = showPromptDialog(promptText, defaultValue, multiLine, password)
            }
            result
        }
    }

    private fun showPromptDialog(
        promptText: String,
        defaultValue: String?,
        multiLine: Boolean,
        password: Boolean
    ): String? {
        return if (multiLine) {

            val textArea = JTextArea(5, 30)
            textArea.text = defaultValue ?: ""
            textArea.lineWrap = true
            textArea.wrapStyleWord = true
            val scrollPane = JScrollPane(textArea)

            val result = JOptionPane.showConfirmDialog(
                parentComponent,
                arrayOf(promptText, scrollPane),
                "Templater Input",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
            )

            if (result == JOptionPane.OK_OPTION) textArea.text else null
        } else if (password) {

            val passwordField = JPasswordField(20)
            val result = JOptionPane.showConfirmDialog(
                parentComponent,
                arrayOf(promptText, passwordField),
                "Templater Input",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
            )

            if (result == JOptionPane.OK_OPTION) String(passwordField.password) else null
        } else {

            JOptionPane.showInputDialog(
                parentComponent,
                promptText,
                "Templater Input",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                defaultValue
            )?.toString()
        }
    }

    override fun suggester(
        textItems: List<*>,
        values: List<*>,
        throwOnCancel: Boolean,
        placeholder: String,
        limit: Int?
    ): Any? {
        return if (SwingUtilities.isEventDispatchThread()) {
            showSuggesterDialog(textItems, values, throwOnCancel, placeholder)
        } else {
            var result: Any? = null
            SwingUtilities.invokeAndWait {
                result = showSuggesterDialog(textItems, values, throwOnCancel, placeholder)
            }
            result
        }
    }

    private fun showSuggesterDialog(
        textItems: List<*>,
        values: List<*>,
        throwOnCancel: Boolean,
        placeholder: String
    ): Any? {
        val items = textItems.map { it.toString() }.toTypedArray()
        val title = if (placeholder.isNotEmpty()) placeholder else "Select an option"

        val selectedIndex = JOptionPane.showOptionDialog(
            parentComponent,
            title,
            "Templater",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            items,
            items.firstOrNull()
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
        return if (SwingUtilities.isEventDispatchThread()) {
            showMultiSuggesterDialog(textItems, values, throwOnCancel, placeholder, limit)
        } else {
            var result: List<*>? = null
            SwingUtilities.invokeAndWait {
                result = showMultiSuggesterDialog(textItems, values, throwOnCancel, placeholder, limit)
            }
            result
        }
    }

    private fun showMultiSuggesterDialog(
        textItems: List<*>,
        values: List<*>,
        throwOnCancel: Boolean,
        placeholder: String,
        @Suppress("UNUSED_PARAMETER") limit: Int?
    ): List<*>? {
        val items = textItems.map { it.toString() }.toTypedArray()
        val title = if (placeholder.isNotEmpty()) placeholder else "Select options"

        val list = JList(items)
        list.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        val scrollPane = JScrollPane(list)
        scrollPane.preferredSize = java.awt.Dimension(300, 200)

        val result = JOptionPane.showConfirmDialog(
            parentComponent,
            arrayOf(title, scrollPane),
            "Templater",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
        )

        return if (result == JOptionPane.OK_OPTION) {
            val selectedIndices = list.selectedIndices
            if (selectedIndices.isNotEmpty()) {
                selectedIndices.map { values[it] }
            } else {
                if (throwOnCancel) {
                    throw IllegalStateException("Multi-suggester was cancelled")
                }
                null
            }
        } else {
            if (throwOnCancel) {
                throw IllegalStateException("Multi-suggester was cancelled")
            }
            null
        }
    }
}

