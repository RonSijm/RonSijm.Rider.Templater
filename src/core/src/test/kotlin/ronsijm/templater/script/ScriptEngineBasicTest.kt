package ronsijm.templater.script

import ronsijm.templater.TestContextFactory
import ronsijm.templater.services.ServiceContainer
import ronsijm.templater.services.mock.MockSystemOperationsService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*


class ScriptEngineBasicTest {

    @Test
    fun `test simple variable assignment`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("let x = \"hello\"")

        assertEquals("hello", engine.getVariable("x"))
    }

    @Test
    fun `test variable assignment without let`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("x = \"world\"")

        assertEquals("world", engine.getVariable("x"))
    }

    @Test
    fun `test string concatenation`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("let greeting = \"Hello\" + \" \" + \"World\"")

        assertEquals("Hello World", engine.getVariable("greeting"))
    }

    @Test
    fun `test string concatenation with variables`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            let first = "Hello"
            let second = "World"
            let combined = first + " " + second
        """.trimIndent())

        assertEquals("Hello", engine.getVariable("first"))
        assertEquals("World", engine.getVariable("second"))
        assertEquals("Hello World", engine.getVariable("combined"))
    }

    @Test
    fun `test function call - date now`() {
        val engine = TestContextFactory.createRealScriptEngine()

        engine.execute("let today = tp.date.now(\"YYYY-MM-DD\")")

        val expected = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        assertEquals(expected, engine.getVariable("today"))
    }

    @Test
    fun `test function call - date tomorrow`() {
        val engine = TestContextFactory.createRealScriptEngine()

        engine.execute("let tomorrow = tp.date.tomorrow(\"YYYY-MM-DD\")")

        val expected = java.time.LocalDate.now().plusDays(1).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        assertEquals(expected, engine.getVariable("tomorrow"))
    }

    @Test
    fun `test string concatenation with function call`() {
        val engine = TestContextFactory.createRealScriptEngine()

        engine.execute("let fileName = \"note-\" + tp.date.now(\"YYYY-MM-DD\")")

        val expected = "note-" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        assertEquals(expected, engine.getVariable("fileName"))
    }

    @Test
    fun `test await keyword is stripped`() {
        val engine = TestContextFactory.createRealScriptEngine()

        engine.execute("let result = await tp.date.now(\"YYYY-MM-DD\")")

        val expected = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        assertEquals(expected, engine.getVariable("result"))
    }

    @Test
    fun `test frontmatter access`() {
        val engine = TestContextFactory.createRealScriptEngine(
            frontmatter = mapOf("title" to "Test Document")
        )

        engine.execute("let docTitle = tp.frontmatter.title")

        assertEquals("Test Document", engine.getVariable("docTitle"))
    }

    @Test
    fun `test multiple statements`() {
        val engine = TestContextFactory.createRealScriptEngine()

        engine.execute("""
            let a = "Hello"
            let b = "World"
            let c = a + " " + b
            let d = c + " " + tp.date.now("YYYY-MM-DD")
        """.trimIndent())

        assertEquals("Hello", engine.getVariable("a"))
        assertEquals("World", engine.getVariable("b"))
        assertEquals("Hello World", engine.getVariable("c"))

        val expected = "Hello World " + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        assertEquals(expected, engine.getVariable("d"))
    }

    @Test
    fun `test system prompt callback`() {
        val mockSystemOps = MockSystemOperationsService(promptResponse = "User Input")
        val services = ServiceContainer.createForTesting(systemOperationsService = mockSystemOps)
        val engine = TestContextFactory.createRealScriptEngine(services = services)

        engine.execute("let userInput = tp.system.prompt(\"Enter name\")")

        assertEquals(1, mockSystemOps.promptCalls.size)
        assertEquals("Enter name", mockSystemOps.promptCalls[0].promptText)
        assertEquals("User Input", engine.getVariable("userInput"))
    }

    @Test
    fun `test const and var keywords`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            const PI = 3.14159
            var counter = 0
            let name = "test"
        """.trimIndent())

        assertEquals(3.14159, engine.getVariable("PI"))
        assertEquals(0, engine.getVariable("counter"))
        assertEquals("test", engine.getVariable("name"))
    }
}

