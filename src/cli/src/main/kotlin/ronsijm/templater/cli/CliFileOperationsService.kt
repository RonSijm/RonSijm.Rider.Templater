package ronsijm.templater.cli

import ronsijm.templater.services.FileOperationService
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes

class CliFileOperationsService(
    private val baseFile: File,
    private val workingDir: File
) : FileOperationService {

    var pendingRename: String? = null
        private set
    var pendingMove: String? = null
        private set

    override fun exists(path: String): Boolean {
        return resolveFile(path).exists()
    }

    override fun rename(newName: String) {
        pendingRename = newName
    }

    override fun move(newPath: String) {
        pendingMove = newPath
    }

    override fun createNew(template: String, filename: String?, openNew: Boolean, folder: String?): String? {
        return try {
            val targetFolder = if (folder != null) resolveFile(folder) else workingDir
            val targetFile = File(targetFolder, filename ?: "new_file.md")
            targetFolder.mkdirs()
            targetFile.writeText(template)
            targetFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    override fun include(path: String): String? {
        val file = resolveFile(path)
        return if (file.exists()) file.readText() else null
    }

    override fun findFile(filename: String): String? {
        return findFileRecursive(workingDir, filename)?.absolutePath
    }

    override fun getCreationDate(path: String?): Long? {
        val file = if (path != null) resolveFile(path) else baseFile
        return try {
            val attrs = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
            attrs.creationTime().toMillis()
        } catch (e: Exception) {
            null
        }
    }

    override fun getLastModifiedDate(path: String?): Long? {
        val file = if (path != null) resolveFile(path) else baseFile
        return try {
            file.lastModified()
        } catch (e: Exception) {
            null
        }
    }

    override fun setCursor(order: Int?) {

    }

    override fun cursorAppend(content: String) {

    }

    override fun getSelection(): String? {

        return null
    }

    override fun getTags(): List<String> {
        val content = if (baseFile.exists()) baseFile.readText() else return emptyList()
        return extractTagsFromFrontmatter(content)
    }

    private fun resolveFile(path: String): File {
        val file = File(path)
        return if (file.isAbsolute) file else File(workingDir, path)
    }

    private fun findFileRecursive(dir: File, filename: String): File? {
        val stack = ArrayDeque<File>()
        stack.add(dir)

        while (stack.isNotEmpty()) {
            val current = stack.removeLast()
            val files = current.listFiles() ?: continue

            for (file in files) {
                if (file.isDirectory) {
                    stack.add(file)
                } else if (file.name == filename || file.nameWithoutExtension == filename) {
                    return file
                }
            }
        }
        return null
    }

    private fun extractTagsFromFrontmatter(content: String): List<String> {
        if (!content.startsWith("---")) return emptyList()
        val endIndex = content.indexOf("---", 3)
        if (endIndex == -1) return emptyList()

        val frontmatter = content.substring(3, endIndex)
        val tagsLine = frontmatter.lines().find { it.trim().startsWith("tags:") }
        return tagsLine?.substringAfter("tags:")?.trim()
            ?.removeSurrounding("[", "]")
            ?.split(",")
            ?.map { it.trim() }
            ?: emptyList()
    }
}
