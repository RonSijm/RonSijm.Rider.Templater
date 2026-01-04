package ronsijm.templater.standalone.ui

import com.formdev.flatlaf.FlatLaf
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.GutterIconInfo
import org.fife.ui.rtextarea.RTextScrollPane
import ronsijm.templater.standalone.ui.gutter.GutterRunIconManager
import ronsijm.templater.standalone.ui.util.ScrollPaneWrapper
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import java.awt.KeyboardFocusManager
import java.awt.image.BufferedImage
import java.io.File
import javax.swing.*

class EditorPanel : JPanel(BorderLayout()) {

    private val textArea: RSyntaxTextArea
    private val scrollPane: RTextScrollPane
    private var currentFile: File? = null
    private var lastSavedContent: String = ""

    private val breakpoints = mutableSetOf<Int>()
    private val breakpointIcons = mutableMapOf<Int, GutterIconInfo>()
    private var currentDebugLine: Int? = null
    private val breakpointListeners = mutableListOf<(Set<Int>) -> Unit>()
    private val contentChangeListeners = mutableListOf<(String) -> Unit>()
    private val runLineListeners = mutableListOf<(Int) -> Unit>()
    private val breakpointToggleListeners = mutableListOf<(Int, Boolean) -> Unit>()
    private lateinit var runIconManager: GutterRunIconManager
    private var currentLineHighlightNormal: Color = Color(50, 50, 50)
    private var currentLineHighlightDebug: Color = Color(70, 100, 70)

    init {
        textArea = RSyntaxTextArea()
        setupTextArea()

        scrollPane = RTextScrollPane(textArea)
        setupScrollPane()
        setupGutterListener()
        setupFocusListener()
        setupRunIconManager()

        add(ScrollPaneWrapper.wrap(scrollPane), BorderLayout.CENTER)
        add(createToolbar(), BorderLayout.NORTH)
    }

    private fun setupRunIconManager() {
        runIconManager = GutterRunIconManager(scrollPane) { line ->
            notifyRunLine(line)
        }
    }

