package ronsijm.templater.editor

import org.junit.Test
import org.junit.Assert.*
import ronsijm.templater.common.TemplateSyntax

class TemplateBreakpointGutterProviderTest {

    private val templateRegex = TemplateSyntax.TEMPLATE_BLOCK_REGEX

    @Test
    fun testSingleLineTemplateBlock() {
        val text = """
            # Test

            <% tp.date.now() %>

            Some text
        """.trimIndent()

        val templateLines = findAllTemplateLines(text)


        assertEquals("Should find one template line", 1, templateLines.size)
        assertTrue("Should include line 3", templateLines.contains(3))
    }

    @Test
    fun testMultiLineTemplateBlock() {
        val text = """
            <%*
            const x = 1;
            const y = 2;
            tR += x + y;
            %>
        """.trimIndent()

        val templateLines = findAllTemplateLines(text)


        assertEquals("Should find 5 template lines", 5, templateLines.size)
        assertTrue("Should include line 1", templateLines.contains(1))
        assertTrue("Should include line 2", templateLines.contains(2))
        assertTrue("Should include line 3", templateLines.contains(3))
        assertTrue("Should include line 4", templateLines.contains(4))
        assertTrue("Should include line 5", templateLines.contains(5))
    }

    @Test
    fun testMultipleTemplateBlocks() {
        val text = """
            <% tp.date.now() %>

            Some text

            <%*
            const x = 1;
            %>
        """.trimIndent()

        val templateLines = findAllTemplateLines(text)


        assertEquals("Should find 4 template lines", 4, templateLines.size)
        assertTrue("Should include line 1", templateLines.contains(1))
        assertTrue("Should include line 5", templateLines.contains(5))
        assertTrue("Should include line 6", templateLines.contains(6))
        assertTrue("Should include line 7", templateLines.contains(7))
    }

    @Test
    fun testLargeMultiLineTemplateBlock() {

        val lines = mutableListOf<String>()
        lines.add("<%*")
        for (i in 1..90) {
            lines.add("const line$i = $i;")
        }
        lines.add("%>")
        val text = lines.joinToString("\n")

        val templateLines = findAllTemplateLines(text)


        assertEquals("Should find 92 template lines", 92, templateLines.size)
        for (i in 1..92) {
            assertTrue("Should include line $i", templateLines.contains(i))
        }
    }

    @Test
    fun testNoTemplateBlocks() {
        val text = """
            # Regular Markdown

            No templates here
        """.trimIndent()

        val templateLines = findAllTemplateLines(text)

        assertEquals("Should find no template lines", 0, templateLines.size)
    }

    @Test
    fun testCalculatePiScenario() {


        val text = """
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

            function cs32(s) {
            let h = 0;
            for (let i = 0; i < s.length; i++) h = (h * 31 + s.charCodeAt(i)) >>> 0;
            return h >>> 0;
            }

            const totalDigits = digitsAfterDecimal + 1;

            spy(30);

            const runs = 3;
            const times = [];
            let lastPi = "";

            for (let r = 0; r < runs; r++) {
            const t0 = now();
            const raw = spy(totalDigits);
            const t1 = now();

            lastPi = raw.startsWith("0") ? raw.slice(1) : raw;
            times.push(t1 - t0);
            }

            times.sort((a, b) => a - b);
            const medianMs = times[Math.floor(times.length / 2)];

            const pi = lastPi;
            const formatted = pi[0] + "." + pi.slice(1);

            const previewLen = Math.min(80, formatted.length);
            const preview = formatted.slice(0, previewLen);
            const tail = formatted.slice(Math.max(0, formatted.length - 20));

            tR += `Pi benchmark\n`;
            tR += `digits_after_decimal: ${'$'}{digitsAfterDecimal}\n`;
            tR += `runs_ms: ${'$'}{times.map(x => x.toFixed(2)).join(", ")}\n`;
            tR += `median_ms: ${'$'}{medianMs.toFixed(2)}\n`;
            tR += `cs32: ${'$'}{cs32(formatted)}\n`;
            tR += `preview: ${'$'}{preview}\n`;
            tR += `tail: ${'$'}{tail}\n`;
            %>
        """.trimIndent()

        val templateLines = findAllTemplateLines(text)
        val totalLines = text.count { it == '\n' } + 1


        assertEquals("Should find all lines in the Calculate Pi script", 92, totalLines)
        assertEquals("Should mark all 92 lines as template lines", 92, templateLines.size)


        assertTrue("Line 1 should be a template line", templateLines.contains(1))
        assertTrue("Line 10 should be a template line", templateLines.contains(10))
        assertTrue("Line 30 should be a template line", templateLines.contains(30))
        assertTrue("Line 50 should be a template line", templateLines.contains(50))
        assertTrue("Line 70 should be a template line", templateLines.contains(70))
        assertTrue("Line 92 should be a template line", templateLines.contains(92))


        for (i in 1..92) {
            assertTrue("Line $i should be a template line", templateLines.contains(i))
        }
    }


    private fun findAllTemplateLines(text: String): Set<Int> {
        val templateLines = mutableSetOf<Int>()
        templateRegex.findAll(text).forEach { match ->

            val startPos = match.range.first
            val endPos = match.range.last + 1


            val startLine = text.substring(0, startPos).count { it == '\n' } + 1
            val endLine = text.substring(0, endPos).count { it == '\n' } + 1


            for (lineNum in startLine..endLine) {
                templateLines.add(lineNum)
            }
        }
        return templateLines
    }
}

