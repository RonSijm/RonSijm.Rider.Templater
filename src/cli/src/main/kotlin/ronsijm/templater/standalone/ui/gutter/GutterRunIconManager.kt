package ronsijm.templater.standalone.ui.gutter

import org.fife.ui.rtextarea.Gutter
import org.fife.ui.rtextarea.GutterIconInfo
import org.fife.ui.rtextarea.RTextScrollPane
import ronsijm.templater.standalone.settings.AppSettings
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.swing.Icon
import javax.swing.ImageIcon

class GutterRunIconManager(
    private val scrollPane: RTextScrollPane,
    private val onRunLine: (Int) -> Unit
) {

    private val runIcons = mutableMapOf<Int, GutterIconInfo>()
    private val runIcon: Icon = createRunIcon()

    fun updateRunIcons(content: String) {
        if (!AppSettings.showRunGutterIcons) {
            clearAllIcons()
            return
        }

        val gutter = scrollPane.gutter
        val templateLines = findTemplateBlockLines(content)


        val linesToRemove = runIcons.keys.filter { it !in templateLines }
        for (line in linesToRemove) {
            removeRunIcon(line)
        }


        for (line in templateLines) {
            if (line !in runIcons) {
                addRunIcon(line)
            }
        }
    }


    fun clearAllIcons() {
        val gutter = scrollPane.gutter
        for ((_, iconInfo) in runIcons) {
            try {
                gutter.removeTrackingIcon(iconInfo)
            } catch (e: Exception) {

            }
        }
        runIcons.clear()
    }

    private fun addRunIcon(line: Int) {
        try {
            val gutter = scrollPane.gutter
            val iconInfo = gutter.addLineTrackingIcon(line - 1, runIcon, "Run template block")
            runIcons[line] = iconInfo
        } catch (e: Exception) {

        }
    }

    private fun removeRunIcon(line: Int) {
        runIcons.remove(line)?.let { iconInfo ->
            try {
                scrollPane.gutter.removeTrackingIcon(iconInfo)
            } catch (e: Exception) {

            }
        }
    }


    private fun findTemplateBlockLines(content: String): Set<Int> {
        val lines = mutableSetOf<Int>()
        val regex = Regex("<%[*-]?")

        var lineNumber = 1
        var index = 0

        for (line in content.lines()) {
            if (regex.containsMatchIn(line)) {
                lines.add(lineNumber)
            }
            lineNumber++
        }

        return lines
    }


    private fun createRunIcon(): Icon {
        val size = 12
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g2d = image.createGraphics()

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)


        g2d.color = Color(76, 175, 80)
        val xPoints = intArrayOf(2, 2, 10)
        val yPoints = intArrayOf(1, 11, 6)
        g2d.fillPolygon(xPoints, yPoints, 3)

        g2d.dispose()
        return ImageIcon(image)
    }


    fun handleRunIconClick(line: Int) {
        if (line in runIcons) {
            onRunLine(line)
        }
    }


    fun hasRunIcon(line: Int): Boolean = line in runIcons
}

