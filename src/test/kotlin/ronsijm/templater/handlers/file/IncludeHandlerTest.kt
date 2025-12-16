package ronsijm.templater.handlers.file

import org.junit.jupiter.api.Test
import ronsijm.templater.ResultAssertions.assertResultEquals
import ronsijm.templater.services.MockFileOperationsService
import ronsijm.templater.services.ServiceContainer

class IncludeHandlerTest : FileHandlerTestBase() {

    private fun createCommand() = getCommand("include")

    @Test
    fun `test calls service`() {
        val command = createCommand()
        val mockFileService = MockFileOperationsService()
        mockFileService.addFile("[[other_file]]", "Included content from [[other_file]]")
        val services = ServiceContainer(fileOperationService = mockFileService)
        val context = createContext(services = services)

        val result = command.execute(listOf("[[other_file]]"), context)

        assertResultEquals("Included content from [[other_file]]", result)
    }
}

