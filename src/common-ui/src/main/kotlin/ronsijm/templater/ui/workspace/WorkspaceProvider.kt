package ronsijm.templater.ui.workspace

import java.io.File

interface WorkspaceProvider {


    fun getRootDirectory(): File?


    fun getActiveFile(): FileHandle?


    fun getOpenFiles(): List<FileHandle>


    fun openFile(file: FileHandle)


    fun getAllFiles(): List<FileHandle>


    fun getMarkdownFiles(): List<FileHandle>


    fun getFileByPath(path: String): FileHandle?


    fun createFile(path: String, content: String): FileHandle?


    fun readFile(file: FileHandle): String?


    fun modifyFile(file: FileHandle, content: String): Boolean


    fun deleteFile(file: FileHandle): Boolean


    fun renameFile(file: FileHandle, newPath: String): FileHandle?


    fun addChangeListener(listener: WorkspaceChangeListener)


    fun removeChangeListener(listener: WorkspaceChangeListener)
}


interface FileHandle {

    val name: String


    val basename: String


    val extension: String


    val path: String


    val absolutePath: String


    val parentPath: String?


    val isDirectory: Boolean


    fun exists(): Boolean


    fun toFile(): File
}


interface WorkspaceChangeListener {
    fun onFileCreated(file: FileHandle)
    fun onFileModified(file: FileHandle)
    fun onFileDeleted(file: FileHandle)
    fun onFileRenamed(oldFile: FileHandle, newFile: FileHandle)
}


class SimpleFileHandle(
    private val file: File,
    private val workspaceRoot: File
) : FileHandle {

    override val name: String get() = file.name

    override val basename: String get() = file.nameWithoutExtension

    override val extension: String get() = file.extension

    override val path: String get() = file.relativeTo(workspaceRoot).path.replace('\\', '/')

    override val absolutePath: String get() = file.absolutePath

    override val parentPath: String? get() = file.parentFile?.relativeTo(workspaceRoot)?.path?.replace('\\', '/')

    override val isDirectory: Boolean get() = file.isDirectory

    override fun exists(): Boolean = file.exists()

    override fun toFile(): File = file

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SimpleFileHandle) return false
        return file.absolutePath == other.file.absolutePath
    }

    override fun hashCode(): Int = file.absolutePath.hashCode()

    override fun toString(): String = "FileHandle($path)"
}

