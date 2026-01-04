package ronsijm.templater.actions

import ronsijm.templater.parser.FrontmatterParser
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.services.DefaultHttpService
import ronsijm.templater.services.ServiceContainer
import ronsijm.templater.services.SystemClipboardService
import ronsijm.templater.services.IntelliJSystemOperationsService
import ronsijm.templater.services.IntelliJFileOperationsService
import ronsijm.templater.services.MermaidExportService
import ronsijm.templater.services.TemplateExecutionService
import ronsijm.templater.settings.TemplaterSettings
import ronsijm.templater.settings.PopupBehavior
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile


class ExecuteTemplateAction : AnAction() {

    private val frontmatterParser = FrontmatterParser()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val document = editor.document
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val settings = TemplaterSettings.getInstance()
        val startTime = if (settings.showExecutionStats) System.currentTimeMillis() else 0L

        val isFromGutter = e.place.startsWith("TemplateGutter")
        val popupBehavior = if (isFromGutter) settings.popupBehaviorGutter else settings.popupBehaviorHotkey

        val selectionModel = editor.selectionModel
        val hasSelection = selectionModel.hasSelection()
        val useSelectionOnly = hasSelection && settings.enableSelectionOnlyExecution

        val selectedText = if (useSelectionOnly) selectionModel.selectedText else null
        val selectionStart = if (useSelectionOnly) selectionModel.selectionStart else 0
        val selectionEnd = if (useSelectionOnly) selectionModel.selectionEnd else 0

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Executing Template", true) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    indicator.text = "Parsing template..."

                    if (useSelectionOnly && selectedText != null) {
                        executeSelectedTemplateInBackground(
                            project, editor, document, file, settings,
                            selectedText, selectionStart, selectionEnd, indicator
                        )
                    } else {
                        executeTemplateInBackground(project, document, file, settings, indicator)
                    }

