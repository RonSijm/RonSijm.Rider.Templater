package ronsijm.templater.services.mock

import ronsijm.templater.services.FileOperationService


object NullFileOperationService : FileOperationService {
    override fun exists(path: String) = false
    override fun rename(newName: String) {}
    override fun move(newPath: String) {}
    override fun createNew(template: String, filename: String?, openNew: Boolean, folder: String?) = null
    override fun include(path: String) = null
    override fun findFile(filename: String) = null
    override fun getCreationDate(path: String?) = null
    override fun getLastModifiedDate(path: String?) = null
    override fun setCursor(order: Int?) {}
    override fun cursorAppend(content: String) {}
    override fun getSelection() = null
    override fun getTags() = emptyList<String>()
}
