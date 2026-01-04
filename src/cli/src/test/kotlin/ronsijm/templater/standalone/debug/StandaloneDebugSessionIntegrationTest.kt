package ronsijm.templater.standalone.debug

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.io.TempDir
import ronsijm.templater.debug.DebugAction
import ronsijm.templater.debug.DebugBreakpoint
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class StandaloneDebugSessionIntegrationTest {

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `breakpoint at document line should pause execution with frontmatter`() {

        val testFile = File(tempDir, "test.md")
        testFile.writeText("""
            ---
            title: Test
            date: 2024-01-01
            ---
            Line 5: First line after frontmatter
            Line 6: <% date.now() %>
            Line 7: Some text
            Line 8: <% date.now() %>
            Line 9: More text
        """.trimIndent())


        val breakpoints = setOf(6)

        val session = StandaloneDebugSession(testFile, breakpoints)

        val pausedAtLines = mutableListOf<Int?>()
        val latch = CountDownLatch(1)

        session.addStateListener(object : StandaloneSessionStateListener {
            override fun onPaused(breakpoint: DebugBreakpoint) {
                pausedAtLines.add(breakpoint.step.displayLineNumber)
                println("Paused at line ${breakpoint.step.displayLineNumber}: ${breakpoint.step.description}")

                session.continueExecution()
            }

            override fun onResumed(action: DebugAction) {
                println("Resumed with action: $action")
            }

            override fun onStopped() {
                println("Session stopped")
                latch.countDown()
            }
        })


        Thread {
            session.execute(testFile.readText())
        }.start()


        assertTrue(latch.await(10, TimeUnit.SECONDS), "Session should complete within 10 seconds")


        assertTrue(pausedAtLines.contains(6),
            "Should pause at line 6, but paused at: $pausedAtLines")

        println("Test passed! Paused at lines: $pausedAtLines")
    }

    @Test
    fun `multiple breakpoints should all be hit`() {
        val testFile = File(tempDir, "test.md")
        testFile.writeText("""
            ---
            title: Test
            ---
            <% const x = 1 %>
            <% const y = 2 %>
            <% const z = 3 %>
        """.trimIndent())


        val breakpoints = setOf(4, 5, 6)

        val session = StandaloneDebugSession(testFile, breakpoints)

        val pausedAtLines = mutableListOf<Int?>()
        val latch = CountDownLatch(1)

        session.addStateListener(object : StandaloneSessionStateListener {
            override fun onPaused(breakpoint: DebugBreakpoint) {
                pausedAtLines.add(breakpoint.step.displayLineNumber)
                session.continueExecution()
            }

            override fun onResumed(action: DebugAction) {}

            override fun onStopped() {
                latch.countDown()
            }
        })

        Thread {
            session.execute(testFile.readText())
        }.start()

        assertTrue(latch.await(10, TimeUnit.SECONDS))


        assertTrue(pausedAtLines.contains(4), "Should pause at line 4")
        assertTrue(pausedAtLines.contains(5), "Should pause at line 5")
        assertTrue(pausedAtLines.contains(6), "Should pause at line 6")

        println("Test passed! Paused at lines: $pausedAtLines")
    }

    @Test
    fun `step into should pause at each statement`() {
        val testFile = File(tempDir, "test.md")
        testFile.writeText("""
            <% const x = 1 %>
            <% const y = 2 %>
        """.trimIndent())

        val session = StandaloneDebugSession(testFile, emptySet())

        val pauseCount = AtomicInteger(0)
        val latch = CountDownLatch(1)

        session.addStateListener(object : StandaloneSessionStateListener {
            override fun onPaused(breakpoint: DebugBreakpoint) {
                val count = pauseCount.incrementAndGet()
                println("Pause #$count at line ${breakpoint.step.displayLineNumber}: ${breakpoint.step.description}")


                if (count < 5) {
                    session.stepInto()
                } else {
                    session.continueExecution()
                }
            }

            override fun onResumed(action: DebugAction) {}

            override fun onStopped() {
                latch.countDown()
            }
        })

        Thread {
            session.execute(testFile.readText())
        }.start()

        assertTrue(latch.await(10, TimeUnit.SECONDS))


        assertTrue(pauseCount.get() >= 3,
            "Should pause at least 3 times (template start + 2 statements), but paused ${pauseCount.get()} times")

        println("Test passed! Paused ${pauseCount.get()} times")
    }
}

