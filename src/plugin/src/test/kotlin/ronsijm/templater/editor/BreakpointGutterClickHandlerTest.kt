package ronsijm.templater.editor

import org.junit.Test
import org.junit.Assert.*
import ronsijm.templater.common.TemplateSyntax

class BreakpointGutterClickHandlerTest {

    private val templateRegex = TemplateSyntax.TEMPLATE_BLOCK_REGEX

    @Test
    fun testLineWithTemplateStart() {
        val text = """
            <%*
            const x = 1;
            %>
        """.trimIndent()


        assertTrue("Line 1 should be detected as template line", isTemplateLine(text, 0))
    }

    @Test
    fun testLineInsideMultiLineBlock() {
        val text = """
            <%*
            const x = 1;
            const y = 2;
            %>
        """.trimIndent()


        assertTrue("Line 2 should be detected as inside template block", isTemplateLine(text, 1))
        assertTrue("Line 3 should be detected as inside template block", isTemplateLine(text, 2))
    }

    @Test
    fun testLineWithTemplateEnd() {
        val text = """
            <%*
            const x = 1;
            %>
        """.trimIndent()


        assertTrue("Line 3 should be detected as template line", isTemplateLine(text, 2))
    }

    @Test
    fun testLineOutsideTemplateBlock() {
        val text = """
            # Regular Markdown

            <%* const x = 1; %>

            More regular text
        """.trimIndent()


        assertFalse("Line 1 should not be detected as template line", isTemplateLine(text, 0))
        assertFalse("Line 2 should not be detected as template line", isTemplateLine(text, 1))
        assertTrue("Line 3 should be detected as template line", isTemplateLine(text, 2))
        assertFalse("Line 4 should not be detected as template line", isTemplateLine(text, 3))
        assertFalse("Line 5 should not be detected as template line", isTemplateLine(text, 4))
    }

    @Test
    fun testCalculatePiScenarioMiddleLine() {

        val text = buildCalculatePiText()


        assertTrue("Line 50 should be detected as inside template block", isTemplateLine(text, 49))
    }

    @Test
    fun testCalculatePiScenarioAllLines() {

        val text = buildCalculatePiText()
        val totalLines = text.count { it == '\n' } + 1

        for (lineIndex in 0 until totalLines) {
            assertTrue("Line ${lineIndex + 1} should be detected as template line", isTemplateLine(text, lineIndex))
        }
    }


    private fun isTemplateLine(text: String, lineIndex: Int): Boolean {
        val lines = text.split('\n')
        if (lineIndex >= lines.size) return false

        val lineStartOffset = text.split('\n').take(lineIndex).sumOf { it.length + 1 }
        val lineText = lines[lineIndex]


        if (templateRegex.containsMatchIn(lineText) || lineText.contains("<%")) {
            return true
        }



        val textBeforeLine = text.substring(0, lineStartOffset)
        val lastOpen = textBeforeLine.lastIndexOf("<%")
        val lastClose = textBeforeLine.lastIndexOf("%>")


        return lastOpen > lastClose
    }

    private fun buildCalculatePiText(): String {
        return """
            <%*
            const digitsText = 500;
            if (digitsText == null) { tR += "Cancelled"; return; }

            const digitsAfterDecimal = Math.max(1, Math.min(200000, parseInt(digitsText, 10) || 0));

            const now = () =>
            (typeof performance !== "undefined" && performance.now) ? performance.now() : Date.now();

            function spy(totalDigits) {
            const len = Math.floor(totalDigits * 10 / 3) + 1;
            const a = new Array(len).fill(2);

            let nines = 0;
            let predigit = 0;
            let out = "";

            for (let j = 0; j < totalDigits; j++) {
            let q = 0;

                for (let i = len - 1; i >= 0; i--) {
                  const x = 10 * a[i] + q * (i + 1);
                  const denom = 2 * (i + 1) - 1;
                  a[i] = x % denom;
                  q = Math.floor(x / denom);
                }

                a[0] = q % 10;
                q = Math.floor(q / 10);

                if (q === 9) {
                  nines++;
                } else if (q === 10) {
                  out += String(predigit + 1);
                  out += "0".repeat(nines);
                  predigit = 0;
                  nines = 0;
                } else {
                  out += String(predigit);
                  predigit = q;
                  if (nines) {
                    out += "9".repeat(nines);
                    nines = 0;
                  }
                }
            }

            out += String(predigit);
            return out;
            }
            %>
        """.trimIndent()
    }
}

