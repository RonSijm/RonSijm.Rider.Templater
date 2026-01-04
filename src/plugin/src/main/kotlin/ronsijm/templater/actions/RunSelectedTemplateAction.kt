package ronsijm.templater.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.ActionManager


class RunSelectedTemplateAction : AnAction(
    "Run Template",
    "Execute the selected template",
    AllIcons.Actions.Execute
) {

    companion object {
        private val TEMPLATE_PATTERNS = listOf("<%*", "<%_", "<%-", "<%")
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)


        val isMarkdown = file?.name?.lowercase()?.let {
            it.endsWith(".md") || it.endsWith(".markdown")
        } ?: false

        if (!isMarkdown || editor == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }


        val selectionModel = editor.selectionModel
        val selectedText = selectionModel.selectedText

        val hasTemplate = selectedText?.let { text ->
            TEMPLATE_PATTERNS.any { pattern -> text.contains(pattern) }
        } ?: false

        e.presentation.isEnabledAndVisible = hasTemplate
    }

    override fun actionPerformed(e: AnActionEvent) {

        val executeAction = ActionManager.getInstance().getAction("Templater.ExecuteTemplate")
        executeAction?.actionPerformed(e)
    }
}
