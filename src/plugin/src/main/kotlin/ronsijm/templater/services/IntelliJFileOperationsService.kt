package ronsijm.templater.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.io.IOException


class IntelliJFileOperationsService(
    private val project: Project,
    private val baseFile: VirtualFile,
    private val frontmatter: Map<String, Any> = emptyMap()
) : FileOperationService {

    var pendingRename: String? = null
        private set
    var pendingMove: String? = null
        private set
    var cursorPosition: Int? = null
        private set
    var cursorAppendContent: String? = null
        private set

    override fun exists(path: String): Boolean {
        val baseDir = baseFile.parent ?: return false
        return baseDir.findFileByRelativePath(path) != null
    }

    override fun rename(newName: String) {
        pendingRename = newName
    }

    override fun move(newPath: String) {
        pendingMove = newPath
    }

    override fun createNew(template: String, filename: String?, openNew: Boolean, folder: String?): String? {
        return try {
            val baseDir = baseFile.parent ?: return null
            val targetDir = if (folder != null) {
                baseDir.findFileByRelativePath(folder) ?: baseDir
            } else {
                baseDir
            }

            val fileName = filename ?: "new-file.md"
            var newFile: VirtualFile? = null
            WriteCommandAction.runWriteCommandAction(project) {
                newFile = targetDir.createChildData(this, fileName)
                newFile?.setBinaryContent(template.toByteArray())
            }

            if (openNew && newFile != null) {
                ApplicationManager.getApplication().invokeLater {
                    FileEditorManager.getInstance(project).openFile(newFile!!, true)
                }
            }

            newFile?.path
        } catch (e: IOException) {
            null
        }
    }

    override fun include(path: String): String? {
        return try {
            val baseDir = baseFile.parent ?: return null
            val file = baseDir.findFileByRelativePath(path) ?: return null
            String(file.contentsToByteArray())
        } catch (e: IOException) {
            null
        }
    }

    override fun findFile(filename: String): String? {
        val baseDir = baseFile.parent ?: return null
        val found = baseDir.findChild(filename)
        return found?.path
    }

    override fun getCreationDate(path: String?): Long? {
        val file = if (path != null) {
            val baseDir = baseFile.parent ?: return null
            baseDir.findFileByRelativePath(path)
        } else {
            baseFile
        }


        return file?.timeStamp
    }

    override fun getLastModifiedDate(path: String?): Long? {
        val file = if (path != null) {
            val baseDir = baseFile.parent ?: return null
            baseDir.findFileByRelativePath(path)
        } else {
            baseFile
        }

        return file?.timeStamp
    }

    override fun setCursor(order: Int?) {
        cursorPosition = order
    }

    override fun cursorAppend(content: String) {
        cursorAppendContent = content
    }

    override fun getSelection(): String? {
        val app = ApplicationManager.getApplication()

        return if (app.isDispatchThread) {
            val editor = FileEditorManager.getInstance(project).selectedTextEditor
            editor?.selectionModel?.selectedText
        } else {
            var result: String? = null
            app.invokeAndWait({
                val editor = FileEditorManager.getInstance(project).selectedTextEditor
                result = editor?.selectionModel?.selectedText
            }, com.intellij.openapi.application.ModalityState.defaultModalityState())
            result
        }
    }

    override fun getTags(): List<String> {
        val tags = mutableListOf<String>()
        frontmatter["tags"]?.let { tagValue ->
            when (tagValue) {
                is List<*> -> tags.addAll(tagValue.mapNotNull { it?.toString() })
                is String -> tags.add(tagValue)
            }
        }
        return tags
    }


    fun applyPendingRename(): Boolean {
        val newName = pendingRename ?: return false
        return try {
            WriteCommandAction.runWriteCommandAction(project) {
                baseFile.rename(this, newName)
            }
            true
        } catch (e: IOException) {
            false
        }
    }


    fun applyPendingMove(): Boolean {
        val targetPath = pendingMove ?: return false
        return try {
            val baseDir = baseFile.parent ?: return false
            val targetDir = baseDir.findFileByRelativePath(targetPath) ?: return false

            WriteCommandAction.runWriteCommandAction(project) {
                baseFile.move(this, targetDir)
            }
            true
        } catch (e: IOException) {
            false
        }
    }
}