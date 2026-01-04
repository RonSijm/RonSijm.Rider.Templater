package ronsijm.templater.standalone.ui.docking

import ModernDocking.app.Docking
import ModernDocking.DockingRegion
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import javax.swing.JFrame
import javax.swing.SwingUtilities
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class DockingLayoutTest {

    private lateinit var testFrame: JFrame
    private lateinit var dockableFileTree: DockableFileTreePanel
    private lateinit var dockableEditor: DockableEditorPanel
    private lateinit var dockableDebug: DockableDebugPanel
    private lateinit var dockableVariables: DockableVariablesPanel
    private lateinit var dockableRender: DockableRenderPanel

    @BeforeEach
    fun setup() {
        val latch = CountDownLatch(1)
        SwingUtilities.invokeLater {
            testFrame = JFrame("Test Docking Layout ${System.currentTimeMillis()}")
            testFrame.setSize(800, 600)


            Docking.initialize(testFrame)


            val rootPanel = ModernDocking.app.RootDockingPanel(testFrame)
            testFrame.contentPane.add(rootPanel)

            latch.countDown()
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Failed to setup test frame")


        val timestamp = System.currentTimeMillis()
        dockableFileTree = DockableFileTreePanel()
        dockableEditor = DockableEditorPanel()
        dockableDebug = DockableDebugPanel()
        dockableVariables = DockableVariablesPanel()
        dockableRender = DockableRenderPanel()
    }

    @AfterEach
    fun tearDown() {
        val latch = CountDownLatch(1)
        SwingUtilities.invokeLater {
            testFrame.dispose()
            latch.countDown()
        }
        latch.await(2, TimeUnit.SECONDS)
    }

    @Test
    fun `test dockable panels have components in hierarchy`() {
        val latch = CountDownLatch(1)
        var fileTreeHasComponents = false
        var editorHasComponents = false
        var debugHasComponents = false
        var variablesHasComponents = false
        var renderHasComponents = false

        SwingUtilities.invokeLater {
            fileTreeHasComponents = dockableFileTree.componentCount > 0
            editorHasComponents = dockableEditor.componentCount > 0
            debugHasComponents = dockableDebug.componentCount > 0
            variablesHasComponents = dockableVariables.componentCount > 0
            renderHasComponents = dockableRender.componentCount > 0
            latch.countDown()
        }

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Component check timed out")
        assertTrue(fileTreeHasComponents, "FileTree should have components")
        assertTrue(editorHasComponents, "Editor should have components")
        assertTrue(debugHasComponents, "Debug should have components")
        assertTrue(variablesHasComponents, "Variables should have components")
        assertTrue(renderHasComponents, "Render should have components")
    }

    @Test
    fun `test dockable panels use BorderLayout`() {
        val latch = CountDownLatch(1)
        var allUseBorderLayout = false

        SwingUtilities.invokeLater {
            allUseBorderLayout =
                dockableFileTree.layout is java.awt.BorderLayout &&
                dockableEditor.layout is java.awt.BorderLayout &&
                dockableDebug.layout is java.awt.BorderLayout &&
                dockableVariables.layout is java.awt.BorderLayout &&
                dockableRender.layout is java.awt.BorderLayout
            latch.countDown()
        }

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Layout check timed out")
        assertTrue(allUseBorderLayout, "All dockable panels should use BorderLayout")
    }
}

