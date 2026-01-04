package ronsijm.templater.standalone.ui

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rtextarea.RTextScrollPane
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.awt.Dimension
import java.awt.event.MouseWheelEvent
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.swing.JFrame
import javax.swing.SwingUtilities


class EditorPanelScrollTest {

    private lateinit var editorPanel: EditorPanel
    private lateinit var frame: JFrame


    private val testContent = (1..200).joinToString("\n") { "Line $it: Some content here to make the line longer" }

    @BeforeEach
    fun setup() {
        val latch = CountDownLatch(1)
        SwingUtilities.invokeLater {
            frame = JFrame("Scroll Test")
            editorPanel = EditorPanel()
            frame.contentPane.add(editorPanel)
            frame.size = Dimension(800, 400)
            frame.isVisible = true
            latch.countDown()
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Failed to create EditorPanel")
    }

    @Test
    fun `test mouse wheel scrolling changes scroll position`() {
        val latch = CountDownLatch(1)
        var initialScrollPos = -1
        var finalScrollPos = -1
        var debugInfo = ""

        SwingUtilities.invokeLater {

            val tempFile = File.createTempFile("scroll_test", ".md")
            tempFile.writeText(testContent)
            editorPanel.loadFile(tempFile)


            frame.validate()
            frame.repaint()


            Thread.sleep(200)


            val scrollPaneField = EditorPanel::class.java.getDeclaredField("scrollPane")
            scrollPaneField.isAccessible = true
            val scrollPane = scrollPaneField.get(editorPanel) as RTextScrollPane

            val textAreaField = EditorPanel::class.java.getDeclaredField("textArea")
            textAreaField.isAccessible = true
            val textArea = textAreaField.get(editorPanel) as RSyntaxTextArea


            initialScrollPos = scrollPane.verticalScrollBar.value

            val viewport = scrollPane.viewport
            debugInfo = """
                Initial scroll: $initialScrollPos
                Viewport size: ${viewport.size}
                View size: ${viewport.viewSize}
                Text area height: ${textArea.height}
                Text area preferred: ${textArea.preferredSize}
                Scrollbar max: ${scrollPane.verticalScrollBar.maximum}
                Scrollbar visible: ${scrollPane.verticalScrollBar.isVisible}
            """.trimIndent()


            val wheelEvent = MouseWheelEvent(
                textArea,
                MouseWheelEvent.MOUSE_WHEEL,
                System.currentTimeMillis(),
                0,
                100, 100,
                0, false,
                MouseWheelEvent.WHEEL_UNIT_SCROLL,
                3,
                1
            )


            textArea.dispatchEvent(wheelEvent)


            Thread.sleep(100)


            finalScrollPos = scrollPane.verticalScrollBar.value

            debugInfo += "\nFinal scroll: $finalScrollPos"

            tempFile.delete()
            frame.dispose()
            latch.countDown()
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS), "Test timed out")

        println(debugInfo)


        assertNotEquals(
            initialScrollPos, finalScrollPos,
            "Scroll position should change after mouse wheel event. Debug:\n$debugInfo"
        )
        assertTrue(
            finalScrollPos > initialScrollPos,
            "Scroll position should increase when scrolling down. Initial: $initialScrollPos, Final: $finalScrollPos"
        )
    }
}
