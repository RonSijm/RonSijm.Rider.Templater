package ronsijm.templater.services

class MockFileOperationsService(
    private val existingFiles: MutableSet<String> = mutableSetOf(),
    private val fileContents: MutableMap<String, String> = mutableMapOf(),
    private val creationDates: MutableMap<String, Long> = mutableMapOf(),
    private val modificationDates: MutableMap<String, Long> = mutableMapOf(),
    private val currentFilePath: String = "current.md",
    private var tags: List<String> = emptyList(),
    private var selection: String? = null
) : FileOperationService {

    // Track calls for verification in tests
    val existsCalls = mutableListOf<String>()
    val renameCalls = mutableListOf<String>()
    val moveCalls = mutableListOf<String>()
    val createNewCalls = mutableListOf<CreateNewCall>()
    val includeCalls = mutableListOf<String>()
    val findFileCalls = mutableListOf<String>()
    val setCursorCalls = mutableListOf<Int?>()
    val cursorAppendCalls = mutableListOf<String>()
    val getSelectionCalls = mutableListOf<Unit>()
    val getTagsCalls = mutableListOf<Unit>()

    data class CreateNewCall(val template: String, val filename: String?, val openNew: Boolean, val folder: String?)

    override fun exists(path: String): Boolean {
        existsCalls.add(path)
        return existingFiles.contains(path)
    }

    override fun rename(newName: String) {
        renameCalls.add(newName)
    }

    override fun move(newPath: String) {
        moveCalls.add(newPath)
    }

    override fun createNew(template: String, filename: String?, openNew: Boolean, folder: String?): String? {
        createNewCalls.add(CreateNewCall(template, filename, openNew, folder))
        val path = filename ?: "new-file.md"
        val fullPath = if (folder != null) "$folder/$path" else path
        existingFiles.add(fullPath)
        fileContents[fullPath] = template
        return fullPath
    }

    override fun include(path: String): String? {
        includeCalls.add(path)
        return fileContents[path]
    }

    override fun findFile(filename: String): String? {
        findFileCalls.add(filename)
        return existingFiles.find { it.endsWith(filename) }
    }

    override fun getCreationDate(path: String?): Long? {
        val filePath = path ?: currentFilePath
        return creationDates[filePath]
    }

    override fun getLastModifiedDate(path: String?): Long? {
        val filePath = path ?: currentFilePath
        return modificationDates[filePath]
    }

    override fun setCursor(order: Int?) {
        setCursorCalls.add(order)
    }

    override fun cursorAppend(content: String) {
        cursorAppendCalls.add(content)
    }

    override fun getSelection(): String? {
        getSelectionCalls.add(Unit)
        return selection
    }

    override fun getTags(): List<String> {
        getTagsCalls.add(Unit)
        return tags
    }

    fun addFile(path: String, content: String = "", creationDate: Long? = null, modificationDate: Long? = null) {
        existingFiles.add(path)
        fileContents[path] = content
        creationDate?.let { creationDates[path] = it }
        modificationDate?.let { modificationDates[path] = it }
    }

    fun getFileContent(path: String): String? {
        return fileContents[path]
    }

    fun setCreationDate(path: String, timestamp: Long) {
        creationDates[path] = timestamp
    }

    fun setModificationDate(path: String, timestamp: Long) {
        modificationDates[path] = timestamp
    }

    fun setTags(newTags: List<String>) {
        tags = newTags
    }

    fun setSelection(newSelection: String?) {
        selection = newSelection
    }
}

