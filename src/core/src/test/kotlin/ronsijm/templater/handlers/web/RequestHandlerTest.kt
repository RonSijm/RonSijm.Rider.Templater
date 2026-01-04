package ronsijm.templater.handlers.web

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.common.ErrorResult
import ronsijm.templater.services.DefaultHttpService
import ronsijm.templater.services.ServiceContainer

class RequestHandlerTest : WebHandlerTestBase() {

    private fun createCommand() = getCommand("request")

    @Test
    fun `test without URL returns error`() {
        val httpService = DefaultHttpService()
        val services = ServiceContainer(httpService = httpService)
        val command = createCommand()
        val context = createContext(services)

        val result = command.execute(emptyList(), context)

        assertTrue(result is ErrorResult, "Expected ErrorResult but got ${result::class.simpleName}")
        assertTrue(result.toString().contains("URL required"))
    }

    @Test
    fun `test with null URL returns error`() {
        val httpService = DefaultHttpService()
        val services = ServiceContainer(httpService = httpService)
        val command = createCommand()
        val context = createContext(services)

        val result = command.execute(listOf(null), context)

        assertTrue(result is ErrorResult, "Expected ErrorResult but got ${result::class.simpleName}")
        assertTrue(result.toString().contains("URL required"))
    }
}
