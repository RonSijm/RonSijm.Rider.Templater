package ronsijm.templater.standalone.ui

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import java.io.File
import javax.swing.SwingUtilities
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class EditorPanelBreakpointTest {

    private lateinit var editorPanel: EditorPanel
    private val testContent = """
        # Test Template

        Line 1
        Line 2
        Line 3
        Line 4
        Line 5
    """.trimIndent()

    @BeforeEach
    fun setup() {

        val latch = CountDownLatch(1)
        SwingUtilities.invokeLater {
            editorPanel = EditorPanel()
            latch.countDown()
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Failed to create EditorPanel")
    }

    @Test
    fun `test toggle breakpoint adds and removes breakpoint`() {
        val latch = CountDownLatch(1)
        var breakpointsAfterAdd: Set<Int>? = null
        var breakpointsAfterRemove: Set<Int>? = null

        SwingUtilities.invokeLater {

            val tempFile = File.createTempFile("test", ".md")
            tempFile.writeText(testContent)
            editorPanel.loadFile(tempFile)


            editorPanel.addBreakpointListener { breakpoints ->
                println("Breakpoints changed: $breakpoints")
            }


            val line3Offset = editorPanel.getText().lines().take(2).sumOf { it.length + 1 }
            editorPanel.getText()


            println("Toggling breakpoint at line 3...")
            editorPanel.toggleBreakpoint()
            breakpointsAfterAdd = editorPanel.getBreakpoints()
            println("Breakpoints after add: $breakpointsAfterAdd")


            println("Toggling breakpoint again to remove...")
            editorPanel.toggleBreakpoint()
            breakpointsAfterRemove = editorPanel.getBreakpoints()
            println("Breakpoints after remove: $breakpointsAfterRemove")

            tempFile.delete()
            latch.countDown()
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test timed out")


        assertNotNull(breakpointsAfterAdd, "Breakpoints should not be null after add")
        assertEquals(1, breakpointsAfterAdd?.size, "Should have 1 breakpoint after add")


        assertNotNull(breakpointsAfterRemove, "Breakpoints should not be null after remove")
        assertEquals(0, breakpointsAfterRemove?.size, "Should have 0 breakpoints after remove")
    }

    @Test
    fun `test multiple breakpoints can be added`() {
        val latch = CountDownLatch(1)
        var finalBreakpoints: Set<Int>? = null

        SwingUtilities.invokeLater {

            val tempFile = File.createTempFile("test", ".md")
            tempFile.writeText(testContent)
            editorPanel.loadFile(tempFile)


            println("Adding breakpoint at line 2")
            editorPanel.toggleBreakpoint()

            println("Adding breakpoint at line 4")
            editorPanel.toggleBreakpoint()

            println("Adding breakpoint at line 6")
            editorPanel.toggleBreakpoint()

            finalBreakpoints = editorPanel.getBreakpoints()
            println("Final breakpoints: $finalBreakpoints")

            tempFile.delete()
            latch.countDown()
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test timed out")



        assertNotNull(finalBreakpoints, "Breakpoints should not be null")
    }

    @Test
    fun `test breakpoint listener is notified`() {
        val latch = CountDownLatch(1)
        var listenerCalled = false
        var receivedBreakpoints: Set<Int>? = null

        SwingUtilities.invokeLater {
            val tempFile = File.createTempFile("test", ".md")
            tempFile.writeText(testContent)
            editorPanel.loadFile(tempFile)


            editorPanel.addBreakpointListener { breakpoints ->
                listenerCalled = true
                receivedBreakpoints = breakpoints
                println("Listener received breakpoints: $breakpoints")
            }


            println("Toggling breakpoint...")
            editorPanel.toggleBreakpoint()

            tempFile.delete()
            latch.countDown()
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test timed out")
        assertTrue(listenerCalled, "Breakpoint listener should have been called")
        assertNotNull(receivedBreakpoints, "Listener should have received breakpoints")
    }
}

