package ronsijm.templater.filesystem

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import ronsijm.templater.filesystem.FileHandle
import ronsijm.templater.filesystem.FileSystem


class IntelliJFileSystem(private val project: Project) : FileSystem {

    override fun getAllFiles(): List<FileHandle> {
        val files = mutableListOf<FileHandle>()
        val baseDir = project.guessProjectDir() ?: return files

        collectFiles(baseDir, files)
        return files
    }

    private fun collectFiles(directory: VirtualFile, files: MutableList<FileHandle>) {
        ApplicationManager.getApplication().runReadAction {
            directory.children.forEach { child ->
                if (child.isDirectory) {
                    collectFiles(child, files)
                } else {
                    files.add(VirtualFileHandle(child, project))
                }
            }
        }
    }

    override fun createFile(path: String, content: String): FileHandle? {
        return WriteCommandAction.runWriteCommandAction<FileHandle?>(project) {
            try {
                val baseDir = project.guessProjectDir() ?: return@runWriteCommandAction null


                val normalizedPath = path.replace('\\', '/')
                val lastSlash = normalizedPath.lastIndexOf('/')

                val (parentPath, fileName) = if (lastSlash >= 0) {
                    normalizedPath.substring(0, lastSlash) to normalizedPath.substring(lastSlash + 1)
                } else {
                    "" to normalizedPath
                }


                val parentDir = if (parentPath.isEmpty()) {
                    baseDir
                } else {
                    findOrCreateDirectory(baseDir, parentPath)
                } ?: return@runWriteCommandAction null


                val file = parentDir.createChildData(this, fileName)
                file.setBinaryContent(content.toByteArray())

                VirtualFileHandle(file, project)
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun findOrCreateDirectory(baseDir: VirtualFile, path: String): VirtualFile? {
        var current = baseDir
        val parts = path.split('/')

        for (part in parts) {
            if (part.isEmpty()) continue

            var child = current.findChild(part)
            if (child == null) {
                child = current.createChildDirectory(this, part)
            }

            if (!child.isDirectory) {
                return null
            }

            current = child
        }

        return current
    }

    override fun findFile(path: String): FileHandle? {
        return ApplicationManager.getApplication().runReadAction<FileHandle?> {
            val baseDir = project.guessProjectDir() ?: return@runReadAction null
            val normalizedPath = path.replace('\\', '/')

            val vf = baseDir.findFileByRelativePath(normalizedPath)
            vf?.let { VirtualFileHandle(it, project) }
        }
    }

    override fun getProjectRoot(): FileHandle? {
        val root = project.guessProjectDir()
        return root?.let { VirtualFileHandle(it, project) }
    }

    override fun rename(file: FileHandle, newPath: String): FileHandle? {
        if (file !is VirtualFileHandle) {
            return null
        }

        return WriteCommandAction.runWriteCommandAction<FileHandle?>(project) {
            try {
                val vf = file.getVirtualFile()
                val newName = newPath.substringAfterLast('/')
                vf.rename(this, newName)
                VirtualFileHandle(vf, project)
            } catch (e: Exception) {
                null
            }
        }
    }
}

