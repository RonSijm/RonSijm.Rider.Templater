package ronsijm.templater.script

import ronsijm.templater.TestContextFactory
import ronsijm.templater.services.ServiceContainer
import ronsijm.templater.services.MockSystemOperationsService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.script.ArrowFunction

class ScriptEngineTest {

    @Test
    fun `test simple variable assignment`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)
        
        engine.execute("let x = \"hello\"")
        
        assertEquals("hello", engine.getVariable("x"))
    }
    
    @Test
    fun `test variable assignment without let`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.execute("x = \"world\"")

        assertEquals("world", engine.getVariable("x"))
    }

    @Test
    fun `test string concatenation`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.execute("let greeting = \"Hello\" + \" \" + \"World\"")

        assertEquals("Hello World", engine.getVariable("greeting"))
    }

    @Test
    fun `test string concatenation with variables`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.execute("""
            let first = "Hello"
            let second = "World"
            let combined = first + " " + second
        """.trimIndent())

        assertEquals("Hello", engine.getVariable("first"))
        assertEquals("World", engine.getVariable("second"))
        assertEquals("Hello World", engine.getVariable("combined"))
    }

    @Test
    fun `test function call - date now`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.execute("let today = tp.date.now(\"YYYY-MM-DD\")")

        // Use dynamic date instead of hardcoded value
        val expected = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        assertEquals(expected, engine.getVariable("today"))
    }

    @Test
    fun `test function call - date tomorrow`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.execute("let tomorrow = tp.date.tomorrow(\"YYYY-MM-DD\")")

        // Use dynamic date instead of hardcoded value
        val expected = java.time.LocalDate.now().plusDays(1).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        assertEquals(expected, engine.getVariable("tomorrow"))
    }

    @Test
    fun `test string concatenation with function call`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.execute("let fileName = \"note-\" + tp.date.now(\"YYYY-MM-DD\")")

        // Use dynamic date instead of hardcoded value
        val expected = "note-" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        assertEquals(expected, engine.getVariable("fileName"))
    }

    @Test
    fun `test await keyword is stripped`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.execute("let result = await tp.date.now(\"YYYY-MM-DD\")")

        // Use dynamic date instead of hardcoded value
        val expected = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        assertEquals(expected, engine.getVariable("result"))
    }

    @Test
    fun `test frontmatter access`() {
        val context = TestContextFactory.create(
            frontmatter = mapOf("title" to "Test Document"),
            fileContent = "test content"
        )
        val engine = ScriptEngine(context)

        engine.execute("let docTitle = tp.frontmatter.title")

        assertEquals("Test Document", engine.getVariable("docTitle"))
    }

    @Test
    fun `test multiple statements`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.execute("""
            let a = "Hello"
            let b = "World"
            let c = a + " " + b
            let d = c + " " + tp.date.now("YYYY-MM-DD")
        """.trimIndent())

        assertEquals("Hello", engine.getVariable("a"))
        assertEquals("World", engine.getVariable("b"))
        assertEquals("Hello World", engine.getVariable("c"))

        // Use dynamic date instead of hardcoded value
        val expected = "Hello World " + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        assertEquals(expected, engine.getVariable("d"))
    }

    @Test
    fun `test system prompt callback`() {
        var promptCalled = false
        var promptMessage = ""

        val mockSystemOps = MockSystemOperationsService(promptResponse = "User Input")
        val services = ServiceContainer.createForTesting(systemOperationsService = mockSystemOps)
        val context = TestContextFactory.create(
            fileContent = "test content",
            services = services
        )
        val engine = ScriptEngine(context)

        engine.execute("let userInput = tp.system.prompt(\"Enter name\")")

        assertEquals(1, mockSystemOps.promptCalls.size)
        assertEquals("Enter name", mockSystemOps.promptCalls[0].promptText)
        assertEquals("User Input", engine.getVariable("userInput"))
    }

    @Test
    fun `test template literal with variable interpolation`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.execute("let i = 5")
        val result = engine.evaluateExpression("`Item number ${'$'}{i}`")

        assertEquals("Item number 5", result)
    }

    @Test
    fun `test template literal with newline escape`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        val result = engine.evaluateExpression("`Line 1\\nLine 2`")

        assertEquals("Line 1\nLine 2", result)
    }

    @Test
    fun `test template literal with tab escape`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        val result = engine.evaluateExpression("`Col1\\tCol2`")

        assertEquals("Col1\tCol2", result)
    }

    @Test
    fun `test tR accumulator initialization`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.initializeResultAccumulator("Initial content")

        assertEquals("Initial content", engine.getResultAccumulator())
    }

    @Test
    fun `test tR accumulator append`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.initializeResultAccumulator("")
        engine.execute("""
            tR += "Hello"
            tR += " World"
        """.trimIndent())

        assertEquals("Hello World", engine.getResultAccumulator())
    }

    @Test
    fun `test tR accumulator set`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.initializeResultAccumulator("Old content")
        engine.execute("tR = \"New content\"")

        assertEquals("New content", engine.getResultAccumulator())
    }

    @Test
    fun `test for loop basic`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.initializeResultAccumulator("")
        engine.execute("""
            for (let i = 1; i <= 3; i++) {
                tR += `${'$'}{i}`
            }
        """.trimIndent())

        assertEquals("123", engine.getResultAccumulator())
    }

    @Test
    fun `test for loop with template literal`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.initializeResultAccumulator("")
        engine.execute("""
            for (let i = 1; i <= 5; i++) {
                tR += `${'$'}{i}. Item number ${'$'}{i}\n`
            }
        """.trimIndent())

        val expected = "1. Item number 1\n2. Item number 2\n3. Item number 3\n4. Item number 4\n5. Item number 5\n"
        assertEquals(expected, engine.getResultAccumulator())
    }

    @Test
    fun `test for loop with less than condition`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.initializeResultAccumulator("")
        engine.execute("""
            for (let i = 0; i < 3; i++) {
                tR += `${'$'}{i}`
            }
        """.trimIndent())

        assertEquals("012", engine.getResultAccumulator())
    }

    @Test
    fun `test for loop with greater than condition`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.initializeResultAccumulator("")
        engine.execute("""
            for (let i = 5; i > 2; i--) {
                tR += `${'$'}{i}`
            }
        """.trimIndent())

        assertEquals("543", engine.getResultAccumulator())
    }

    @Test
    fun `test for loop with greater than or equal condition`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.initializeResultAccumulator("")
        engine.execute("""
            for (let i = 3; i >= 1; i--) {
                tR += `${'$'}{i}`
            }
        """.trimIndent())

        assertEquals("321", engine.getResultAccumulator())
    }

    @Test
    fun `test for loop with comments`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.initializeResultAccumulator("")
        engine.execute("""
            // Generate a numbered list
            for (let i = 1; i <= 3; i++) {
                // Add item to result
                tR += `${'$'}{i}. Item\n`
            }
        """.trimIndent())

        assertEquals("1. Item\n2. Item\n3. Item\n", engine.getResultAccumulator())
    }

    @Test
    fun `test for loop with variable in body`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.initializeResultAccumulator("")
        engine.execute("""
            let prefix = "Task"
            for (let i = 1; i <= 2; i++) {
                tR += `${'$'}{prefix} ${'$'}{i}\n`
            }
        """.trimIndent())

        assertEquals("Task 1\nTask 2\n", engine.getResultAccumulator())
    }

    @Test
    fun `test const and var keywords`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.execute("""
            const x = 10
            var y = 20
        """.trimIndent())

        assertEquals(10, engine.getVariable("x"))
        assertEquals(20, engine.getVariable("y"))
    }

    @Test
    fun `test if statement basic`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.initializeResultAccumulator("")
        engine.execute("""
            let x = 5
            if (x < 10) {
                tR += "less than 10"
            }
        """.trimIndent())

        assertEquals("less than 10", engine.getResultAccumulator())
    }

    @Test
    fun `test if else statement`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.initializeResultAccumulator("")
        engine.execute("""
            let x = 15
            if (x < 10) {
                tR += "less than 10"
            } else {
                tR += "greater or equal to 10"
            }
        """.trimIndent())

        assertEquals("greater or equal to 10", engine.getResultAccumulator())
    }

    @Test
    fun `test if else if else statement`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.initializeResultAccumulator("")
        engine.execute("""
            let hour = 15
            if (hour < 12) {
                tR += "morning"
            } else if (hour < 18) {
                tR += "afternoon"
            } else {
                tR += "evening"
            }
        """.trimIndent())

        assertEquals("afternoon", engine.getResultAccumulator())
    }

    @Test
    fun `test new Date and getHours`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.execute("const now = new Date()")
        val now = engine.getVariable("now")
        assertNotNull(now)

        engine.execute("const hour = now.getHours()")
        val hour = engine.getVariable("hour")
        assertNotNull(hour)
        assertTrue(hour is Int)
        assertTrue((hour as Int) in 0..23)
    }

    @Test
    fun `test greeting based on time`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.initializeResultAccumulator("")

        // Test morning (simulate hour = 10)
        engine.execute("""
            const hour = 10
            if (hour < 12) {
                tR += "Good morning!"
            } else if (hour < 18) {
                tR += "Good afternoon!"
            } else {
                tR += "Good evening!"
            }
        """.trimIndent())

        assertEquals("Good morning!", engine.getResultAccumulator())
    }

    @Test
    fun `test comparison operators`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.initializeResultAccumulator("")

        // Test <=
        engine.execute("""
            let x = 5
            if (x <= 5) {
                tR += "A"
            }
        """.trimIndent())

        // Test >=
        engine.execute("""
            let y = 10
            if (y >= 10) {
                tR += "B"
            }
        """.trimIndent())

        // Test ==
        engine.execute("""
            let z = 7
            if (z == 7) {
                tR += "C"
            }
        """.trimIndent())

        assertEquals("ABC", engine.getResultAccumulator())
    }

    @Test
    fun `test exact user example with new Date and if else`() {
        val context = TestContextFactory.create(fileContent = "test content")
        val engine = ScriptEngine(context)

        engine.initializeResultAccumulator("")
        engine.execute("""
            const hour = new Date().getHours();
            if (hour < 12) {
                tR += "Good morning! ☀️";
            } else if (hour < 18) {
                tR += "Good afternoon! 🌤️";
            } else {
                tR += "Good evening! 🌙";
            }
        """.trimIndent())

        val result = engine.getResultAccumulator()

        // Should contain one of the three greetings based on current time
        assertTrue(
            result.contains("Good morning!") ||
            result.contains("Good afternoon!") ||
            result.contains("Good evening!"),
            "Expected a greeting but got: $result"
        )
    }

    @Test
    fun `test arrow function parsing`() {
        val context = TestContextFactory.create(fileContent = "test")
        val engine = ScriptEngine(context)

        engine.execute("const double = x => x * 2")

        val fn = engine.getVariable("double")
        assertNotNull(fn, "Arrow function should be stored")
        assertTrue(fn is ArrowFunction, "Should be ArrowFunction but was ${fn?.javaClass}")
    }

    @Test
    fun `test arrow function execution`() {
        val context = TestContextFactory.create(fileContent = "test")
        val engine = ScriptEngine(context)

        engine.execute("""
            const double = x => x * 2
            const result = double(5)
            tR += result
        """.trimIndent())

        val result = engine.getResultAccumulator()
        assertEquals("10", result, "double(5) should be 10")
    }

    @Test
    fun `test arrow function with method call in body`() {
        val context = TestContextFactory.create(fileContent = "test")
        val engine = ScriptEngine(context)

        // First test: simple filter without method call
        engine.execute("""
            const items = "apple,banana,apricot"
            const fruits = items.split(",")
            tR += "fruits: " + fruits.join(", ") + "\n"
        """.trimIndent())

        val result1 = engine.getResultAccumulator()
        assertTrue(result1.contains("apple"), "Should contain apple in split result: $result1")

        // Second test: filter with arrow function
        val context2 = TestContextFactory.create(fileContent = "test")
        val engine2 = ScriptEngine(context2)

        // First verify the array is created correctly
        engine2.execute("const nums = [1, 2, 3, 4, 5]")
        val numsVar = engine2.getVariable("nums")
        println("nums variable: $numsVar (type: ${numsVar?.javaClass})")

        // Now test filter
        engine2.execute("const evens = nums.filter(x => x > 2)")
        val evensVar = engine2.getVariable("evens")
        println("evens variable: $evensVar (type: ${evensVar?.javaClass})")

        engine2.execute("tR += evens.join(\", \")")

        val result2 = engine2.getResultAccumulator()
        println("result2: '$result2'")
        assertTrue(result2.contains("3"), "Should contain 3: $result2")
        assertTrue(result2.contains("4"), "Should contain 4: $result2")
        assertTrue(result2.contains("5"), "Should contain 5: $result2")
    }

    @Test
    fun `test arrow function parsing directly`() {
        val templateContext = TestContextFactory.create(fileContent = "test")
        val scriptContext = ScriptContext(templateContext)
        val moduleRegistry = ModuleRegistry(scriptContext)
        val evaluator = ScriptEvaluator(scriptContext, moduleRegistry)

        // Test that arrow function is parsed correctly
        val arrowFn = evaluator.evaluateExpression("x => x > 2")
        assertTrue(arrowFn is ArrowFunction, "Should parse as ArrowFunction: $arrowFn (${arrowFn?.javaClass})")

        // Test executing the arrow function
        if (arrowFn is ArrowFunction) {
            val result = evaluator.executeArrowFunction(arrowFn, listOf(5))
            assertEquals(true, result, "5 > 2 should be true: $result")

            val result2 = evaluator.executeArrowFunction(arrowFn, listOf(1))
            assertEquals(false, result2, "1 > 2 should be false: $result2")
        }
    }
}

