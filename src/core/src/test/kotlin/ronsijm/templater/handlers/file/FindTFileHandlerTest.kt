package ronsijm.templater.handlers.file

import org.junit.jupiter.api.Test
import ronsijm.templater.ResultAssertions.assertResultEquals
import ronsijm.templater.services.mock.MockFileOperationsService
import ronsijm.templater.services.ServiceContainer

class FindTFileHandlerTest : FileHandlerTestBase() {

    private fun createCommand() = getCommand("find_tfile")

    @Test
    fun `test returns empty string`() {
        val command = createCommand()
        val mockFileService = MockFileOperationsService()
        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(listOf("filename.md"), context)


        assertResultEquals("", result)
    }
}
