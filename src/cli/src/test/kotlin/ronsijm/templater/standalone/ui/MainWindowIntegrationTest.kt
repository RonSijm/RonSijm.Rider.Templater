package ronsijm.templater.standalone.ui

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import javax.swing.SwingUtilities
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MainWindowIntegrationTest {

    private lateinit var mainWindow: MainWindow
    private var setupSuccessful = false

    @BeforeAll
    fun setup() {
        val latch = CountDownLatch(1)
        SwingUtilities.invokeLater {
            try {
                mainWindow = MainWindow()
                setupSuccessful = true
                latch.countDown()
            } catch (e: Exception) {
                println("Failed to create MainWindow: ${e.message}")
                e.printStackTrace()
                latch.countDown()
            }
        }
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Failed to create MainWindow")
    }

    @AfterAll
    fun tearDown() {
        if (setupSuccessful) {
            val latch = CountDownLatch(1)
            SwingUtilities.invokeLater {
                mainWindow.dispose()
                latch.countDown()
            }
            latch.await(2, TimeUnit.SECONDS)
        }
    }

    @Test
    fun `test main window is created successfully`() {
        assertTrue(setupSuccessful, "MainWindow setup should succeed")
        assertNotNull(mainWindow, "MainWindow should be created")
    }

    @Test
    fun `test main window is visible`() {
        val latch = CountDownLatch(1)
        var isVisible = false

        SwingUtilities.invokeLater {
            isVisible = mainWindow.isVisible
            latch.countDown()
        }

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Check visibility timed out")
        assertTrue(isVisible, "MainWindow should be visible")
    }

    @Test
    fun `test main window has correct title`() {
        val latch = CountDownLatch(1)
        var title = ""

        SwingUtilities.invokeLater {
            title = mainWindow.title
            latch.countDown()
        }

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Get title timed out")
        assertEquals("Templater", title, "MainWindow should have correct title")
    }

    @Test
    fun `test main window has correct size`() {
        val latch = CountDownLatch(1)
        var width = 0
        var height = 0

        SwingUtilities.invokeLater {
            width = mainWindow.width
            height = mainWindow.height
            latch.countDown()
        }

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Get size timed out")

        assertTrue(width >= 800, "MainWindow should have width of at least 800, was $width")
        assertTrue(height >= 600, "MainWindow should have height of at least 600, was $height")
    }

    @Test
    fun `test main window has menu bar`() {
        val latch = CountDownLatch(1)
        var hasMenuBar = false

        SwingUtilities.invokeLater {
            hasMenuBar = mainWindow.jMenuBar != null
            latch.countDown()
        }

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Check menu bar timed out")
        assertTrue(hasMenuBar, "MainWindow should have a menu bar")
    }

    @Test
    fun `test main window has content pane`() {
        val latch = CountDownLatch(1)
        var hasContentPane = false
        var componentCount = 0

        SwingUtilities.invokeLater {
            hasContentPane = mainWindow.contentPane != null
            componentCount = mainWindow.contentPane.componentCount
            latch.countDown()
        }

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Check content pane timed out")
        assertTrue(hasContentPane, "MainWindow should have a content pane")
        assertTrue(componentCount > 0, "MainWindow content pane should have components")
    }

    @Test
    fun `test main window can be disposed`() {
        val latch = CountDownLatch(1)
        var disposeSuccessful = false

        SwingUtilities.invokeLater {
            try {
                mainWindow.dispose()
                disposeSuccessful = true
            } catch (e: Exception) {
                println("Dispose failed: ${e.message}")
                e.printStackTrace()
            }
            latch.countDown()
        }

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Dispose timed out")
        assertTrue(disposeSuccessful, "MainWindow should dispose successfully")
    }
}

