package ronsijm.templater.parallel

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class DependencyAnalyzerTest {

    private val analyzer = DependencyAnalyzer()

    private fun createBlock(id: Int, command: String, isExecution: Boolean = true) = TemplateBlock(
        id = id,
        matchText = if (isExecution) "<%* $command %>" else "<% $command %>",
        command = command,
        isExecution = isExecution,
        leftTrim = "",
        rightTrim = "",
        originalStart = 0,
        originalEnd = 0
    )

    @Test
    fun `test detects variable declaration with let`() {
        val block = createBlock(0, "let counter = 0")
        val analysis = analyzer.analyze(block)

        assertTrue(analysis.variablesWritten.contains("counter"))
        assertFalse(analysis.variablesRead.contains("counter"))
    }

    @Test
    fun `test detects variable declaration with const`() {
        val block = createBlock(0, "const name = \"test\"")
        val analysis = analyzer.analyze(block)

        assertTrue(analysis.variablesWritten.contains("name"))
    }

    @Test
    fun `test detects variable assignment`() {
        val block = createBlock(0, "counter = counter + 1")
        val analysis = analyzer.analyze(block)

        assertTrue(analysis.variablesWritten.contains("counter"))
        assertTrue(analysis.variablesRead.contains("counter"))
    }

    @Test
    fun `test detects tR write`() {
        val block = createBlock(0, "tR += \"hello\"")
        val analysis = analyzer.analyze(block)

        assertTrue(analysis.hasTrWrite)
    }

    @Test
    fun `test detects barrier function - prompt`() {
        val block = createBlock(0, "let name = await tp.system.prompt(\"Enter name\")")
        val analysis = analyzer.analyze(block)

        assertTrue(analysis.isBarrier)
    }

    @Test
    fun `test detects barrier function - suggester`() {
        val block = createBlock(0, "let choice = await tp.system.suggester([\"A\", \"B\"])")
        val analysis = analyzer.analyze(block)

        assertTrue(analysis.isBarrier)
    }

    @Test
    fun `test pure function is not barrier`() {
        val block = createBlock(0, "let date = tp.date.now()")
        val analysis = analyzer.analyze(block)

        assertFalse(analysis.isBarrier)
    }

    @Test
    fun `test interpolation block reads variable`() {
        val block = createBlock(0, "counter", isExecution = false)
        val analysis = analyzer.analyze(block)

        assertTrue(analysis.variablesRead.contains("counter"))
        assertTrue(analysis.variablesWritten.isEmpty())
    }

    @Test
    fun `test interpolation block reads tR`() {
        val block = createBlock(0, "tR", isExecution = false)
        val analysis = analyzer.analyze(block)

        assertTrue(analysis.variablesRead.contains("tR"))
    }

    @Test
    fun `test dependency - read after write`() {
        val block1 = createBlock(0, "let x = 5")
        val block2 = createBlock(1, "x", isExecution = false)

        val analysis1 = analyzer.analyze(block1)
        val analysis2 = analyzer.analyze(block2)

        assertTrue(analysis2.dependsOn(analysis1))
        assertFalse(analysis1.dependsOn(analysis2))
    }

    @Test
    fun `test dependency - write after write`() {
        val block1 = createBlock(0, "let x = 5")
        val block2 = createBlock(1, "x = 10")

        val analysis1 = analyzer.analyze(block1)
        val analysis2 = analyzer.analyze(block2)

        assertTrue(analysis2.dependsOn(analysis1))
    }

    @Test
    fun `test dependency - tR accumulation`() {
        val block1 = createBlock(0, "tR += \"A\"")
        val block2 = createBlock(1, "tR += \"B\"")

        val analysis1 = analyzer.analyze(block1)
        val analysis2 = analyzer.analyze(block2)

        assertTrue(analysis2.dependsOn(analysis1))
    }

    @Test
    fun `test no dependency - independent blocks`() {
        val block1 = createBlock(0, "let x = 5")
        val block2 = createBlock(1, "let y = 10")

        val analysis1 = analyzer.analyze(block1)
        val analysis2 = analyzer.analyze(block2)

        assertFalse(analysis2.dependsOn(analysis1))
        assertFalse(analysis1.dependsOn(analysis2))
    }

    @Test
    fun `test barrier creates dependency`() {
        val block1 = createBlock(0, "let name = await tp.system.prompt(\"Name\")")
        val block2 = createBlock(1, "let x = 5")

        val analysis1 = analyzer.analyze(block1)
        val analysis2 = analyzer.analyze(block2)

        assertTrue(analysis2.dependsOn(analysis1))
    }
}
