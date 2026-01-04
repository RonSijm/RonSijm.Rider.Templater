package ronsijm.templater.modules

import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.filesystem.FileHandle
import ronsijm.templater.filesystem.FileSystem
import ronsijm.templater.filesystem.VirtualFileHandle
import com.intellij.openapi.project.Project
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.ApplicationManager


class AppModule(
    private val context: TemplateContext,
    private val fileSystem: FileSystem,
    private val project: Project
) {


    inner class Vault {

        private var cachedFiles: List<FileHandle>? = null
        private var cacheTimestamp: Long = 0
        private val cacheValidityMs = 5000L


        fun getAllLoadedFiles(): List<FileHandle> {
            val now = System.currentTimeMillis()


            cachedFiles?.let { cached ->
                if (now - cacheTimestamp < cacheValidityMs) {
                    return cached
                }
            }

            val files = fileSystem.getAllFiles()


            cachedFiles = files
            cacheTimestamp = now
            return files
        }


        fun getMarkdownFiles(): List<FileHandle> {
            return getAllLoadedFiles().filter { it.extension == "md" }
        }


        fun getAbstractFileByPath(path: String): FileHandle? {
            return fileSystem.findFile(path)
        }


        fun create(path: String, data: String): FileHandle? {
            return fileSystem.createFile(path, data)
        }


        fun read(file: FileHandle): String {
            return file.read()
        }


        fun modify(file: FileHandle, data: String) {
            file.write(data)
        }


        fun delete(file: FileHandle) {
            file.delete()
        }


        fun rename(file: FileHandle, newPath: String): FileHandle? {
            return fileSystem.rename(file, newPath)
        }
    }


    inner class FileManager {

        fun renameFile(file: FileHandle, newPath: String): FileHandle? {
            return vault.rename(file, newPath)
        }
    }


    inner class Workspace {

        fun getActiveFile(): FileHandle? {
            val vf = FileEditorManager.getInstance(project).selectedFiles.firstOrNull()
            return vf?.let { VirtualFileHandle(it, project) }
        }
    }


    inner class Commands {

        fun executeCommandById(commandId: String) {
            val actionManager = ActionManager.getInstance()
            val action = actionManager.getAction(commandId)
            action?.let {
                ApplicationManager.getApplication().invokeLater {

                }
            }
        }
    }


    val vault = Vault()
    val fileManager = FileManager()
    val workspace = Workspace()
    val commands = Commands()
}
