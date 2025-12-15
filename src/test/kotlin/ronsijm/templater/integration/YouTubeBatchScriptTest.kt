package ronsijm.templater.integration

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import ronsijm.templater.TestContextFactory
import ronsijm.templater.parser.TemplateParser

/**
 * Tests for YouTube batch processing script compatibility.
 * Based on: https://github.com/SilentVoid13/Templater/discussions/1638
 * 
 * This script processes multiple YouTube links from a file and creates notes.
 * It uses advanced JavaScript features that require incremental support.
 */
class YouTubeBatchScriptTest {

    @Test
    fun `test basic variable assignment and string operations`() {
        val context = TestContextFactory.create(fileContent = "https://youtu.be/test123")
        val parser = TemplateParser()

        val template = """
            <%*
            const outputFolder = "sources/youtube"
            tR += outputFolder
            %>
        """.trimIndent()

        val result = parser.parse(template, context)
        assertTrue(result.contains("sources/youtube"))
    }

    @Test
    fun `test for loop with counter`() {
        val context = TestContextFactory.create(fileContent = "test")
        val parser = TemplateParser()

        val template = """
            <%*
            let count = 0
            for (let i = 1; i <= 3; i++) {
                count = count + 1
            }
            tR += count
            %>
        """.trimIndent()

        val result = parser.parse(template, context)
        assertTrue(result.contains("3"))
    }

    @Test
    fun `test string concatenation for building content`() {
        val context = TestContextFactory.create(fileContent = "test")
        val parser = TemplateParser()

        val template = """
            <%*
            const title = "Test Video"
            const author = "Test Channel"
            const content = "# by " + author + "\n## " + title
            tR += content
            %>
        """.trimIndent()

        val result = parser.parse(template, context)
        assertTrue(result.contains("# by Test Channel"))
        assertTrue(result.contains("## Test Video"))
    }

    @Test
    fun `test template literal for building YAML frontmatter`() {
        val context = TestContextFactory.create(fileContent = "test")
        val parser = TemplateParser()

        // Use ${'$'} to escape the $ in Kotlin raw strings
        val template = """
            <%*
            const title = "My Video Title"
            const channel = "My Channel"
            const duration = "05:30"
            tR += `---
aliases: ${'$'}{title}
channel_name: ${'$'}{channel}
duration: ${'$'}{duration}
---`
            %>
        """.trimIndent()

        val result = parser.parse(template, context)
        assertTrue(result.contains("aliases: My Video Title"))
        assertTrue(result.contains("channel_name: My Channel"))
        assertTrue(result.contains("duration: 05:30"))
    }

    @Test
    fun `test web request to fetch data`() {
        val context = TestContextFactory.create(fileContent = "test")
        val parser = TemplateParser()

        // Using a simple JSON API to test web requests work
        val template = """
            <% tp.web.request("https://jsonplaceholder.typicode.com/todos/1", "title") %>
        """.trimIndent()

        val result = parser.parse(template, context)
        // The API returns a todo item - check it's not an error and has content
        assertFalse(result.contains("Error"), "Should not contain error: $result")
        assertTrue(result.trim().isNotEmpty(), "Should have content: $result")
    }

    @Test
    fun `test if-else conditional logic`() {
        val context = TestContextFactory.create(fileContent = "test")
        val parser = TemplateParser()

        val template = """
            <%*
            const count = 0
            if (count == 0) {
                tR += "No items"
            } else {
                tR += "Has items"
            }
            %>
        """.trimIndent()

        val result = parser.parse(template, context)
        assertTrue(result.contains("No items"))
    }

    @Test
    fun `test reading file content`() {
        val fileContent = """
            https://youtu.be/video1
            https://youtu.be/video2
            https://youtu.be/video3
        """.trimIndent()
        
        val context = TestContextFactory.create(fileContent = fileContent)
        val parser = TemplateParser()

        val template = """
            <% tp.file.content %>
        """.trimIndent()

        val result = parser.parse(template, context)
        assertTrue(result.contains("https://youtu.be/video1"))
        assertTrue(result.contains("https://youtu.be/video2"))
        assertTrue(result.contains("https://youtu.be/video3"))
    }

