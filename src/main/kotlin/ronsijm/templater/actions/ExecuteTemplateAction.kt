package ronsijm.templater.actions

import ronsijm.templater.parser.FrontmatterParser
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.parser.TemplateParser
import ronsijm.templater.parallel.ParallelTemplateParser
import ronsijm.templater.services.ServiceContainer
import ronsijm.templater.services.IntelliJSystemOperationsService
import ronsijm.templater.services.IntelliJFileOperationsService
import ronsijm.templater.settings.TemplaterSettings
import ronsijm.templater.settings.PopupBehavior
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile

/**
 * Action to execute template in the current file.
 * If text is selected, only the selected template block(s) are executed.
 * Otherwise, the entire document is processed.
 */
class ExecuteTemplateAction : AnAction() {

    private val frontmatterParser = FrontmatterParser()
    private val templateParser = TemplateParser()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val document = editor.document
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val settings = TemplaterSettings.getInstance()
        val startTime = if (settings.showExecutionStats) System.currentTimeMillis() else 0L

        try {
            // Check if there's a selection and if selection-only execution is enabled
            val selectionModel = editor.selectionModel
            val hasSelection = selectionModel.hasSelection()
            val useSelectionOnly = hasSelection && settings.enableSelectionOnlyExecution

            if (useSelectionOnly) {
                executeSelectedTemplate(project, editor, document, file, settings)
            } else {
                executeTemplate(project, document, file, settings)
            }

            // Show success popup based on settings
            if (settings.popupBehavior == PopupBehavior.ALWAYS) {
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
        } catch (ex: Exception) {
            // Show error popup unless set to NEVER
            if (settings.popupBehavior != PopupBehavior.NEVER) {
                Messages.showErrorDialog(
                    project,
                    "Error executing template: ${ex.message}",
                    "Templater Error"
                )
            }
        }
    }

    override fun update(e: AnActionEvent) {
        // Enable action only when editor is available
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = editor != null
    }

    /**
     * Execute template only for the selected text
     */
    private fun executeSelectedTemplate(
        project: Project,
        editor: Editor,
        document: Document,
        file: VirtualFile,
        settings: TemplaterSettings
    ) {
        val selectionModel = editor.selectionModel
        val selectedText = selectionModel.selectedText ?: return
        val selectionStart = selectionModel.selectionStart
        val selectionEnd = selectionModel.selectionEnd

        // Get full document content for frontmatter parsing
        val fullContent = document.text
        val parseResult = frontmatterParser.parse(fullContent)

        // Create service container
        val fileOperationService = IntelliJFileOperationsService(project, file, parseResult.frontmatter)
        val services = ServiceContainer(
            systemOperationsService = IntelliJSystemOperationsService(project),
            fileOperationService = fileOperationService
        )

        // Create template context
        val context = TemplateContext(
            frontmatter = parseResult.frontmatter,
            frontmatterParser = frontmatterParser,
            fileName = file.name,
            filePath = file.path,
            fileContent = parseResult.content,
            services = services
        )

        // Parse only the selected text
        val processedSelection = if (settings.enableParallelExecution) {
            val parallelParser = ParallelTemplateParser(
                validateSyntax = settings.enableSyntaxValidation,
                services = services,
                enableParallel = true
            )
            parallelParser.parse(selectedText, context, project)
        } else {
            templateParser.parse(selectedText, context, project)
        }

        // Replace only the selected text and apply pending file operations
        WriteCommandAction.runWriteCommandAction(project) {
            document.replaceString(selectionStart, selectionEnd, processedSelection)

            // Clear selection after execution
            selectionModel.removeSelection()

            // Apply pending rename operation from service
            if (fileOperationService.pendingRename != null) {
                try {
                    file.rename(this, fileOperationService.pendingRename!!)
                } catch (ex: Exception) {
                    Messages.showErrorDialog(
                        project,
                        "Failed to rename file: ${ex.message}",
                        "Templater Error"
                    )
                }
            }

            // Apply pending move operation from service
            if (fileOperationService.pendingMove != null) {
                try {
                    val targetPath = fileOperationService.pendingMove!!
                    val parentPath = targetPath.substringBeforeLast("/", "")

                    if (parentPath.isNotEmpty()) {
                        val baseDir = file.parent
                        var targetDir = baseDir

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
    }

    /**
     * Execute template in the document
     */
    private fun executeTemplate(project: Project, document: Document, file: VirtualFile, settings: TemplaterSettings) {
        val content = document.text

        // Parse frontmatter
        val parseResult = frontmatterParser.parse(content)

        // Create service container with real implementations
        // Pass frontmatter to file service for tags extraction
        val fileOperationService = IntelliJFileOperationsService(project, file, parseResult.frontmatter)
        val services = ServiceContainer(
            systemOperationsService = IntelliJSystemOperationsService(project),
            fileOperationService = fileOperationService
        )

        // Create template context - all operations are handled by services
        val context = TemplateContext(
            frontmatter = parseResult.frontmatter,
            frontmatterParser = frontmatterParser,
            fileName = file.name,
            filePath = file.path,
            fileContent = parseResult.content,
            services = services
        )

        // Parse templates in content (not in frontmatter)
        // Use parallel parser if experimental feature is enabled
        val processedContent = if (settings.enableParallelExecution) {
            val parallelParser = ParallelTemplateParser(
                validateSyntax = settings.enableSyntaxValidation,
                services = services,
                enableParallel = true
            )
            parallelParser.parse(parseResult.content, context, project)
        } else {
            templateParser.parse(parseResult.content, context, project)
        }

        // Reconstruct the full document
        val newContent = if (parseResult.hasFrontmatter) {
            buildFrontmatterDocument(parseResult.frontmatter, processedContent)
        } else {
            processedContent
        }

        // Update document and apply pending file operations
        WriteCommandAction.runWriteCommandAction(project) {
            document.setText(newContent)

            // Apply pending rename operation from service
            if (fileOperationService.pendingRename != null) {
                try {
                    file.rename(this, fileOperationService.pendingRename!!)
                } catch (ex: Exception) {
                    Messages.showErrorDialog(
                        project,
                        "Failed to rename file: ${ex.message}",
                        "Templater Error"
                    )
                }
            }

            // Apply pending move operation from service
            if (fileOperationService.pendingMove != null) {
                try {
                    val targetPath = fileOperationService.pendingMove!!
                    val parentPath = targetPath.substringBeforeLast("/", "")

                    if (parentPath.isNotEmpty()) {
                        val baseDir = file.parent
                        var targetDir = baseDir

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
    }
    
    /**
     * Build document with frontmatter
     */
    private fun buildFrontmatterDocument(frontmatter: Map<String, Any>, content: String): String {
        val sb = StringBuilder()
        sb.append("---\n")
        
        // Write frontmatter
        frontmatter.forEach { (key, value) ->
            sb.append("$key: $value\n")
        }
        
        sb.append("---\n")
        sb.append(content)
        
        return sb.toString()
    }
}

