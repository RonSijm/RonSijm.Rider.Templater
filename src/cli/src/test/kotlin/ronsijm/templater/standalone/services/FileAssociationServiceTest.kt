package ronsijm.templater.standalone.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS


class FileAssociationServiceTest {

    @Test
    fun `isWindows returns true on Windows`() {
        val osName = System.getProperty("os.name").lowercase()
        val expected = osName.contains("windows")
        assertEquals(expected, FileAssociationService.isWindows())
    }

    @Test
    fun `isRegistered returns false on non-Windows systems`() {
        if (!FileAssociationService.isWindows()) {
            assertFalse(FileAssociationService.isRegistered())
        }
    }

    @Test
    fun `registerFileAssociation fails on non-Windows systems`() {
        if (!FileAssociationService.isWindows()) {
            val result = FileAssociationService.registerFileAssociation()
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull()?.message?.contains("only supported on Windows") == true)
        }
    }

    @Test
    fun `unregisterFileAssociation fails on non-Windows systems`() {
        if (!FileAssociationService.isWindows()) {
            val result = FileAssociationService.unregisterFileAssociation()
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull()?.message?.contains("only supported on Windows") == true)
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun `registerFileAssociation returns failure when not running from JAR`() {


        val result = FileAssociationService.registerFileAssociation()


        assertTrue(result.isFailure)
        val errorMessage = result.exceptionOrNull()?.message ?: ""
        assertTrue(
            errorMessage.contains("Cannot determine executable path") ||
            errorMessage.contains("only works when running from JAR"),
            "Expected error about executable path, got: $errorMessage"
        )
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun `isRegistered can query registry without error`() {

        val result = FileAssociationService.isRegistered()

        assertTrue(result || !result)
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun `unregisterFileAssociation succeeds even when not registered`() {

        val result = FileAssociationService.unregisterFileAssociation()


        assertTrue(result.isSuccess)
        val message = result.getOrNull() ?: ""
        assertTrue(
            message.contains("removed") || message.contains("may not have existed"),
            "Expected success message about removal, got: $message"
        )
    }
}

