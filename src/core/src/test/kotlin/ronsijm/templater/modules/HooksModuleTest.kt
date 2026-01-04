package ronsijm.templater.modules

import ronsijm.templater.TestContextFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class HooksModuleTest {

    @Test
    fun `test on_all_templates_executed registers callback`() {
        val context = TestContextFactory.create()
        val hooksModule = HooksModule(context)

        var callbackExecuted = false
        val callback: () -> Unit = { callbackExecuted = true }

        val result = hooksModule.executeCommand("on_all_templates_executed", listOf(callback))

        assertEquals("", result)
        assertTrue(hooksModule.hasCallbacks())
    }

    @Test
    fun `test executeAllCallbacks runs registered callbacks`() {
        val context = TestContextFactory.create()
        val hooksModule = HooksModule(context)

        var callback1Executed = false
        var callback2Executed = false

        val callback1: () -> Unit = { callback1Executed = true }
        val callback2: () -> Unit = { callback2Executed = true }

        hooksModule.executeCommand("on_all_templates_executed", listOf(callback1))
        hooksModule.executeCommand("on_all_templates_executed", listOf(callback2))

        hooksModule.executeAllCallbacks()

        assertTrue(callback1Executed)
        assertTrue(callback2Executed)
        assertFalse(hooksModule.hasCallbacks())
    }

    @Test
    fun `test executeAllCallbacks handles exceptions gracefully`() {
        val context = TestContextFactory.create()
        val hooksModule = HooksModule(context)

        var callback2Executed = false

        val callback1: () -> Unit = { throw RuntimeException("Test exception") }
        val callback2: () -> Unit = { callback2Executed = true }

        hooksModule.executeCommand("on_all_templates_executed", listOf(callback1))
        hooksModule.executeCommand("on_all_templates_executed", listOf(callback2))


        assertDoesNotThrow {
            hooksModule.executeAllCallbacks()
        }


        assertTrue(callback2Executed)
    }

    @Test
    fun `test on_all_templates_executed with no callback does nothing`() {
        val context = TestContextFactory.create()
        val hooksModule = HooksModule(context)

        val result = hooksModule.executeCommand("on_all_templates_executed", emptyList())

        assertEquals("", result)
        assertFalse(hooksModule.hasCallbacks())
    }

    @Test
    fun `test on_all_templates_executed with invalid callback does nothing`() {
        val context = TestContextFactory.create()
        val hooksModule = HooksModule(context)

        val result = hooksModule.executeCommand("on_all_templates_executed", listOf("not a callback"))

        assertEquals("", result)
        assertFalse(hooksModule.hasCallbacks())
    }

    @Test
    fun `test unknown command returns empty string`() {
        val context = TestContextFactory.create()
        val hooksModule = HooksModule(context)

        val result = hooksModule.executeCommand("unknown", emptyList())

        assertEquals("", result)
    }

    @Test
    fun `test hasCallbacks returns false when no callbacks registered`() {
        val context = TestContextFactory.create()
        val hooksModule = HooksModule(context)

        assertFalse(hooksModule.hasCallbacks())
    }
}
