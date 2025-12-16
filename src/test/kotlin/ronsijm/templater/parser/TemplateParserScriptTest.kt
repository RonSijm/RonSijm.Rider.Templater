package ronsijm.templater.parser

import ronsijm.templater.TestContextFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TemplateParserScriptTest {

    private val parser = TemplateParser()

    @Test
    fun `test callout template pattern with underscore trim`() {
        val content = """
            <%*
            let calloutType = "bug"
            let foldState = "+"
            let title = "My Title"
            _%>

            > [!<% calloutType %>]<% foldState %> <% title %>
        """.trimIndent()

        val context = TestContextFactory.create(fileContent = "test content")
        val result = parser.parse(content, context)

        println("DEBUG result: '$result'")
        assertTrue(result.contains("> [!bug]+ My Title"), "Expected '> [!bug]+ My Title' but got: $result")
    }

    @Test
    fun `test callout template with await and suggester simulation`() {
        // Simulate what happens when tp.system.suggester returns a value
        val mockSystemOps = ronsijm.templater.services.MockSystemOperationsService(
            suggesterResponse = "bug"  // Simulate user selecting "bug"
        )
        val services = ronsijm.templater.services.ServiceContainer(
            systemOperationsService = mockSystemOps
        )

        val content = """
            <%*
            const callouts = {
                "bug": "Bug",
                "info": "Info"
            };
            const typeNames = [];
            const typeLabels = [];
            Object.keys(callouts).forEach(key => typeNames.push(key) && typeLabels.push(callouts[key]));
            let calloutType = await tp.system.suggester(typeLabels, typeNames, false, "Select type");
            let foldState = "+";
            let title = "Test Title";
            _%>

            > [!<% calloutType %>]<% foldState %> <% title %>
        """.trimIndent()

        val context = TestContextFactory.create(fileContent = "test content", services = services)
        val result = parser.parse(content, context)

        println("DEBUG result with suggester: '$result'")
        assertTrue(result.contains("> [!bug]+ Test Title"), "Expected '> [!bug]+ Test Title' but got: $result")
    }

    @Test
    fun `test full callout template file structure`() {
        // Simulate the exact file structure with markdown documentation and code fences
        val mockSystemOps = ronsijm.templater.services.MockSystemOperationsService(
            suggesterResponse = "bug",
            promptResponse = "My Title"
        )
        val services = ronsijm.templater.services.ServiceContainer(
            systemOperationsService = mockSystemOps
        )

        // This mimics the actual file structure
        val content = """
            # 3 - Interactive Callout with Fold Options

            Description here.

            ```
            <%*
            const callouts = {
                "bug": "Bug",
                "info": "Info"
            };
            const typeNames = [];
            const typeLabels = [];
            Object.keys(callouts).forEach(key => typeNames.push(key) && typeLabels.push(callouts[key]));
            let calloutType = await tp.system.suggester(typeLabels, typeNames, false, "Select type");
            let foldState = "+";
            let title = await tp.system.prompt("Title", "");
            _%>

            > [!<% calloutType %>]<% foldState %> <% title %>
            ```
        """.trimIndent()

        val context = TestContextFactory.create(fileContent = "test content", services = services)
        val result = parser.parse(content, context)

        println("DEBUG full file result: '$result'")
        // The template blocks should be replaced even inside code fences
        assertTrue(result.contains("> [!bug]+ My Title"), "Expected '> [!bug]+ My Title' but got: $result")
    }

    @Test
    fun `test regex match order`() {
        // Verify that regex matches are in document order
        val templateRegex = Regex("""<%([_-])?(\*)?(.+?)([_-])?%>""", RegexOption.DOT_MATCHES_ALL)

        val content = """
            <%* let x = "hello"; _%>
            <% x %>
            <%* let y = "world"; %>
            <% y %>
        """.trimIndent()

        val matches = templateRegex.findAll(content).toList()

        println("Match order:")
        matches.forEachIndexed { index, match ->
            val isExecution = match.groupValues[2] == "*"
            val command = match.groupValues[3].trim()
            println("  $index: ${if (isExecution) "EXEC" else "INTERP"} - '$command' at ${match.range}")
        }

        // Verify order: exec, interp, exec, interp
        assertEquals(4, matches.size, "Expected 4 matches")
        assertEquals("*", matches[0].groupValues[2], "First should be execution")
        assertEquals("", matches[1].groupValues[2], "Second should be interpolation")
        assertEquals("*", matches[2].groupValues[2], "Third should be execution")
        assertEquals("", matches[3].groupValues[2], "Fourth should be interpolation")
    }

    @Test
    fun `test exact callout file content`() {
        // Test with the EXACT content from the user's file
        val mockSystemOps = ronsijm.templater.services.MockSystemOperationsService(
            suggesterResponses = listOf("bug", "+"),  // First call returns "bug", second returns "+"
            promptResponse = "My Title"
        )
        val services = ronsijm.templater.services.ServiceContainer(
            systemOperationsService = mockSystemOps
        )

        // This is the exact content from the file (without BOM)
        val content = """# 3 - Interactive Callout with Fold Options

Full-featured callout creator with type selection, fold state options (static/expanded/collapsed), and title input. Uses selected text as title or prompts if none selected. Types are sorted alphabetically with color-coded emoji labels.

**Source:** https://github.com/SilentVoid13/Templater/discussions/922#discussioncomment-8545285

```
<%*
const callouts = {
	// Colors: 🟥🟧🟨🟩🟦🟪⬛️⬜️🟫
	"bug":          "🟥 Bug",
	"danger":       "🟥 Danger | Error",
	"fail":         "🟥 Fail | Failure | Missing",
	"warning":      "🟧 Warning | Attention | Caution",
	"help":         "🟧 Help | FAQ | Question",
	"success":      "🟩 Success | Done | Check",
	"abstract":     "🟦 Abstract | Summary | TLDR",
	"example":      "🟦 Example",
	"hint":         "🟦 Hint | Important | Tip",
	"info":         "🟦 Info",
	"note":         "🟦 Note",
	"todo":         "🟦 Todo",
	"cite":         "⬜️ Cite | Quote",

	// Custom types (via Callout Manager)
	"link":         "🟨 Link",
	"presentation": "🟨 Presentation",
	"money":        "🟨 Money",
	"chart":        "🟦 Line Chart",
	"visual":       "🟪 Styled Quote | Visual Quote",
	"visual-img":   "🟪 Styled Image | Visual Image",
	"image":        "🟪 Image",
	"brain":        "🟪 Brain | AI",
};

const typeNames = [];
const typeLabels = [];

Object.keys(callouts)
	.sort() // Remove this line to use predefined order.
	.forEach(key =>
		typeNames.push(key) && typeLabels.push(callouts[key])
	);

let calloutType = await tp.system.suggester(
	typeLabels,
	typeNames,
	false,
	"Select callout type."
);

// Stop here when the prompt was cancelled (ESC).
if (!calloutType) {
  return;
}

let foldState = await tp.system.suggester(
	["Static", "Expanded", "Collapsed"],
	["", "+", "-"],
	false,
	"Select callout folding option."
);

let title = await tp.file.selection();
if (!title) {
	title = await tp.system.prompt("Title Text", "");
}
_%>

> [!<% calloutType %>]<% foldState %> <% title %><%* tp.file.cursor() %>
```
"""

        val context = TestContextFactory.create(fileContent = "test content", services = services)

        // Enable debug logging
        System.setProperty("ronsijm.templater.debug", "true")

        val result = parser.parse(content, context)

        println("DEBUG exact file result:")
        println(result)
        println("---END---")

        println("Suggester calls: ${mockSystemOps.suggesterCalls.size}")
        mockSystemOps.suggesterCalls.forEachIndexed { i, call ->
            println("  Call $i: placeholder='${call.placeholder}', values=${call.values}")
        }
        println("Prompt calls: ${mockSystemOps.promptCalls.size}")
        mockSystemOps.promptCalls.forEachIndexed { i, call ->
            println("  Call $i: promptText='${call.promptText}'")
        }

        // Check that variables were substituted
        assertTrue(result.contains("> [!bug]+"), "Expected '> [!bug]+' but got: $result")
        assertTrue(result.contains("My Title"), "Expected 'My Title' in result but got: $result")
    }

    @Test
    fun `test parallel parser with callout template - sequential mode`() {
        // Test with parallel parser in sequential mode (enableParallel = false)
        // This tests the ParallelTemplateParser's sequential execution path
        val mockSystemOps = ronsijm.templater.services.MockSystemOperationsService(
            suggesterResponse = "bug",
            promptResponse = "My Title"
        )
        val services = ronsijm.templater.services.ServiceContainer(
            systemOperationsService = mockSystemOps
        )

        val content = """
            <%*
            const callouts = {
                "bug": "Bug",
                "info": "Info"
            };
            const typeNames = [];
            const typeLabels = [];
            Object.keys(callouts).forEach(key => typeNames.push(key) && typeLabels.push(callouts[key]));
            let calloutType = await tp.system.suggester(typeLabels, typeNames, false, "Select type");
            let foldState = "+";
            let title = await tp.system.prompt("Title", "");
            _%>

            > [!<% calloutType %>]<% foldState %> <% title %>
        """.trimIndent()

        val context = TestContextFactory.create(fileContent = "test content", services = services)
        val parallelParser = ronsijm.templater.parallel.ParallelTemplateParser(
            validateSyntax = true,
            services = services,
            enableParallel = false  // Use sequential mode to avoid coroutines dependency in tests
        )
        val result = parallelParser.parse(content, context)

        println("DEBUG parallel parser (sequential mode) result: '$result'")
        assertTrue(result.contains("> [!bug]+ My Title"), "Expected '> [!bug]+ My Title' but got: $result")
    }

    @Test
    fun `test execution command with variable assignment`() {
        val content = """
            <%* let greeting = "Hello World" -%>
            Message: <% greeting %>
        """.trimIndent()

        val context = TestContextFactory.create(fileContent = "test content")
        val result = parser.parse(content, context)
        
        assertTrue(result.contains("Message: Hello World"))
    }
    
    @Test
    fun `test execution command with date function`() {
        val content = """
            <%* let today = tp.date.now("yyyy-MM-dd") -%>
            Today: <% today %>
        """.trimIndent()
        
        val context = TestContextFactory.create(fileContent = "test content")
        val result = parser.parse(content, context)

        assertTrue(result.contains("Today:"))
        assertTrue(result.contains("-")) // Date should contain dashes
    }

    @Test
    fun `test multiple execution commands`() {
        val content = """
            <%* let a = "Hello" -%>
            <%* let b = "World" -%>
            <%* let c = a + " " + b -%>
            Result: <% c %>
        """.trimIndent()

        val context = TestContextFactory.create(fileContent = "test content")
        val result = parser.parse(content, context)

        assertTrue(result.contains("Result: Hello World"))
    }

    @Test
    fun `test string concatenation in execution command`() {
        val content = """
            <%* let fileName = "note-" + tp.date.now("yyyy-MM-dd") -%>
            File: <% fileName %>
        """.trimIndent()

        val context = TestContextFactory.create(fileContent = "test content")
        val result = parser.parse(content, context)

        assertTrue(result.contains("File: note-"))
    }

    @Test
    fun `test Move dot md scenario`() {
        val content = """
            <%*
            let qcFileName = "TestNote"
            titleName = qcFileName + " " + tp.date.now("yyyy-MM-dd")
            -%>
            ---
            title: <% qcFileName %>
            date: <% tp.file.creation_date("yyyy-MM-dd HH:mm:ss") %>
            tags: quick_note
            topic:
            ---

            Content here
        """.trimIndent()

        val context = TestContextFactory.create(fileContent = "test content")
        val result = parser.parse(content, context)

        // Should not throw an error
        assertNotNull(result)
        assertTrue(result.contains("title: TestNote"))
        assertTrue(result.contains("tags: quick_note"))
    }

    @Test
    fun `test execution command removes itself`() {
        val content = """
            Before
            <%* let x = "hidden" -%>
            After
        """.trimIndent()

        val context = TestContextFactory.create(fileContent = "test content")
        val result = parser.parse(content, context)

        // Execution command should be removed
        assertFalse(result.contains("<%*"))
        assertFalse(result.contains("let x"))
        assertTrue(result.contains("Before"))
        assertTrue(result.contains("After"))
    }

    @Test
    fun `test whitespace trimming with execution command`() {
        val content = "Line1\n<%* let x = \"test\" -%>\nLine2"

        val context = TestContextFactory.create(fileContent = "test content")
        val result = parser.parse(content, context)

        // Should trim the newline after the execution command
        assertEquals("Line1\nLine2", result)
    }

    @Test
    fun `test frontmatter access in script`() {
        val content = """
            <%* let docTitle = tp.frontmatter.title -%>
            Title: <% docTitle %>
        """.trimIndent()

        val context = TestContextFactory.create(
            frontmatter = mapOf("title" to "My Document"),
            fileContent = "test content"
        )
        val result = parser.parse(content, context)

        assertTrue(result.contains("Title: My Document"))
    }

    @Test
    fun `test complex concatenation`() {
        val content = """
            <%*
            let part1 = "Hello"
            let part2 = "World"
            let part3 = tp.date.now("yyyy")
            let combined = part1 + " " + part2 + " " + part3
            -%>
            Result: <% combined %>
        """.trimIndent()

        val context = TestContextFactory.create(fileContent = "test content")
        val result = parser.parse(content, context)

        assertTrue(result.contains("Result: Hello World"))
    }

    @Test
    fun `test forEach with single expression arrow function body`() {
        // Test forEach with single expression body (no braces)
        val content = """
            <%*
            const items = ["a", "b", "c"];
            const result = [];

            items.forEach(item => result.push(item));
            _%>
            Result: <% result.join(", ") %>
        """.trimIndent()

        val context = TestContextFactory.create(fileContent = "test content")
        val result = parser.parse(content, context)

        println("DEBUG forEach single expression result: '$result'")
        assertTrue(result.contains("Result: a, b, c"), "Expected 'Result: a, b, c' but got: $result")
    }

    @Test
    fun `test forEach with multi-statement arrow function body - simple`() {
        // Simpler test to debug multi-statement arrow function bodies
        val content = """
            <%*
            const items = ["a", "b", "c"];
            const result = [];

            items.forEach((item, index) => {
                result.push(item);
            });
            _%>
            Result: <% result.join(", ") %>
        """.trimIndent()

        val context = TestContextFactory.create(fileContent = "test content")
        val result = parser.parse(content, context)

        println("DEBUG forEach simple multi-statement result: '$result'")
        assertTrue(result.contains("Result: a, b, c"), "Expected 'Result: a, b, c' but got: $result")
    }

    @Test
    fun `test forEach with let variable in block body`() {
        // Test that let variables inside forEach block body work
        val content = """
            <%*
            const items = ["hello#world", "foo#bar"];
            const result = [];

            items.forEach((item, index) => {
                let part = item.split('#')[0];
                result.push(part);
            });
            _%>
            Result: <% result.join(", ") %>
        """.trimIndent()

        val context = TestContextFactory.create(fileContent = "test content")
        val result = parser.parse(content, context)

        println("DEBUG forEach let variable result: '$result'")
        assertTrue(result.contains("Result: hello, foo"), "Expected 'Result: hello, foo' but got: $result")
    }

    @Test
    fun `test map access with method chain - two step`() {
        // Test that map[key] followed by method() chains work (two separate statements)
        val content = """
            <%*
            const obj = {
                "a": "hello#world",
                "b": "foo#bar"
            };
            const keys = Object.keys(obj);
            const result = [];

            keys.forEach((key) => {
                let val = obj[key];
                let part = val.split('#')[0];
                result.push(part);
            });
            _%>
            Result: <% result.join(", ") %>
        """.trimIndent()

        val context = TestContextFactory.create(fileContent = "test content")
        val result = parser.parse(content, context)

        println("DEBUG map access two-step result: '$result'")
        assertTrue(result.contains("Result: hello, foo"), "Expected 'Result: hello, foo' but got: $result")
    }

    @Test
    fun `test map access with direct method chain`() {
        // Test that map[key].method() works in a single expression
        val content = """
            <%*
            const obj = {
                "a": "hello#world",
                "b": "foo#bar"
            };
            const keys = Object.keys(obj);
            const result = [];

            keys.forEach((key) => {
                let part = obj[key].split('#')[0];
                result.push(part);
            });
            _%>
            Result: <% result.join(", ") %>
        """.trimIndent()

        val context = TestContextFactory.create(fileContent = "test content")
        val result = parser.parse(content, context)

        println("DEBUG map access direct chain result: '$result'")
        assertTrue(result.contains("Result: hello, foo"), "Expected 'Result: hello, foo' but got: $result")
    }

    @Test
    fun `test forEach with multi-statement arrow function body`() {
        // This tests the pattern from template 4 - Numbered Callout with Hashtag Support
        // Note: Using ${'$'} to escape $ in the raw string for template literals
        val content = """
            <%*
            const callouts = {
                "bug": "🟥 🪳 Bug #hello",
                "info": "🟦 ⓘ Info",
                "warning": "🟧 ⚠️ Warning"
            };
            const typeNames = [];
            const typeLabels = [];

            Object.keys(callouts).forEach((key, index) => {
                typeNames.push(key);
                let label = callouts[key].split('#')[0].trim();
                typeLabels.push(`${'$'}{index + 1}. ${'$'}{label}`);
            });
            _%>
            Names: <% typeNames.join(", ") %>
            Labels: <% typeLabels.join(", ") %>
        """.trimIndent()

        val context = TestContextFactory.create(fileContent = "test content")
        val result = parser.parse(content, context)

        println("DEBUG forEach multi-statement result: '$result'")
        assertTrue(result.contains("Names: bug, info, warning"), "Expected names but got: $result")
        assertTrue(result.contains("Labels: 1. 🟥 🪳 Bug, 2. 🟦 ⓘ Info, 3. 🟧 ⚠️ Warning"), "Expected labels but got: $result")
    }
}

