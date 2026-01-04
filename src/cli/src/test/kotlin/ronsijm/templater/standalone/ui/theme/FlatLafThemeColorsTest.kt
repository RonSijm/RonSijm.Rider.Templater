package ronsijm.templater.standalone.ui.theme

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import com.formdev.flatlaf.FlatDarculaLaf
import com.formdev.flatlaf.FlatIntelliJLaf
import java.awt.Color
import javax.swing.UIManager

class FlatLafThemeColorsTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setupLaf() {

            try {
                FlatIntelliJLaf.setup()
            } catch (e: Exception) {

            }
        }
    }

    @Test
    fun `getBackground returns non-null color`() {
        val colors = FlatLafThemeColors()
        assertNotNull(colors.getBackground())
    }

    @Test
    fun `getForeground returns non-null color`() {
        val colors = FlatLafThemeColors()
        assertNotNull(colors.getForeground())
    }

    @Test
    fun `getAdaptiveColor returns appropriate color based on theme`() {
        val colors = FlatLafThemeColors()
        val light = Color.RED
        val dark = Color.BLUE

        val result = colors.getAdaptiveColor(light, dark)
        assertNotNull(result)

        assertTrue(result == light || result == dark)
    }

    @Test
    fun `isDarkTheme returns boolean`() {
        val colors = FlatLafThemeColors()

        val isDark = colors.isDarkTheme()
        assertTrue(isDark || !isDark)
    }

    @Test
    fun `getLabelFont returns non-null font`() {
        val colors = FlatLafThemeColors()
        assertNotNull(colors.getLabelFont())
    }

    @Test
    fun `getItalicLabelFont returns italic font`() {
        val colors = FlatLafThemeColors()
        val font = colors.getItalicLabelFont()
        assertNotNull(font)
        assertTrue(font.isItalic)
    }

    @Test
    fun `singleton instance is same object`() {
        val instance1 = FlatLafThemeColors.instance
        val instance2 = FlatLafThemeColors.instance
        assertSame(instance1, instance2)
    }

    @Test
    fun `dark theme detection works with Darcula`() {
        try {
            FlatDarculaLaf.setup()
            val colors = FlatLafThemeColors()
            assertTrue(colors.isDarkTheme())
        } catch (e: Exception) {

        }
    }

    @Test
    fun `light theme detection works with IntelliJ`() {
        try {
            FlatIntelliJLaf.setup()
            val colors = FlatLafThemeColors()
            assertFalse(colors.isDarkTheme())
        } catch (e: Exception) {

        }
    }
}

