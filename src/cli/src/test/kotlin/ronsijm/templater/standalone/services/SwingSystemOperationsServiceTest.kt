package ronsijm.templater.standalone.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import ronsijm.templater.services.SystemOperationsService
import java.awt.GraphicsEnvironment


class SwingSystemOperationsServiceTest {

    companion object {
        private var isHeadless: Boolean = true

        @JvmStatic
        @BeforeAll
        fun checkHeadless() {
            isHeadless = GraphicsEnvironment.isHeadless()
            if (isHeadless) {
                println("Running in headless mode - dialog tests will be skipped")
            }
        }
    }

    @Test
    fun `service implements SystemOperationsService interface`() {
        val service = SwingSystemOperationsService()
        assertTrue(service is SystemOperationsService)
    }

    @Test
    fun `service can be created with null parent component`() {
        val service = SwingSystemOperationsService(null)
        assertNotNull(service)
    }

    @Test
    fun `service can be created with default constructor`() {
        val service = SwingSystemOperationsService()
        assertNotNull(service)
    }

    @Test
    fun `prompt method exists with correct signature`() {
        val service = SwingSystemOperationsService()


        val method = service::class.java.getMethod(
            "prompt",
            String::class.java,
            String::class.java,
            Boolean::class.java,
            Boolean::class.java
        )
        assertNotNull(method)
    }

    @Test
    fun `suggester method exists with correct signature`() {
        val service = SwingSystemOperationsService()


        val method = service::class.java.getMethod(
            "suggester",
            List::class.java,
            List::class.java,
            Boolean::class.java,
            String::class.java,
            Integer::class.java
        )
        assertNotNull(method)
    }

    @Test
    fun `multiSuggester method exists with correct signature`() {
        val service = SwingSystemOperationsService()


        val method = service::class.java.getMethod(
            "multiSuggester",
            List::class.java,
            List::class.java,
            Boolean::class.java,
            String::class.java,
            Integer::class.java
        )
        assertNotNull(method)
    }
}

