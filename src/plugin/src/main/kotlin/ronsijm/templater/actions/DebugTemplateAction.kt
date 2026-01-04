package ronsijm.templater.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import ronsijm.templater.debug.ActiveDebugSession
import ronsijm.templater.debug.DebugAction
import ronsijm.templater.debug.DebuggingTemplateParser
import ronsijm.templater.debug.TemplaterDebugService
import ronsijm.templater.modules.IntelliJAppModuleProvider
import ronsijm.templater.parser.FrontmatterParser
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.services.DefaultHttpService
import ronsijm.templater.services.IntelliJFileOperationsService
import ronsijm.templater.services.IntelliJSystemOperationsService
import ronsijm.templater.services.ServiceContainer
import ronsijm.templater.services.SystemClipboardService
import ronsijm.templater.settings.TemplaterSettings
import ronsijm.templater.utils.ProgressIndicatorCancellationChecker


class DebugTemplateAction : AnAction() {

    private val frontmatterParser = FrontmatterParser()

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
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val document = editor.document
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val debugService = TemplaterDebugService.getInstance(project)


        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Templater Debugger")
        toolWindow?.show()


        val session = debugService.startSession(file)


        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Debugging Template", true) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    indicator.text = "Starting debug session..."


                    session.progressIndicator = indicator

                    val content = ApplicationManager.getApplication().runReadAction<String> { document.text }
                    val frontmatterResult = frontmatterParser.parse(content)

                    val settings = TemplaterSettings.getInstance()
                    val services = createServices(project, file, frontmatterResult.frontmatter)
                    val context = createTemplateContext(frontmatterResult, file, services)

                    val debugParser = DebuggingTemplateParser(
                        validateSyntax = settings.enableSyntaxValidation,
                        services = services
                    )


                    val breakpoints = session.getBreakpoints()



                    val frontmatterLineOffset = if (frontmatterResult.hasFrontmatter) {


                        frontmatterResult.frontmatterRaw.count { it == '\n' } + 3
                    } else {
                        0
                    }




                    val validBreakpoints = breakpoints.filter { it > frontmatterLineOffset }.toSet()


                    val startInStepMode = validBreakpoints.isEmpty()


                    session.variableUpdater = debugParser.getVariableUpdater()



                    debugParser.startDebugSession(
                        onBreakpoint = { breakpoint ->

                            if (indicator.isCanceled || !session.isActive) {
                                return@startDebugSession DebugAction.STOP
                            }

                            session.onBreakpointHit(breakpoint)
                        },
                        onComplete = { _ ->

                            ApplicationManager.getApplication().invokeLater {
                                debugService.stopSession()
                            }
                        },
                        startInStepMode = startInStepMode,
                        onBlockProcessed = if (settings.debugIncrementalUpdates) { _, _, currentDocument, _ ->


                            ApplicationManager.getApplication().invokeLater {
                                WriteCommandAction.runWriteCommandAction(project) {

                                    val fullDocument = if (frontmatterResult.frontmatterRaw.isNotEmpty()) {
                                        "---\n${frontmatterResult.frontmatterRaw}\n---\n${currentDocument}"
                                    } else {
                                        currentDocument
                                    }
                                    document.setText(fullDocument)
                                }
                            }
                        } else null
                    )


                    validBreakpoints.forEach { lineNumber ->
                        debugParser.addBreakpoint(lineNumber)
                    }

                    val cancellationChecker = ProgressIndicatorCancellationChecker(indicator)
                    val appModuleProvider = IntelliJAppModuleProvider(context, project)

                    indicator.text = "Executing template with debugging..."


                    val result = debugParser.parse(
                        content = frontmatterResult.content,
                        context = context,
                        appModuleProvider = appModuleProvider,
                        cancellationChecker = cancellationChecker,
                        documentLineOffset = frontmatterLineOffset
                    )

                    if (!result.wasStopped && !indicator.isCanceled) {


                        if (!settings.debugIncrementalUpdates) {
                            ApplicationManager.getApplication().invokeLater {
                                WriteCommandAction.runWriteCommandAction(project) {
                                    val fullDocument = if (frontmatterResult.frontmatterRaw.isNotEmpty()) {
                                        "---\n${frontmatterResult.frontmatterRaw}\n---\n${result.result}"
                                    } else {
                                        result.result
                                    }
                                    document.setText(fullDocument)
                                }
                            }
                        }
                    }

                } catch (ex: Exception) {
                    ApplicationManager.getApplication().invokeLater {
                        debugService.stopSession()
                    }
                }
            }

            override fun onCancel() {
                session.stop()
                debugService.stopSession()
            }
        })
    }

    private fun createServices(
        project: Project,
        file: VirtualFile,
        frontmatter: Map<String, Any>
    ): ServiceContainer {
        val fileOperationService = IntelliJFileOperationsService(project, file, frontmatter)
        return ServiceContainer(
            clipboardService = SystemClipboardService(),
            httpService = DefaultHttpService(),
            systemOperationsService = IntelliJSystemOperationsService(project),
            fileOperationService = fileOperationService
        )
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
}

