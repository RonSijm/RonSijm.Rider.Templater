package ronsijm.templater.standalone.actions

import ronsijm.templater.standalone.events.EventBus
import ronsijm.templater.standalone.events.FileSavedEvent
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.KeyStroke

class OpenFolderAction(
    private val parent: JFrame,
    private val onFolderSelected: (File) -> Unit
) : Action {

    override val name = "Open Folder..."
    override val description = "Open a folder in the file tree"
    override val accelerator: KeyStroke = KeyStroke.getKeyStroke("ctrl shift O")

    override fun execute() {
        val chooser = JFileChooser()
        chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        chooser.dialogTitle = "Open Folder"

        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            onFolderSelected(chooser.selectedFile)
        }
    }
}

class OpenFileAction(
    private val parent: JFrame,
    private val onFileSelected: (File) -> Unit
) : Action {

    override val name = "Open File..."
    override val description = "Open a file in the editor"
    override val accelerator: KeyStroke = KeyStroke.getKeyStroke("ctrl O")

    override fun execute() {
        val chooser = JFileChooser()
        chooser.dialogTitle = "Open File"

        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            onFileSelected(chooser.selectedFile)
        }
    }
}

class SaveFileAction(
    private val context: ActionContext,
    private val onSave: () -> Unit
) : Action {

    override val name = "Save"
    override val description = "Save the current file"
    override val accelerator: KeyStroke = KeyStroke.getKeyStroke("ctrl S")

    override fun isEnabled(): Boolean {
        return context.getCurrentFile() != null
    }

    override fun execute() {
        val file = context.getCurrentFile()
        if (file != null) {
            onSave()
            EventBus.publish(FileSavedEvent(file))
        }
    }
}

class OpenRecentFolderAction(
    private val folder: File,
    private val onFolderSelected: (File) -> Unit
) : Action {

    override val name: String = folder.absolutePath
    override val description = "Open recently used folder"

    override fun isEnabled(): Boolean {
        return folder.exists() && folder.isDirectory
    }

    override fun execute() {
        if (isEnabled()) {
            onFolderSelected(folder)
        }
    }
}

