package ronsijm.templater.filesystem

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ronsijm.templater.filesystem.FileHandle


class VirtualFileHandle(
    private val virtualFile: VirtualFile,
    private val project: Project
) : FileHandle {

    override val path: String
        get() = virtualFile.path

    override val name: String
        get() = virtualFile.name

    override val extension: String
        get() = virtualFile.extension ?: ""

    override val isDirectory: Boolean
        get() = virtualFile.isDirectory

    override val parent: FileHandle?
        get() = virtualFile.parent?.let { VirtualFileHandle(it, project) }

    override fun read(): String {
        return ApplicationManager.getApplication().runReadAction<String> {
            String(virtualFile.contentsToByteArray())
        }
    }

    override fun write(content: String) {
        WriteCommandAction.runWriteCommandAction(project) {
            virtualFile.setBinaryContent(content.toByteArray())
        }
    }

    override fun delete() {
        WriteCommandAction.runWriteCommandAction(project) {
            virtualFile.delete(this)
        }
    }

    override fun exists(): Boolean {
        return virtualFile.exists()
    }


    fun getVirtualFile(): VirtualFile = virtualFile

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VirtualFileHandle) return false
        return virtualFile == other.virtualFile
    }

    override fun hashCode(): Int {
        return virtualFile.hashCode()
    }

    override fun toString(): String {
        return "VirtualFileHandle(path='$path')"
    }
}