                    if (popupBehavior == PopupBehavior.ALWAYS) {
                        ApplicationManager.getApplication().invokeLater {
                            val message = if (settings.showExecutionStats) {
                                val elapsed = System.currentTimeMillis() - startTime
                                val mode = if (settings.enableParallelExecution) "parallel (experimental)" else "sequential"
                                val scope = if (useSelectionOnly) "selection" else "document"
                                "Template executed successfully!\nScope: $scope\nMode: $mode\nTime: ${elapsed}ms"
                            } else {
                                "Template executed successfully!"
                            }
                            Messages.showInfoMessage(project, message, "Templater")
                        }
                    }
                } catch (ex: Exception) {
                    if (popupBehavior != PopupBehavior.NEVER) {
                        ApplicationManager.getApplication().invokeLater {
                            Messages.showErrorDialog(
                                project,
                                "Error executing template: ${ex.message}",
                                "Templater Error"
                            )
                        }
                    }
                }
            }
        })
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = editor != null
    }


    private fun executeSelectedTemplateInBackground(
        project: Project,
        editor: Editor,
        document: Document,
        file: VirtualFile,
        settings: TemplaterSettings,
        selectedText: String,
        selectionStart: Int,
        selectionEnd: Int,
        indicator: ProgressIndicator
    ) {
        val fullContent = ApplicationManager.getApplication().runReadAction<String> { document.text }
        val frontmatterResult = frontmatterParser.parse(fullContent)

        val (services, fileOperationService) = createServices(project, file, frontmatterResult.frontmatter)
        val context = createTemplateContext(frontmatterResult, file, services)

        val executionService = TemplateExecutionService(project, settings, services)
        val parseResult = executionService.execute(selectedText, context, indicator)


        parseResult.mermaidDiagram?.let { mermaid ->
            MermaidExportService(project, settings).saveDiagram(mermaid, file)
        }

        ApplicationManager.getApplication().invokeAndWait {
            WriteCommandAction.runWriteCommandAction(project) {
                document.replaceString(selectionStart, selectionEnd, parseResult.content)
                editor.selectionModel.removeSelection()
                applyPendingFileOperations(project, file, fileOperationService)
            }
        }
    }


    private fun executeTemplateInBackground(
        project: Project,
        document: Document,
        file: VirtualFile,
        settings: TemplaterSettings,
        indicator: ProgressIndicator
    ) {
        val content = ApplicationManager.getApplication().runReadAction<String> { document.text }
        val frontmatterResult = frontmatterParser.parse(content)

        val (services, fileOperationService) = createServices(project, file, frontmatterResult.frontmatter)
        val context = createTemplateContext(frontmatterResult, file, services)

        val executionService = TemplateExecutionService(project, settings, services)
        val parseResult = executionService.execute(frontmatterResult.content, context, indicator)


        parseResult.mermaidDiagram?.let { mermaid ->
            MermaidExportService(project, settings).saveDiagram(mermaid, file)
        }

        val newContent = if (frontmatterResult.hasFrontmatter) {
            buildFrontmatterDocument(frontmatterResult.frontmatter, parseResult.content)
        } else {
            parseResult.content
        }

        ApplicationManager.getApplication().invokeAndWait {
            WriteCommandAction.runWriteCommandAction(project) {
                document.setText(newContent)
                applyPendingFileOperations(project, file, fileOperationService)
            }
        }
    }


    private fun createServices(
        project: Project,
        file: VirtualFile,
        frontmatter: Map<String, Any>
    ): Pair<ServiceContainer, IntelliJFileOperationsService> {
        val fileOperationService = IntelliJFileOperationsService(project, file, frontmatter)
        val services = ServiceContainer(
            clipboardService = SystemClipboardService(),
            httpService = DefaultHttpService(),
            systemOperationsService = IntelliJSystemOperationsService(project),
            fileOperationService = fileOperationService
        )
        return Pair(services, fileOperationService)
    }


    private fun createTemplateContext(
        parseResult: FrontmatterParser.ParseResult,
        file: VirtualFile,
        services: ServiceContainer
    ): TemplateContext {
        return TemplateContext(
            frontmatter = parseResult.frontmatter,
            frontmatterParser = frontmatterParser,
            fileName = file.name,
            filePath = file.path,
            fileContent = parseResult.content,
            services = services
        )
    }


    private fun applyPendingFileOperations(
        project: Project,
        file: VirtualFile,
        fileOperationService: IntelliJFileOperationsService
    ) {
        fileOperationService.pendingRename?.let { newName ->
            try {
                file.rename(this, newName)
            } catch (ex: Exception) {
                Messages.showErrorDialog(
                    project,
                    "Failed to rename file: ${ex.message}",
                    "Templater Error"
                )
            }
        }

        fileOperationService.pendingMove?.let { targetPath ->
            try {
                val parentPath = targetPath.substringBeforeLast("/", "")

                if (parentPath.isNotEmpty()) {
                    var targetDir: VirtualFile? = file.parent

                    if (targetPath.startsWith("/")) {
                        targetDir = project.guessProjectDir()
                        val pathParts = parentPath.substring(1).split("/")
                        for (part in pathParts) {
                            if (part.isNotEmpty()) {
                                targetDir = targetDir?.findChild(part)
                                    ?: targetDir?.createChildDirectory(this, part)
                            }
                        }
                    }

                    if (targetDir != null && targetDir != file.parent) {
                        file.move(this, targetDir)
                    }
                }
            } catch (ex: Exception) {
                Messages.showErrorDialog(
                    project,
                    "Failed to move file: ${ex.message}",
                    "Templater Error"
                )
            }
        }
    }


    private fun buildFrontmatterDocument(frontmatter: Map<String, Any>, content: String): String {
        val sb = StringBuilder()
        sb.append("---\n")
        frontmatter.forEach { (key, value) ->
            sb.append("$key: $value\n")
        }
        sb.append("---\n")
        sb.append(content)
        return sb.toString()
    }
}
