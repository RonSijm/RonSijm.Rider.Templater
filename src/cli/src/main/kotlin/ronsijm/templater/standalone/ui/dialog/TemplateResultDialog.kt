package ronsijm.templater.standalone.ui.dialog

import java.awt.BorderLayout
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import javax.swing.*

class TemplateResultDialog(
    parent: JFrame,
    private val result: String,
    private val currentFile: File?,
    private val onOverwrite: (String) -> Unit
) : JDialog(parent, "Template Result", false) {

    init {
        setupUI()
    }

    private fun setupUI() {
        defaultCloseOperation = DISPOSE_ON_CLOSE
        setSize(800, 600)
        setLocationRelativeTo(parent)


        val textArea = JTextArea(result)
        textArea.isEditable = false
        textArea.lineWrap = true
        textArea.wrapStyleWord = true

        val scrollPane = JScrollPane(textArea)
        contentPane.add(scrollPane, BorderLayout.CENTER)


        val buttonPanel = JPanel()


        val overwriteButton = JButton("Overwrite")
        overwriteButton.addActionListener {
            handleOverwrite(textArea.text)
        }
        buttonPanel.add(overwriteButton)


        val saveAsButton = JButton("Save As...")
        saveAsButton.addActionListener {
            handleSaveAs(textArea.text)
        }
        buttonPanel.add(saveAsButton)


        val copyButton = JButton("Copy to Clipboard")
        copyButton.addActionListener {
            handleCopyToClipboard()
        }
        buttonPanel.add(copyButton)


        val closeButton = JButton("Close")
        closeButton.addActionListener { dispose() }
        buttonPanel.add(closeButton)

        contentPane.add(buttonPanel, BorderLayout.SOUTH)
    }

    private fun handleOverwrite(text: String) {
        if (currentFile == null) {
            JOptionPane.showMessageDialog(
                this,
                "No file is currently open",
                "Cannot Overwrite",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        val confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to overwrite ${currentFile.name}?",
            "Confirm Overwrite",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        )

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                currentFile.writeText(text)
                onOverwrite(text)
                JOptionPane.showMessageDialog(this, "File overwritten successfully!")
                dispose()
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(
                    this,
                    "Error writing file: ${e.message}",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                )
            }
        }
    }

    private fun handleSaveAs(text: String) {
        val fileChooser = JFileChooser()
        if (currentFile != null) {
            fileChooser.currentDirectory = currentFile.parentFile
            fileChooser.selectedFile = File(currentFile.parentFile, "output.md")
        }

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            try {
                file.writeText(text)
                JOptionPane.showMessageDialog(this, "File saved successfully!")
                dispose()
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(
                    this,
                    "Error saving file: ${e.message}",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                )
            }
        }
    }

    private fun handleCopyToClipboard() {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val selection = StringSelection(result)
        clipboard.setContents(selection, selection)
        JOptionPane.showMessageDialog(this, "Copied to clipboard!")
    }

    companion object {

        fun show(
            parent: JFrame,
            result: String,
            currentFile: File?,
            onOverwrite: (String) -> Unit
        ) {
            val dialog = TemplateResultDialog(parent, result, currentFile, onOverwrite)
            dialog.isVisible = true
        }
    }
}

