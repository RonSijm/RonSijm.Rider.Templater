package ronsijm.templater.script.methods

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ronsijm.templater.script.ArrowFunction

class ArrayMethodExecutorTest {


    private val simpleArrowExecutor: ArrowFunctionExecutor = { arrow, args ->
        val param = arrow.parameters.firstOrNull() ?: "x"
        val value = args.firstOrNull()

        when {
            arrow.body.contains(">") -> {
                val parts = arrow.body.split(">").map { it.trim() }
                val threshold = parts[1].toIntOrNull() ?: 0
                (value as? Number)?.toInt()?.let { it > threshold } ?: false
            }
            arrow.body.contains("* 2") -> {
                (value as? Number)?.toInt()?.times(2)
            }
            arrow.body.contains("+ 1") -> {
                (value as? Number)?.toInt()?.plus(1)
            }
            arrow.body == "true" -> true
            arrow.body == "false" -> false
            arrow.body.contains("-") && args.size >= 2 -> {

                val a = (args[0] as? Number)?.toInt() ?: 0
                val b = (args[1] as? Number)?.toInt() ?: 0
                a - b
            }
            else -> value
        }
    }


    @Test
    fun `length returns list size`() {
        val result = ArrayMethodExecutor.execute(listOf(1, 2, 3), "length", emptyList(), simpleArrowExecutor)
        assertEquals(3, result)
    }

    @Test
    fun `length returns 0 for empty list`() {
        val result = ArrayMethodExecutor.execute(emptyList<Any>(), "length", emptyList(), simpleArrowExecutor)
        assertEquals(0, result)
    }


    @Test
    fun `join with default separator`() {
        val result = ArrayMethodExecutor.execute(listOf("a", "b", "c"), "join", emptyList(), simpleArrowExecutor)
        assertEquals("a,b,c", result)
    }

    @Test
    fun `join with custom separator`() {
        val result = ArrayMethodExecutor.execute(listOf("a", "b", "c"), "join", listOf(" - "), simpleArrowExecutor)
        assertEquals("a - b - c", result)
    }

    @Test
    fun `join handles null values`() {
        val result = ArrayMethodExecutor.execute(listOf("a", null, "c"), "join", listOf(","), simpleArrowExecutor)
        assertEquals("a,,c", result)
    }


    @Test
    fun `includes finds existing element`() {
        val result = ArrayMethodExecutor.execute(listOf(1, 2, 3), "includes", listOf(2), simpleArrowExecutor)
        assertEquals(true, result)
    }

    @Test
    fun `includes returns false for missing element`() {
        val result = ArrayMethodExecutor.execute(listOf(1, 2, 3), "includes", listOf(5), simpleArrowExecutor)
        assertEquals(false, result)
    }

    @Test
    fun `contains is alias for includes`() {
        val result = ArrayMethodExecutor.execute(listOf("a", "b"), "contains", listOf("b"), simpleArrowExecutor)
        assertEquals(true, result)
    }


    @Test
    fun `indexOf finds element index`() {
        val result = ArrayMethodExecutor.execute(listOf("a", "b", "c"), "indexOf", listOf("b"), simpleArrowExecutor)
        assertEquals(1, result)
    }

    @Test
    fun `indexOf returns -1 for missing element`() {
        val result = ArrayMethodExecutor.execute(listOf("a", "b", "c"), "indexOf", listOf("z"), simpleArrowExecutor)
        assertEquals(-1, result)
    }


    @Test
    fun `slice with start only`() {
        val result = ArrayMethodExecutor.execute(listOf(1, 2, 3, 4, 5), "slice", listOf(2), simpleArrowExecutor)
        assertEquals(listOf(3, 4, 5), result)
    }

    @Test
    fun `slice with start and end`() {
        val result = ArrayMethodExecutor.execute(listOf(1, 2, 3, 4, 5), "slice", listOf(1, 4), simpleArrowExecutor)
        assertEquals(listOf(2, 3, 4), result)
    }

