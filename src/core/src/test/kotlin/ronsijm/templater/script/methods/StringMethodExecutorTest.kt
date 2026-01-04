package ronsijm.templater.script.methods

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class StringMethodExecutorTest {


    @Test
    fun `test split with delimiter`() {
        val result = StringMethodExecutor.execute("a,b,c", "split", listOf(","))
        assertEquals(listOf("a", "b", "c"), result)
    }

    @Test
    fun `test split with empty delimiter`() {
        val result = StringMethodExecutor.execute("abc", "split", listOf(""))
        assertEquals(listOf("a", "b", "c"), result)
    }

    @Test
    fun `test split with no args uses empty delimiter`() {
        val result = StringMethodExecutor.execute("abc", "split", emptyList())
        assertEquals(listOf("a", "b", "c"), result)
    }


    @Test
    fun `test trim removes whitespace`() {
        val result = StringMethodExecutor.execute("  hello  ", "trim", emptyList())
        assertEquals("hello", result)
    }

    @Test
    fun `test trimStart removes leading whitespace`() {
        val result = StringMethodExecutor.execute("  hello  ", "trimStart", emptyList())
        assertEquals("hello  ", result)
    }

    @Test
    fun `test trimLeft is alias for trimStart`() {
        val result = StringMethodExecutor.execute("  hello  ", "trimLeft", emptyList())
        assertEquals("hello  ", result)
    }

    @Test
    fun `test trimEnd removes trailing whitespace`() {
        val result = StringMethodExecutor.execute("  hello  ", "trimEnd", emptyList())
        assertEquals("  hello", result)
    }

    @Test
    fun `test trimRight is alias for trimEnd`() {
        val result = StringMethodExecutor.execute("  hello  ", "trimRight", emptyList())
        assertEquals("  hello", result)
    }


    @Test
    fun `test toLowerCase`() {
        val result = StringMethodExecutor.execute("HELLO World", "toLowerCase", emptyList())
        assertEquals("hello world", result)
    }

    @Test
    fun `test toLocaleLowerCase is alias for toLowerCase`() {
        val result = StringMethodExecutor.execute("HELLO", "toLocaleLowerCase", emptyList())
        assertEquals("hello", result)
    }

    @Test
    fun `test toUpperCase`() {
        val result = StringMethodExecutor.execute("hello World", "toUpperCase", emptyList())
        assertEquals("HELLO WORLD", result)
    }

    @Test
    fun `test toLocaleUpperCase is alias for toUpperCase`() {
        val result = StringMethodExecutor.execute("hello", "toLocaleUpperCase", emptyList())
        assertEquals("HELLO", result)
    }


    @Test
    fun `test substring with start and end`() {
        val result = StringMethodExecutor.execute("hello world", "substring", listOf(0, 5))
        assertEquals("hello", result)
    }

    @Test
    fun `test substring with only start`() {
        val result = StringMethodExecutor.execute("hello world", "substring", listOf(6))
        assertEquals("world", result)
    }

    @Test
    fun `test substring with out of bounds indices`() {
        val result = StringMethodExecutor.execute("hello", "substring", listOf(-5, 100))
        assertEquals("hello", result)
    }


    @Test
    fun `test slice with positive indices`() {
        val result = StringMethodExecutor.execute("hello world", "slice", listOf(0, 5))
        assertEquals("hello", result)
    }

    @Test
    fun `test slice with negative start index`() {
        val result = StringMethodExecutor.execute("hello world", "slice", listOf(-5))
        assertEquals("world", result)
    }

    @Test
    fun `test slice with negative end index`() {
        val result = StringMethodExecutor.execute("hello world", "slice", listOf(0, -6))
        assertEquals("hello", result)
    }


    @Test
    fun `test charAt returns character at index`() {
        val result = StringMethodExecutor.execute("hello", "charAt", listOf(1))
        assertEquals("e", result)
    }

    @Test
    fun `test charAt with out of bounds returns empty string`() {
        val result = StringMethodExecutor.execute("hello", "charAt", listOf(10))
        assertEquals("", result)
    }


    @Test
    fun `test indexOf finds substring`() {
        val result = StringMethodExecutor.execute("hello world", "indexOf", listOf("world"))
        assertEquals(6, result)
    }

    @Test
    fun `test indexOf returns -1 when not found`() {
        val result = StringMethodExecutor.execute("hello", "indexOf", listOf("xyz"))
        assertEquals(-1, result)
    }


    @Test
    fun `test lastIndexOf finds last occurrence`() {
        val result = StringMethodExecutor.execute("hello hello", "lastIndexOf", listOf("hello"))
        assertEquals(6, result)
    }

    @Test
    fun `test lastIndexOf returns -1 when not found`() {
        val result = StringMethodExecutor.execute("hello", "lastIndexOf", listOf("xyz"))
        assertEquals(-1, result)
    }


    @Test
    fun `test includes returns true when substring exists`() {
        val result = StringMethodExecutor.execute("hello world", "includes", listOf("world"))
        assertEquals(true, result)
    }

    @Test
    fun `test includes returns false when substring not found`() {
        val result = StringMethodExecutor.execute("hello", "includes", listOf("xyz"))
        assertEquals(false, result)
    }

    @Test
    fun `test contains is alias for includes`() {
        val result = StringMethodExecutor.execute("hello world", "contains", listOf("world"))
        assertEquals(true, result)
    }


    @Test
    fun `test startsWith returns true for matching prefix`() {
        val result = StringMethodExecutor.execute("hello world", "startsWith", listOf("hello"))
        assertEquals(true, result)
    }

    @Test
    fun `test startsWith returns false for non-matching prefix`() {
        val result = StringMethodExecutor.execute("hello world", "startsWith", listOf("world"))
        assertEquals(false, result)
    }

    @Test
    fun `test endsWith returns true for matching suffix`() {
        val result = StringMethodExecutor.execute("hello world", "endsWith", listOf("world"))
        assertEquals(true, result)
    }

    @Test
    fun `test endsWith returns false for non-matching suffix`() {
        val result = StringMethodExecutor.execute("hello world", "endsWith", listOf("hello"))
        assertEquals(false, result)
    }


    @Test
    fun `test replace replaces first occurrence only`() {
        val result = StringMethodExecutor.execute("hello hello", "replace", listOf("hello", "hi"))
        assertEquals("hi hello", result)
    }

    @Test
    fun `test replaceAll replaces all occurrences`() {
        val result = StringMethodExecutor.execute("hello hello", "replaceAll", listOf("hello", "hi"))
        assertEquals("hi hi", result)
    }


    @Test
    fun `test padStart pads to target length`() {
        val result = StringMethodExecutor.execute("5", "padStart", listOf(3, "0"))
        assertEquals("005", result)
    }

    @Test
    fun `test padStart with default pad character`() {
        val result = StringMethodExecutor.execute("hi", "padStart", listOf(5))
        assertEquals("   hi", result)
    }

    @Test
    fun `test padEnd pads to target length`() {
        val result = StringMethodExecutor.execute("5", "padEnd", listOf(3, "0"))
        assertEquals("500", result)
    }


    @Test
    fun `test repeat repeats string n times`() {
        val result = StringMethodExecutor.execute("ab", "repeat", listOf(3))
        assertEquals("ababab", result)
    }

    @Test
    fun `test repeat with zero returns empty string`() {
        val result = StringMethodExecutor.execute("ab", "repeat", listOf(0))
        assertEquals("", result)
    }


    @Test
    fun `test length returns string length`() {
        val result = StringMethodExecutor.execute("hello", "length", emptyList())
        assertEquals(5, result)
    }


    @Test
    fun `test unsupported method returns null`() {
        val result = StringMethodExecutor.execute("hello", "unknownMethod", emptyList())
        assertNull(result)
    }


    @Test
    fun `test isSupported returns true for supported methods`() {
        assertTrue(StringMethodExecutor.isSupported("split"))
        assertTrue(StringMethodExecutor.isSupported("trim"))
        assertTrue(StringMethodExecutor.isSupported("toLowerCase"))
        assertTrue(StringMethodExecutor.isSupported("includes"))
    }

    @Test
    fun `test isSupported returns false for unsupported methods`() {
        assertFalse(StringMethodExecutor.isSupported("unknownMethod"))
        assertFalse(StringMethodExecutor.isSupported("foo"))
    }
}
