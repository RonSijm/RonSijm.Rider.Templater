package ronsijm.templater.script

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.TestContextFactory

class ArrayFromTest {

    @Test
    fun `test Array from with length and map function`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            const arr = Array.from({ length: 5 }, (_, i) => i * 2)
        """.trimIndent())

        val arr = engine.getVariable("arr") as? List<*>
        assertEquals(listOf(0, 2, 4, 6, 8), arr)
    }

    @Test
    fun `test Array from with length only`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            const arr = Array.from({ length: 3 })
        """.trimIndent())

        val arr = engine.getVariable("arr") as? List<*>
        assertEquals(listOf(null, null, null), arr)
    }

    @Test
    fun `test Array from with existing array`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            const original = [1, 2, 3]
            const copy = Array.from(original)
        """.trimIndent())

        val copy = engine.getVariable("copy") as? List<*>
        assertEquals(listOf(1, 2, 3), copy)
    }

    @Test
    fun `test Array from with existing array and map function`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            const original = [1, 2, 3]
            const doubled = Array.from(original, (x) => x * 2)
        """.trimIndent())

        val doubled = engine.getVariable("doubled") as? List<*>
        assertEquals(listOf(2, 4, 6), doubled)
    }

    @Test
    fun `test Array from with object creation pattern`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            const items = Array.from({ length: 3 }, (_, i) => ({ id: i, name: 'item' + i }))
        """.trimIndent())

        val items = engine.getVariable("items") as? List<*>
        assertEquals(3, items?.size)

        val firstItem = items?.get(0) as? Map<*, *>
        assertEquals(0, firstItem?.get("id"))
        assertEquals("item0", firstItem?.get("name"))
    }
}
