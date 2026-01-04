package ronsijm.templater.cli.filesystem

import ronsijm.templater.filesystem.FileHandle
import java.io.File

class JavaFileHandle(private val file: File) : FileHandle {

    override val path: String
        get() = file.absolutePath

    override val name: String
        get() = file.name

    override val extension: String
        get() = file.extension

    override val isDirectory: Boolean
        get() = file.isDirectory

    override val parent: FileHandle?
        get() = file.parentFile?.let { JavaFileHandle(it) }

    override fun read(): String {
        return file.readText()
    }

    override fun write(content: String) {
        file.writeText(content)
    }

    override fun delete() {
        file.delete()
    }

    override fun exists(): Boolean {
        return file.exists()
    }

    fun getFile(): File = file

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JavaFileHandle) return false
        return file == other.file
    }

    override fun hashCode(): Int {
        return file.hashCode()
    }

    override fun toString(): String {
        return "JavaFileHandle(path='$path')"
    }
}

