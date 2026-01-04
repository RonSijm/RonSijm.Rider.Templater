package ronsijm.templater.standalone.ui

import com.formdev.flatlaf.FlatDarculaLaf
import java.awt.BorderLayout
import java.awt.Dimension
import java.io.File
import javax.swing.*


object BreakpointVisualTest {

    @JvmStatic
    fun main(args: Array<String>) {

        FlatDarculaLaf.setup()

        SwingUtilities.invokeLater {
            val frame = JFrame("Breakpoint Visual Test")
            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            frame.setSize(800, 600)

            val editorPanel = EditorPanel()


            val testContent = """
                # Breakpoint Test

                This is line 3
                This is line 4
                This is line 5
                This is line 6
                This is line 7
                This is line 8
                This is line 9
                This is line 10

                ## Instructions

                1. Click in the GUTTER (gray area to the left of line numbers)
                2. You should see a RED CIRCLE appear
                3. Click again to remove the breakpoint
                4. Try clicking on different lines

                Current breakpoints will be shown below.
            """.trimIndent()

            val tempFile = File.createTempFile("breakpoint-test", ".md")
            tempFile.writeText(testContent)
            tempFile.deleteOnExit()

            editorPanel.loadFile(tempFile)


            val breakpointLabel = JLabel("Breakpoints: (none)")
            editorPanel.addBreakpointListener { breakpoints ->
                if (breakpoints.isEmpty()) {
                    breakpointLabel.text = "Breakpoints: (none)"
                } else {
                    breakpointLabel.text = "Breakpoints: ${breakpoints.sorted().joinToString(", ")}"
                }
                println("Breakpoints updated: $breakpoints")
            }


            val toolbar = JPanel()
            val toggleButton = JButton("Toggle Breakpoint at Current Line")
            toggleButton.addActionListener {
                editorPanel.toggleBreakpoint()
            }
            toolbar.add(toggleButton)
            toolbar.add(breakpointLabel)


            frame.layout = BorderLayout()
            frame.add(toolbar, BorderLayout.NORTH)
            frame.add(editorPanel, BorderLayout.CENTER)


            val infoPanel = JPanel()
            infoPanel.layout = BoxLayout(infoPanel, BoxLayout.Y_AXIS)
            infoPanel.border = BorderFactory.createTitledBorder("Test Instructions")
            infoPanel.add(JLabel("1. Click in the GUTTER (gray area left of line numbers)"))
            infoPanel.add(JLabel("2. Look for RED CIRCLES to appear"))
            infoPanel.add(JLabel("3. Click 'Toggle Breakpoint' button to test programmatically"))
            infoPanel.add(JLabel("4. Check console for debug output"))
            infoPanel.preferredSize = Dimension(800, 100)
            frame.add(infoPanel, BorderLayout.SOUTH)

            frame.setLocationRelativeTo(null)
            frame.isVisible = true

            println("=".repeat(60))
            println("BREAKPOINT VISUAL TEST")
            println("=".repeat(60))
            println("Click in the gutter to add/remove breakpoints")
            println("Watch the console for debug output")
            println("Watch the editor for red circle icons")
            println("=".repeat(60))
        }
    }
}

