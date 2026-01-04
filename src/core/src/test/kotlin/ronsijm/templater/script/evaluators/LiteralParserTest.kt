package ronsijm.templater.script.evaluators

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LiteralParserTest {


    private lateinit var parser: LiteralParser

    private val recursiveEvaluator: (String) -> Any? = { expr ->
        val trimmed = expr.trim()
        when {
            trimmed.startsWith("\"") && trimmed.endsWith("\"") -> trimmed.substring(1, trimmed.length - 1)
            trimmed.startsWith("'") && trimmed.endsWith("'") -> trimmed.substring(1, trimmed.length - 1)
            trimmed.startsWith("{") && trimmed.endsWith("}") -> parser.parseObjectLiteral(trimmed)
            trimmed.startsWith("[") && trimmed.endsWith("]") -> parser.parseArrayLiteral(trimmed)
            trimmed == "true" -> true
            trimmed == "false" -> false
            trimmed == "null" -> null
            else -> trimmed.toIntOrNull() ?: trimmed.toDoubleOrNull() ?: trimmed
        }
    }

    init {
        parser = LiteralParser(recursiveEvaluator)
    }


    @Test
    fun `parseArrayLiteral parses empty array`() {
        val result = parser.parseArrayLiteral("[]")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseArrayLiteral parses number array`() {
        val result = parser.parseArrayLiteral("[1, 2, 3]")
        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun `parseArrayLiteral parses string array`() {
        val result = parser.parseArrayLiteral("[\"a\", \"b\", \"c\"]")
        assertEquals(listOf("a", "b", "c"), result)
    }

    @Test
    fun `parseArrayLiteral parses mixed array`() {
        val result = parser.parseArrayLiteral("[1, \"two\", true]")
        assertEquals(listOf(1, "two", true), result)
    }

    @Test
    fun `parseArrayLiteral parses nested array`() {
        val result = parser.parseArrayLiteral("[[1, 2], [3, 4]]")
        assertEquals(2, result.size)
    }

    @Test
    fun `parseArrayLiteral handles single quoted strings`() {
        val result = parser.parseArrayLiteral("['a', 'b']")
        assertEquals(listOf("a", "b"), result)
    }


    @Test
    fun `parseObjectLiteral parses empty object`() {
        val result = parser.parseObjectLiteral("{}")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseObjectLiteral parses simple object`() {
        val result = parser.parseObjectLiteral("{name: \"John\", age: 30}")
        assertEquals("John", result["name"])
        assertEquals(30, result["age"])
    }

    @Test
    fun `parseObjectLiteral parses object with quoted keys`() {
        val result = parser.parseObjectLiteral("{\"name\": \"John\"}")
        assertEquals("John", result["name"])
    }

    @Test
    fun `parseObjectLiteral parses object with single quoted keys`() {
        val result = parser.parseObjectLiteral("{'name': 'John'}")
        assertEquals("John", result["name"])
    }

    @Test
    fun `parseObjectLiteral parses nested object`() {
        val result = parser.parseObjectLiteral("{outer: {inner: 1}}")
        assertEquals(1, result.size)
        assertTrue(result.containsKey("outer"))
        val innerObj = result["outer"]
        assertTrue(innerObj is Map<*, *>)
        assertEquals(1, (innerObj as Map<*, *>)["inner"])
    }


    @Test
    fun `parseStringLiteral parses double quoted string`() {
        val result = parser.parseStringLiteral("\"hello world\"")
        assertEquals("hello world", result)
    }

    @Test
    fun `parseStringLiteral parses single quoted string`() {
        val result = parser.parseStringLiteral("'hello world'")
        assertEquals("hello world", result)
    }

    @Test
    fun `parseStringLiteral parses empty string`() {
        val result = parser.parseStringLiteral("\"\"")
        assertEquals("", result)
    }


    @Test
    fun `parseNumber parses integer`() {
        val result = parser.parseNumber("42")
        assertEquals(42, result)
    }

    @Test
    fun `parseNumber parses negative integer`() {
        val result = parser.parseNumber("-42")
        assertEquals(-42, result)
    }

    @Test
    fun `parseNumber parses double`() {
        val result = parser.parseNumber("3.14")
        assertEquals(3.14, result)
    }

    @Test
    fun `parseNumber returns null for non-number`() {
        val result = parser.parseNumber("abc")
        assertNull(result)
    }


    @Test
    fun `toNumber converts Int`() {
        val result = parser.toNumber(42)
        assertEquals(42, result)
    }

    @Test
    fun `toNumber converts Double`() {
        val result = parser.toNumber(3.14)
        assertEquals(3.14, result)
    }

    @Test
    fun `toNumber converts String to Int`() {
        val result = parser.toNumber("42")
        assertEquals(42, result)
    }

    @Test
    fun `toNumber converts String to Double`() {
        val result = parser.toNumber("3.14")
        assertEquals(3.14, result)
    }

    @Test
    fun `toNumber returns null for non-convertible`() {
        val result = parser.toNumber("abc")
        assertNull(result)
    }


    @Test
    fun `isArrayLiteral returns true for array`() {
        assertTrue(LiteralParser.isArrayLiteral("[1, 2, 3]"))
    }

    @Test
    fun `isArrayLiteral returns false for non-array`() {
        assertFalse(LiteralParser.isArrayLiteral("{a: 1}"))
    }

    @Test
    fun `isObjectLiteral returns true for object`() {
        assertTrue(LiteralParser.isObjectLiteral("{a: 1}"))
    }

    @Test
    fun `isDoubleQuotedString returns true for double quoted`() {
        assertTrue(LiteralParser.isDoubleQuotedString("\"hello\""))
    }

    @Test
    fun `isSingleQuotedString returns true for single quoted`() {
        assertTrue(LiteralParser.isSingleQuotedString("'hello'"))
    }

    @Test
    fun `isTemplateLiteral returns true for template literal`() {
        assertTrue(LiteralParser.isTemplateLiteral("`hello`"))
    }
}