    @Test
    fun `slice with negative start`() {
        val result = ArrayMethodExecutor.execute(listOf(1, 2, 3, 4, 5), "slice", listOf(-2), simpleArrowExecutor)
        assertEquals(listOf(4, 5), result)
    }


    @Test
    fun `reverse returns reversed list`() {
        val result = ArrayMethodExecutor.execute(listOf(1, 2, 3), "reverse", emptyList(), simpleArrowExecutor)
        assertEquals(listOf(3, 2, 1), result)
    }


    @Test
    fun `filter with arrow function`() {
        val arrow = ArrowFunction(listOf("x"), "x > 2", true)
        val result = ArrayMethodExecutor.execute(listOf(1, 2, 3, 4, 5), "filter", listOf(arrow), simpleArrowExecutor)
        assertEquals(listOf(3, 4, 5), result)
    }

    @Test
    fun `filter without callback filters null and empty`() {
        val result = ArrayMethodExecutor.execute(listOf("a", null, "", "b"), "filter", emptyList(), simpleArrowExecutor)
        assertEquals(listOf("a", "b"), result)
    }


    @Test
    fun `map with arrow function`() {
        val arrow = ArrowFunction(listOf("x"), "x * 2", true)
        val result = ArrayMethodExecutor.execute(listOf(1, 2, 3), "map", listOf(arrow), simpleArrowExecutor)
        assertEquals(listOf(2, 4, 6), result)
    }

    @Test
    fun `map without callback returns original list`() {
        val result = ArrayMethodExecutor.execute(listOf(1, 2, 3), "map", emptyList(), simpleArrowExecutor)
        assertEquals(listOf(1, 2, 3), result)
    }


    @Test
    fun `find returns first matching element`() {
        val arrow = ArrowFunction(listOf("x"), "x > 2", true)
        val result = ArrayMethodExecutor.execute(listOf(1, 2, 3, 4), "find", listOf(arrow), simpleArrowExecutor)
        assertEquals(3, result)
    }

    @Test
    fun `find returns null when no match`() {
        val arrow = ArrowFunction(listOf("x"), "x > 10", true)
        val result = ArrayMethodExecutor.execute(listOf(1, 2, 3), "find", listOf(arrow), simpleArrowExecutor)
        assertNull(result)
    }


    @Test
    fun `some returns true when any element matches`() {
        val arrow = ArrowFunction(listOf("x"), "x > 2", true)
        val result = ArrayMethodExecutor.execute(listOf(1, 2, 3), "some", listOf(arrow), simpleArrowExecutor)
        assertEquals(true, result)
    }

    @Test
    fun `some returns false when no element matches`() {
        val arrow = ArrowFunction(listOf("x"), "x > 10", true)
        val result = ArrayMethodExecutor.execute(listOf(1, 2, 3), "some", listOf(arrow), simpleArrowExecutor)
        assertEquals(false, result)
    }

    @Test
    fun `some without callback returns true for non-empty list`() {
        val result = ArrayMethodExecutor.execute(listOf(1, 2), "some", emptyList(), simpleArrowExecutor)
        assertEquals(true, result)
    }


    @Test
    fun `every returns true when all elements match`() {
        val arrow = ArrowFunction(listOf("x"), "x > 0", true)
        val result = ArrayMethodExecutor.execute(listOf(1, 2, 3), "every", listOf(arrow), simpleArrowExecutor)
        assertEquals(true, result)
    }

    @Test
    fun `every returns false when some elements dont match`() {
        val arrow = ArrowFunction(listOf("x"), "x > 2", true)
        val result = ArrayMethodExecutor.execute(listOf(1, 2, 3), "every", listOf(arrow), simpleArrowExecutor)
        assertEquals(false, result)
    }


    @Test
    fun `forEach returns null`() {
        val arrow = ArrowFunction(listOf("x"), "x", true)
        val result = ArrayMethodExecutor.execute(listOf(1, 2, 3), "forEach", listOf(arrow), simpleArrowExecutor)
        assertNull(result)
    }


