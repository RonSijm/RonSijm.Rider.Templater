package ronsijm.templater.ui.theme

import java.awt.Color

interface ThemeColors {

    fun getBackground(): Color


    fun getForeground(): Color


    fun getAdaptiveColor(light: Color, dark: Color): Color


    fun isDarkTheme(): Boolean


    fun getLabelFont(): java.awt.Font


    fun getItalicLabelFont(): java.awt.Font
}


class DefaultThemeColors(private val darkMode: Boolean = false) : ThemeColors {

    override fun getBackground(): Color = if (darkMode) Color(43, 43, 43) else Color.WHITE

    override fun getForeground(): Color = if (darkMode) Color(187, 187, 187) else Color.BLACK

    override fun getAdaptiveColor(light: Color, dark: Color): Color = if (darkMode) dark else light

    override fun isDarkTheme(): Boolean = darkMode

    override fun getLabelFont(): java.awt.Font = java.awt.Font("Dialog", java.awt.Font.PLAIN, 12)

    override fun getItalicLabelFont(): java.awt.Font = java.awt.Font("Dialog", java.awt.Font.ITALIC, 12)
}


object AdaptiveColor {

    fun of(light: Color, dark: Color, themeColors: ThemeColors): Color {
        return themeColors.getAdaptiveColor(light, dark)
    }


    fun darken(color: Color, amount: Int = 50): Color {
        return Color(
            maxOf(0, color.red - amount),
            maxOf(0, color.green - amount),
            maxOf(0, color.blue - amount),
            color.alpha
        )
    }


    fun lighten(color: Color, amount: Int = 50): Color {
        return Color(
            minOf(255, color.red + amount),
            minOf(255, color.green + amount),
            minOf(255, color.blue + amount),
            color.alpha
        )
    }


    fun parseHex(hex: String): Color? {
        return try {
            val cleanHex = hex.removePrefix("#")
            Color.decode("#$cleanHex")
        } catch (e: Exception) {
            null
        }
    }


    fun withAlpha(color: Color, alpha: Int): Color {
        return Color(color.red, color.green, color.blue, alpha)
    }
}