    @Test
    fun `test string split method`() {
        val context = TestContextFactory.create(fileContent = "a,b,c")
        val parser = TemplateParser()

        val template = """
            <%*
            const content = "a,b,c"
            const parts = content.split(",")
            tR += parts[0]
            %>
        """.trimIndent()

        val result = parser.parse(template, context)
        assertTrue(result.contains("a"), "Result should contain 'a': $result")
    }

    @Test
    fun `test string trim method`() {
        val context = TestContextFactory.create(fileContent = "test")
        val parser = TemplateParser()

        val template = """
            <%*
            const text = "  hello world  "
            const trimmed = text.trim()
            tR += trimmed
            %>
        """.trimIndent()

        val result = parser.parse(template, context)
        assertEquals("hello world", result.trim())
    }

    @Test
    fun `test string replaceAll method`() {
        val context = TestContextFactory.create(fileContent = "test")
        val parser = TemplateParser()

        val template = """
            <%*
            const text = "hello-world-test"
            const replaced = text.replaceAll("-", " ")
            tR += replaced
            %>
        """.trimIndent()

        val result = parser.parse(template, context)
        assertTrue(result.contains("hello world test"))
    }

    @Test
    fun `test string padStart method`() {
        val context = TestContextFactory.create(fileContent = "test")
        val parser = TemplateParser()

        val template = """
            <%*
            const num = "5"
            const padded = num.padStart(2, "0")
            tR += padded
            %>
        """.trimIndent()

        val result = parser.parse(template, context)
        assertTrue(result.contains("05"))
    }

    @Test
    fun `test array indexing`() {
        val context = TestContextFactory.create(fileContent = "test")
        val parser = TemplateParser()

        val template = """
            <%*
            const items = "one,two,three"
            const parts = items.split(",")
            tR += parts[1]
            %>
        """.trimIndent()

        val result = parser.parse(template, context)
        assertTrue(result.contains("two"))
    }

    @Test
    fun `test array length property`() {
        val context = TestContextFactory.create(fileContent = "test")
        val parser = TemplateParser()

        val template = """
            <%*
            const items = "a,b,c,d"
            const parts = items.split(",")
            tR += parts.length
            %>
        """.trimIndent()

        val result = parser.parse(template, context)
        assertTrue(result.contains("4"))
    }

    @Test
    fun `test array join method`() {
        val context = TestContextFactory.create(fileContent = "test")
        val parser = TemplateParser()

        val template = """
            <%*
            const items = "a,b,c"
            const parts = items.split(",")
            const joined = parts.join(" - ")
            tR += joined
            %>
        """.trimIndent()

        val result = parser.parse(template, context)
        assertTrue(result.contains("a - b - c"))
    }

    @Test
    fun `test for-of loop with array`() {
        val context = TestContextFactory.create(fileContent = "test")
        val parser = TemplateParser()

        val template = """
            <%*
            const items = "apple,banana,cherry"
            const fruits = items.split(",")
            for (const fruit of fruits) {
                tR += fruit + "\n"
            }
            %>
        """.trimIndent()

        val result = parser.parse(template, context)
        assertTrue(result.contains("apple"), "Should contain apple: $result")
        assertTrue(result.contains("banana"), "Should contain banana: $result")
        assertTrue(result.contains("cherry"), "Should contain cherry: $result")
    }

    @Test
    fun `test for-of loop with let declaration`() {
        val context = TestContextFactory.create(fileContent = "test")
        val parser = TemplateParser()

        val template = """
            <%*
            const numbers = "1,2,3"
            const nums = numbers.split(",")
            let sum = 0
            for (let num of nums) {
                tR += num + " "
            }
            %>
        """.trimIndent()

        val result = parser.parse(template, context)
        assertTrue(result.contains("1 2 3"))
    }

    @Test
    fun `test arrow function with filter`() {
        val context = TestContextFactory.create(fileContent = "test")
        val parser = TemplateParser()

        val template = """
            <%*
            const items = "apple,banana,apricot,cherry"
            const fruits = items.split(",")
            const aFruits = fruits.filter(x => x.startsWith("a"))
            tR += aFruits.join(", ")
            %>
        """.trimIndent()

        val result = parser.parse(template, context)
        assertTrue(result.contains("apple"), "Should contain apple: $result")
        assertTrue(result.contains("apricot"), "Should contain apricot: $result")
        assertFalse(result.contains("banana"), "Should not contain banana: $result")
        assertFalse(result.contains("cherry"), "Should not contain cherry: $result")
    }

