package ronsijm.templater.script

import ronsijm.templater.TestContextFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*


class ScriptEngineVariableUpdaterTest {

    @Test
    fun `test variable updater with unquoted string treats as literal`() {
        val engine = TestContextFactory.createScriptEngine()
        engine.execute("let userName = \"Developer\"")

        val updater = engine.createVariableUpdater()



        val success = updater.updateVariable("userName", "Ron")

        assertTrue(success, "Update should succeed")
        assertEquals("Ron", engine.getVariable("userName"), "Variable should be 'Ron', not null")
    }

    @Test
    fun `test variable updater with quoted string evaluates normally`() {
        val engine = TestContextFactory.createScriptEngine()
        engine.execute("let name = \"old\"")

        val updater = engine.createVariableUpdater()
        updater.updateVariable("name", "\"new value\"")

        assertEquals("new value", engine.getVariable("name"))
    }

    @Test
    fun `test variable updater with number evaluates normally`() {
        val engine = TestContextFactory.createScriptEngine()
        engine.execute("let count = 0")

        val updater = engine.createVariableUpdater()
        updater.updateVariable("count", "42")

        assertEquals(42, engine.getVariable("count"))
    }

    @Test
    fun `test variable updater with boolean evaluates normally`() {
        val engine = TestContextFactory.createScriptEngine()
        engine.execute("let flag = false")

        val updater = engine.createVariableUpdater()
        updater.updateVariable("flag", "true")

        assertEquals(true, engine.getVariable("flag"))
    }

    @Test
    fun `test variable updater with null evaluates normally`() {
        val engine = TestContextFactory.createScriptEngine()
        engine.execute("let value = \"something\"")

        val updater = engine.createVariableUpdater()
        updater.updateVariable("value", "null")

        assertNull(engine.getVariable("value"))
    }

    @Test
    fun `test variable updater with expression evaluates normally`() {
        val engine = TestContextFactory.createScriptEngine()
        engine.execute("let x = 5")
        engine.execute("let result = 0")

        val updater = engine.createVariableUpdater()
        updater.updateVariable("result", "x + 10")

        assertEquals(15, engine.getVariable("result"))
    }

    @Test
    fun `test variable updater with array evaluates normally`() {
        val engine = TestContextFactory.createScriptEngine()
        engine.execute("let items = []")

        val updater = engine.createVariableUpdater()
        updater.updateVariable("items", "[1, 2, 3]")

        val items = engine.getVariable("items") as List<*>
        assertEquals(3, items.size)
        assertEquals(1, items[0])
    }

    @Test
    fun `test variable updater with unquoted text containing spaces`() {
        val engine = TestContextFactory.createScriptEngine()
        engine.execute("let message = \"old\"")

        val updater = engine.createVariableUpdater()

        updater.updateVariable("message", "Hello World")

        assertEquals("Hello World", engine.getVariable("message"))
    }
}

