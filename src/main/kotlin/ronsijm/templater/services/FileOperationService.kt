package ronsijm.templater.services

/**
 * Abstraction for file operations - makes testing possible without touching the filesystem
 */
interface FileOperationService {
    fun exists(path: String): Boolean
    fun rename(newName: String)
    fun move(newPath: String)
    fun createNew(template: String, filename: String?, openNew: Boolean, folder: String?): String?
    fun include(path: String): String?
    fun findFile(filename: String): String?
    fun getCreationDate(path: String? = null): Long?
    fun getLastModifiedDate(path: String? = null): Long?
    fun setCursor(order: Int?)
    fun cursorAppend(content: String)
    fun getSelection(): String?
    fun getTags(): List<String>
}

