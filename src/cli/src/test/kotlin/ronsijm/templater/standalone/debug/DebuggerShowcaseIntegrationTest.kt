package ronsijm.templater.standalone.debug

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import ronsijm.templater.debug.DebugAction
import ronsijm.templater.debug.DebugBreakpoint


class DebuggerShowcaseIntegrationTest {

    @Test
    fun `test debugger showcase with breakpoints at lines 23 and 24`() {


        val possiblePaths = listOf(
            "docs/Examples/Debugging/Debugger Showcase.md",
            "../../../docs/Examples/Debugging/Debugger Showcase.md",
            "../../docs/Examples/Debugging/Debugger Showcase.md"
        )

        val showcaseFile = possiblePaths
            .map { File(it) }
            .firstOrNull { it.exists() }

        if (showcaseFile == null) {
            println("Skipping test - Debugger Showcase file not found. Tried:")
            possiblePaths.forEach { println("  - ${File(it).absolutePath}") }
            return
        }

        val content = showcaseFile.readText()


        val breakpoints = setOf(23, 24)

        println("\n=== INTEGRATION TEST: Debugger Showcase ===")
        println("File: ${showcaseFile.absolutePath}")
        println("Breakpoints: $breakpoints")
        println("===========================================\n")

        val session = StandaloneDebugSession(showcaseFile, breakpoints)

        val pausedEvents = mutableListOf<PauseEvent>()
        val latch = CountDownLatch(1)

        session.addStateListener(object : StandaloneSessionStateListener {
            override fun onPaused(breakpoint: DebugBreakpoint) {
                val event = PauseEvent(
                    lineNumber = breakpoint.step.displayLineNumber,
                    description = breakpoint.step.description
                )
                pausedEvents.add(event)

                println("TEST: Paused at line ${event.lineNumber}: ${event.description}")


                println("TEST: User clicks 'Continue' button")
                session.continueExecution()
            }

            override fun onResumed(action: DebugAction) {
                println("TEST: Resumed with action: $action")
            }

            override fun onStopped() {
                println("TEST: Session stopped")
                println("TEST: Total pauses: ${pausedEvents.size}")
                latch.countDown()
            }
        })


        Thread {
            session.execute(content)
        }.start()


        assertTrue(latch.await(30, TimeUnit.SECONDS), "Session should complete within 30 seconds")

        println("\n=== TEST RESULTS ===")
        println("Paused ${pausedEvents.size} times:")
        pausedEvents.forEachIndexed { index, event ->
            println("  ${index + 1}. Line ${event.lineNumber}: ${event.description}")
        }
        println("====================\n")


        assertEquals(2, pausedEvents.size,
            "Should pause exactly 2 times (once at line 23, once at line 24), but paused ${pausedEvents.size} times at lines: ${pausedEvents.map { it.lineNumber }}")


        assertEquals(23, pausedEvents[0].lineNumber,
            "First pause should be at line 23, but was at line ${pausedEvents[0].lineNumber}")


        assertEquals(24, pausedEvents[1].lineNumber,
            "Second pause should be at line 24, but was at line ${pausedEvents[1].lineNumber}")


        assertTrue(pausedEvents[0].description.contains("counter + 5") ||
                   pausedEvents[0].description.contains("counter = counter + 5"),
            "First pause should be at 'counter = counter + 5', but was: ${pausedEvents[0].description}")

        assertTrue(pausedEvents[1].description.contains("counter * 2") ||
                   pausedEvents[1].description.contains("counter = counter * 2"),
            "Second pause should be at 'counter = counter * 2', but was: ${pausedEvents[1].description}")

        println("? TEST PASSED: Both breakpoints were hit in the correct order!")
    }

    data class PauseEvent(
        val lineNumber: Int?,
        val description: String
    )
}

