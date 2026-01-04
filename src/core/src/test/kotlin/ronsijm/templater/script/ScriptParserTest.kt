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
        assertEquals("1", loopInfo.startExpr)
        assertEquals("5", loopInfo.endExpr)
        assertEquals("<=", loopInfo.conditionOperator)
        assertTrue(loopInfo.isIncrement)
    }

    @Test
    fun `test parse for loop header with greater than or equal`() {
        val header = "for (let i = 10; i >= 1; i--)"

        val loopInfo = parser.parseForLoopHeader(header)

        assertNotNull(loopInfo)
        assertEquals("i", loopInfo!!.varName)
        assertEquals("10", loopInfo.startExpr)
        assertEquals("1", loopInfo.endExpr)
        assertEquals(">=", loopInfo.conditionOperator)
        assertFalse(loopInfo.isIncrement)
    }

    @Test
    fun `test parse for loop header with less than`() {
        val header = "for (let i = 0; i < 10; i++)"

        val loopInfo = parser.parseForLoopHeader(header)

        assertNotNull(loopInfo)
        assertEquals("i", loopInfo!!.varName)
        assertEquals("0", loopInfo.startExpr)
        assertEquals("10", loopInfo.endExpr)
        assertEquals("<", loopInfo.conditionOperator)
        assertTrue(loopInfo.isIncrement)
    }

    @Test
    fun `test parse for loop header with greater than`() {
        val header = "for (let i = 10; i > 0; i--)"

        val loopInfo = parser.parseForLoopHeader(header)

        assertNotNull(loopInfo)
        assertEquals("i", loopInfo!!.varName)
        assertEquals("10", loopInfo.startExpr)
        assertEquals("0", loopInfo.endExpr)
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
        assertEquals(4, result.third)
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
        assertEquals(0, result.third.size)
        assertEquals(4, result.fourth)
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
        assertEquals(1, result.third.size)
        assertNull(result.third[0].first)
        assertEquals("tR += \"no\"", result.third[0].second[0])
        assertEquals(8, result.fourth)
    }

    @Test
    fun `test parse for loop header without spaces`() {
        val header = "for(let i=1;i<=5;i++)"

        val loopInfo = parser.parseForLoopHeader(header)

        assertNotNull(loopInfo)
        assertEquals("i", loopInfo!!.varName)
        assertEquals("1", loopInfo.startExpr)
        assertEquals("5", loopInfo.endExpr)
    }

    @Test
    fun `test parse invalid for loop header returns null`() {
        val header = "invalid loop header"

        val loopInfo = parser.parseForLoopHeader(header)

        assertNull(loopInfo)
    }

    @Test
    fun `test splitStatements with for loop`() {

        val code = "let sum = 0; for (let i = 0; i < 3; i++) { sum += 1; }\nreturn sum;"

        val statements = parser.splitStatements(code)


        assertEquals(3, statements.size, "Should have 3 statements: $statements")
        assertEquals("let sum = 0", statements[0])
        assertTrue(statements[1].startsWith("for ("), "Second statement should be for loop: ${statements[1]}")
        assertEquals("return sum", statements[2])
    }

    @Test
    fun `test splitStatements with function body format`() {

        val code = "let sum = 0\nfor (let i = 0; i < 3; i++) {\n    sum += 1\n}\nreturn sum"

        val statements = parser.splitStatements(code)

        println("Statements: $statements")
        assertEquals(3, statements.size, "Should have 3 statements: $statements")
        assertEquals("let sum = 0", statements[0])
        assertTrue(statements[1].startsWith("for ("), "Second statement should be for loop: ${statements[1]}")
        assertEquals("return sum", statements[2])
    }

    @Test
    fun `test extractForLoop from statements`() {

        val statements1 = listOf(
            "let sum = 0",
            "for (let i = 0; i < 3; i++) {",
            "sum += 1",
            "}",
            "return sum"
        )

        val loopInfo1 = parser.extractForLoop(statements1, 1)

        println("Loop info 1: $loopInfo1")
        assertNotNull(loopInfo1, "Should extract for loop")

        assertEquals("for (let i = 0; i < 3; i++) {", loopInfo1!!.first, "Loop header")
        assertEquals(listOf("sum += 1"), loopInfo1.second, "Loop body")
        assertEquals(4, loopInfo1.third, "Next index after loop")


        val statements2 = listOf(
            "let sum = 0",
            "for (let i = 0; i < 3; i++) {\n    sum += 1\n}",
            "return sum"
        )

        val loopInfo2 = parser.extractForLoop(statements2, 1)

        println("Loop info 2: $loopInfo2")
        assertNotNull(loopInfo2, "Should extract for loop from single statement")
        assertEquals("for (let i = 0; i < 3; i++)", loopInfo2!!.first, "Loop header without brace")
        assertEquals(listOf("sum += 1"), loopInfo2.second, "Loop body")
        assertEquals(2, loopInfo2.third, "Next index after loop")
    }
}
