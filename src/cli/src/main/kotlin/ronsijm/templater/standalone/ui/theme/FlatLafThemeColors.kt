package ronsijm.templater.standalone.ui.theme

import com.formdev.flatlaf.FlatLaf
import ronsijm.templater.ui.theme.ThemeColors
import java.awt.Color
import java.awt.Font
import javax.swing.UIManager

class FlatLafThemeColors : ThemeColors {

    override fun getBackground(): Color {
        return UIManager.getColor("Panel.background") ?: Color.WHITE
    }

    override fun getForeground(): Color {
        return UIManager.getColor("Panel.foreground") ?: Color.BLACK
    }

    override fun getAdaptiveColor(light: Color, dark: Color): Color {
        return if (isDarkTheme()) dark else light
    }

    override fun isDarkTheme(): Boolean {
        return FlatLaf.isLafDark()
    }

    override fun getLabelFont(): Font {
        return UIManager.getFont("Label.font") ?: Font("Dialog", Font.PLAIN, 12)
    }

    override fun getItalicLabelFont(): Font {
        val baseFont = getLabelFont()
        return baseFont.deriveFont(Font.ITALIC)
    }

    companion object {
        val instance: FlatLafThemeColors by lazy { FlatLafThemeColors() }
    }
}

