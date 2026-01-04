package ronsijm.templater.ui.theme

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.awt.Color

class ThemeColorsTest {

    @Test
    fun `DefaultThemeColors light mode returns light background`() {
        val colors = DefaultThemeColors(darkMode = false)
        assertEquals(Color.WHITE, colors.getBackground())
    }

    @Test
    fun `DefaultThemeColors dark mode returns dark background`() {
        val colors = DefaultThemeColors(darkMode = true)
        assertEquals(Color(43, 43, 43), colors.getBackground())
    }

    @Test
    fun `DefaultThemeColors light mode returns dark foreground`() {
        val colors = DefaultThemeColors(darkMode = false)
        assertEquals(Color.BLACK, colors.getForeground())
    }

    @Test
    fun `DefaultThemeColors dark mode returns light foreground`() {
        val colors = DefaultThemeColors(darkMode = true)
        assertEquals(Color(187, 187, 187), colors.getForeground())
    }

    @Test
    fun `getAdaptiveColor returns light color in light mode`() {
        val colors = DefaultThemeColors(darkMode = false)
        val light = Color.RED
        val dark = Color.BLUE
        assertEquals(light, colors.getAdaptiveColor(light, dark))
    }

    @Test
    fun `getAdaptiveColor returns dark color in dark mode`() {
        val colors = DefaultThemeColors(darkMode = true)
        val light = Color.RED
        val dark = Color.BLUE
        assertEquals(dark, colors.getAdaptiveColor(light, dark))
    }

    @Test
    fun `isDarkTheme returns correct value`() {
        assertFalse(DefaultThemeColors(darkMode = false).isDarkTheme())
        assertTrue(DefaultThemeColors(darkMode = true).isDarkTheme())
    }

    @Test
    fun `getLabelFont returns non-null font`() {
        val colors = DefaultThemeColors()
        assertNotNull(colors.getLabelFont())
        assertEquals("Dialog", colors.getLabelFont().family)
    }

    @Test
    fun `getItalicLabelFont returns italic font`() {
        val colors = DefaultThemeColors()
        val font = colors.getItalicLabelFont()
        assertNotNull(font)
        assertTrue(font.isItalic)
    }
}

class AdaptiveColorTest {

    @Test
    fun `of returns correct color based on theme`() {
        val lightTheme = DefaultThemeColors(darkMode = false)
        val darkTheme = DefaultThemeColors(darkMode = true)

        val light = Color.RED
        val dark = Color.BLUE

        assertEquals(light, AdaptiveColor.of(light, dark, lightTheme))
        assertEquals(dark, AdaptiveColor.of(light, dark, darkTheme))
    }

    @Test
    fun `darken reduces RGB values`() {
        val original = Color(100, 150, 200)
        val darkened = AdaptiveColor.darken(original, 50)

        assertEquals(50, darkened.red)
        assertEquals(100, darkened.green)
        assertEquals(150, darkened.blue)
    }

    @Test
    fun `darken does not go below zero`() {
        val original = Color(30, 20, 10)
        val darkened = AdaptiveColor.darken(original, 50)

        assertEquals(0, darkened.red)
        assertEquals(0, darkened.green)
        assertEquals(0, darkened.blue)
    }

    @Test
    fun `lighten increases RGB values`() {
        val original = Color(100, 150, 200)
        val lightened = AdaptiveColor.lighten(original, 50)

        assertEquals(150, lightened.red)
        assertEquals(200, lightened.green)
        assertEquals(250, lightened.blue)
    }

    @Test
    fun `lighten does not exceed 255`() {
        val original = Color(230, 240, 250)
        val lightened = AdaptiveColor.lighten(original, 50)

        assertEquals(255, lightened.red)
        assertEquals(255, lightened.green)
        assertEquals(255, lightened.blue)
    }

    @ParameterizedTest
    @ValueSource(strings = ["#ff0000", "ff0000", "#FF0000", "FF0000"])
    fun `parseHex parses valid hex colors`(hex: String) {
        val color = AdaptiveColor.parseHex(hex)
        assertNotNull(color)
        assertEquals(255, color!!.red)
        assertEquals(0, color.green)
        assertEquals(0, color.blue)
    }

    @Test
    fun `parseHex returns null for invalid hex`() {
        assertNull(AdaptiveColor.parseHex("invalid"))
        assertNull(AdaptiveColor.parseHex(""))
        assertNull(AdaptiveColor.parseHex("gggggg"))
    }

    @Test
    fun `withAlpha creates semi-transparent color`() {
        val original = Color(100, 150, 200)
        val transparent = AdaptiveColor.withAlpha(original, 128)

        assertEquals(100, transparent.red)
        assertEquals(150, transparent.green)
        assertEquals(200, transparent.blue)
        assertEquals(128, transparent.alpha)
    }
}

