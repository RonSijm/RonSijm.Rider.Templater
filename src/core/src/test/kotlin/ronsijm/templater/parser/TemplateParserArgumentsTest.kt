package ronsijm.templater.parser

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.utils.ArgumentParser


class TemplateParserArgumentsTest {


    private fun parseArguments(argsString: String): List<Any?> {
        return ArgumentParser.parseArgumentString(argsString)
    }

    @Test
    fun `test parse simple string arguments`() {
        val result = parseArguments(""""Hello", "World"""")
        assertEquals(2, result.size)
        assertEquals("Hello", result[0])
        assertEquals("World", result[1])
    }

    @Test
    fun `test parse array arguments`() {
        val result = parseArguments("""["Option 1", "Option 2", "Option 3"], ["opt1", "opt2", "opt3"]""")
        assertEquals(2, result.size)

        assertTrue(result[0] is List<*>)
        val firstArray = result[0] as List<*>
        assertEquals(3, firstArray.size)
        assertEquals("Option 1", firstArray[0])
        assertEquals("Option 2", firstArray[1])
        assertEquals("Option 3", firstArray[2])

        assertTrue(result[1] is List<*>)
        val secondArray = result[1] as List<*>
        assertEquals(3, secondArray.size)
        assertEquals("opt1", secondArray[0])
        assertEquals("opt2", secondArray[1])
        assertEquals("opt3", secondArray[2])
    }

    @Test
    fun `test parse mixed types`() {
        val result = parseArguments("""123, "text", true, false, null""")
        assertEquals(5, result.size)
        assertEquals(123, result[0])
        assertEquals("text", result[1])
        assertEquals(true, result[2])
        assertEquals(false, result[3])
        assertNull(result[4])
    }

    @Test
    fun `test parse number array`() {
        val result = parseArguments("""[1, 2, 3], ["a", "b", "c"]""")
        assertEquals(2, result.size)

        assertTrue(result[0] is List<*>)
        val numberArray = result[0] as List<*>
        assertEquals(3, numberArray.size)
        assertEquals(1, numberArray[0])
        assertEquals(2, numberArray[1])
        assertEquals(3, numberArray[2])
    }

    @Test
    fun `test parse empty array`() {
        val result = parseArguments("""[], ["a"]""")
        assertEquals(2, result.size)

        assertTrue(result[0] is List<*>)
        val emptyArray = result[0] as List<*>
        assertEquals(0, emptyArray.size)

        assertTrue(result[1] is List<*>)
        val singleArray = result[1] as List<*>
        assertEquals(1, singleArray.size)
        assertEquals("a", singleArray[0])
    }

    @Test
    fun `test parse single quotes`() {
        val result = parseArguments("""'Hello', 'World'""")
        assertEquals(2, result.size)
        assertEquals("Hello", result[0])
        assertEquals("World", result[1])
    }

    @Test
    fun `test parse double values`() {
        val result = parseArguments("""3.14, 2.718""")
        assertEquals(2, result.size)
        assertEquals(3.14, result[0])
        assertEquals(2.718, result[1])
    }

    @Test
    fun `test parse empty string`() {
        val result = parseArguments("")
        assertEquals(0, result.size)
    }

    @Test
    fun `test parse nested arrays`() {
        val result = parseArguments("""[[1, 2], [3, 4]]""")
        assertEquals(1, result.size)

        assertTrue(result[0] is List<*>)
        val outerArray = result[0] as List<*>
        assertEquals(2, outerArray.size)

        assertTrue(outerArray[0] is List<*>)
        val firstInner = outerArray[0] as List<*>
        assertEquals(2, firstInner.size)
        assertEquals(1, firstInner[0])
        assertEquals(2, firstInner[1])

        assertTrue(outerArray[1] is List<*>)
        val secondInner = outerArray[1] as List<*>
        assertEquals(2, secondInner.size)
        assertEquals(3, secondInner[0])
        assertEquals(4, secondInner[1])
    }
}
