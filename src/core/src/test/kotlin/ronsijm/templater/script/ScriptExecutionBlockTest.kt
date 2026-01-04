package ronsijm.templater.script

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.parser.TemplateParser
import ronsijm.templater.TestContextFactory

class ScriptExecutionBlockTest {

    @Test
    fun `test simple execution block`() {
        val template = """
            <%*
            tR += 'Hello World';
            %>
        """.trimIndent()

        val context = TestContextFactory.create()
        val parser = TemplateParser(validateSyntax = false)
        val result = parser.parse(template, context)

        println("Result: '$result'")
        assertTrue(result.contains("Hello World"), "Result should contain 'Hello World'")
    }

    @Test
    fun `test execution block with Date now`() {
        val template = """
            <%*
            const now = () => Date.now();
            tR += 'Time: ' + now();
            %>
        """.trimIndent()

        val context = TestContextFactory.create()
        val parser = TemplateParser(validateSyntax = false)
        val result = parser.parse(template, context)

        println("Result: '$result'")
        assertTrue(result.contains("Time:"), "Result should contain 'Time:'")
    }

    @Test
    fun `test execution block with performance now`() {
        val template = """
            <%*
            const now = () => (typeof performance !== 'undefined' && performance.now) ? performance.now() : Date.now();
            tR += 'Time: ' + now();
            %>
        """.trimIndent()

        val context = TestContextFactory.create()
        val parser = TemplateParser(validateSyntax = false)
        val result = parser.parse(template, context)

        println("Result: '$result'")
        assertTrue(result.contains("Time:"), "Result should contain 'Time:'")
    }

    @Test
    fun `test BenchEscapeHeavy simplified`() {
        val template = """
            <%*
            const now = () => (typeof performance !== 'undefined' && performance.now) ? performance.now() : Date.now();
            const RUNS = 100;
            const STR_LEN = 10;

            function htmlEscape(s) {
                let r = '';
                for (let i = 0; i < s.length; i++) {
                    const c = s.charCodeAt(i);
                    if (c === 38) r += '&amp;';
                    else if (c === 60) r += '&lt;';
                    else if (c === 62) r += '&gt;';
                    else if (c === 34) r += '&quot;';
                    else if (c === 39) r += '&#39;';
                    else r += s[i];
                }
                return r;
            }

            const base = ('<&>"\'' + ' loremIPSUM0123 ').repeat(64).slice(0, STR_LEN);

            const t0 = now();
            let bytesOut = 0;
            for (let i = 0; i < RUNS; i++) bytesOut += htmlEscape(base + i).length;
            const t1 = now();

            const ms = t1 - t0;
            const mb = bytesOut / (1024 * 1024);
            const mbps = mb / (ms / 1000);

            tR += '# Escape-heavy benchmark\n\n';
            tR += `Runs: ${'$'}{RUNS}, StrLen: ${'$'}{STR_LEN}\n\n`;
            tR += `Time: ${'$'}{ms.toFixed(3)} ms\n`;
            tR += `Output: ${'$'}{mb.toFixed(3)} MiB\n`;
            tR += `Throughput: ${'$'}{mbps.toFixed(3)} MiB/s\n`;
            %>
        """.trimIndent()

        val context = TestContextFactory.create()
        val parser = TemplateParser(validateSyntax = false)
        val result = parser.parse(template, context)

        println("Result: '$result'")
        assertTrue(result.contains("Escape-heavy benchmark"), "Result should contain 'Escape-heavy benchmark'")
        assertTrue(result.contains("Runs:"), "Result should contain 'Runs:'")
    }

    @Test
    fun `test repeat and slice`() {
        val template = """
            <%*
            const base = ('<&>"\'' + ' loremIPSUM0123 ').repeat(64).slice(0, 10);
            tR += 'Base: ' + base;
            %>
        """.trimIndent()

        val context = TestContextFactory.create()
        val parser = TemplateParser(validateSyntax = false)
        val result = parser.parse(template, context)

        println("Result: '$result'")
        assertTrue(result.contains("Base:"), "Result should contain 'Base:'")
    }

    @Test
    fun `test simple repeat`() {
        val template = """
            <%*
            const base = 'hello'.repeat(3);
            tR += 'Base: ' + base;
            %>
        """.trimIndent()

        val context = TestContextFactory.create()
        val parser = TemplateParser(validateSyntax = false)
        val result = parser.parse(template, context)

        println("Result: '$result'")
        assertTrue(result.contains("Base: hellohellohello"), "Result should contain 'Base: hellohellohello'")
    }

    @Test
    fun `test repeat then slice`() {
        val template = """
            <%*
            const base = 'hello'.repeat(3).slice(0, 5);
            tR += 'Base: ' + base;
            %>
        """.trimIndent()

        val context = TestContextFactory.create()
        val parser = TemplateParser(validateSyntax = false)
        val result = parser.parse(template, context)

        println("Result: '$result'")
        assertTrue(result.contains("Base: hello"), "Result should contain 'Base: hello'")
    }

    @Test
    fun `test parenthesized expression with repeat`() {
        val template = """
            <%*
            const base = ('hello' + ' world').repeat(2);
            tR += 'Base: ' + base;
            %>
        """.trimIndent()

        val context = TestContextFactory.create()
        val parser = TemplateParser(validateSyntax = false)
        val result = parser.parse(template, context)

        println("Result: '$result'")
        assertTrue(result.contains("Base: hello worldhello world"), "Result should contain 'Base: hello worldhello world'")
    }

    @Test
    fun `test parenthesized expression with escaped quote`() {
        val template = """
            <%*
            const base = ('<&>"\'' + ' lorem').repeat(2);
            tR += 'Base: ' + base;
            %>
        """.trimIndent()

        val context = TestContextFactory.create()
        val parser = TemplateParser(validateSyntax = false)
        val result = parser.parse(template, context)

        println("Result: '$result'")
        assertTrue(result.contains("Base:"), "Result should contain 'Base:'")
    }

    @Test
    fun `test simple escaped quote`() {
        val template = """
            <%*
            const base = 'hello\'world';
            tR += 'Base: ' + base;
            %>
        """.trimIndent()

        println("Template content:")
        println(template)
        println("---")

        val context = TestContextFactory.create()
        val parser = TemplateParser(validateSyntax = false)

        try {
            val result = parser.parse(template, context)
            println("Result: '$result'")
            assertTrue(result.contains("Base: hello'world"), "Result should contain \"Base: hello'world\"")
        } catch (e: Exception) {
            println("Exception: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}
