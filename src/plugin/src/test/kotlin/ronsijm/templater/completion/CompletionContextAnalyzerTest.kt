package ronsijm.templater.completion

import org.junit.Test
import org.junit.Assert.*
import ronsijm.templater.completion.CompletionContextAnalyzer.CompletionContext


class CompletionContextAnalyzerTest {

    @Test
    fun `test not in template - plain text`() {
        val result = CompletionContextAnalyzer.analyzeContext("Hello world")
        assertTrue("Should not be in template", result is CompletionContext.NotInTemplate)
    }

    @Test
    fun `test not in template - after closed block`() {
        val result = CompletionContextAnalyzer.analyzeContext("<% tp.date.now() %> some text")
        assertTrue("Should not be in template after closed block", result is CompletionContext.NotInTemplate)
    }

    @Test
    fun `test module suggestion - empty after opening tag`() {
        val result = CompletionContextAnalyzer.analyzeContext("<%")
        assertTrue("Should suggest modules", result is CompletionContext.ModuleSuggestion)
        assertEquals("", (result as CompletionContext.ModuleSuggestion).prefix)
    }

    @Test
    fun `test module suggestion - with space after opening tag`() {
        val result = CompletionContextAnalyzer.analyzeContext("<% ")
        assertTrue("Should suggest modules", result is CompletionContext.ModuleSuggestion)
        assertEquals("", (result as CompletionContext.ModuleSuggestion).prefix)
    }

    @Test
    fun `test module suggestion - partial t`() {
        val result = CompletionContextAnalyzer.analyzeContext("<% t")
        assertTrue("Should suggest modules", result is CompletionContext.ModuleSuggestion)
        assertEquals("t", (result as CompletionContext.ModuleSuggestion).prefix)
    }

    @Test
    fun `test module suggestion - partial tp`() {
        val result = CompletionContextAnalyzer.analyzeContext("<% tp")
        assertTrue("Should suggest modules", result is CompletionContext.ModuleSuggestion)
        assertEquals("tp", (result as CompletionContext.ModuleSuggestion).prefix)
    }

    @Test
    fun `test module suggestion - after tp dot`() {
        val result = CompletionContextAnalyzer.analyzeContext("<% tp.")
        assertTrue("Should suggest modules", result is CompletionContext.ModuleSuggestion)
        assertEquals("", (result as CompletionContext.ModuleSuggestion).prefix)
    }

    @Test
    fun `test module suggestion - partial module name`() {
        val result = CompletionContextAnalyzer.analyzeContext("<% tp.da")
        assertTrue("Should suggest modules", result is CompletionContext.ModuleSuggestion)
        assertEquals("da", (result as CompletionContext.ModuleSuggestion).prefix)
    }

    @Test
    fun `test function suggestion - date module`() {
        val result = CompletionContextAnalyzer.analyzeContext("<% tp.date.")
        assertTrue("Should suggest functions", result is CompletionContext.FunctionSuggestion)
        val suggestion = result as CompletionContext.FunctionSuggestion
        assertEquals("date", suggestion.module)
        assertEquals("", suggestion.partial)
    }

    @Test
    fun `test function suggestion - partial function name`() {
        val result = CompletionContextAnalyzer.analyzeContext("<% tp.date.no")
        assertTrue("Should suggest functions", result is CompletionContext.FunctionSuggestion)
        val suggestion = result as CompletionContext.FunctionSuggestion
        assertEquals("date", suggestion.module)
        assertEquals("no", suggestion.partial)
    }

    @Test
    fun `test function suggestion - system module`() {
        val result = CompletionContextAnalyzer.analyzeContext("<% tp.system.pro")
        assertTrue("Should suggest functions", result is CompletionContext.FunctionSuggestion)
        val suggestion = result as CompletionContext.FunctionSuggestion
        assertEquals("system", suggestion.module)
        assertEquals("pro", suggestion.partial)
    }

    @Test
    fun `test execution syntax - star`() {
        val result = CompletionContextAnalyzer.analyzeContext("<%* tp.date.")
        assertTrue("Should suggest functions with execution syntax", result is CompletionContext.FunctionSuggestion)
        val suggestion = result as CompletionContext.FunctionSuggestion
        assertEquals("date", suggestion.module)
    }

    @Test
    fun `test whitespace control syntax - dash`() {
        val result = CompletionContextAnalyzer.analyzeContext("<%- tp.file.")
        assertTrue("Should suggest functions with whitespace control", result is CompletionContext.FunctionSuggestion)
        val suggestion = result as CompletionContext.FunctionSuggestion
        assertEquals("file", suggestion.module)
    }

    @Test
    fun `test module matching - date prefix`() {
        val modules = CompletionContextAnalyzer.getMatchingModules("da")
        assertEquals(1, modules.size)
        assertEquals("date", modules[0].name)
    }

    @Test
    fun `test module matching - empty prefix returns all`() {
        val modules = CompletionContextAnalyzer.getMatchingModules("")
        assertEquals(8, modules.size)
    }

    @Test
    fun `test module matching - case insensitive`() {
        val modules = CompletionContextAnalyzer.getMatchingModules("DA")
        assertEquals(1, modules.size)
        assertEquals("date", modules[0].name)
    }

    @Test
    fun `test static functions - hooks module`() {
        val functions = CompletionContextAnalyzer.getStaticFunctions("hooks", "")
        assertEquals(1, functions.size)
        assertEquals("on_all_templates_executed", functions[0].name)
    }

    @Test
    fun `test static functions - config module`() {
        val functions = CompletionContextAnalyzer.getStaticFunctions("config", "")
        assertEquals(4, functions.size)
        assertTrue(functions.any { it.name == "active_file" })
        assertTrue(functions.any { it.name == "run_mode" })
    }

    @Test
    fun `test static functions - app module`() {
        val functions = CompletionContextAnalyzer.getStaticFunctions("app", "")
        assertEquals(4, functions.size)
        assertTrue(functions.any { it.name == "vault" })
        assertTrue(functions.any { it.name == "workspace" })
    }

    @Test
    fun `test static functions - partial match`() {
        val functions = CompletionContextAnalyzer.getStaticFunctions("config", "act")
        assertEquals(1, functions.size)
        assertEquals("active_file", functions[0].name)
    }

    @Test
    fun `test static functions - unknown module returns empty`() {
        val functions = CompletionContextAnalyzer.getStaticFunctions("unknown", "")
        assertTrue(functions.isEmpty())
    }

    @Test
    fun `test nested template blocks - uses innermost`() {

        val result = CompletionContextAnalyzer.analyzeContext("<% tp.date.now() %> text <% tp.file.")
        assertTrue("Should suggest functions for innermost block", result is CompletionContext.FunctionSuggestion)
        val suggestion = result as CompletionContext.FunctionSuggestion
        assertEquals("file", suggestion.module)
    }
}

