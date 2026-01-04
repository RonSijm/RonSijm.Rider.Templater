package ronsijm.templater.standalone.ui

import ronsijm.templater.standalone.RecentFolders
import ronsijm.templater.standalone.ui.util.ScrollPaneWrapper
import java.awt.BorderLayout
import java.io.File
import javax.swing.*
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeWillExpandListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.ExpandVetoException

class FileTreePanel : JPanel(BorderLayout()) {

    private val tree: JTree
    private val treeModel: DefaultTreeModel
    private val rootNode: DefaultMutableTreeNode
    private var currentFolder: File? = null
    private val breadcrumbPanel: JPanel


    private val fileSelectionListeners = mutableListOf<(File) -> Unit>()
    private val fileOpenInTabListeners = mutableListOf<(File) -> Unit>()
    private val fileOpenInWindowListeners = mutableListOf<(File) -> Unit>()


    private class FileNode(val file: File, val displayName: String = file.name) {
        override fun toString(): String = displayName
    }

    init {
        rootNode = DefaultMutableTreeNode("No folder opened")
        treeModel = DefaultTreeModel(rootNode)
        tree = JTree(treeModel)
        breadcrumbPanel = JPanel(java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 2, 2))

        setupUI()
        setupListeners()
    }

    private fun setupUI() {

        tree.cellRenderer = FileTreeCellRenderer()

        val scrollPane = JScrollPane(tree)

        add(ScrollPaneWrapper.wrap(scrollPane), BorderLayout.CENTER)


        val topPanel = JPanel(BorderLayout())


        breadcrumbPanel.border = BorderFactory.createEmptyBorder(2, 5, 2, 5)
        topPanel.add(breadcrumbPanel, BorderLayout.CENTER)


        val toolbar = JToolBar()
        toolbar.isFloatable = false

        val refreshButton = JButton("Refresh")
        refreshButton.addActionListener { refreshTree() }
        toolbar.add(refreshButton)

        topPanel.add(toolbar, BorderLayout.EAST)
        add(topPanel, BorderLayout.NORTH)
    }


    private class FileTreeCellRenderer : javax.swing.tree.DefaultTreeCellRenderer() {
        private val folderIcon = UIManager.getIcon("FileView.directoryIcon")
        private val fileIcon = UIManager.getIcon("FileView.fileIcon")

        override fun getTreeCellRendererComponent(
            tree: JTree?,
            value: Any?,
            sel: Boolean,
            expanded: Boolean,
            leaf: Boolean,
            row: Int,
            hasFocus: Boolean
        ): java.awt.Component {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)

            val node = value as? DefaultMutableTreeNode
            val userObj = node?.userObject

            if (userObj is FileNode) {
                icon = if (userObj.file.isDirectory) folderIcon else fileIcon
            }

            return this
        }
    }

    private fun setupListeners() {

        tree.addTreeSelectionListener { event ->
            val node = event.path.lastPathComponent as? DefaultMutableTreeNode
            val userObj = node?.userObject
            if (userObj is FileNode) {
                val file = userObj.file
                if (file.isFile) {
                    notifyFileSelected(file)
                }
            } else if (userObj is String && userObj == "No folder opened") {

                openFolder()
            }
        }


        tree.addTreeWillExpandListener(object : TreeWillExpandListener {
            override fun treeWillExpand(event: TreeExpansionEvent) {
                val node = event.path.lastPathComponent as? DefaultMutableTreeNode
                if (node != null) {
                    loadChildrenIfNeeded(node)
                }
            }

            override fun treeWillCollapse(event: TreeExpansionEvent) {

            }
        })


        tree.componentPopupMenu = createContextMenu()
    }

    private fun createContextMenu(): JPopupMenu {
        val menu = JPopupMenu()

        val openInTabItem = JMenuItem("Open in Tab")
        openInTabItem.addActionListener {
            val node = tree.lastSelectedPathComponent as? DefaultMutableTreeNode
            val userObj = node?.userObject
            if (userObj is FileNode && userObj.file.isFile) {
                notifyFileOpenInTab(userObj.file)
            }
        }
        menu.add(openInTabItem)

        val openInWindowItem = JMenuItem("Open in Window")
        openInWindowItem.addActionListener {
            val node = tree.lastSelectedPathComponent as? DefaultMutableTreeNode
            val userObj = node?.userObject
            if (userObj is FileNode && userObj.file.isFile) {
                notifyFileOpenInWindow(userObj.file)
            }
        }
        menu.add(openInWindowItem)

        menu.addSeparator()

        val openFolderItem = JMenuItem("Open Folder")
        openFolderItem.addActionListener {
            val node = tree.lastSelectedPathComponent as? DefaultMutableTreeNode
            val userObj = node?.userObject
            if (userObj is FileNode && userObj.file.isDirectory) {
                loadFolder(userObj.file)
            }
        }
        menu.add(openFolderItem)

        return menu
    }

    fun openFolder() {
        val chooser = JFileChooser()
        chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY


        val recentFolder = RecentFolders.getMostRecentFolder()
        if (recentFolder != null) {
            chooser.currentDirectory = recentFolder
        }

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFolder = chooser.selectedFile
            loadFolder(currentFolder!!)


            RecentFolders.addFolder(currentFolder!!)
        }
    }

    fun loadFolder(folder: File) {
        currentFolder = folder
        rootNode.removeAllChildren()
        rootNode.userObject = FileNode(folder, folder.name)

        addFilesToNode(rootNode, folder)

        treeModel.reload()
        expandFirstLevel()
        updateBreadcrumb()
    }

    private fun updateBreadcrumb() {
        breadcrumbPanel.removeAll()

        val folder = currentFolder
        if (folder == null) {
            breadcrumbPanel.add(JLabel("No folder opened"))
        } else {

            val pathParts = mutableListOf<File>()
            var current: File? = folder
            while (current != null) {
                pathParts.add(0, current)
                current = current.parentFile
            }


            for (i in pathParts.indices) {
                if (i > 0) {
                    breadcrumbPanel.add(JLabel(" > "))
                }

                val part = pathParts[i]
                val button = JButton(if (i == 0) part.absolutePath else part.name)
                button.isBorderPainted = false
                button.isContentAreaFilled = false
                button.isFocusPainted = false
                button.cursor = java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR)
                button.addActionListener {
                    loadFolder(part)
                }
                breadcrumbPanel.add(button)
            }
        }

        breadcrumbPanel.revalidate()
        breadcrumbPanel.repaint()
    }

    private fun addFilesToNode(node: DefaultMutableTreeNode, folder: File) {
        val files = folder.listFiles() ?: return


        val sorted = files.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))

        for (file in sorted) {
            val fileNode = FileNode(file)
            val childNode = DefaultMutableTreeNode(fileNode)
            node.add(childNode)

            if (file.isDirectory) {

                childNode.add(DefaultMutableTreeNode("Loading..."))
            }
        }
    }


    private fun loadChildrenIfNeeded(node: DefaultMutableTreeNode) {
        val userObj = node.userObject
        if (userObj !is FileNode) return

        val folder = userObj.file
        if (!folder.isDirectory) return


        if (node.childCount > 0) {
            val firstChild = node.firstChild as? DefaultMutableTreeNode
            val firstChildObj = firstChild?.userObject


            if (firstChildObj is String && firstChildObj == "Loading...") {
                node.removeAllChildren()
                addFilesToNode(node, folder)
                treeModel.reload(node)
            }
        }
    }

    private fun expandFirstLevel() {
        tree.expandRow(0)
    }

    private fun refreshTree() {
        currentFolder?.let { loadFolder(it) }
    }


    fun addFileSelectionListener(listener: (File) -> Unit) {
        fileSelectionListeners.add(listener)
    }


    fun removeFileSelectionListener(listener: (File) -> Unit) {
        fileSelectionListeners.remove(listener)
    }

    private fun notifyFileSelected(file: File) {
        fileSelectionListeners.forEach { it(file) }
    }


    fun getSelectedFile(): File? {
        val node = tree.lastSelectedPathComponent as? DefaultMutableTreeNode
        val userObj = node?.userObject
        return if (userObj is FileNode) userObj.file else null
    }


    fun getCurrentFolder(): File? = currentFolder


    fun addFileOpenInTabListener(listener: (File) -> Unit) {
        fileOpenInTabListeners.add(listener)
    }


    fun addFileOpenInWindowListener(listener: (File) -> Unit) {
        fileOpenInWindowListeners.add(listener)
    }


    private fun notifyFileOpenInTab(file: File) {
        fileOpenInTabListeners.forEach { it(file) }
    }


    private fun notifyFileOpenInWindow(file: File) {
        fileOpenInWindowListeners.forEach { it(file) }
    }
}

