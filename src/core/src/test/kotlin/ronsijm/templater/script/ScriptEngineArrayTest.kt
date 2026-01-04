package ronsijm.templater.script

import ronsijm.templater.TestContextFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*


class ScriptEngineArrayTest {

    @Test
    fun `test array reduce`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            let arr = [1, 2, 3, 4]
            let sum = arr.reduce((a, b) => a + b, 0)
        """.trimIndent())

        val sum = engine.getVariable("sum")
        assertEquals(10, (sum as Number).toInt())
    }

    @Test
    fun `test array element assignment`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            let arr = [1, 2, 3]
            arr[1] = 10
        """.trimIndent())

        val arr = engine.getVariable("arr") as List<*>
        assertEquals(10, arr[1])
    }

    @Test
    fun `test new Array with fill`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            let arr = new Array(5).fill(2)
        """.trimIndent())

        val arr = engine.getVariable("arr")
        assertNotNull(arr, "arr should have a value")
        assertTrue(arr is List<*>, "arr should be a list")
        assertEquals(5, (arr as List<*>).size, "arr should have 5 elements")
        assertEquals(listOf(2, 2, 2, 2, 2), arr, "arr should be filled with 2s")
    }

    @Test
    fun `test array literal`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            let arr = [1, 2, 3, 4, 5]
        """.trimIndent())

        val arr = engine.getVariable("arr") as List<*>
        assertEquals(5, arr.size)
        assertEquals(1, arr[0])
        assertEquals(5, arr[4])
    }

    @Test
    fun `test array push`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            let arr = [1, 2]
            arr.push(3)
        """.trimIndent())

        val arr = engine.getVariable("arr") as List<*>
        assertEquals(3, arr.size)
        assertEquals(3, arr[2])
    }

    @Test
    fun `test array length`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            let arr = [1, 2, 3, 4, 5]
            let len = arr.length
        """.trimIndent())

        assertEquals(5, engine.getVariable("len"))
    }
}

