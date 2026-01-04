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


class DockingIntegrationTest {

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
            testFrame = JFrame("Test Docking")
            testFrame.setSize(800, 600)


            Docking.initialize(testFrame)


            val rootPanel = ModernDocking.app.RootDockingPanel(testFrame)
            testFrame.contentPane.add(rootPanel)


            dockableFileTree = DockableFileTreePanel()
            dockableEditor = DockableEditorPanel()
            dockableDebug = DockableDebugPanel()
            dockableVariables = DockableVariablesPanel()
            dockableRender = DockableRenderPanel()

            latch.countDown()
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Failed to setup test frame")
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
    fun `test all dockable panels can be created`() {
        assertNotNull(dockableFileTree, "FileTree panel should be created")
        assertNotNull(dockableEditor, "Editor panel should be created")
        assertNotNull(dockableDebug, "Debug panel should be created")
        assertNotNull(dockableVariables, "Variables panel should be created")
        assertNotNull(dockableRender, "Render panel should be created")
    }

    @Test
    fun `test dockable panels have correct persistent IDs`() {
        assertEquals("FileTree", dockableFileTree.persistentID)
        assertEquals("Editor", dockableEditor.persistentID)
        assertEquals("Debug", dockableDebug.persistentID)
        assertEquals("Variables", dockableVariables.persistentID)
        assertEquals("Render", dockableRender.persistentID)
    }

    @Test
    fun `test dockable panels have correct tab text`() {
        assertEquals("Files", dockableFileTree.tabText)
        assertEquals("Editor", dockableEditor.tabText)
        assertEquals("Debug", dockableDebug.tabText)
        assertEquals("Variables", dockableVariables.tabText)
        assertEquals("Render", dockableRender.tabText)
    }

    @Test
    fun `test dockable panels allow floating`() {
        assertTrue(dockableFileTree.isFloatingAllowed)
        assertTrue(dockableEditor.isFloatingAllowed)
        assertTrue(dockableDebug.isFloatingAllowed)
        assertTrue(dockableVariables.isFloatingAllowed)
        assertTrue(dockableRender.isFloatingAllowed)
    }

    @Test
    fun `test dockable panels are not closable`() {
        assertFalse(dockableFileTree.isClosable)
        assertFalse(dockableEditor.isClosable)
        assertFalse(dockableDebug.isClosable)
        assertFalse(dockableVariables.isClosable)
        assertFalse(dockableRender.isClosable)
    }

    @Test
    fun `test dockable panels are not limited to root`() {
        assertFalse(dockableFileTree.isLimitedToRoot)
        assertFalse(dockableEditor.isLimitedToRoot)
        assertFalse(dockableDebug.isLimitedToRoot)
        assertFalse(dockableVariables.isLimitedToRoot)
        assertFalse(dockableRender.isLimitedToRoot)
    }

    @Test
    fun `test dockable panels contain their wrapped panels`() {
        assertNotNull(dockableFileTree.panel, "FileTree should contain wrapped panel")
        assertNotNull(dockableEditor.panel, "Editor should contain wrapped panel")
        assertNotNull(dockableDebug.panel, "Debug should contain wrapped panel")
        assertNotNull(dockableVariables.panel, "Variables should contain wrapped panel")
        assertNotNull(dockableRender.panel, "Render should contain wrapped panel")
    }

    @Test
    fun `test dockable panels are JPanel instances`() {
        assertTrue(dockableFileTree is javax.swing.JPanel)
        assertTrue(dockableEditor is javax.swing.JPanel)
        assertTrue(dockableDebug is javax.swing.JPanel)
        assertTrue(dockableVariables is javax.swing.JPanel)
        assertTrue(dockableRender is javax.swing.JPanel)
    }
}

