package ronsijm.templater.ui.visualization

import ronsijm.templater.debug.DebugBreakpoint
import ronsijm.templater.debug.visualization.DataStructureSnapshot
import ronsijm.templater.debug.visualization.StateChange
import ronsijm.templater.ui.theme.ThemeColors
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

class AlgorithmVisualizationPanel(
    private val themeColors: ThemeColors
) : JPanel(BorderLayout()) {

    private val statusLabel = JLabel("No data structures to visualize")
    private val visualizationArea = JPanel()
    private val scrollPane = JScrollPane(visualizationArea)


    private var currentSnapshots: List<DataStructureSnapshot> = emptyList()
    private var currentChanges: List<StateChange> = emptyList()


    private val arrayRenderer = ArrayVisualizationRenderer(themeColors)

    init {
        setupUI()
    }

    private fun setupUI() {

        val statusPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
            add(statusLabel, BorderLayout.WEST)
        }


        visualizationArea.layout = BoxLayout(visualizationArea, BoxLayout.Y_AXIS)
        visualizationArea.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED

        add(statusPanel, BorderLayout.NORTH)
        add(scrollPane, BorderLayout.CENTER)

        preferredSize = Dimension(400, 300)
    }


    fun updateVisualization(breakpoint: DebugBreakpoint) {
        val step = breakpoint.step
        currentSnapshots = step.dataSnapshots
        currentChanges = step.stateChanges

        if (currentSnapshots.isEmpty()) {
            statusLabel.text = "No data structures to visualize"
            visualizationArea.removeAll()
            visualizationArea.revalidate()
            visualizationArea.repaint()
            return
        }

        statusLabel.text = "Visualizing ${currentSnapshots.size} data structure(s)"
        renderVisualizations()
    }


    fun clearVisualization() {
        statusLabel.text = "No data structures to visualize"
        currentSnapshots = emptyList()
        currentChanges = emptyList()
        visualizationArea.removeAll()
        visualizationArea.revalidate()
        visualizationArea.repaint()
    }

    private fun renderVisualizations() {
        visualizationArea.removeAll()

        for (snapshot in currentSnapshots) {


            val visualizationComponent = when (snapshot.dataType) {
                DataStructureSnapshot.DataType.ARRAY,
                DataStructureSnapshot.DataType.LIST -> {
                    arrayRenderer.render(snapshot, currentChanges)
                }
                DataStructureSnapshot.DataType.MAP,
                DataStructureSnapshot.DataType.SET -> {

                    createPlaceholderPanel(snapshot)
                }
                else -> {

                    createPlaceholderPanel(snapshot)
                }
            }

            visualizationArea.add(visualizationComponent)
            visualizationArea.add(Box.createVerticalStrut(20))
        }

        visualizationArea.revalidate()
        visualizationArea.repaint()
    }

    private fun createPlaceholderPanel(snapshot: DataStructureSnapshot): JPanel {
        return JPanel(BorderLayout()).apply {
            border = BorderFactory.createTitledBorder("${snapshot.variableName} (${snapshot.dataType})")
            add(JLabel("Visualization for ${snapshot.dataType} not yet implemented"), BorderLayout.CENTER)
            preferredSize = Dimension(300, 100)
            maximumSize = Dimension(Int.MAX_VALUE, 100)
        }
    }
}

