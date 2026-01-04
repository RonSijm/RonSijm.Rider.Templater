package ronsijm.templater.standalone

import java.io.File
import java.util.prefs.Preferences

object RecentFolders {
    private const val MAX_RECENT = 10
    private const val PREFS_KEY = "recent_folders"

    private val prefs = Preferences.userNodeForPackage(RecentFolders::class.java)

    fun addFolder(folder: File) {
        val recent = getRecentFolders().toMutableList()
        recent.removeIf { it.absolutePath == folder.absolutePath }
        recent.add(0, folder)

        if (recent.size > MAX_RECENT) {
            recent.subList(MAX_RECENT, recent.size).clear()
        }

        saveRecentFolders(recent)
    }

    fun getRecentFolders(): List<File> {
        val paths = prefs.get(PREFS_KEY, "")
        if (paths.isEmpty()) return emptyList()

        return paths.split("|")
            .filter { it.isNotEmpty() }
            .map { File(it) }
            .filter { it.exists() && it.isDirectory }
    }

    fun getMostRecentFolder(): File? {
        return getRecentFolders().firstOrNull()
    }

    fun clearRecentFolders() {
        prefs.remove(PREFS_KEY)
    }

    private fun saveRecentFolders(folders: List<File>) {
        val paths = folders.joinToString("|") { it.absolutePath }
        prefs.put(PREFS_KEY, paths)
    }
}

