package ronsijm.templater.script

import ronsijm.templater.TestContextFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*


class ScriptEnginePiCalculationTest {

    @Test
    fun `test array access in function`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            function testArrayAccess() {
                const a = new Array(5).fill(2)
                let val = a[0]
                return val
            }

            let result1 = testArrayAccess()
        """.trimIndent())

        val result1 = engine.getVariable("result1")
        assertEquals(2, (result1 as Number).toInt(), "Array access in function should work")
    }

    @Test
    fun `test array access with loop variable`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            function testArrayAccessWithLoop() {
                const a = new Array(5).fill(2)
                let sum = 0
                for (let i = 0; i < 5; i++) {
                    sum += a[i]
                }
                return sum
            }

            let result2 = testArrayAccessWithLoop()
        """.trimIndent())

        val result2 = engine.getVariable("result2")
        assertEquals(10, (result2 as Number).toInt(), "Array access with loop should work")
    }

    @Test
    fun `test if-else inside function`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            function testIfElse(x) {
                let out = ""
                if (x === 9) {
                    out = "nine"
                } else if (x === 10) {
                    out = "ten"
                } else {
                    out = "other"
                }
                return out
            }

            let result3 = testIfElse(5)
            let result4 = testIfElse(9)
            let result5 = testIfElse(10)
        """.trimIndent())

        val result3 = engine.getVariable("result3")
        val result4 = engine.getVariable("result4")
        val result5 = engine.getVariable("result5")
        assertEquals("other", result3, "testIfElse(5) should return 'other'")
        assertEquals("nine", result4, "testIfElse(9) should return 'nine'")
        assertEquals("ten", result5, "testIfElse(10) should return 'ten'")
    }

    @Test
    fun `test loop with simple if inside function`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            function testLoopWithIfSimple() {
                let out = ""
                for (let j = 0; j < 3; j++) {
                    if (j === 0) {
                        out += "zero"
                    }
                }
                return out
            }

            let result6a = testLoopWithIfSimple()
        """.trimIndent())

        val result6a = engine.getVariable("result6a")
        assertEquals("zero", result6a, "testLoopWithIfSimple should return 'zero'")
    }

    @Test
    fun `test loop with if-else inside function`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            function testLoopWithIfElse() {
                let out = ""
                for (let j = 0; j < 3; j++) {
                    if (j === 0) {
                        out += "zero"
                    } else if (j === 1) {
                        out += "one"
                    } else {
                        out += "other"
                    }
                }
                return out
            }

            let result6 = testLoopWithIfElse()
        """.trimIndent())

        val result6 = engine.getVariable("result6")
        assertEquals("zerooneother", result6, "testLoopWithIfElse should return 'zerooneother'")
    }

    @Test
    fun `test nested loops with array modification`() {
        val engine = TestContextFactory.createScriptEngine()

        engine.execute("""
            function testNestedLoopWithArray() {
                const a = new Array(3).fill(1)
                for (let i = 0; i < 3; i++) {
                    for (let j = 0; j < 2; j++) {
                        a[i] = a[i] + 1
                    }
                }
                return a[0] + a[1] + a[2]
            }

            let result7 = testNestedLoopWithArray()
        """.trimIndent())

        val result7 = engine.getVariable("result7")


        assertEquals(9, (result7 as Number).toInt(), "testNestedLoopWithArray should return 9")
    }

    @Test
    fun `test simple pi digit extraction`() {
        val engine = TestContextFactory.createScriptEngine()


        engine.execute("""
            function extractDigit(value) {
                return value % 10
            }

            let d1 = extractDigit(31)
            let d2 = extractDigit(45)
        """.trimIndent())

        val d1 = engine.getVariable("d1")
        val d2 = engine.getVariable("d2")
        assertEquals(1, (d1 as Number).toInt(), "extractDigit(31) should return 1")
        assertEquals(5, (d2 as Number).toInt(), "extractDigit(45) should return 5")
    }
}