    @Test
    fun `test arrow function with map`() {
        val context = TestContextFactory.create(fileContent = "test")
        val parser = TemplateParser()

        val template = """
            <%*
            const items = "a,b,c"
            const letters = items.split(",")
            const upper = letters.map(x => x.toUpperCase())
            tR += upper.join("-")
            %>
        """.trimIndent()

        val result = parser.parse(template, context)
        assertTrue(result.contains("A-B-C"), "Should contain A-B-C: $result")
    }

    @Test
    fun `test arrow function stored in variable`() {
        val context = TestContextFactory.create(fileContent = "test")
        val parser = TemplateParser()

        val template = """
            <%*
            const double = x => x * 2
            const result = double(5)
            tR += result
            %>
        """.trimIndent()

        val result = parser.parse(template, context)
        assertTrue(result.contains("10"), "Should contain 10: $result")
    }

    @Test
    fun `test arrow function with parentheses`() {
        val context = TestContextFactory.create(fileContent = "test")
        val parser = TemplateParser()

        val template = """
            <%*
            const add = (a, b) => a + b
            const result = add(3, 4)
            tR += result
            %>
        """.trimIndent()

        val result = parser.parse(template, context)
        assertTrue(result.contains("7"), "Should contain 7: $result")
    }

    @Test
    fun `test try-catch block catches errors`() {
        val context = TestContextFactory.create(fileContent = "test")
        val parser = TemplateParser()

        val template = """
            <%*
            let result = "start"
            try {
                result = result + "-try"
            } catch (e) {
                result = result + "-catch"
            }
            tR += result
            %>
        """.trimIndent()

        val result = parser.parse(template, context)
        assertTrue(result.contains("start-try"), "Should execute try block: $result")
    }

    @Test
    fun `test try-catch with finally`() {
        val context = TestContextFactory.create(fileContent = "test")
        val parser = TemplateParser()

        val template = """
            <%*
            let result = ""
            try {
                result = result + "try-"
            } catch (e) {
                result = result + "catch-"
            } finally {
                result = result + "finally"
            }
            tR += result
            %>
        """.trimIndent()

        val result = parser.parse(template, context)
        assertTrue(result.contains("try-finally"), "Should execute try and finally: $result")
    }

    @Test
    fun `test object literal parsing`() {
        val context = TestContextFactory.create(fileContent = "test")
        val parser = TemplateParser()

        val template = """
            <%*
            const options = { url: "https://example.com", method: "GET" }
            tR += options.url
            %>
        """.trimIndent()

        val result = parser.parse(template, context)
        assertTrue(result.contains("https://example.com"), "Should parse object literal: $result")
    }

    @Test
    fun `test tp obsidian request with object literal`() {
        val context = TestContextFactory.create(fileContent = "test")
        val parser = TemplateParser()

        // tp.obsidian.request({url: x}) should work like tp.web.request(x)
        val template = """
            <% tp.obsidian.request({ url: "https://jsonplaceholder.typicode.com/todos/1" }) %>
        """.trimIndent()

        val result = parser.parse(template, context)
        assertFalse(result.contains("Error"), "Should not contain error: $result")
        assertTrue(result.contains("userId") || result.contains("title"), "Should return JSON data: $result")
    }

    @Test
    fun `test tp obsidian request with string url`() {
        val context = TestContextFactory.create(fileContent = "test")
        val parser = TemplateParser()

        // tp.obsidian.request(url) should also work with plain string
        val template = """
            <% tp.obsidian.request("https://jsonplaceholder.typicode.com/todos/1") %>
        """.trimIndent()

        val result = parser.parse(template, context)
        assertFalse(result.contains("Error"), "Should not contain error: $result")
        assertTrue(result.contains("userId") || result.contains("title"), "Should return JSON data: $result")
    }
}
