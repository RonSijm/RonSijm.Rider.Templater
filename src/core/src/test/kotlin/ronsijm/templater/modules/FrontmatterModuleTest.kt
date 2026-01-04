package ronsijm.templater.modules

import ronsijm.templater.parser.FrontmatterParser
import ronsijm.templater.parser.TemplateContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class FrontmatterModuleTest {

    @Test
    fun `test get simple frontmatter value`() {
        val frontmatter = mapOf(
            "title" to "My Document",
            "author" to "John Doe",
            "date" to "2024-01-15"
        )

        val context = TemplateContext(
            frontmatter = frontmatter,
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md",
            filePath = "/test.md"
        )

        val module = FrontmatterModule(context)

        assertEquals("My Document", module.getValue(listOf("frontmatter", "title")))
        assertEquals("John Doe", module.getValue(listOf("frontmatter", "author")))
        assertEquals("2024-01-15", module.getValue(listOf("frontmatter", "date")))
    }

    @Test
    fun `test get nested frontmatter value`() {
        val frontmatter = mapOf(
            "metadata" to mapOf(
                "category" to "tutorial",
                "tags" to listOf("kotlin", "testing")
            )
        )

        val context = TemplateContext(
            frontmatter = frontmatter,
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md",
            filePath = "/test.md"
        )

        val module = FrontmatterModule(context)

        assertEquals("tutorial", module.getValue(listOf("frontmatter", "metadata", "category")))
        assertEquals(listOf("kotlin", "testing"), module.getValue(listOf("frontmatter", "metadata", "tags")))
    }

    @Test
    fun `test get non-existent frontmatter value returns null`() {
        val frontmatter = mapOf(
            "title" to "My Document"
        )

        val context = TemplateContext(
            frontmatter = frontmatter,
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md",
            filePath = "/test.md"
        )

        val module = FrontmatterModule(context)

        assertNull(module.getValue(listOf("frontmatter", "nonexistent")))
    }

    @Test
    fun `test get all frontmatter`() {
        val frontmatter = mapOf(
            "title" to "My Document",
            "author" to "John Doe",
            "tags" to listOf("tag1", "tag2")
        )

        val context = TemplateContext(
            frontmatter = frontmatter,
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md",
            filePath = "/test.md"
        )

        val module = FrontmatterModule(context)
        val all = module.getAll()

        assertEquals(frontmatter, all)
        assertEquals("My Document", all["title"])
        assertEquals("John Doe", all["author"])
        assertEquals(listOf("tag1", "tag2"), all["tags"])
    }

    @Test
    fun `test empty frontmatter`() {
        val context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md",
            filePath = "/test.md"
        )

        val module = FrontmatterModule(context)

        assertNull(module.getValue(listOf("frontmatter", "title")))
        assertTrue(module.getAll().isEmpty())
    }

    @Test
    fun `test invalid path returns null`() {
        val frontmatter = mapOf(
            "title" to "My Document"
        )

        val context = TemplateContext(
            frontmatter = frontmatter,
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md",
            filePath = "/test.md"
        )

        val module = FrontmatterModule(context)


        assertNull(module.getValue(listOf("invalid", "title")))


        assertNull(module.getValue(emptyList()))
    }

    @Test
    fun `test deeply nested frontmatter`() {
        val frontmatter = mapOf(
            "level1" to mapOf(
                "level2" to mapOf(
                    "level3" to "deep value"
                )
            )
        )

        val context = TemplateContext(
            frontmatter = frontmatter,
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md",
            filePath = "/test.md"
        )

        val module = FrontmatterModule(context)

        assertEquals("deep value", module.getValue(listOf("frontmatter", "level1", "level2", "level3")))
    }

    @Test
    fun `test frontmatter with different data types`() {
        val frontmatter: Map<String, Any> = mapOf(
            "string" to "text",
            "number" to 42,
            "boolean" to true,
            "list" to listOf(1, 2, 3)
        )

        val context = TemplateContext(
            frontmatter = frontmatter,
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md",
            filePath = "/test.md"
        )

        val module = FrontmatterModule(context)

        assertEquals("text", module.getValue(listOf("frontmatter", "string")))
        assertEquals(42, module.getValue(listOf("frontmatter", "number")))
        assertEquals(true, module.getValue(listOf("frontmatter", "boolean")))
        assertEquals(listOf(1, 2, 3), module.getValue(listOf("frontmatter", "list")))
    }
}