    @Test
    fun `push on mutable list returns new length`() {
        val list = mutableListOf(1, 2, 3)
        val result = ArrayMethodExecutor.execute(list, "push", listOf(4, 5), simpleArrowExecutor)
        assertEquals(5, result)
        assertEquals(listOf(1, 2, 3, 4, 5), list)
    }

    @Test
    fun `push on immutable list returns new list`() {
        val list = listOf(1, 2, 3)
        val result = ArrayMethodExecutor.execute(list, "push", listOf(4), simpleArrowExecutor)
        assertEquals(listOf(1, 2, 3, 4), result)
    }


    @Test
    fun `pop on mutable list removes and returns last element`() {
        val list = mutableListOf(1, 2, 3)
        val result = ArrayMethodExecutor.execute(list, "pop", emptyList(), simpleArrowExecutor)
        assertEquals(3, result)
        assertEquals(listOf(1, 2), list)
    }

    @Test
    fun `pop on immutable list returns last element`() {
        val result = ArrayMethodExecutor.execute(listOf(1, 2, 3), "pop", emptyList(), simpleArrowExecutor)
        assertEquals(3, result)
    }


    @Test
    fun `shift on mutable list removes and returns first element`() {
        val list = mutableListOf(1, 2, 3)
        val result = ArrayMethodExecutor.execute(list, "shift", emptyList(), simpleArrowExecutor)
        assertEquals(1, result)
        assertEquals(listOf(2, 3), list)
    }


    @Test
    fun `concat merges arrays`() {
        val result = ArrayMethodExecutor.execute(listOf(1, 2), "concat", listOf(listOf(3, 4)), simpleArrowExecutor)
        assertEquals(listOf(1, 2, 3, 4), result)
    }

    @Test
    fun `concat adds single elements`() {
        val result = ArrayMethodExecutor.execute(listOf(1, 2), "concat", listOf(3, 4), simpleArrowExecutor)
        assertEquals(listOf(1, 2, 3, 4), result)
    }


    @Test
    fun `flat flattens nested arrays`() {
        val result = ArrayMethodExecutor.execute(listOf(1, listOf(2, 3), listOf(4)), "flat", emptyList(), simpleArrowExecutor)
        assertEquals(listOf(1, 2, 3, 4), result)
    }

    @Test
    fun `flat with depth 0 returns original`() {
        val nested = listOf(1, listOf(2, 3))
        val result = ArrayMethodExecutor.execute(nested, "flat", listOf(0), simpleArrowExecutor)
        assertEquals(nested, result)
    }


    @Test
    fun `sort without comparator sorts as strings`() {
        val result = ArrayMethodExecutor.execute(listOf("c", "a", "b"), "sort", emptyList(), simpleArrowExecutor)
        assertEquals(listOf("a", "b", "c"), result)
    }

    @Test
    fun `sort with comparator function`() {
        val arrow = ArrowFunction(listOf("a", "b"), "a - b", true)
        val result = ArrayMethodExecutor.execute(listOf(3, 1, 2), "sort", listOf(arrow), simpleArrowExecutor)
        assertEquals(listOf(1, 2, 3), result)
    }


    @Test
    fun `isSupported returns true for supported methods`() {
        assertTrue(ArrayMethodExecutor.isSupported("join"))
        assertTrue(ArrayMethodExecutor.isSupported("filter"))
        assertTrue(ArrayMethodExecutor.isSupported("map"))
        assertTrue(ArrayMethodExecutor.isSupported("length"))
    }

    @Test
    fun `isSupported returns false for unsupported methods`() {
        assertFalse(ArrayMethodExecutor.isSupported("unknownMethod"))
        assertFalse(ArrayMethodExecutor.isSupported("splice"))
    }


    @Test
    fun `unsupported method returns null`() {
        val result = ArrayMethodExecutor.execute(listOf(1, 2, 3), "unknownMethod", emptyList(), simpleArrowExecutor)
        assertNull(result)
    }
}
