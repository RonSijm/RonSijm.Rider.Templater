package ronsijm.templater.standalone.ui

import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

class DetachablePanel(
    private val title: String,
    private val content: JComponent,
    private val parent: JFrame
) : JPanel(BorderLayout()) {

    private var detachedWindow: JFrame? = null
    private var isDetached = false
    private var originalParent: JComponent? = null

    init {
        add(content, BorderLayout.CENTER)
        add(createToolbar(), BorderLayout.NORTH)
    }

    private fun createToolbar(): JToolBar {
        val toolbar = JToolBar()
        toolbar.isFloatable = false

        val titleLabel = JLabel(title)
        toolbar.add(titleLabel)
        toolbar.add(Box.createHorizontalGlue())

        val detachButton = JButton("?")
        detachButton.toolTipText = "Detach panel"
        detachButton.addActionListener {
            if (isDetached) {
                dock()
            } else {
                detach()
            }
        }
        toolbar.add(detachButton)

        return toolbar
    }


    fun detach() {
        if (isDetached) return


        originalParent = this.parent as? JComponent


        originalParent?.remove(this)
        originalParent?.revalidate()
        originalParent?.repaint()


        detachedWindow = JFrame(title).apply {
            defaultCloseOperation = JFrame.DO_NOTHING_ON_CLOSE
            add(content, BorderLayout.CENTER)
            add(createDockToolbar(), BorderLayout.NORTH)
            size = Dimension(600, 400)
            setLocationRelativeTo(parent)

            addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent) {
                    dock()
                }
            })

            isVisible = true
        }

        isDetached = true
    }


    fun dock() {
        if (!isDetached) return


        detachedWindow?.remove(content)
        detachedWindow?.dispose()
        detachedWindow = null


        add(content, BorderLayout.CENTER)


        originalParent?.add(this)
        originalParent?.revalidate()
        originalParent?.repaint()

        isDetached = false
    }

    private fun createDockToolbar(): JToolBar {
        val toolbar = JToolBar()
        toolbar.isFloatable = false

        val titleLabel = JLabel(title)
        toolbar.add(titleLabel)
        toolbar.add(Box.createHorizontalGlue())

        val dockButton = JButton("?")
        dockButton.toolTipText = "Dock panel"
        dockButton.addActionListener {
            dock()
        }
        toolbar.add(dockButton)

        return toolbar
    }


    fun getContent(): JComponent = content


    fun isDetached(): Boolean = isDetached
}

