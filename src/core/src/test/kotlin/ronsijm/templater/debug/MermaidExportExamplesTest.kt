package ronsijm.templater.debug

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import ronsijm.templater.parser.FrontmatterParser
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.services.ServiceContainer
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.Executors
import java.util.concurrent.TimeoutException


@Tag("slow")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MermaidExportExamplesTest {

    private fun findProjectRoot(): File {
        var dir = File(System.getProperty("user.dir"))
        while (dir.parentFile != null) {
            if (File(dir, "settings.gradle.kts").exists()) {
                return dir
            }
            dir = dir.parentFile
        }
        return File(System.getProperty("user.dir"))
    }

    private val projectRoot = findProjectRoot()
    private val examplesDir = File(projectRoot, "docs/Examples")
    private val outputDir = File(projectRoot, "docs/Examples_Mermaid")
    private val frontmatterParser = FrontmatterParser()
    private var exportedCount = 0
    private var failedCount = 0
    private var skippedCount = 0

    @BeforeAll
    fun setup() {
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
    }

    @Test
    fun `export Mermaid diagrams for all example templates`() {
        assertTrue(examplesDir.exists(), "Examples directory should exist at ${examplesDir.absolutePath}")

        val templateFiles = examplesDir.walkTopDown()
            .filter { it.isFile && it.extension == "md" }
            .toList()

        assertTrue(templateFiles.isNotEmpty(), "Should find at least one template file")

        println("Found ${templateFiles.size} template files to process")

        for (templateFile in templateFiles) {
            processTemplateFileWithTimeout(templateFile, 5)
        }

        println("\n=== Mermaid Export Summary ===")
        println("Total files: ${templateFiles.size}")
        println("Exported: $exportedCount")
        println("Failed: $failedCount")
        println("Skipped (timeout): $skippedCount")
        println("Output: ${outputDir.absolutePath}")

        assertTrue(exportedCount > 0, "At least some Mermaid exports should succeed")
    }

    private fun processTemplateFileWithTimeout(templateFile: File, timeoutSeconds: Long) {
        val executor = Executors.newSingleThreadExecutor()
        val future = executor.submit<Boolean> { processTemplateFile(templateFile) }
        try {
            future.get(timeoutSeconds, TimeUnit.SECONDS)
        } catch (e: TimeoutException) {
            future.cancel(true)
            skippedCount++
            println("Skipped (timeout): ${templateFile.name}")
        } catch (e: Exception) {
            failedCount++
            println("Failed: ${templateFile.name} - ${e.message}")
        } finally {
            executor.shutdownNow()
        }
    }

    private fun processTemplateFile(templateFile: File): Boolean {
        val content = templateFile.readText()
        val parseResult = frontmatterParser.parse(content)

        val relativePath = templateFile.relativeTo(examplesDir).path
        val outputSubDir = File(outputDir, relativePath).parentFile
        outputSubDir?.mkdirs()

        val baseName = templateFile.nameWithoutExtension
        val outputFile = File(outputSubDir ?: outputDir, "$baseName.mmd")

        val context = TemplateContext(
            frontmatter = parseResult.frontmatter,
            frontmatterParser = frontmatterParser,
            fileName = templateFile.name,
            filePath = templateFile.absolutePath,
            fileContent = parseResult.content,
            services = ServiceContainer()
        )

        val tracingParser = TracingTemplateParser(validateSyntax = false)

        try {
            tracingParser.parse(parseResult.content, context)
        } catch (e: Exception) {

        }

        val title = parseResult.frontmatter["title"]?.toString() ?: baseName
        val mermaidContent = tracingParser.exportMermaidFlowchart(title)

        outputFile.writeText(mermaidContent)
        exportedCount++
        println("Exported: $relativePath -> ${outputFile.name}")
        return true
    }

    @Test
    fun `verify Mermaid output is valid`() {
        val simpleTemplate = "---\ntitle: Test Template\n---\nHello <%  \"World\" %>!"

        val parseResult = frontmatterParser.parse(simpleTemplate)
        val context = TemplateContext(
            frontmatter = parseResult.frontmatter,
            frontmatterParser = frontmatterParser,
            fileName = "test.md",
            filePath = "/test/test.md",
            fileContent = parseResult.content,
            services = ServiceContainer()
        )

        val tracingParser = TracingTemplateParser(validateSyntax = false)
        tracingParser.parse(parseResult.content, context)

        val mermaid = tracingParser.exportMermaidFlowchart("Test Template")

        assertTrue(mermaid.startsWith("flowchart TD"), "Should start with flowchart TD")
        assertTrue(mermaid.contains("Test Template"), "Should contain title")

        assertTrue(mermaid.contains("n0") || mermaid.contains("-->"), "Should contain nodes or edges")
    }

    @Test
    fun `export sequence diagram variant`() {
        val template = "---\ntitle: Sequence Test\n---\nName: <% tp.file.name() %>"

        val parseResult = frontmatterParser.parse(template)
        val context = TemplateContext(
            frontmatter = parseResult.frontmatter,
            frontmatterParser = frontmatterParser,
            fileName = "sequence-test.md",
            filePath = "/test/sequence-test.md",
            fileContent = parseResult.content,
            services = ServiceContainer()
        )

        val tracingParser = TracingTemplateParser(validateSyntax = false)
        tracingParser.parse(parseResult.content, context)

        val sequence = tracingParser.exportMermaidSequence("Sequence Test")

        assertTrue(sequence.startsWith("sequenceDiagram"), "Should start with sequenceDiagram")
        assertTrue(sequence.contains("participant"), "Should contain participants")
    }
}