    private fun setupFocusListener() {
        textArea.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseClicked(e: java.awt.event.MouseEvent) {
                textArea.requestFocusInWindow()
            }
        })

        scrollPane.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseClicked(e: java.awt.event.MouseEvent) {
                textArea.requestFocusInWindow()
            }
        })


        scrollPane.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseEntered(e: java.awt.event.MouseEvent) {

                val focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().focusOwner
                if (focusOwner !is javax.swing.text.JTextComponent || focusOwner == textArea) {
                    textArea.requestFocusInWindow()
                }
            }
        })


        textArea.document.addDocumentListener(object : javax.swing.event.DocumentListener {
            override fun insertUpdate(e: javax.swing.event.DocumentEvent?) {
                notifyContentChanged()
            }

            override fun removeUpdate(e: javax.swing.event.DocumentEvent?) {
                notifyContentChanged()
            }

            override fun changedUpdate(e: javax.swing.event.DocumentEvent?) {
                notifyContentChanged()
            }
        })
    }

    private fun setupTextArea() {

        textArea.syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_NONE
        textArea.isCodeFoldingEnabled = false
        textArea.font = Font("JetBrains Mono", Font.PLAIN, 14)
        textArea.tabSize = 4
        textArea.isAutoIndentEnabled = true
        textArea.paintTabLines = true


        applyThemeColors()


        textArea.isFocusable = true
        textArea.requestFocusInWindow()
    }


    private fun applyThemeColors() {
        val isDark = FlatLaf.isLafDark()


        textArea.background = UIManager.getColor("TextArea.background")
            ?: if (isDark) Color(43, 43, 43) else Color.WHITE


        textArea.foreground = UIManager.getColor("TextArea.foreground")
            ?: if (isDark) Color(169, 183, 198) else Color.BLACK


        val bgColor = textArea.background
        currentLineHighlightNormal = if (isDark) {
            Color(
                minOf(bgColor.red + 10, 255),
                minOf(bgColor.green + 10, 255),
                minOf(bgColor.blue + 10, 255)
            )
        } else {
            Color(
                maxOf(bgColor.red - 15, 0),
                maxOf(bgColor.green - 15, 0),
                maxOf(bgColor.blue - 15, 0)
            )
        }
        textArea.currentLineHighlightColor = currentLineHighlightNormal


        currentLineHighlightDebug = if (isDark) {
            Color(70, 100, 70)
        } else {
            Color(200, 230, 200)
        }
    }

    private fun setupScrollPane() {
        scrollPane.setLineNumbersEnabled(true)
        scrollPane.setFoldIndicatorEnabled(true)



        scrollPane.isIconRowHeaderEnabled = true

        val gutter = scrollPane.gutter
        gutter.setBookmarkingEnabled(false)


        scrollPane.verticalScrollBar.unitIncrement = 16
        scrollPane.horizontalScrollBar.unitIncrement = 16


        scrollPane.setWheelScrollingEnabled(true)
    }

    private fun setupGutterListener() {

        val gutter = scrollPane.gutter

        val mouseListener = object : java.awt.event.MouseAdapter() {
            override fun mouseClicked(e: java.awt.event.MouseEvent) {
                try {

                    val viewPos = java.awt.Point(textArea.x, e.y)
                    val offset = textArea.viewToModel2D(viewPos)

                    if (offset >= 0) {
                        val lineNumber = textArea.getLineOfOffset(offset) + 1
                        toggleBreakpointAtLine(lineNumber)
                    }
                } catch (ex: Exception) {

                }
            }
        }


        gutter.addMouseListener(mouseListener)
        for (i in 0 until gutter.componentCount) {
            val child = gutter.getComponent(i)
            child.addMouseListener(mouseListener)
        }
    }

    private fun createToolbar(): JToolBar {
        val toolbar = JToolBar()
        toolbar.isFloatable = false





        return toolbar
    }

    fun openFile() {
        val chooser = JFileChooser()
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            loadFile(chooser.selectedFile)
        }
    }

    fun loadFile(file: File) {
        try {
            currentFile = file
            textArea.text = file.readText()


            textArea.syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_NONE


            textArea.caretPosition = 0


            lastSavedContent = textArea.text

            breakpoints.clear()
            notifyBreakpointsChanged()
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                this,
                "Error loading file: ${e.message}",
                "Error",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    fun saveFile() {
        val file = currentFile ?: return
        try {
            file.writeText(textArea.text)
            lastSavedContent = textArea.text
            JOptionPane.showMessageDialog(
                this,
                "File saved successfully",
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            )
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                this,
                "Error saving file: ${e.message}",
                "Error",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    fun toggleBreakpoint() {
        val line = textArea.caretLineNumber + 1
        toggleBreakpointAtLine(line)
    }

    private fun toggleBreakpointAtLine(line: Int) {
        val isAdding = !breakpoints.contains(line)

        if (breakpoints.contains(line)) {
            breakpoints.remove(line)
            removeBreakpointIcon(line)
        } else {
            breakpoints.add(line)
            addBreakpointIcon(line)
        }
        notifyBreakpointsChanged()


        notifyBreakpointToggled(line, isAdding)
    }

    private fun addBreakpointIcon(line: Int) {
        try {
            val gutter = scrollPane.gutter
            val icon = createBreakpointIcon()


            val offset = textArea.getLineStartOffset(line - 1)


            val info = gutter.addOffsetTrackingIcon(offset, icon)
            breakpointIcons[line] = info


            gutter.repaint()
            scrollPane.repaint()
            this.repaint()
        } catch (e: Exception) {

        }
    }

    private fun removeBreakpointIcon(line: Int) {
        try {
            val gutter = scrollPane.gutter
            val info = breakpointIcons.remove(line)
            if (info != null) {
                gutter.removeTrackingIcon(info)
            }
        } catch (e: Exception) {

        }
    }

    private fun createBreakpointIcon(): Icon {

        val size = 16
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()


        g.setRenderingHint(
            java.awt.RenderingHints.KEY_ANTIALIASING,
            java.awt.RenderingHints.VALUE_ANTIALIAS_ON
        )


        val padding = 2
        g.color = Color(220, 50, 50)
        g.fillOval(padding, padding, size - padding * 2 - 1, size - padding * 2 - 1)


        g.color = Color(180, 30, 30)
        g.drawOval(padding, padding, size - padding * 2 - 1, size - padding * 2 - 1)

        g.dispose()
        return ImageIcon(image)
    }

    fun getBreakpoints(): Set<Int> = breakpoints.toSet()

    fun getCurrentFile(): File? = currentFile

    fun getText(): String = textArea.text

    fun addBreakpointListener(listener: (Set<Int>) -> Unit) {
        breakpointListeners.add(listener)
    }

    private fun notifyBreakpointsChanged() {
        breakpointListeners.forEach { it(breakpoints.toSet()) }
    }

    fun addContentChangeListener(listener: (String) -> Unit) {
        contentChangeListeners.add(listener)
    }

    private fun notifyContentChanged() {
        contentChangeListeners.forEach { it(textArea.text) }

        runIconManager.updateRunIcons(textArea.text)
    }


    fun highlightDebugLine(line: Int) {
        currentDebugLine = line
        try {

            val offset = textArea.getLineStartOffset(line - 1)
            textArea.caretPosition = offset


            val gutter = scrollPane.gutter
            val icon = createDebugLineIcon()
            gutter.addLineTrackingIcon(line - 1, icon)


            textArea.currentLineHighlightColor = currentLineHighlightDebug
            textArea.repaint()
        } catch (e: Exception) {

        }
    }


    fun clearDebugLineHighlight() {
        currentDebugLine = null
        textArea.currentLineHighlightColor = currentLineHighlightNormal
        textArea.repaint()
    }

    private fun createDebugLineIcon(): Icon {

        val size = 12
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()


        g.color = Color(50, 150, 220)
        val xPoints = intArrayOf(0, size - 4, 0)
        val yPoints = intArrayOf(0, size / 2, size - 1)
        g.fillPolygon(xPoints, yPoints, 3)

        g.dispose()
        return ImageIcon(image)
    }


    fun hasUnsavedChanges(): Boolean {
        return textArea.text != lastSavedContent
    }


    fun addRunLineListener(listener: (Int) -> Unit) {
        runLineListeners.add(listener)
    }

    private fun notifyRunLine(line: Int) {
        for (listener in runLineListeners) {
            listener(line)
        }
    }


    fun addBreakpointToggleListener(listener: (Int, Boolean) -> Unit) {
        breakpointToggleListeners.add(listener)
    }

    private fun notifyBreakpointToggled(line: Int, isAdded: Boolean) {
        for (listener in breakpointToggleListeners) {
            listener(line, isAdded)
        }
    }


    fun updateRunIcons() {
        runIconManager.updateRunIcons(textArea.text)
    }


    override fun requestFocusInWindow(): Boolean {
        return textArea.requestFocusInWindow()
    }
}
