package ronsijm.templater.handlers.file

import org.junit.jupiter.api.Test
import ronsijm.templater.ResultAssertions.assertResultEquals
import ronsijm.templater.services.mock.MockFileOperationsService
import ronsijm.templater.services.ServiceContainer

class ExistsHandlerTest : FileHandlerTestBase() {

    private fun createCommand() = getCommand("exists")

    @Test
    fun `test returns true when file exists`() {
        val command = createCommand()
        val mockFileService = MockFileOperationsService()
        mockFileService.addFile("/existing/file.md")
        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(listOf("/existing/file.md"), context)

        assertResultEquals("true", result)
    }

    @Test
    fun `test returns false when file does not exist`() {
        val command = createCommand()
        val mockFileService = MockFileOperationsService()
        mockFileService.addFile("/existing/file.md")
        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(listOf("/nonexistent/file.md"), context)

        assertResultEquals("false", result)
    }
}
