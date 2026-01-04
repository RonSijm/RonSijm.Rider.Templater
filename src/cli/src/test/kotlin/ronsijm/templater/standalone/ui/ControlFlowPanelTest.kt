package ronsijm.templater.standalone.ui

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.formdev.flatlaf.FlatIntelliJLaf
import ronsijm.templater.settings.MermaidNodeStyles
import java.awt.Dimension

class ControlFlowPanelTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setupLaf() {
            try {
                FlatIntelliJLaf.setup()
            } catch (e: Exception) {

            }
        }
    }

    private lateinit var panel: ControlFlowPanel

    @BeforeEach
    fun setUp() {
        panel = ControlFlowPanel()
        panel.size = Dimension(800, 600)
    }

    @Test
    fun `panel initializes without error`() {
        assertNotNull(panel)
    }

    @Test
    fun `getGraph returns null initially`() {
        assertNull(panel.getGraph())
    }

    @Test
    fun `clear does not throw`() {
        assertDoesNotThrow { panel.clear() }
    }

    @Test
    fun `refresh does not throw with no content`() {
        assertDoesNotThrow { panel.refresh() }
    }

    @Test
    fun `setNodeStyles does not throw`() {
        val styles = MermaidNodeStyles()
        assertDoesNotThrow { panel.setNodeStyles(styles) }
    }

    @Test
    fun `updateContent with empty string does not throw`() {
        assertDoesNotThrow { panel.updateContent("") }
    }

    @Test
    fun `updateContent with simple template does not throw`() {
        val template = "Hello <% \"World\" %>!"
        assertDoesNotThrow { panel.updateContent(template) }
    }

    @Test
    fun `updateContent with execution block does not throw`() {
        val template = """
            <%* let x = 1 %>
            <% x %>
        """.trimIndent()
        assertDoesNotThrow { panel.updateContent(template) }
    }

    @Test
    fun `updateContent with if statement does not throw`() {
        val template = """
            <%*
            if (true) {
                let x = 1
            } else {
                let x = 2
            }
            %>
        """.trimIndent()
        assertDoesNotThrow { panel.updateContent(template) }
    }

    @Test
    fun `updateContent with for loop does not throw`() {
        val template = """
            <%*
            for (let i = 0; i < 10; i++) {
                console.log(i)
            }
            %>
        """.trimIndent()
        assertDoesNotThrow { panel.updateContent(template) }
    }

    @Test
    fun `updateContent with function does not throw`() {
        val template = """
            <%*
            function greet(name) {
                return "Hello " + name
            }
            %>
        """.trimIndent()
        assertDoesNotThrow { panel.updateContent(template) }
    }

    @Test
    fun `clear after updateContent does not throw`() {
        panel.updateContent("Hello <% \"World\" %>!")
        assertDoesNotThrow { panel.clear() }
    }

    @Test
    fun `refresh after updateContent does not throw`() {
        panel.updateContent("Hello <% \"World\" %>!")
        assertDoesNotThrow { panel.refresh() }
    }

    @Test
    fun `multiple updateContent calls do not throw`() {
        assertDoesNotThrow {
            panel.updateContent("First <% 1 %>")
            panel.updateContent("Second <% 2 %>")
            panel.updateContent("Third <% 3 %>")
        }
    }
}

