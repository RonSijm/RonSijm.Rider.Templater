package ronsijm.templater.script

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ScriptParserTest {

    private val parser = ScriptParser()

    @Test
    fun `test parse for loop header with less than or equal`() {
        val header = "for (let i = 1; i <= 5; i++)"
        
        val loopInfo = parser.parseForLoopHeader(header)
        
        assertNotNull(loopInfo)
        assertEquals("i", loopInfo!!.varName)
        assertEquals(1, loopInfo.startValue)
        assertEquals(5, loopInfo.endValue)
        assertEquals("<=", loopInfo.conditionOperator)
        assertTrue(loopInfo.isIncrement)
    }

    @Test
    fun `test parse for loop header with greater than or equal`() {
        val header = "for (let i = 10; i >= 1; i--)"
        
        val loopInfo = parser.parseForLoopHeader(header)
        
        assertNotNull(loopInfo)
        assertEquals("i", loopInfo!!.varName)
        assertEquals(10, loopInfo.startValue)
        assertEquals(1, loopInfo.endValue)
        assertEquals(">=", loopInfo.conditionOperator)
        assertFalse(loopInfo.isIncrement)
    }

    @Test
    fun `test parse for loop header with less than`() {
        val header = "for (let i = 0; i < 10; i++)"
        
        val loopInfo = parser.parseForLoopHeader(header)
        
        assertNotNull(loopInfo)
        assertEquals("i", loopInfo!!.varName)
        assertEquals(0, loopInfo.startValue)
        assertEquals(10, loopInfo.endValue)
        assertEquals("<", loopInfo.conditionOperator)
        assertTrue(loopInfo.isIncrement)
    }

    @Test
    fun `test parse for loop header with greater than`() {
        val header = "for (let i = 10; i > 0; i--)"
        
        val loopInfo = parser.parseForLoopHeader(header)
        
        assertNotNull(loopInfo)
        assertEquals("i", loopInfo!!.varName)
        assertEquals(10, loopInfo.startValue)
        assertEquals(0, loopInfo.endValue)
        assertEquals(">", loopInfo.conditionOperator)
        assertFalse(loopInfo.isIncrement)
    }

    @Test
    fun `test check condition less than or equal`() {
        assertTrue(parser.checkCondition(1, "<=", 5))
        assertTrue(parser.checkCondition(5, "<=", 5))
        assertFalse(parser.checkCondition(6, "<=", 5))
    }

    @Test
    fun `test check condition greater than or equal`() {
        assertTrue(parser.checkCondition(10, ">=", 5))
        assertTrue(parser.checkCondition(5, ">=", 5))
        assertFalse(parser.checkCondition(4, ">=", 5))
    }

    @Test
    fun `test check condition less than`() {
        assertTrue(parser.checkCondition(1, "<", 5))
        assertFalse(parser.checkCondition(5, "<", 5))
        assertFalse(parser.checkCondition(6, "<", 5))
    }

    @Test
    fun `test check condition greater than`() {
        assertTrue(parser.checkCondition(10, ">", 5))
        assertFalse(parser.checkCondition(5, ">", 5))
        assertFalse(parser.checkCondition(4, ">", 5))
    }

    @Test
    fun `test extract for loop from statements`() {
        val statements = listOf(
            "for (let i = 1; i <= 3; i++)",
            "{",
            "tR += \"Item\"",
            "}"
        )

        val result = parser.extractForLoop(statements, 0)

        assertNotNull(result)
        assertEquals("for (let i = 1; i <= 3; i++)", result!!.first)
        assertEquals(1, result.second.size)
        assertEquals("tR += \"Item\"", result.second[0])
        assertEquals(4, result.third) // Next index after loop
    }

    @Test
    fun `test extract if statement from statements`() {
        val statements = listOf(
            "if (x > 5)",
            "{",
            "tR += \"yes\"",
            "}"
        )
        
        val result = parser.extractIfStatement(statements, 0)
        
        assertNotNull(result)
        assertEquals("if (x > 5)", result!!.first)
        assertEquals(1, result.second.size)
        assertEquals("tR += \"yes\"", result.second[0])
        assertEquals(0, result.third.size) // No else branches
        assertEquals(4, result.fourth) // Next index after if
    }

    @Test
    fun `test extract if else statement from statements`() {
        val statements = listOf(
            "if (x > 5)",
            "{",
            "tR += \"yes\"",
            "}",
            "else",
            "{",
            "tR += \"no\"",
            "}"
        )
        
        val result = parser.extractIfStatement(statements, 0)
        
        assertNotNull(result)
        assertEquals("if (x > 5)", result!!.first)
        assertEquals(1, result.second.size)
        assertEquals("tR += \"yes\"", result.second[0])
        assertEquals(1, result.third.size) // One else branch
        assertNull(result.third[0].first) // Else has no condition
        assertEquals("tR += \"no\"", result.third[0].second[0])
        assertEquals(8, result.fourth) // Next index after if/else
    }

    @Test
    fun `test parse for loop header without spaces`() {
        val header = "for(let i=1;i<=5;i++)"
        
        val loopInfo = parser.parseForLoopHeader(header)
        
        assertNotNull(loopInfo)
        assertEquals("i", loopInfo!!.varName)
        assertEquals(1, loopInfo.startValue)
        assertEquals(5, loopInfo.endValue)
    }

    @Test
    fun `test parse invalid for loop header returns null`() {
        val header = "invalid loop header"
        
        val loopInfo = parser.parseForLoopHeader(header)
        
        assertNull(loopInfo)
    }
}

