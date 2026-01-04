package ronsijm.templater.cli.filesystem

import ronsijm.templater.filesystem.FileHandle
import ronsijm.templater.filesystem.FileSystem
import java.io.File

class JavaFileSystem(private val rootDir: File) : FileSystem {

    override fun getAllFiles(): List<FileHandle> {
        val files = mutableListOf<FileHandle>()
        collectFiles(rootDir, files)
        return files
    }

    private fun collectFiles(directory: File, files: MutableList<FileHandle>) {
        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                collectFiles(file, files)
            } else {
                files.add(JavaFileHandle(file))
            }
        }
    }

    override fun createFile(path: String, content: String): FileHandle? {
        return try {
            val file = if (File(path).isAbsolute) {
                File(path)
            } else {
                File(rootDir, path)
            }


            file.parentFile?.mkdirs()


            file.writeText(content)

            JavaFileHandle(file)
        } catch (e: Exception) {
            null
        }
    }

    override fun findFile(path: String): FileHandle? {
        val file = if (File(path).isAbsolute) {
            File(path)
        } else {
            File(rootDir, path)
        }

        return if (file.exists()) {
            JavaFileHandle(file)
        } else {
            null
        }
    }

    override fun getProjectRoot(): FileHandle? {
        return if (rootDir.exists()) {
            JavaFileHandle(rootDir)
        } else {
            null
        }
    }

    override fun rename(file: FileHandle, newPath: String): FileHandle? {
        if (file !is JavaFileHandle) {
            return null
        }

        return try {
            val oldFile = file.getFile()
            val newFile = File(oldFile.parent, newPath)

            if (oldFile.renameTo(newFile)) {
                JavaFileHandle(newFile)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

