package ronsijm.templater.standalone.ui

import ronsijm.templater.standalone.ui.settings.*
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.*

class SettingsDialog(parent: JFrame) : JDialog(parent, "Settings", true) {

    private val tabbedPane = JTabbedPane()
    private val configurables = mutableListOf<Configurable>()
    private lateinit var applyButton: JButton

    init {
        setupUI()
        resetAll()
    }

    private fun setupUI() {
        setSize(700, 500)
        setLocationRelativeTo(parent)
        layout = BorderLayout()


        val generalConfigurable = GeneralConfigurable()
        val executionConfigurable = ExecutionConfigurable()
        val mermaidConfigurable = MermaidConfigurable()
        val afterRunningConfigurable = AfterRunningConfigurable()
        val hotkeysConfigurable = HotkeysConfigurable()

        configurables.addAll(listOf(
            generalConfigurable,
            executionConfigurable,
            mermaidConfigurable,
            afterRunningConfigurable,
            hotkeysConfigurable
        ))


        for (configurable in configurables) {
            tabbedPane.addTab(configurable.displayName, configurable.createComponent())
        }

        add(tabbedPane, BorderLayout.CENTER)
        add(createButtonPanel(), BorderLayout.SOUTH)


        tabbedPane.addChangeListener { updateApplyButtonState() }
    }

    private fun createButtonPanel(): JPanel {
        val panel = JPanel(FlowLayout(FlowLayout.RIGHT))

        val okButton = JButton("OK")
        okButton.addActionListener {
            applyAll()
            dispose()
        }

        val cancelButton = JButton("Cancel")
        cancelButton.addActionListener {
            disposeAll()
            dispose()
        }

        applyButton = JButton("Apply")
        applyButton.addActionListener {
            applyAll()
            updateApplyButtonState()
        }

        val resetButton = JButton("Reset")
        resetButton.addActionListener {
            resetAll()
            updateApplyButtonState()
        }

        panel.add(okButton)
        panel.add(cancelButton)
        panel.add(applyButton)
        panel.add(resetButton)

        return panel
    }


    private fun resetAll() {
        for (configurable in configurables) {
            configurable.reset()
        }
        updateApplyButtonState()
    }


    private fun applyAll() {
        for (configurable in configurables) {
            if (configurable.isModified()) {
                configurable.apply()
            }
        }
    }


    private fun disposeAll() {
        for (configurable in configurables) {
            configurable.disposeUIResources()
        }
    }


    private fun isAnyModified(): Boolean {
        return configurables.any { it.isModified() }
    }


    private fun updateApplyButtonState() {
        applyButton.isEnabled = isAnyModified()
    }
}

