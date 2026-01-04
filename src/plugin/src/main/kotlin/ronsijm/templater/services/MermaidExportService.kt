package ronsijm.templater.services

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import ronsijm.templater.settings.MermaidOutputLocation
import ronsijm.templater.settings.TemplaterSettings
import java.io.File


class MermaidExportService(
    private val project: Project,
    private val settings: TemplaterSettings
) {
    companion object {
        private val LOG = Logger.getInstance(MermaidExportService::class.java)
    }


    fun saveDiagram(mermaidContent: String, sourceFile: VirtualFile): String? {
        val outputPath = resolveOutputPath(sourceFile) ?: return null

        return try {
            val outputFile = File(outputPath)
            outputFile.parentFile?.mkdirs()
            outputFile.writeText(mermaidContent)
            outputPath
        } catch (e: Exception) {
            LOG.warn("Failed to save Mermaid diagram to $outputPath: ${e.message}")
            null
        }
    }


    private fun resolveOutputPath(sourceFile: VirtualFile): String? {
        return when (settings.mermaidOutputLocation) {
            MermaidOutputLocation.SAME_AS_SCRIPT -> {
                val parentPath = sourceFile.parent?.path ?: return null
                val baseName = sourceFile.nameWithoutExtension
                "$parentPath/$baseName.mmd"
            }
            MermaidOutputLocation.DEDICATED_FOLDER -> {
                val folder = settings.mermaidOutputFolder.ifEmpty {
                    project.guessProjectDir()?.path ?: return null
                }
                val baseName = sourceFile.nameWithoutExtension
                "$folder/$baseName.mmd"
            }
        }
    }
}

