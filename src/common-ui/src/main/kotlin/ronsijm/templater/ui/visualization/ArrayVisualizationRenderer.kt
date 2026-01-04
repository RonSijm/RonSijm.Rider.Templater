package ronsijm.templater.ui.visualization

import ronsijm.templater.debug.visualization.DataStructureSnapshot
import ronsijm.templater.debug.visualization.StateChange
import ronsijm.templater.ui.theme.ThemeColors
import java.awt.*
import javax.swing.BorderFactory
import javax.swing.JPanel

class ArrayVisualizationRenderer(
    private val themeColors: ThemeColors
) {

    fun render(snapshot: DataStructureSnapshot, stateChanges: List<StateChange>): JPanel {
        val elements = snapshot.value as? List<*> ?: emptyList<Any?>()

        return ArrayVisualizationComponent(
            variableName = snapshot.variableName,
            elements = elements,
            stateChanges = stateChanges,
            themeColors = themeColors
        )
    }
}


private class ArrayVisualizationComponent(
    private val variableName: String,
    private val elements: List<*>,
    private val stateChanges: List<StateChange>,
    private val themeColors: ThemeColors
) : JPanel() {

    companion object {
        private const val ELEMENT_WIDTH = 60
        private const val ELEMENT_HEIGHT = 60
        private const val ELEMENT_SPACING = 10
        private const val INDEX_HEIGHT = 20
        private const val PADDING = 10
    }

    init {
        border = BorderFactory.createTitledBorder("$variableName [${elements.size}]")

        val width = PADDING * 2 + elements.size * ELEMENT_WIDTH + (elements.size - 1) * ELEMENT_SPACING
        val height = PADDING * 2 + ELEMENT_HEIGHT + INDEX_HEIGHT

        preferredSize = Dimension(width, height)
        maximumSize = Dimension(Int.MAX_VALUE, height)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)


        val comparedIndices = mutableSetOf<Int>()
        val updatedIndices = mutableSetOf<Int>()
        val swappedIndices = mutableSetOf<Int>()

        for (change in stateChanges) {
            when (change) {
                is StateChange.Comparison -> {
                    comparedIndices.add(change.index1)
                    comparedIndices.add(change.index2)
                }
                is StateChange.ElementUpdate -> {
                    updatedIndices.add(change.index)
                }
                is StateChange.ElementSwap -> {
                    swappedIndices.add(change.index1)
                    swappedIndices.add(change.index2)
                }
                else -> {}
            }
        }


        for (i in elements.indices) {
            val x = PADDING + i * (ELEMENT_WIDTH + ELEMENT_SPACING)
            val y = PADDING

            drawElement(g2d, x, y, i, elements[i], comparedIndices, updatedIndices, swappedIndices)
        }
    }

    private fun drawElement(
        g2d: Graphics2D,
        x: Int,
        y: Int,
        index: Int,
        value: Any?,
        comparedIndices: Set<Int>,
        updatedIndices: Set<Int>,
        swappedIndices: Set<Int>
    ) {

        val (fillColor, borderColor) = when {
            swappedIndices.contains(index) -> {

                Color(200, 200, 200) to Color(120, 120, 120)
            }
            updatedIndices.contains(index) -> {

                Color(210, 210, 210) to Color(130, 130, 130)
            }
            comparedIndices.contains(index) -> {

                Color(220, 220, 220) to Color(140, 140, 140)
            }
            else -> {

                Color(230, 230, 230) to Color(150, 150, 150)
            }
        }


        g2d.color = fillColor
        g2d.fillRect(x, y, ELEMENT_WIDTH, ELEMENT_HEIGHT)

        g2d.color = borderColor
        g2d.stroke = BasicStroke(2f)
        g2d.drawRect(x, y, ELEMENT_WIDTH, ELEMENT_HEIGHT)


        g2d.color = Color.BLACK
        g2d.font = Font("SansSerif", Font.BOLD, 16)
        val valueStr = value?.toString() ?: "null"
        val fm = g2d.fontMetrics
        val valueWidth = fm.stringWidth(valueStr)
        val valueX = x + (ELEMENT_WIDTH - valueWidth) / 2
        val valueY = y + (ELEMENT_HEIGHT + fm.ascent) / 2 - 2
        g2d.drawString(valueStr, valueX, valueY)


        g2d.color = Color.BLACK
        g2d.font = Font("SansSerif", Font.PLAIN, 12)
        val indexStr = "[$index]"
        val indexWidth = g2d.fontMetrics.stringWidth(indexStr)
        val indexX = x + (ELEMENT_WIDTH - indexWidth) / 2
        val indexY = y + ELEMENT_HEIGHT + 15
        g2d.drawString(indexStr, indexX, indexY)
    }
}

