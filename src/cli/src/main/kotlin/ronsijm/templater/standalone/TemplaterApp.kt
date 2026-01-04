package ronsijm.templater.standalone

import com.formdev.flatlaf.FlatDarculaLaf
import ronsijm.templater.standalone.services.TemplateExecutionService
import ronsijm.templater.standalone.settings.AppSettings
import ronsijm.templater.standalone.ui.MainWindow
import java.io.File
import javax.swing.SwingUtilities
import javax.swing.UIManager

fun main(args: Array<String>) {
    if (args.contains("--run")) {
        runHeadless(args)
        return
    }

    FlatDarculaLaf.setup()

    UIManager.put("Component.arc", 8)
    UIManager.put("Button.arc", 8)
    UIManager.put("TextComponent.arc", 8)

    val fileToOpen = args.firstOrNull()?.let { File(it) }?.takeIf { it.exists() }

    SwingUtilities.invokeLater {
        val mainWindow = MainWindow()
        mainWindow.isVisible = true

        fileToOpen?.let { file ->
            if (file.isFile) {
                mainWindow.editorPanel.loadFile(file)
                file.parentFile?.let { parent ->
                    mainWindow.fileTreePanel.loadFolder(parent)
                }
            } else if (file.isDirectory) {
                mainWindow.fileTreePanel.loadFolder(file)
            }
        }
    }
}

private fun runHeadless(args: Array<String>) {
    val fileArg = args.filter { !it.startsWith("--") }.firstOrNull()

    if (fileArg == null) {
        System.err.println("Error: No file specified for --run")
        System.err.println("Usage: templater --run <file.md>")
        kotlin.system.exitProcess(1)
    }

    val file = File(fileArg)
    if (!file.exists()) {
        System.err.println("Error: File not found: $fileArg")
        kotlin.system.exitProcess(1)
    }

    if (!file.isFile) {
        System.err.println("Error: Not a file: $fileArg")
        kotlin.system.exitProcess(1)
    }

    try {
        val content = file.readText()
        val result = TemplateExecutionService.execute(content, file)

        if (result.success) {

            val behavior = AppSettings.getAfterRunningBehavior()

            when (behavior) {
                AppSettings.AfterRunningBehavior.OVERWRITE_AUTOMATICALLY -> {
                    file.writeText(result.output)
                    println("Template executed successfully. File overwritten: ${file.absolutePath}")
                }
                AppSettings.AfterRunningBehavior.SAVE_SIDE_BY_SIDE -> {
                    val postfix = AppSettings.getSideBySidePostfix()
                    val outputFile = File(
                        file.parentFile,
                        file.nameWithoutExtension + postfix + "." + file.extension
                    )
                    outputFile.writeText(result.output)
                    println("Template executed successfully. Output saved to: ${outputFile.absolutePath}")
                }
            }
        } else {
            System.err.println("Error executing template: ${result.error?.message}")
            result.error?.printStackTrace()
            kotlin.system.exitProcess(1)
        }
    } catch (e: Exception) {
        System.err.println("Error: ${e.message}")
        e.printStackTrace()
        kotlin.system.exitProcess(1)
    }
}

