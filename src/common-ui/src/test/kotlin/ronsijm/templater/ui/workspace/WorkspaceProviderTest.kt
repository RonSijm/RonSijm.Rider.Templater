package ronsijm.templater.ui.workspace

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class SimpleFileHandleTest {

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `name returns file name`() {
        val file = File(tempDir, "test.md")
        file.createNewFile()
        val handle = SimpleFileHandle(file, tempDir)

        assertEquals("test.md", handle.name)
    }

    @Test
    fun `basename returns name without extension`() {
        val file = File(tempDir, "test.md")
        file.createNewFile()
        val handle = SimpleFileHandle(file, tempDir)

        assertEquals("test", handle.basename)
    }

    @Test
    fun `extension returns file extension`() {
        val file = File(tempDir, "test.md")
        file.createNewFile()
        val handle = SimpleFileHandle(file, tempDir)

        assertEquals("md", handle.extension)
    }

    @Test
    fun `path returns relative path`() {
        val subDir = File(tempDir, "subdir")
        subDir.mkdir()
        val file = File(subDir, "test.md")
        file.createNewFile()
        val handle = SimpleFileHandle(file, tempDir)

        assertEquals("subdir/test.md", handle.path)
    }

    @Test
    fun `absolutePath returns full path`() {
        val file = File(tempDir, "test.md")
        file.createNewFile()
        val handle = SimpleFileHandle(file, tempDir)

        assertEquals(file.absolutePath, handle.absolutePath)
    }

    @Test
    fun `parentPath returns parent folder relative path`() {
        val subDir = File(tempDir, "subdir")
        subDir.mkdir()
        val file = File(subDir, "test.md")
        file.createNewFile()
        val handle = SimpleFileHandle(file, tempDir)

        assertEquals("subdir", handle.parentPath)
    }

    @Test
    fun `parentPath returns null for root file`() {
        val file = File(tempDir, "test.md")
        file.createNewFile()
        val handle = SimpleFileHandle(file, tempDir)


        assertTrue(handle.parentPath.isNullOrEmpty())
    }

    @Test
    fun `isDirectory returns true for directory`() {
        val dir = File(tempDir, "subdir")
        dir.mkdir()
        val handle = SimpleFileHandle(dir, tempDir)

        assertTrue(handle.isDirectory)
    }

    @Test
    fun `isDirectory returns false for file`() {
        val file = File(tempDir, "test.md")
        file.createNewFile()
        val handle = SimpleFileHandle(file, tempDir)

        assertFalse(handle.isDirectory)
    }

    @Test
    fun `exists returns true for existing file`() {
        val file = File(tempDir, "test.md")
        file.createNewFile()
        val handle = SimpleFileHandle(file, tempDir)

        assertTrue(handle.exists())
    }

    @Test
    fun `exists returns false for non-existing file`() {
        val file = File(tempDir, "nonexistent.md")
        val handle = SimpleFileHandle(file, tempDir)

        assertFalse(handle.exists())
    }

    @Test
    fun `toFile returns underlying file`() {
        val file = File(tempDir, "test.md")
        file.createNewFile()
        val handle = SimpleFileHandle(file, tempDir)

        assertEquals(file, handle.toFile())
    }

    @Test
    fun `equals returns true for same file`() {
        val file = File(tempDir, "test.md")
        file.createNewFile()
        val handle1 = SimpleFileHandle(file, tempDir)
        val handle2 = SimpleFileHandle(file, tempDir)

        assertEquals(handle1, handle2)
    }

    @Test
    fun `equals returns false for different files`() {
        val file1 = File(tempDir, "test1.md")
        val file2 = File(tempDir, "test2.md")
        file1.createNewFile()
        file2.createNewFile()
        val handle1 = SimpleFileHandle(file1, tempDir)
        val handle2 = SimpleFileHandle(file2, tempDir)

        assertNotEquals(handle1, handle2)
    }

    @Test
    fun `hashCode is consistent with equals`() {
        val file = File(tempDir, "test.md")
        file.createNewFile()
        val handle1 = SimpleFileHandle(file, tempDir)
        val handle2 = SimpleFileHandle(file, tempDir)

        assertEquals(handle1.hashCode(), handle2.hashCode())
    }

    @Test
    fun `toString returns readable representation`() {
        val file = File(tempDir, "test.md")
        file.createNewFile()
        val handle = SimpleFileHandle(file, tempDir)

        assertEquals("FileHandle(test.md)", handle.toString())
    }
}

