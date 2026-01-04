package ronsijm.templater.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.wm.ToolWindowManager


class ShowControlFlowAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)


        val isMarkdown = file?.name?.lowercase()?.let {
            it.endsWith(".md") || it.endsWith(".markdown")
        } ?: false

        e.presentation.isEnabledAndVisible = isMarkdown
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return


        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Templater Control Flow")
        toolWindow?.show()
    }
}

