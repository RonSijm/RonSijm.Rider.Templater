package ronsijm.templater.modules

import ronsijm.templater.parser.TemplateContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope

/**
 * AppModule provides access to Rider/IntelliJ Platform APIs
 * Implements tp.app.* functions (Rider-adapted from Obsidian)
 */
class AppModule(private val context: TemplateContext, private val project: Project) {
    
    /**
     * Vault API - adapted for Rider's VirtualFileSystem
     */
    inner class Vault {
        /**
         * Get all loaded files in the project
         */
        fun getAllLoadedFiles(): List<VirtualFile> {
            val files = mutableListOf<VirtualFile>()
            val baseDir = project.guessProjectDir() ?: return files

            fun collectFiles(dir: VirtualFile) {
                dir.children.forEach { child ->
                    if (child.isDirectory) {
                        collectFiles(child)
                    } else {
                        files.add(child)
                    }
                }
            }

            collectFiles(baseDir)
            return files
        }

        /**
         * Get all markdown files
         */
        fun getMarkdownFiles(): List<VirtualFile> {
            return getAllLoadedFiles().filter { it.extension == "md" }
        }
        
        /**
         * Get file by path (relative to project root)
         */
        fun getAbstractFileByPath(path: String): VirtualFile? {
            return project.guessProjectDir()?.findFileByRelativePath(path)
        }
        
        /**
         * Create a new file
         */
        fun create(path: String, data: String): VirtualFile? {
            return WriteCommandAction.runWriteCommandAction<VirtualFile>(project) {
                try {
                    val parent = project.guessProjectDir()?.findFileByRelativePath(path.substringBeforeLast("/"))
                        ?: project.guessProjectDir()
                    val fileName = path.substringAfterLast("/")
                    val file = parent?.createChildData(this, fileName)
                    file?.setBinaryContent(data.toByteArray())
                    file
                } catch (e: Exception) {
                    null
                }
            }
        }
        
        /**
         * Read file content
         */
        fun read(file: VirtualFile): String {
            return String(file.contentsToByteArray())
        }
        
        /**
         * Modify file content
         */
        fun modify(file: VirtualFile, data: String) {
            WriteCommandAction.runWriteCommandAction(project) {
                file.setBinaryContent(data.toByteArray())
            }
        }
        
        /**
         * Delete file
         */
        fun delete(file: VirtualFile) {
            WriteCommandAction.runWriteCommandAction(project) {
                file.delete(this)
            }
        }
        
        /**
         * Rename file
         */
        fun rename(file: VirtualFile, newPath: String) {
            WriteCommandAction.runWriteCommandAction(project) {
                file.rename(this, newPath)
            }
        }
    }
    
    /**
     * FileManager API - adapted for Rider
     */
    inner class FileManager {
        /**
         * Rename file (simplified - doesn't update links)
         */
        fun renameFile(file: VirtualFile, newPath: String) {
            vault.rename(file, newPath)
        }
    }
    
    /**
     * Workspace API - adapted for Rider
     */
    inner class Workspace {
        /**
         * Get the currently active file
         */
        fun getActiveFile(): VirtualFile? {
            return FileEditorManager.getInstance(project).selectedFiles.firstOrNull()
        }
    }
    
    /**
     * Commands API - adapted for Rider's action system
     */
    inner class Commands {
        /**
         * Execute a Rider action by ID
         */
        fun executeCommandById(commandId: String) {
            val actionManager = ActionManager.getInstance()
            val action = actionManager.getAction(commandId)
            action?.let {
                ApplicationManager.getApplication().invokeLater {
                    // Execute action
                }
            }
        }
    }
    
    // Public API instances
    val vault = Vault()
    val fileManager = FileManager()
    val workspace = Workspace()
    val commands = Commands()
}

