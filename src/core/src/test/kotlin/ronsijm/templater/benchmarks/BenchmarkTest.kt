package ronsijm.templater.benchmarks

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.TestContextFactory
import ronsijm.templater.common.TemplateSyntax
import ronsijm.templater.parser.TemplateParser
import java.io.File


@Tag("slow")
class BenchmarkTest {

    private fun findProjectRoot(): File {
        var dir = File(System.getProperty("user.dir"))
        while (dir != null && !File(dir, "gradlew.bat").exists()) {
            dir = dir.parentFile
        }
        return dir ?: File(System.getProperty("user.dir"))
    }

    @Test
    fun `test BenchEscapeHeavy benchmark`() {
        val projectRoot = findProjectRoot()
        val benchmarkFile = File(projectRoot, "docs/Benchmarks/Templating/BenchEscapeHeavy.md")
        if (!benchmarkFile.exists()) {
            println("BenchEscapeHeavy.md not found, skipping")
            return
        }

        val content = benchmarkFile.readText()
        println("File content length: ${content.length}")
        println("First 100 chars: ${content.take(100)}")
        println("First char code: ${content.firstOrNull()?.code}")
        println("Has BOM: ${content.startsWith("\uFEFF")}")


        val matches = TemplateSyntax.TEMPLATE_BLOCK_REGEX.findAll(content).toList()
        println("Template matches found: ${matches.size}")
        if (matches.isNotEmpty()) {
            println("First match: ${matches[0].value.take(100)}")
        }

        val context = TestContextFactory.create(
            fileContent = content,
            filePath = benchmarkFile.absolutePath
        )

        val parser = TemplateParser(validateSyntax = false)

        val startTime = System.nanoTime()
        val result = parser.parse(content, context)
        val endTime = System.nanoTime()
        val elapsedMs = (endTime - startTime) / 1_000_000.0

        println("\n=== BenchEscapeHeavy Performance ===")
        println("Template execution time: ${"%.3f".format(elapsedMs)} ms")
        println("Result:\n$result")
        println("=====================================\n")


        assertTrue(result.contains("Escape-heavy benchmark"), "Should contain benchmark title")
        assertTrue(result.contains("Runs:"), "Should contain runs info")
        assertTrue(result.contains("Time:"), "Should contain time info")
        assertTrue(result.contains("Output:"), "Should contain output info")
        assertTrue(result.contains("Throughput:"), "Should contain throughput info")
    }

    @Test
    fun `test BenchSuite benchmark`() {
        val projectRoot = findProjectRoot()
        val benchmarkFile = File(projectRoot, "docs/Benchmarks/Templating/BenchSuite.md")
        if (!benchmarkFile.exists()) {
            println("BenchSuite.md not found, skipping")
            return
        }

        val content = benchmarkFile.readText()
        val context = TestContextFactory.create(
            fileContent = content,
            filePath = benchmarkFile.absolutePath
        )

        val parser = TemplateParser()

        val startTime = System.nanoTime()
        val result = parser.parse(content, context)
        val endTime = System.nanoTime()
        val elapsedMs = (endTime - startTime) / 1_000_000.0

        println("\n=== BenchSuite Performance ===")
        println("Template execution time: ${"%.3f".format(elapsedMs)} ms")
        println("Result:\n$result")
        println("=====================================\n")

        assertFalse(result.isEmpty(), "Result should not be empty")
    }

    @Test
    fun `test BenchLoopRenderLike benchmark`() {
        val projectRoot = findProjectRoot()
        val benchmarkFile = File(projectRoot, "docs/Benchmarks/Templating/BenchLoopRenderLike.md")
        if (!benchmarkFile.exists()) {
            println("BenchLoopRenderLike.md not found, skipping")
            return
        }

        val content = benchmarkFile.readText()
        val context = TestContextFactory.create(
            fileContent = content,
            filePath = benchmarkFile.absolutePath
        )

        val parser = TemplateParser()
        val result = parser.parse(content, context)

        println("BenchLoopRenderLike result length: ${result.length}")
        println("BenchLoopRenderLike result:\n$result")

        assertFalse(result.isEmpty(), "Result should not be empty")
    }

    @Test
    fun `test BenchFrontmatterLookup benchmark`() {
        val projectRoot = findProjectRoot()
        val benchmarkFile = File(projectRoot, "docs/Benchmarks/Templating/BenchFrontmatterLookup.md")
        if (!benchmarkFile.exists()) {
            println("BenchFrontmatterLookup.md not found, skipping")
            return
        }

        val content = benchmarkFile.readText()
        val context = TestContextFactory.create(
            fileContent = content,
            filePath = benchmarkFile.absolutePath
        )

        val parser = TemplateParser()
        val result = parser.parse(content, context)

        println("BenchFrontmatterLookup result length: ${result.length}")
        println("BenchFrontmatterLookup result:\n$result")

        assertFalse(result.isEmpty(), "Result should not be empty")
    }

    @Test
    fun `test BenchManySmallTags benchmark`() {
        val projectRoot = findProjectRoot()
        val benchmarkFile = File(projectRoot, "docs/Benchmarks/Templating/BenchManySmallTags.md")
        if (!benchmarkFile.exists()) {
            println("BenchManySmallTags.md not found, skipping")
            return
        }

        val content = benchmarkFile.readText()
        val context = TestContextFactory.create(
            fileContent = content,
            filePath = benchmarkFile.absolutePath
        )

        val parser = TemplateParser()
        val result = parser.parse(content, context)

        println("BenchManySmallTags result length: ${result.length}")
        println("BenchManySmallTags result:\n$result")

        assertFalse(result.isEmpty(), "Result should not be empty")
    }
}
